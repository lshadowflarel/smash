package no.ntnu.item.smash.sim.model;

import java.util.Arrays;

import no.ntnu.item.smash.sim.data.Constants;
import no.ntnu.item.smash.sim.data.stats.HouseholdStatistics;
import no.ntnu.item.smash.sim.structure.Device;
import no.ntnu.item.smash.sim.structure.Plan;
import no.ntnu.item.smash.sim.structure.Room;
import no.ntnu.item.smash.sim.structure.Schedule;

public class LightModel extends DeviceModel {

	/* Variables */
	private int lightID;
	private double[] plan;
	private double[] schedule;

	// simulation
	private boolean lightOn; // 5 min resolution

	public LightModel(Device device, Room room, Plan plan) {
		super(device, room);
		this.lightID = device.getId();
		this.plan = plan.getHourlyPlan();
		this.schedule = Schedule.createHighResolutionSchedule(Arrays.copyOf(
				this.plan, this.plan.length));
		// room.getIlluminanceStat().setLumen(((Light)device).getDesignLumen());
	}

	public double[] getSchedule() {
		return schedule;
	}

	public synchronized void updateSchedule(double[] schedule) {
		this.schedule = schedule;

		System.out.println("[POLICY] Schedule changed for " + room.getName()
				+ " light" + lightID + " " + currentDay);
	}

	@Override
	public void run() {
		int second = 0;
		int lightOnTime = 0;
		boolean addLight = true;
		model.synchTimeToCSS(device, 0, second / 3600, currentDay, currentMonth,
				currentYear);

		stat.writeYearHeadingToFiles(currentYear);

		while (run) {

			if (isControlPeriod(second) && model.getContract()==2) {
				lightOn = false;
			} else if (schedule[(second / 300) % 288] == 0) {
				lightOn = false;
				if (!addLight) {
					room.getIlluminanceStat().removeLightNum(lightID);
					addLight = true;
				}
			} else if (schedule[(second / 300) % 288] > 0) {
				lightOn = true;
				if (addLight) {
					room.getIlluminanceStat().addLightNum(lightID);
					addLight = false;
				}
			}

			// when the light is on (lightOn is true), it generates input
			if (lightOn) {
				lightOnTime++;
			}

			if (second > 0 && second % syncInterval == syncInterval - 1) {
				int index = second / syncInterval;
				hourlyEnergy[index % (24 * 3600 / syncInterval)] = (device
						.getWatt() * ((double) lightOnTime / (double) 3600)) * 0.001;
				// System.out.println(hourlyEnergy[index%288]);
				hourlyPeakLoad[index % (24 * 3600 / syncInterval)] = (lightOnTime > 300 * 0.2 ? device
						.getWatt() : 0);
				room.getIlluminanceStat().setIlluminance(
						index % (24 * 3600 / syncInterval));
				// System.out.println(room.getIlluminanceStat().getIlluminance());

				lightOnTime = 0;

				// sync to the EventManager
				while (!model.onStandby()) {
					try {
						Thread.sleep(5);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				model.getLock().periodicSync(model, device.getName(), index);

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
							* hourlyEnergy[index % (24 * 3600 / syncInterval)];         
				}
			}

			// if we reach 1 hour
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
					model.synchTimeToCSS(device, (second+1)%3600==0?0:30, (second + 1) / 3600,
							currentDay, currentMonth, currentYear);
				}

				// if we reach 24 hours
				if (second % (24 * 3600) == (24 * 3600 - 1)) {
					// System.out.println("HH"+currentDay);
					// store stat data
					stat.addEnergyData(hourlyEnergy);
					// stat.addRTempData(hourlyRTemp);

					stat.addPlanData(plan, true);
					stat.addScheduleData(schedule);
					stat.addPeakLoadData(hourlyPeakLoad);
					room.getIlluminanceStat().addIllumData();
					// stat.addIlluminanceData(room.getIlluminanceStat().getHourlyIlluminance());

					model.getRoomStat().addEnergyDataAtDay(
							room,
							currentDay,
							HouseholdStatistics.aggregateData(hourlyEnergy, 12,
									false));
					model.getRoomStat().addCostDataAtDay(
							room,
							currentDay,
							HouseholdStatistics.aggregateData(hourlyCost, 12,
									false));
					model.getRoomStat().addPeakLoadDataDay(
							room,
							currentDay,
							HouseholdStatistics.aggregateData(hourlyPeakLoad,
									12, false));

					model.getEnergyMonitor().addEnergyDataAtDay(
							currentDay,
							HouseholdStatistics.aggregateData(hourlyEnergy, 12,
									false));
					model.getEnergyMonitor().addCostDataAtDay(
							currentDay,
							HouseholdStatistics.aggregateData(hourlyCost, 12,
									false));
					model.getEnergyMonitor().addPriceDataAtDay(
							currentDay,
							HouseholdStatistics.aggregateData(hourlyPrice, 12,
									true));
					model.getEnergyMonitor().addPeakLoadDataDay(
							currentDay,
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

					if (second + 1 < simTime) {
						schedule = Schedule.createHighResolutionSchedule(Arrays
								.copyOf(plan, plan.length));
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
}
