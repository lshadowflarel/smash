package no.ntnu.item.smash.sim.model;

import java.util.Arrays;

import no.ntnu.item.smash.css.em.EnergyManagement;
import no.ntnu.item.smash.sim.data.Constants;
import no.ntnu.item.smash.sim.data.stats.ComfortStatistics;
import no.ntnu.item.smash.sim.data.stats.ElectricWaterHeaterStatistics;
import no.ntnu.item.smash.sim.data.stats.HouseholdStatistics;
import no.ntnu.item.smash.sim.data.utility.WaterDemandUtility;
import no.ntnu.item.smash.sim.externalsp.DSO;
import no.ntnu.item.smash.sim.externalsp.WeatherService;
import no.ntnu.item.smash.sim.structure.Device;
import no.ntnu.item.smash.sim.structure.ElectricWaterHeater;
import no.ntnu.item.smash.sim.structure.Plan;
import no.ntnu.item.smash.sim.structure.Room;
import no.ntnu.item.smash.sim.structure.Schedule;

public class ElectricWaterHeaterModel extends DeviceModel {

	// constants
	private static final double waterDensity = 1; // kg/litre
	private static final double cp = 4186; // J/Ckg

	// variables
	private double tankHeight; // m
	private double tankDiameter; // m
	private double tankRadius; // m
	private double tankVolume; // litre
	private double surfaceArea; // m^2
	private double rValue;
	private double g;
	private double c; // J/C
	private double tankTemp; // C
	private double ambientTemp; // C (room temp)
	private double inputTemp;
	private double rdash; // watt/C
	private double b; // watt/C
	private double[][] hourlyWaterDemand;
	private String wdemandData;
	private double waterDemand; // litre/s
	private double wattage;

	private double energyInput = 0; // watt

	// statistics
	private double[] hourlyWaterTemp = new double[(24 * 3600 / syncInterval)];
	private double[] waterDemandStat = new double[(24 * 3600 / syncInterval)];
	private double[] dodStat_temp = new double[24 * 12];
	private String[] comfortLVStat_temp = new String[24 * 12];

	public ElectricWaterHeaterModel(Device device, Room room, Plan plan,
			double targetTemp, String wdemandData, ElectricWaterHeater wh) {
		super(device, room);
		this.plan = plan.getHourlyPlan();
		this.schedule = Schedule.createHighResolutionSchedule(Arrays.copyOf(
				this.plan, this.plan.length));
		createPowerScheduleFromSchedule();
		this.wdemandData = wdemandData;
		this.tankTemp = this.schedule[0];

		configureWaterHeater(wh);
		calculateProperties();
	}

	private void configureWaterHeater(ElectricWaterHeater wh) {
		tankHeight = wh.getTankHeight();
		tankDiameter = wh.getTankDiameter();
		tankVolume = wh.getTankVolume();
		wattage = wh.getWatt();
		rValue = wh.getrValue();
	}

	private void calculateProperties() {
		tankRadius = tankDiameter / 2;
		surfaceArea = (2 * 3.14 * (tankRadius * tankRadius))
				+ (2 * 3.14 * tankRadius * tankHeight);
		g = surfaceArea / rValue;
		c = tankVolume * waterDensity * cp;
	}

	public void setStatistics(String path, String name) {
		stat = new ElectricWaterHeaterStatistics(path, name);
		comfortStat = new ComfortStatistics(path, name);
	}

	public double[] getSchedule() {
		return schedule;
	}

	public double getTankTemperature() {
		return tankTemp;
	}

	@Override
	public void updateSchedule(double[] schedule) {
		this.schedule = schedule;
		createPowerScheduleFromSchedule();
		// System.out.println("EWH schedule updated.");
	}

	public boolean isWorking() {
		return energyInput > 0 ? true : false;
	}

