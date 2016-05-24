package no.ntnu.item.smash.sim.model;

import java.util.ArrayList;
import java.util.Arrays;

import no.ntnu.item.smash.sim.data.Constants;
import no.ntnu.item.smash.sim.data.stats.ComfortStatistics;
import no.ntnu.item.smash.sim.data.stats.EVStatistics;
import no.ntnu.item.smash.sim.data.stats.HouseholdStatistics;
import no.ntnu.item.smash.sim.data.utility.ConsumerPlanUtility;
import no.ntnu.item.smash.sim.externalsp.DSO;
import no.ntnu.item.smash.sim.structure.Device;
import no.ntnu.item.smash.sim.structure.EVLiionBattery;
import no.ntnu.item.smash.sim.structure.Room;

public class EVChargerModel extends DeviceModel {

	private final boolean DEBUG = false;

	// variables
	private String scheduleFile;
	private EVLiionBattery battery;
	private boolean lv2Charging;
	private int schDuration; // second
	private double[] currentTask;
	private boolean taskFixed = false;

	// simulation
	private int second = 0;
	private int simDay = 1;
	private int schDay = 1;
	private double energyInput = 0; // watt
	private double SoC = 90;
	private double maxSoC = 90;
	private double batteryEnergy = 0; // wh
	private double startSecond = 0; // second
	private int startInterval = -1;
	private int endInterval = -1;
	private int latestStartSecond = 0;
	private int latestEndSecond = 0;

	// stats
	private double[] socStats = new double[2];
	private double[] dodStat_SoC = new double[24 * 12];
	private String[] comfortLVStat_SoC = new String[24 * 12];

	public EVChargerModel(Device device, Room room, String scheduleFile) {
		super(device, room);
		this.scheduleFile = scheduleFile;
		battery = (EVLiionBattery) device;
		lv2Charging = battery.canChargeLV2();
		plan = new double[(86400 / syncInterval) * 2];
		schedule = new double[(86400 / syncInterval) * 2];
	}

	public double getCurrentSoc() {
		return SoC;
	}

	public boolean lv2Charging() {
		return lv2Charging;
	}

	public void setStatistics(String path, String name) {
		stat = new EVStatistics(path, name);
		comfortStat = new ComfortStatistics(path, name);
	}

	@Override
	public void updateSchedule(double[] schedule) { // assume there's only one
													// task with task ID #1 and
													// that the charging will be
													// done until the battery is
													// at 90% or as long as it
													// can

		if (lv2Charging && model.getCEMS() != null) {
			// before updating the schedule, check first if CEMS must be
			// informed of
			// any free slots
			int startIntervalToInform = -1;
			int endIntervalToInform = -1;
			if (taskFixed && !DSO.isEVContract(model.getContract())) {
				ArrayList<Integer> freeIntervals = new ArrayList<Integer>();
				for (int i = 0; i < schedule.length; i++) {
					if (this.schedule[i] == 1 && schedule[i] == 0) {
						freeIntervals.add(i);
					}
				}

				model.getCEMS().informNonEVCustChargeTime(simDay,
						freeIntervals, battery.getWatt(), false,
						model.getFeederID());
			}

			// also check if CEMS must be informed of any selected slots
			ArrayList<Integer> chargeIntervals = new ArrayList<Integer>();
			for (int i = 0; i < schedule.length; i++) {
				if (this.schedule[i] == 0 && schedule[i] == 1) {
					chargeIntervals.add(i);
				}
			}

			if (!DSO.isEVContract(model.getContract()))
				model.getCEMS().informNonEVCustChargeTime(simDay,
						chargeIntervals, battery.getWatt(), true,
						model.getFeederID());
			else if (model.getCEMS().getEVChargeAlgorithm() == 2)
				model.getCEMS().informCSSDecision(simDay, chargeIntervals,
						battery.getWatt(), true, model.getFeederID(),
						model.getModelID());
			taskFixed = true;
		}

		// for(int i=0;i<schedule.length;i++)System.out.println(simDay + " " + i
		// + " " + schedule[i]);
		this.schedule = schedule;

		int curInterval = (second / 300) % 288;

		boolean found = false;
		for (int i = curInterval + 1; i < schedule.length; i++) {
			if (schedule[i] == 1) {
				startSecond = ((simDay - 1) * 86400) + (i * 300);
				startInterval = i;
				found = true;
				break;
			}
		}

		for (int i = schedule.length - 1; i >= 0; i--) {
			if (found && schedule[i] == 1) {
				endInterval = i + 1;
				break;
			}
		}
		createPowerScheduleFromSchedule();

		if (!found) {
			startSecond = -1;
			startInterval = -1;
			endInterval = -1;
		}

		if (DEBUG)
			System.out.println("[Model " + model.getModelID() + "] "
					+ "New start should be " + startSecond + " ("
					+ startInterval + "-" + endInterval + ")");
	}

