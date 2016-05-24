package no.ntnu.item.smash.sim.model;

import java.util.Arrays;

import no.ntnu.item.smash.sim.data.Constants;
import no.ntnu.item.smash.sim.data.stats.HouseholdStatistics;
import no.ntnu.item.smash.sim.data.utility.LoadDataUtility;
import no.ntnu.item.smash.sim.structure.Device;
import no.ntnu.item.smash.sim.structure.Room;

public class BulkLoadModel extends DeviceModel {

	private double[] loadData;

	public BulkLoadModel(Device device, Room room) {
		super(device, room);
		loadData = new double[24 * 3600 / syncInterval];
	}

	@Override
	public void updateSchedule(double[] schedule) {

	}

	@Override
	public void run() {
		int second = 0;
		// read load data for the first day
		loadData = LoadDataUtility.readLoadDataForDay(model.getModelID(),
				currentDay, currentMonth, currentYear);
		powerSchedule = Arrays.copyOf(loadData, loadData.length);
		model.synchTimeToCSS(device, 0, second / 3600, currentDay,
				currentMonth, currentYear);

		stat.writeYearHeadingToFiles(currentYear);

		while (run) {
			// read what should be generated at a time
			double heatInput = loadData[(second / syncInterval)
					% (24 * 3600 / syncInterval)];

			if (second > 0 && second % syncInterval == syncInterval - 1) {
				int index = second / syncInterval;
				hourlyEnergy[index % (24 * 3600 / syncInterval)] = heatInput
						* (((double) syncInterval) / 3600) * 0.001;
				hourlyPeakLoad[index % (24 * 3600 / syncInterval)] = heatInput;

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
						// read load data for next day
						loadData = LoadDataUtility.readLoadDataForDay(
								model.getModelID(), currentDay, currentMonth,
								currentYear);

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

	public double[] getExpectedPeakLoad(int atInterval, int updateInterval,
			int syncInterval) {
		double[] duplicate = loadData == null ? null : Arrays.copyOf(loadData,
				loadData.length);
		int dupDay = currentDay;
		int dupMonth = currentMonth;
		int dupYear = currentYear;

		if (duplicate == null) {
			duplicate = LoadDataUtility.readLoadDataForDay(model.getModelID(),
					model.getStartDay(), model.getStartMonth(),
					model.getStartYear());
		} else if (atInterval % 288 == 0 || (atInterval - 6) % 288 == 0) {
			if (dupDay++ == Constants.getNumDaysInMonth(dupMonth, dupYear)) {
				dupDay = 1;
				if (dupMonth++ == 12) {
					dupMonth = 1;
					dupYear++;
				}
			}
			duplicate = LoadDataUtility.readLoadDataForDay(model.getModelID(),
					dupDay, dupMonth, dupYear);
		}

		double[] halfHourPeak = new double[updateInterval * 60 / syncInterval];
		for (int i = 0; i < halfHourPeak.length; i++) {
			double load = duplicate[atInterval % (24 * 3600 / syncInterval) + i];
			halfHourPeak[i] = load;
		}

		return halfHourPeak;
	}
}