	// TODO: will have a problem on the first day of the simulation and last day
	// of the month
	public double[] getExtendedSchedule(int extendedHours, int day, int month,
			int year) {
		double[] exschedule = new double[(24 * 3600 / syncInterval)
				+ (2 * (3600 / syncInterval) * extendedHours)];

		double[] rSch = room.getExtendedSchedule(extendedHours, day, month,
				year);
		double[] rSchedule = Arrays.copyOf(rSch, (24 * 3600 / syncInterval));

		double[] whSchedule = Arrays.copyOf(this.schedule,
				(24 * 3600 / syncInterval));

		int j = 0;
		for (int i = extendedHours; i > 0; i--) {
			double expectedTemp = whSchedule[(24 * 3600 / syncInterval)
					- (extendedHours * (3600 / syncInterval))];
			if (expectedTemp == -9999) {
				expectedTemp = whSchedule[(23 * 3600 / syncInterval)
						- (extendedHours * (3600 / syncInterval))]
						- EnergyManagement
								.estimatedWaterTempDrop(
										WeatherService.getActualTemp(
												(24 - i) * 3600,
												day - 1 == 0 ? 1 : day - 1,
												month, year),
										rSchedule[(23 * 3600 / syncInterval)
												- (extendedHours * (3600 / syncInterval))],
										tankHeight,
										tankDiameter,
										tankVolume,
										rValue,
										whSchedule[(23 * 3600 / syncInterval)
												- (extendedHours * (3600 / syncInterval))],
										3600);

				for (int y = 0; y < 3600 / syncInterval; y++)
					whSchedule[(24 * 3600 / syncInterval)
							- (extendedHours * (3600 / syncInterval)) + y] = expectedTemp;
			}

			for (int x = 0; x < 3600 / syncInterval; x++)
				exschedule[j * (3600 / syncInterval) + x] = expectedTemp;
			j++;
		}

		for (int i = 0; i < 24; i++) {
			double expectedTemp = whSchedule[i * (3600 / syncInterval)];
			if (expectedTemp == -9999) {
				expectedTemp = whSchedule[i == 0 ? 23 * (3600 / syncInterval)
						: (i - 1) * (3600 / syncInterval)]
						- EnergyManagement.estimatedWaterTempDrop(
								WeatherService.getActualTemp(i * 3600, day,
										month, year),
								rSchedule[i == 0 ? 23 * (3600 / syncInterval)
										: (i - 1) * (3600 / syncInterval)],
								tankHeight, tankDiameter, tankVolume, rValue,
								whSchedule[i == 0 ? 23 * (3600 / syncInterval)
										: (i - 1) * (3600 / syncInterval)],
								3600);

				for (int y = 0; y < (3600 / syncInterval); y++)
					whSchedule[i * (3600 / syncInterval) + y] = expectedTemp;
			}

			for (int x = 0; x < (3600 / syncInterval); x++)
				exschedule[(i + extendedHours) * (3600 / syncInterval) + x] = expectedTemp;
		}

		for (int i = 24 + extendedHours; i < 2 * extendedHours + 24; i++) {
			double expectedTemp = whSchedule[(i - extendedHours)
					* (3600 / syncInterval) % (24 * 3600 / syncInterval)];
			if (expectedTemp == -9999) {
				expectedTemp = whSchedule[(i == 24 + extendedHours ? 23 * (3600 / syncInterval)
						: (i - extendedHours) * (3600 / syncInterval))
						% (24 * 3600 / syncInterval) - (3600 / syncInterval)]
						- EnergyManagement
								.estimatedWaterTempDrop(
										WeatherService
												.getActualTemp(
														(i - extendedHours)
																* (3600 / syncInterval)
																% (24 * 3600 / syncInterval),
														day + 1 > Constants
																.getNumDaysInMonth(
																		month,
																		year) ? day
																: day + 1,
														month, year),
										rSchedule[(i == 24 + extendedHours ? 23 * (3600 / syncInterval)
												: (i - extendedHours)
														* (3600 / syncInterval))
												% (24 * 3600 / syncInterval)
												- (3600 / syncInterval)],
										tankHeight,
										tankDiameter,
										tankVolume,
										rValue,
										whSchedule[(i == 24 + extendedHours ? 23 * (3600 / syncInterval)
												: (i - extendedHours)
														* (3600 / syncInterval))
												% (24 * 3600 / syncInterval)
												- (3600 / syncInterval)], 3600);

				for (int y = 0; y < (3600 / syncInterval); y++)
					whSchedule[(i - extendedHours) * (3600 / syncInterval)
							% (24 * 3600 / syncInterval) + y] = expectedTemp;
			}

			for (int x = 0; x < (3600 / syncInterval); x++)
				exschedule[i * (3600 / syncInterval) + x] = expectedTemp;
		}

		return exschedule;
	}

