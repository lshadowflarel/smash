package no.ntnu.item.smash.sim.model;

import java.util.ArrayList;

import no.ntnu.item.smash.sim.data.Constants;
import no.ntnu.item.smash.sim.data.stats.HouseholdStatistics;
import no.ntnu.item.smash.sim.externalsp.WeatherService;
import no.ntnu.item.smash.sim.structure.BuildingThermalIntegrity;
import no.ntnu.item.smash.sim.structure.Device;
import no.ntnu.item.smash.sim.structure.ElectricWaterHeater;
import no.ntnu.item.smash.sim.structure.Room;
import no.ntnu.item.smash.sim.structure.Schedule;

public class HeaterModel extends DeviceModel {

	/* Constants */
	// Thermal properties
	private double[] thermalProp = BuildingThermalIntegrity.NORWAY_HEAVYINSULATED;

	// Solar radiation (winter)
	private double S = 10000 / (24 * 30); // W/m2 20000
	private double INTERNAL = 50;
	private static final double FRACT_S_WALL = 0.4;
	private static final double FRACT_S_AIR = 0.6;
	private static final double DENS_AIR = 1.225;
	private static final double CP_AIR = 1005.4; // J

	// simulation
	private boolean thermostatOn = false;
	private double roomTemp = 21;
	private double iwallTemp = 21;
	private double ewallTemp = 21;

	// stats
	private double[] dodStat_temp = new double[24 * 12];
	private String[] comfortLVStat_temp = new String[24 * 12];

	public HeaterModel(Device device, Room room, double[] thermalProp) {
		super(device, room);
		configureRoom(room);
		this.thermalProp = thermalProp;
		if (room.getName().startsWith("Bath")) {
			S = 0;
			INTERNAL = 0;
		} else if (room.getName().startsWith("Bed")) {
			S = 5000 / (24 * 30); // 10000
			INTERNAL = 0;
		} else if (room.getName().equals("Kitchen")
				|| room.getName().equals("Guest")
				|| room.getName().equals("Work")) {
			S = 2500 / (24 * 30); // 5000
			INTERNAL = 0;
		}
	}

	public void configureRoom(Room room) {
		schedule = room.getSchedule().getSchedule();
		createPowerScheduleFromSchedule();
		roomTemp = room.getPlannedSchedule().getHourlyPlan()[0];
		iwallTemp = roomTemp;
		ewallTemp = iwallTemp - 1;
	}

	public synchronized void updateSchedule(double[] schedule) {
		this.schedule = schedule;
		room.setSchedule(new Schedule(schedule));
		createPowerScheduleFromSchedule();

		// System.out.println("[POLICY] Schedule changed for " + room.getName()
		// + ".");
	}

	public double getCurrentTemperature() {
		return roomTemp;
	}

	private double getAdjacentRoomTemperature(int second) {
		ArrayList<String> adjRooms = room.getAdjacentRooms();
		double adjRoomTemp = 0;
		for (int i = 0; i < adjRooms.size(); i++) {
			double t = ((HeaterModel) model.getEnvironment()
					.getDeviceModelByName(adjRooms.get(i))).getSchedule()[(second / syncInterval)
					% (24 * 3600 / syncInterval)];
			if (t == -9999) {
				t = ((HeaterModel) model.getEnvironment().getDeviceModelByName(
						adjRooms.get(i))).getRoom().getExtendedSchedule(0,
						currentDay, currentMonth, currentYear)[(second / syncInterval)
						% (24 * 3600 / syncInterval)];
			}

			adjRoomTemp += t;
			// really should be
			// room.getCurrentTemperature()
			// here but we have thread
			// problem
		}
		adjRoomTemp = adjRooms.size() > 0 ? adjRoomTemp / adjRooms.size()
				: iwallTemp;
		return adjRoomTemp;
	}