	/*
	 * Only used when it's a result of CEMS control
	 */
	public void shiftSchedule(int fromInterval, int toInterval) {
		if (DEBUG)
			System.out.println("[Model "
					+ model.getModelID()
					+ "] "
					+ " Rescheduled to move from "
					+ fromInterval
					+ " to "
					+ toInterval
					+ " (curInt="
					+ (second / 300 >= 288 ? (second / 300) % 288
							: second / 300) + ")");
		schedule[fromInterval] = 0;
		if (toInterval != -1) {
			schedule[toInterval] = 1;
			if (toInterval > endInterval)
				endInterval = toInterval;
		}
		createPowerScheduleFromSchedule();
	}

	public ArrayList<String[]> getComfortLVStats() {
		ArrayList<String[]> stats = new ArrayList<String[]>();
		stats.add(comfortLVStat_SoC);
		return stats;
	}

	@Override
	public void run() {
		lv2Charging = battery.canChargeLV2();
		readSchedule();
		double peakPower = 0;
		double accumEnergy = 0;
		model.synchTimeToCSS(device, 0, second / 3600, currentDay,
				currentMonth, currentYear);
		stat.writeYearHeadingToFiles(currentYear);
		comfortStat.writeYearHeadingToFiles(currentYear);

		while (run) {
			if (second == latestEndSecond) {
				socStats[0] = second;
				socStats[1] = SoC;
				((EVStatistics) stat).addSoCData(socStats);
				if (DEBUG)
					System.out.println("[Model " + model.getModelID() + "] "
							+ "Done charging at " + second + " SoC=" + SoC);
				currentTask = null;
				schDay++;
				readSchedule();
			}

			// simulate the event when the user is about to start charging the
			// car in the next second (start of next hour)
			// CSS needs to decide whether to allow this or shift the charging
			// or something else
			if (second + 1 == startSecond) {
				int earliestStartSecond = (int) startSecond;
				SoC = Math.max(0, SoC - currentTask[1]);
				if (((!model.getCSSSuspended() || model.getCEMS() != null))
						&& !taskFixed) {
					if (DEBUG)
						System.out.println("[Model " + model.getModelID()
								+ "] " + "Ask CSS at " + second
								+ ". SoC is now " + SoC);

					// charger notify CSS and requests further instruction
					askCSS(earliestStartSecond);
				}
			}

			// determine whether it's charging
			int curInterval = second / 300 >= 288 ? (second / 300) % 288
					: second / 300;

			if (schedule[curInterval] != 0 && SoC < maxSoC) {
				energyInput = (lv2Charging ? battery.getWatt() : 1100);

				// calculate new SoC
				batteryEnergy += energyInput / 3600;
				SoC = (batteryEnergy / (battery.getkWh() * 1000)) * 100;
			} else {
				energyInput = 0;
			}

			if (energyInput > peakPower)
				peakPower = energyInput;
			accumEnergy += energyInput / 3600;

			// stat after 5 min
			if (second > 0 && second % syncInterval == syncInterval - 1) {
				int atHour = second / syncInterval;

				hourlyEnergy[atHour % (24 * 3600 / syncInterval)] = accumEnergy * 0.001;
				hourlyPeakLoad[atHour % (24 * 3600 / syncInterval)] = peakPower;
				comfortMeasureVals.add(SoC);
				Object[] comfortStats = calculateDoD(comfortMeasureVals, "SoC");
				if (comfortStats != null) {
					dodStat_SoC[atHour % (24 * 3600 / syncInterval)] = (Double) comfortStats[0];
					comfortLVStat_SoC[atHour % (24 * 3600 / syncInterval)] = (String) comfortStats[1];
					comfortMeasureVals.clear();

					if (second % (24 * 3600) == (24 * 3600 - 1)) {
						comfortStat.addComfortStat(model.getEnvironment()
								.getComfortMeasureByName("SoC"), dodStat_SoC,
								comfortLVStat_SoC);
					}
				}

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

			// if we reach half an hour
			if (second > 0 && second % 1800 == 1799) {
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
					stat.addPlanData(Arrays.copyOf(plan, 288), false);
					stat.addScheduleData(Arrays.copyOf(schedule, 288));
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
									60 / model.getEventManager().getDSO()
											.getContract(model.getContract())
											.getPriceChangeInterval(), true));
					model.getEnergyMonitor().addPeakLoadDataDay(currentDay,
							hourlyPeakLoad);

					// if we reach one month
					if (currentDay++ == Constants.getNumDaysInMonth(
							currentMonth, currentYear)) {
						currentDay = 1;
						if(second + 1 < simTime) {
							stat.writeDataToFiles(currentMonth);
							comfortStat.writeDataToFiles(currentMonth);
						}
						model.informMonthReached(currentMonth);

						// if we reach one year
						if (currentMonth++ == 12) {
							currentMonth = 1;
							currentYear++;
							if (second + 1 < simTime) {
								stat.writeYearHeadingToFiles(currentYear);
								comfortStat
										.writeYearHeadingToFiles(currentYear);
								model.updateCurrentYear(currentYear);
							}
						}
					}

					simDay++;

					if (second + 1 < simTime) {
						initializePlanSchedule();
						if (schDay < simDay
								&& (currentTask == null || startInterval == -1)) {
							schDay = simDay;
							readSchedule();
						}

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
				comfortStat.writeDataToFiles(currentMonth);
				model.informSimDone(currentDay, currentMonth, currentYear);
			}
		}
	}

