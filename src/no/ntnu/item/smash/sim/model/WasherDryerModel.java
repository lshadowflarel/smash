package no.ntnu.item.smash.sim.model;

import java.util.ArrayList;
import java.util.Arrays;

import no.ntnu.item.smash.sim.data.Constants;
import no.ntnu.item.smash.sim.data.stats.HouseholdStatistics;
import no.ntnu.item.smash.sim.data.utility.ConsumerPlanUtility;
import no.ntnu.item.smash.sim.structure.Device;
import no.ntnu.item.smash.sim.structure.Room;
import no.ntnu.item.smash.sim.structure.WashingMachineDryer;

public class WasherDryerModel extends DeviceModel {

	// variables
	private String scheduleFile;
	private ArrayList<double[]> tasks;
	private WashingMachineDryer wm;

	// simulation
	private int simDay = 1;
	private double energyInput = 0; // watt
	private double startWSecond, endWSecond, startDSecond, endDSecond;
	private boolean overrideAllowed = true;
	private double[] currentTask;
	private int taskID = 0;

	public WasherDryerModel(Device device, Room room, String scheduleFile) {
		super(device, room);
		this.scheduleFile = scheduleFile;
		wm = (WashingMachineDryer) device;
	}

	@Override
	public void updateSchedule(double[] schedule) {
		// System.out.println(device.getName() + " receives schedule update.");
		// System.out.println("OLD>> " + startWSecond + "-" + endWSecond + " , "
		// + startDSecond + "-" + endDSecond);
		for (int i = 0; i < this.schedule.length; i++) {
			if (this.schedule[i] == taskID || this.schedule[i] == -taskID) {
				powerSchedule[i] = 0;
			}
		}

		this.schedule = schedule;
		int startWInterval = 0, startDInterval = 0;
		for (int i = 0; i < schedule.length; i++) {
			if (schedule[i] == taskID) {
				startWSecond = (simDay - 1) * 24 * 3600 + (i * syncInterval);
				startWInterval = i;
				break;
			}
		}
		for (int j = 0; j < schedule.length; j++) {
			if (schedule[j] == -taskID) {
				startDSecond = (simDay - 1) * 24 * 3600 + (j * syncInterval);
				startDInterval = j;
				break;
			}
		}

		int washProg = (int) currentTask[0];
		int dryProg = (int) currentTask[1];

		if (washProg > 0) {
			endWSecond = startWSecond + wm.getWashDurationFromProgram(washProg)
					* 60;
			for (int i = startWInterval; i < startWInterval
					+ ((endWSecond - startWSecond) / syncInterval); i++) {
				powerSchedule[i] = ((WashingMachineDryer) device)
						.getWWattFromProgramAndTime(washProg, (i
								- startWInterval + 1)
								* syncInterval);
			}
		}
		if (dryProg > 0) {
			endDSecond = startDSecond + wm.getDryDurationFromProgram(dryProg)
					* 60;
			for (int i = startDInterval; i < startDInterval
					+ ((endDSecond - startDSecond) / syncInterval); i++) {
				powerSchedule[i] = ((WashingMachineDryer) device)
						.getDWattFromProgramAndTime(dryProg, (i
								- startDInterval + 1)
								* syncInterval);
			}
		}
		// System.out.println("NEW>> " + startWSecond + "-" + endWSecond + " , "
		// + startDSecond + "-" + endDSecond);

		overrideAllowed = false;
	}