	@Override
	public void run() {
		hourlyWaterDemand = WaterDemandUtility.readWaterDemand(wdemandData,
				currentDay);

		int second = 0;
		int heatingOnTime = 0;
		double wDemand = 0;

		model.synchTimeToCSS(device, 0, second / 3600, currentDay,
				currentMonth, currentYear);

		stat.writeYearHeadingToFiles(currentYear);
		comfortStat.writeYearHeadingToFiles(currentYear);

		while (run) {
			// the heater's thermostat detects the different between the
			// tank water temperature and target temperature
			double difference = tankTemp
					- schedule[(second / syncInterval)
							% (24 * 3600 / syncInterval)];

			if (isControlPeriod(second)
					&& (model.getContract() == DSO.CONTRACT_RTPDLC || model
							.getContract() == DSO.CONTRACT_RTPDLCEV)) {
				schedule[(second / syncInterval) % (24 * 3600 / syncInterval)] = 0;
				energyInput = 0;
			} else if (difference >= 1.5) {
				energyInput = 0;
			} else if (difference <= -1.5) {
				energyInput = wattage;
			}

			if (energyInput == wattage)
				heatingOnTime++;

			// the tank water temperature fluctuates according to several
			// factors
			if (second < simTime
					&& second % 3600 < 60 * hourlyWaterDemand[1][(second / 3600) % 24])
				waterDemand = hourlyWaterDemand[0][(second / 3600) % 24]
						/ (60 * hourlyWaterDemand[1][(second / 3600) % 24]);
			else
				waterDemand = 0;

			wDemand += waterDemand;
			// if(wDemand>0) System.out.println("wd "+ second);
			ambientTemp = room.getSchedule().getSchedule()[(second / syncInterval)
					% (24 * 3600 / syncInterval)];
			if (ambientTemp == -9999)
				ambientTemp = room.getExtendedSchedule(0, currentDay,
						currentMonth, currentYear)[(second / syncInterval)
						% (24 * 3600 / syncInterval)]; // this should be current
														// temp but the
														// threading
														// causes inconsistency
														// problem so to be able
														// to
														// reproduce the same
														// value
														// we use this instead
			inputTemp = WeatherService.getActualLakeTemp(second, currentDay,
					currentMonth, currentYear);
			b = waterDensity * waterDemand * cp;
			rdash = 1.0 / (b + g);

			tankTemp = (tankTemp * Math.exp(-1.0 * (1.0 / (rdash * c))))
					+ ((g * rdash * ambientTemp + b * rdash * inputTemp + energyInput
							* rdash) * (1.0 - Math.exp(-1.0
							* (1.0 / (rdash * c)))));

			if (second > 0 && second % syncInterval == syncInterval - 1) {
				int atHour = second / syncInterval;

				hourlyEnergy[atHour % (24 * 3600 / syncInterval)] = (wattage
						* heatingOnTime / 3600) * 0.001;
				hourlyWaterTemp[atHour % (24 * 3600 / syncInterval)] = tankTemp;
				waterDemandStat[atHour % (24 * 3600 / syncInterval)] = wDemand;
				hourlyOTemp[atHour % (24 * 3600 / syncInterval)] = inputTemp;
				hourlyPeakLoad[atHour % (24 * 3600 / syncInterval)] = (heatingOnTime > 0 ? device
						.getWatt() : 0);
				comfortMeasureVals.add((double)Math.round(tankTemp));
				comfortMeasureVals.add(heatingOnTime > 0 ? 1.0 : 0.0);
				Object[] comfortStats = calculateDoD(comfortMeasureVals,
						"WaterCondition");
				if (comfortStats != null) {
					dodStat_temp[atHour % (24 * 3600 / syncInterval)] = (Double) comfortStats[0];
					comfortLVStat_temp[atHour % (24 * 3600 / syncInterval)] = (String) comfortStats[1];
					comfortMeasureVals.clear();

					if (second % (24 * 3600) == (24 * 3600 - 1)) {
						comfortStat.addComfortStat(model.getEnvironment()
								.getComfortMeasureByName("WaterCondition"),
								dodStat_temp, comfortLVStat_temp);
					}
				}

				heatingOnTime = 0;
				wDemand = 0;

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
					((ElectricWaterHeaterStatistics) stat)
							.addWaterTempData(hourlyWaterTemp);
					((ElectricWaterHeaterStatistics) stat)
							.addWaterDemandData(waterDemandStat);
					stat.addPlanData(plan, true);
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
					model.getEnergyMonitor().addOTempDataAtDay(
							currentDay,
							HouseholdStatistics.aggregateData(hourlyOTemp,
									3600 / syncInterval, true));
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

					if (second + 1 < simTime) {
						hourlyWaterDemand = WaterDemandUtility.readWaterDemand(
								wdemandData, currentDay);
						schedule = Schedule.createHighResolutionSchedule(Arrays
								.copyOf(plan, plan.length));
						createPowerScheduleFromSchedule();
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

				// try {
				// Thread.sleep(50);
				// } catch (InterruptedException e) {
				//
				// }
			}

			if (++second == simTime) {
				run = false;
				stat.writeDataToFiles(currentMonth);
				comfortStat.writeDataToFiles(currentMonth);
				model.informSimDone(currentDay, currentMonth, currentYear);
			}
		}
	}
}