	private void readSchedule() {
		if (DEBUG)
			System.out.println("[Model " + model.getModelID() + "] " + "("
					+ lv2Charging + ") " + "Schedule read for day " + schDay);
		ArrayList<double[]> tasks = ConsumerPlanUtility.readPlan(scheduleFile,
				schDay);
		double newSoC = SoC;
		if (tasks.size() > 0) {
			currentTask = tasks.get(0);

			startInterval = (int) ((currentTask[2] * 60) / 300);
			startSecond = ((schDay - 1) * 24 * 3600) + (startInterval * 300);
			newSoC = Math.max(0, SoC - currentTask[1]);
			batteryEnergy = (newSoC / 100) * battery.getkWh() * 1000;

			// populate plan and schedule
			double maxDuration = currentTask[3] * 60;
			double estDuration = (((battery.getkWh() * (maxSoC / 100) * 1000) - batteryEnergy) / (lv2Charging ? battery
					.getWatt() : 1100)) * 3600;
			schDuration = (int) Math.ceil(Math.min(maxDuration, estDuration));
			latestEndSecond = (int) (startSecond + (currentTask[3] * 60));
			latestStartSecond = (int) Math.ceil(latestEndSecond - schDuration);
			endInterval = startInterval
					+ (schDuration % 300 == 0 ? schDuration / 300
							: schDuration / 300 + 1);

			if (schDay > simDay) {
				startInterval += 288;
				endInterval += 288;
			}
			for (int i = startInterval; i <= endInterval && i < 576; i++) {
				plan[i] = 1;
				schedule[i] = 1;
			}
		}
		if (DEBUG)
			System.out.println("[Model " + model.getModelID() + "] " + "Start="
					+ startSecond + " (" + startInterval + ") End="
					+ (startSecond + schDuration) + " (" + endInterval
					+ ") SoC=" + newSoC);
		createPowerScheduleFromSchedule();
		taskFixed = false;
	}

	private void initializePlanSchedule() {
		if (DEBUG)
			System.out.println("[Model " + model.getModelID() + "] "
					+ "Initialize plan on day " + simDay);
		for (int i = 288; i < 576; i++) {
			schedule[i - 288] = schedule[i];
			plan[i - 288] = plan[i];
		}
		Arrays.fill(schedule, 288, 576, 0);
		Arrays.fill(plan, 288, 576, 0);

		if (startInterval >= 288 && endInterval > 288) {
			startInterval -= 288;
			endInterval -= 288;
		} else if (startInterval < 288 && endInterval > 288) {
			startInterval = 0;
			endInterval -= 288;
		}
		if (DEBUG)
			System.out.println("[Model " + model.getModelID() + "] "
					+ "After init: Start=" + startSecond + " (" + startInterval
					+ ") End=" + (startSecond + schDuration) + " ("
					+ endInterval + ")");
	}