	@Override
	public void run() {
		documentDailyPlan();
		calculateOperationTime();
		this.schedule = Arrays.copyOf(plan, plan.length);

		int second = 0;
		double peakPower = 0;
		double accumEnergy = 0;
		model.synchTimeToCSS(device, 0, second / 3600, currentDay,
				currentMonth, currentYear);
		stat.writeYearHeadingToFiles(currentYear);

		while (run) {
			// determine the power (watt) from the program and the time of day
			if (second >= startWSecond && second <= endWSecond) {
				energyInput = wm.getWWattFromProgramAndTime(
						(int) currentTask[0], second - startWSecond);
			} else if (second >= startDSecond && second <= endDSecond) {
				energyInput = wm.getDWattFromProgramAndTime(
						(int) currentTask[1], second - startDSecond);
			} else
				energyInput = 0;
			if (energyInput > peakPower)
				peakPower = energyInput;
			accumEnergy += energyInput / 3600;

			if ((endDSecond > -1 && second == endDSecond)
					|| (endDSecond == -1 && second == endWSecond)) {
				calculateOperationTime();

				// simulate the event when the user is about to start the
				// washing machine
				// CSS needs to decide whether to allow this or shift the
				// washing machine or something else
				if (!model.getCSSSuspended()
						&& overrideAllowed
						&& ((startWSecond > -1 && second == startWSecond) || (startWSecond == -1 && second == startDSecond))) {
					// washing machine notify the CSS and requests further
					// instruction
					askCSS();
				}
			}

			// stat after 5 min
			if (second > 0 && second % syncInterval == syncInterval - 1) {
				int atHour = second / syncInterval;

				hourlyEnergy[atHour % (24 * 3600 / syncInterval)] = accumEnergy * 0.001;
				hourlyPeakLoad[atHour % (24 * 3600 / syncInterval)] = peakPower;

				accumEnergy = 0;
				peakPower = 0;

				// sync to the EventManager
				while (!model.onStandby()) {
					try {
						Thread.sleep(5);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				model.getLock().periodicSync(model, device.getName(),
						(second + 1) / syncInterval);

				int priceInterval = model.getEventManager().getDSO()
						.getContract(model.getContract())
						.getPriceChangeInterval();
				if ((second + 1) % (priceInterval * 60) == 0) {
					hourlyPrice[(second / (priceInterval * 60))
							% hourlyPrice.length] = model
							.getEventManager()
							.getDSO()
							.getContract(model.getContract())
							.getActElectricityPriceAtTime(second, currentDay,
									currentMonth, currentYear);
					hourlyCost[(second / (priceInterval * 60))
							% hourlyPrice.length] = hourlyPrice[(second / (priceInterval * 60))
							% hourlyPrice.length]
							* HouseholdStatistics.aggregateData(hourlyEnergy,
									(priceInterval * 60) / syncInterval, false)[(second / (priceInterval * 60))
									% hourlyPrice.length];
				}
			}

			// stat after 1 hour
			if (second > 0 && second % 1800 == 1799) {
				// simulate the event when the user is about to start the
				// washing machine in the next second (start of next hour)
				// CSS needs to decide whether to allow this or shift the
				// washing machine or something else
				if (!model.getCSSSuspended()
						&& overrideAllowed
						&& ((startWSecond > -1 && second + 1 == startWSecond) || (startWSecond == -1 && second + 1 == startDSecond))) {
					// washing machine notify the CSS and requests further
					// instruction
					askCSS();
				}

				if (second + 1 < simTime
						&& second % (24 * 3600) != (24 * 3600 - 1)) {
					while (model.synced) {
						try {
							Thread.sleep(5);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					model.synchTimeToCSS(device, (second + 1) % 3600 == 0 ? 0
							: 30, (second + 1) / 3600, currentDay,
							currentMonth, currentYear);
				}

				// if we reach 24 hours
				if (second % (24 * 3600) == (24 * 3600 - 1)) {
					// store stat data
					stat.addEnergyData(hourlyEnergy);
					stat.addPlanData(plan, false);
					stat.addScheduleData(schedule);
					stat.addPeakLoadData(hourlyPeakLoad);

					model.getEnergyMonitor().addEnergyDataAtDay(
							currentDay,
							HouseholdStatistics.aggregateData(hourlyEnergy,
									3600 / syncInterval, false));
					model.getEnergyMonitor().addCostDataAtDay(
							currentDay,
							HouseholdStatistics.aggregateData(hourlyCost,
									60 / model.getEventManager().getDSO()
											.getContract(model.getContract())
											.getPriceChangeInterval(), false));
					model.getEnergyMonitor().addPriceDataAtDay(
							currentDay,
							HouseholdStatistics.aggregateData(hourlyPrice,
									3600 / (model.getEventManager().getDSO()
											.getContract(model.getContract())
											.getPriceChangeInterval() * 60),
									true));
					model.getEnergyMonitor().addPeakLoadDataDay(currentDay,
							hourlyPeakLoad);

					// if we reach one month
					if (currentDay++ == Constants.getNumDaysInMonth(
							currentMonth, currentYear)) {
						currentDay = 1;
						stat.writeDataToFiles(currentMonth);
						model.informMonthReached(currentMonth);

						// if we reach one year
						if (currentMonth++ == 12) {
							currentMonth = 1;
							currentYear++;
							if (second + 1 < simTime) {
								stat.writeYearHeadingToFiles(currentYear);
								model.updateCurrentYear(currentYear);
							}
						}
					}

					simDay++;

					if (second + 1 < simTime) {
						taskID = 0;
						documentDailyPlan();
						calculateOperationTime();
						this.schedule = Arrays.copyOf(plan, plan.length);
						while (model.synced) {
							try {
								Thread.sleep(5);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
						model.synchTimeToCSS(device, 0, (second + 1) / 3600,
								currentDay, currentMonth, currentYear);
						setCurrentDate(currentDay, currentMonth, currentYear);
					}
				}
			}

			if (++second == simTime) {
				run = false;
				stat.writeDataToFiles(currentMonth);
				model.informSimDone(currentDay, currentMonth, currentYear);
			}
		}
	}

	public int getCurrentTaskID() {
		return taskID;
	}

	public double[] getCurrentTask() {
		return currentTask;
	}

	private void calculateOperationTime() {
		overrideAllowed = true;

		if (tasks.size() > 0) {
			taskID++;
			currentTask = tasks.remove(0);
			int washProg = (int) currentTask[0];
			int dryProg = (int) currentTask[1];
			if (washProg > 0) {
				startWSecond = (simDay - 1) * 24 * 3600 + currentTask[2] * 3600;
				endWSecond = startWSecond
						+ wm.getWashDurationFromProgram(washProg) * 60;
			} else {
				startWSecond = -1;
				endWSecond = -1;
			}
			if (dryProg > 0 && washProg > 0) {
				startDSecond = endWSecond + currentTask[3] * 60;
				endDSecond = startDSecond
						+ wm.getDryDurationFromProgram(dryProg) * 60;
			} else if (washProg == 0) {
				startDSecond = (simDay - 1) * 24 * 3600 + currentTask[2] * 3600;
				endDSecond = startDSecond
						+ wm.getDryDurationFromProgram(dryProg) * 60;
			} else {
				startDSecond = -1;
				endDSecond = -1;
			}
		} else {
			startWSecond = -1;
			endWSecond = -1;
			startDSecond = -1;
			endDSecond = -1;
		}
	}

	private void documentDailyPlan() {
		plan = new double[(24 * 3600 / syncInterval)];
		powerSchedule = new double[plan.length];
		tasks = ConsumerPlanUtility.readPlan(scheduleFile, currentDay);
		// daily plan is an array of size 24 representing a 24-hour plan
		for (int i = 0; i < tasks.size(); i++) {
			double[] task = tasks.get(i);

			int washProg = (int) task[0];
			int dryProg = (int) task[1];
			if (washProg > 0) {
				int start = (int) task[2] * (3600 / syncInterval);
				int end = start + (int) wm.getWashDurationFromProgram(washProg)
						/ (syncInterval / 60);
				for (int s = start; s < end; s++) {
					plan[s] = i + 1; // wash task gets a positive id
					powerSchedule[s] = ((WashingMachineDryer) device)
							.getWWattFromProgramAndTime(washProg,
									(s - start + 1) * syncInterval);
				}
			}
			if (dryProg > 0) {
				int start = washProg > 0 ? ((int) task[2] * (3600 / syncInterval))
						+ ((int) wm.getWashDurationFromProgram(washProg) / (syncInterval / 60))
						+ ((int) task[3] * 60)
						: (int) task[2] * (3600 / syncInterval);
				int end = start + (int) wm.getDryDurationFromProgram(dryProg)
						/ (syncInterval / 60);
				for (int s = start; s < end; s++) {
					plan[s] = -(i + 1); // dry task gets a negative id
					powerSchedule[s] = ((WashingMachineDryer) device)
							.getDWattFromProgramAndTime(dryProg,
									(s - start + 1) * syncInterval);
				}
			}
		}
	}

	private void askCSS() {
		double[] task = Arrays.copyOf(currentTask, currentTask.length + 3);
		// System.out.println(device.getName() +
		// " requests CSS for instruction. Task ID=" + taskID);
		task[5] = currentDay;
		task[6] = currentMonth;
		task[7] = currentYear;
		model.getCSS()
				.getCss()
				.requestStartDevice(wm.getType(), wm.getName(), task, taskID,
						null);

		// System.out.println("Instructions received by " + device.getName() +
		// ".");
	}

}