	@Override
	public void run() {
		int second = 0;
		int heatingOnTime = 0;
		model.synchTimeToCSS(device, 0, second / 3600, currentDay,
				currentMonth, currentYear);

		stat.writeYearHeadingToFiles(currentYear);
		comfortStat.writeYearHeadingToFiles(currentYear);

		double areaEWall = (room.getWidth() * room.getHeight())
				+ (room.getLength() * room.getHeight());
		double areaIWall = areaEWall;
		double areaCeiling = room.getWidth() * room.getLength();
		double areaFloor = areaCeiling;
		double areaWindow = room.getWindowNo() * room.getWindowL()
				* room.getWindowW();

		// mass
		double airMass = 3 * CP_AIR * DENS_AIR * room.getWidth()
				* room.getLength() * room.getHeight();
		double iwallMass = 40881.6 * areaFloor;
		double ewallMass = (400 * areaEWall * 0.02 * 1000 * 2.5)
				+ (1.205 * areaEWall * 0.02 * 1000 * 1.01)
				+ (32 * areaEWall * 0.15 * 1000 * 0.67)
				+ (13 * areaEWall * 0.02 * 1000 * 1.09);

		// u values
		double u_ewall = areaEWall
				/ thermalProp[BuildingThermalIntegrity.R_WALLS_INDEX];
		double u_window = areaWindow
				* thermalProp[BuildingThermalIntegrity.U_WINDOW_INDEX];
		double u_iwall = (room.getDoorArea() / thermalProp[BuildingThermalIntegrity.R_DOOR_INDEX])
				+ (areaIWall / thermalProp[BuildingThermalIntegrity.R_WALLS_INDEX])
				+ (areaCeiling / thermalProp[BuildingThermalIntegrity.R_CEILING_INDEX])
				+ (areaFloor / thermalProp[BuildingThermalIntegrity.R_FLOORS_INDEX]);

		// initializes wall temps
		iwallTemp = (roomTemp + getAdjacentRoomTemperature(second)) / 2;
		ewallTemp = WeatherService.getActualTemp(second, currentDay,
				currentMonth, currentYear) < 0 ? roomTemp
				+ WeatherService.getActualTemp(second, currentDay,
						currentMonth, currentYear)
				: (roomTemp + WeatherService.getActualTemp(second, currentDay,
						currentMonth, currentYear) / 2);

		while (run) {
			// the heater's thermostat detects the difference between the
			// room temperature and target temperature
			double targetTemp = schedule[(second / syncInterval)
					% (24 * 3600 / syncInterval)];
			double difference = roomTemp - targetTemp;

			if (isControlPeriod(second) && model.getContract() == 2) {
				thermostatOn = false;
			} else if (difference < -0.5) {
				thermostatOn = true;
			} else if (difference >= 0.5) {
				thermostatOn = false;
			}

			// when the heater is active (thermostatOn is true), it
			// generates heat input
			double heatInput = 0;
			if (thermostatOn) {
				heatInput = device.getWatt();
				heatingOnTime++;
			}

			// get outdoor temperature
			double oTemp = WeatherService.getActualTemp(second, currentDay,
					currentMonth, currentYear);

			double ewhHeat = 0;
			ArrayList<Device> ewhs = room
					.getDevicesByType(Device.DEVTYPE_EWATERHEATER);

			for (int i = 0; i < ewhs.size(); i++) {
				ElectricWaterHeater ewh = (ElectricWaterHeater) ewhs.get(i);
				double rVal = ewh.getrValue();
				double surfaceArea = (2 * 3.14 * (ewh.getTankDiameter() / 2
						* ewh.getTankDiameter() / 2))
						+ (2 * 3.14 * ewh.getTankDiameter() / 2 * ewh
								.getTankHeight());
				ElectricWaterHeaterModel ewhModel = (ElectricWaterHeaterModel) model
						.getEnvironment().getDeviceModelMap().get(ewh);
				double t = ewhModel.getSchedule()[(second / syncInterval)
						% (24 * 3600 / syncInterval)];
				if (t == -9999) {
					t = ewhModel.getExtendedSchedule(0, currentDay,
							currentMonth, currentYear)[(second / syncInterval)
							% (24 * 3600 / syncInterval)];
				}
				ewhHeat += (1 / rVal) * surfaceArea * (t - roomTemp);
				// * (ewhModel.getTankTemperature() - roomTemp);
			}

			// double correctedHeatInput = heatInput -
			// ((((0.97*1*1)/0.91)-1)*heatInput) - (((1-0.97)/0.97)*heatInput);

			double correctedHeatInput = heatInput
					- ((((4 - (0.94 + 0.8 + 1)) * (1 * 0.97 * 1.05)) - 1) * heatInput)
					- (((1 - 0.8) / 0.8) * heatInput); // radiant heater

			if (room.getName().startsWith("Bath")) {
				correctedHeatInput = heatInput
						- ((((4 - (0.91 + 0.955 + 1)) * (1 * 0.98 * 1.05)) - 1) * heatInput)
						- (((1 - 0.91) / 0.91) * heatInput);// floor heating
			}

			iwallTemp += (1 / iwallMass)
					* ((FRACT_S_WALL * S * areaWindow * 0.5)
							+ (u_iwall * (roomTemp - iwallTemp))
							+ (0.5 * INTERNAL) + (0.5 * ewhHeat)
							+ (0.5 * correctedHeatInput) + (u_iwall * (getAdjacentRoomTemperature(second) - iwallTemp)));
			ewallTemp += (1 / ewallMass)
					* ((u_ewall * (oTemp - ewallTemp))
							+ (u_ewall * (roomTemp - ewallTemp))
							+ (0.5 * ewhHeat) + (0.45 * correctedHeatInput));
			roomTemp += (1 / airMass)
					* ((u_window * (oTemp - roomTemp))
							+ (FRACT_S_AIR * S * areaWindow * 0.5)
							+ (u_ewall * (ewallTemp - roomTemp))
							+ (u_iwall * (iwallTemp - roomTemp))
							+ (0.5 * INTERNAL) + (0 * ewhHeat) + (0.05 * correctedHeatInput));

			if (second > 0 && second % syncInterval == syncInterval - 1) {
				int index = second / syncInterval;
				hourlyEnergy[index % (24 * 3600 / syncInterval)] = (device
						.getWatt() * ((double) heatingOnTime / (double) 3600)) * 0.001;
				hourlyRTemp[index % (24 * 3600 / syncInterval)] = roomTemp;
				hourlyOTemp[index % (24 * 3600 / syncInterval)] = oTemp;
				hourlyPeakLoad[index % (24 * 3600 / syncInterval)] = (heatingOnTime > 0 ? device
						.getWatt() : 0);
				comfortMeasureVals.add((double) Math.round(roomTemp));
				Object[] comfortStats = calculateDoD(comfortMeasureVals,
						"AirTemperature");
				if (comfortStats != null) {
					dodStat_temp[index % (24 * 3600 / syncInterval)] = (Double) comfortStats[0];
					comfortLVStat_temp[index % (24 * 3600 / syncInterval)] = (String) comfortStats[1];
					comfortMeasureVals.clear();

					if (second % (24 * 3600) == (24 * 3600) - 1) {
						comfortStat.addComfortStat(model.getEnvironment()
								.getComfortMeasureByName("AirTemperature"),
								dodStat_temp, comfortLVStat_temp);
					}
				}

				heatingOnTime = 0;

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
					stat.addRTempData(hourlyRTemp);
					stat.addPlanData(room.getPlannedSchedule().getHourlyPlan(),
							true);
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
						if (second + 1 < simTime) {
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
						double[] plan = new double[24];
						System.arraycopy(room.getPlannedSchedule()
								.getHourlyPlan(), 0, plan, 0, plan.length);
						room.setSchedule(new Schedule(Schedule
								.createHighResolutionSchedule(plan)));
						schedule = room.getSchedule().getSchedule();
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