	// here we call this method even if CSS is suspended because it is the
	// method that communicates with CEMS
	@SuppressWarnings("unchecked")
	private void askCSS(int earliestStartSecond) {
		String[] pstart = {};
		String[] pstart2 = null;
		ArrayList<String> possibleStart = new ArrayList<String>();

		if (model.getCEMS() != null
				&& model.getCEMS().getEVChargeAlgorithm() == 2
				&& DSO.isEVContract(model.getContract()) && lv2Charging) {
			if (DEBUG)
				System.out.println("[Model " + model.getModelID() + "] "
						+ device.getName() + "-" + device.getId()
						+ " requests CEMS for lv2 charging permission.");
			Object[] nonPossible_possible = model
					.getCEMS()
					.requestNonPossibleStartTime(
							simDay,
							(int) (earliestStartSecond - ((simDay - 1) * 24 * 3600)),
							schDuration / 60, battery.getWatt(),
							model.getFeederID());
			possibleStart = (ArrayList<String>) nonPossible_possible[0];
			pstart2 = new String[((ArrayList<String>) nonPossible_possible[1])
					.size()];
			for (int i = 0; i < pstart2.length; i++) {
				pstart2[i] = ((ArrayList<String>) nonPossible_possible[1])
						.get(i);
			}

			if (DEBUG)
				System.out.println("[Model " + model.getModelID() + "] "
						+ device.getName() + "-" + device.getId()
						+ " receives CEMS suggestion.");

			if (possibleStart.size() == 0) {
				// already know that it's not possible to charge
				startSecond = -1;
				startInterval = -1;
				endInterval = -1;
				model.getCEMS().informCSSDecision(simDay,
						new ArrayList<Integer>(), battery.getWatt(), true,
						model.getFeederID(), model.getModelID());
				taskFixed = true;
				return;
			}
		}

		Object[] info = new Object[3];
		info[0] = pstart2 == null ? pstart : pstart2;

		double[] t = Arrays.copyOf(currentTask, currentTask.length + 4);

		if (!model.getCSSSuspended()
				&& (model.getCEMS() == null || model.getCEMS()
						.getEVChargeAlgorithm() != 3)) {
			if (DEBUG)
				System.out.println("[Model " + model.getModelID() + "] "
						+ "requests CSS for instructions.");
			t[1] = SoC;
			t[4] = currentDay;
			t[5] = currentMonth;
			t[6] = currentYear;
			t[7] = (lv2Charging ? battery.getWatt() : 1100);
			info[1] = battery.getkWh();
			model.getCSS()
					.getCss()
					.requestStartDevice(battery.getType(), battery.getName(),
							t, 0, info);

			if (DEBUG)
				System.out.println("[Model " + model.getModelID() + "] "
						+ "Instructions received.");
		} else if (model.getCEMS().getEVChargeAlgorithm() == 2) {
			if (DSO.isEVContract(model.getContract()) && lv2Charging) {
				if (possibleStart.size() > 0) {
					String firstPossible = possibleStart.get(0);
					String[] hourMin = firstPossible.split(":");
					int hour = Integer.parseInt(hourMin[0]);
					int min = Integer.parseInt(hourMin[1]);

					startSecond = ((simDay - 1) * 24 * 3600) + (hour * 3600)
							+ (min * 60);
				} else {
					startSecond = -1;
				}
			} else {
				startSecond = earliestStartSecond;
			}

			for (int i = startInterval; i < endInterval; i++) {
				schedule[i] = 0;
			}

			startInterval = startSecond == -1 ? -1
					: (int) (((int) (startSecond - ((simDay - 1) * 24 * 3600)) / 60) / 5);
			endInterval = startSecond == -1 ? -1
					: startInterval
							+ ((int) Math.ceil(schDuration / 60.0) % 5 == 0 ? (schDuration / 60) / 5
									: (int) (Math.ceil(schDuration / 60.0) / 5) + 1)
							+ 1;

			ArrayList<Integer> chargeIntervals = new ArrayList<Integer>();
			for (int i = startInterval; i < endInterval; i++) {
				schedule[i] = 1;
				chargeIntervals.add(i);
			}

			if (DEBUG)
				System.out.println("[Model " + model.getModelID() + "] "
						+ "Start should be " + startSecond + " ("
						+ startInterval + "-" + endInterval + ")");
			if (DSO.isEVContract(model.getContract()) && lv2Charging) {
				model.getCEMS().informCSSDecision(simDay, chargeIntervals,
						battery.getWatt(), true, model.getFeederID(),
						model.getModelID());
			}
		}

		if (model.getCEMS() != null && DSO.isEVContract(model.getContract())
				&& lv2Charging) { // EVContract customers
			if (model.getCEMS().getEVChargeAlgorithm() == 1) { // first
				// algorithm
				// if(model.getModelID()==2)
				// System.out.println("[Model " + model.getModelID() + "] " +
				// device.getName() + "-" +
				// device.getId()
				// + " requests CEMS for lv2 charging permission.");
				double[] suggestion = model
						.getCEMS()
						.requestChargeTime(
								simDay,
								(int) (earliestStartSecond - ((simDay - 1) * 24 * 3600)),
								(int) (startSecond - ((simDay - 1) * 24 * 3600)),
								(int) Math.ceil(schDuration / 60.0),
								(int) (latestStartSecond - ((currentDay - 1) * 24 * 3600)),
								battery.getWatt(), model.getFeederID());
				for (int i = 0; i < (earliestStartSecond - ((simDay - 1) * 24 * 3600)) / 300; i++) {
					suggestion[i] = schedule[i];
				}
				updateSchedule(suggestion);
				// System.out.println(device.getName() + "-" +
				// device.getId()
				// + " receives CEMS suggestion: " + (startSecond - ((simDay
				// -
				// 1) * 24 * 3600)));
			} else if (model.getCEMS().getEVChargeAlgorithm() == 3) {
				double[] suggestion = model
						.getCEMS()
						.requestStrictChargeTime(
								simDay,
								(int) (earliestStartSecond - ((simDay - 1) * 24 * 3600)),
								schDuration / 60,
								(int) (latestStartSecond - ((currentDay - 1) * 24 * 3600)),
								battery.getWatt(), model.getFeederID());
				for (int i = 0; i < (earliestStartSecond - ((simDay - 1) * 24 * 3600)) / 300; i++) {
					suggestion[i] = schedule[i];
				}

				updateSchedule(suggestion);
			}
		} else if (lv2Charging && model.getCEMS() != null
				&& model.getCSSSuspended()) {
			// get the priority - can (almost) always charge when they want to -
			// other EVs may have to be rescheduled
			if (DEBUG)
				System.out.println("[Model " + model.getModelID() + "] "
						+ "inform charge time (nonEVCust).");
			ArrayList<Integer> chargeIntervals = new ArrayList<Integer>();
			for (int i = 0; i < schedule.length; i++) {
				if (schedule[i] == 1)
					chargeIntervals.add(i);
			}
			model.getCEMS().informNonEVCustChargeTime(simDay, chargeIntervals,
					battery.getWatt(), true, model.getFeederID());
		}

		taskFixed = true;
	}

	public int getCurrentTaskStartInterval() {
		return startInterval;
	}

	public int getCurrentTaskEndInterval() {
		return endInterval;
	}

	public int getLatestStartSecond() {
		return latestStartSecond;
	}

	public int getLatestEndSecond() {
		return latestEndSecond;
	}

	public int getRequiredChargeDuration() {
		return schDuration;
	}

	public int getModelID() {
		return model.getModelID();
	}

	public boolean getTaskFixed() {
		return taskFixed;
	}

	@Override
	protected void createPowerScheduleFromSchedule() {
		powerSchedule = new double[schedule.length];
		for (int i = 0; i < powerSchedule.length; i++) {
			if (schedule[i] != 0 && schedule[i] != -9999) {
				powerSchedule[i] = lv2Charging ? device.getWatt() : 1100;
			}
		}
	}

	public double[] getExpectedPeakLoad(int atInterval, int updateInterval,
			int syncInterval) {
		double[] duplicate = Arrays.copyOf(schedule, schedule.length);
		int dupDay = currentDay;
		if (atInterval % 288 == 0 || (atInterval - 6) % 288 == 0) {
			if (dupDay++ == Constants.getNumDaysInMonth(currentMonth,
					currentYear)) {
				dupDay = 1;
			}
			Arrays.fill(duplicate, 0, 288, 0);
			for (int i = 288; i < 576; i++) {
				duplicate[i - 288] = (duplicate[i] == 0 ? 0 : -duplicate[i]);
			}
			Arrays.fill(duplicate, 288, 576, 0);
		}

		double[] halfHourPeak = new double[updateInterval * 60 / syncInterval];
		for (int i = 0; i < halfHourPeak.length; i++) {
			double sch = duplicate[atInterval % (24 * 3600 / syncInterval) + i];
			if (sch != 0) {
				halfHourPeak[i] = lv2Charging ? device.getWatt() : 1100;
			}
		}

		return halfHourPeak;
	}
}
