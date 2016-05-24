package no.ntnu.item.smash.sim.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import no.ntnu.item.smash.css.core.SystemContext;
import no.ntnu.item.smash.css.em.EnergyManagement;
import no.ntnu.item.smash.sim.core.EventManager.SyncLock;
import no.ntnu.item.smash.sim.data.Constants;
import no.ntnu.item.smash.sim.data.stats.EVStatistics;
import no.ntnu.item.smash.sim.data.stats.EnergyUsageStatistics;
import no.ntnu.item.smash.sim.data.stats.EntityStatistics;
import no.ntnu.item.smash.sim.data.stats.HouseholdStatistics;
import no.ntnu.item.smash.sim.data.stats.RoomStatistics;
import no.ntnu.item.smash.sim.externalsp.CEMS;
import no.ntnu.item.smash.sim.externalsp.DSO;
import no.ntnu.item.smash.sim.externalsp.DSO.PricingContract;
import no.ntnu.item.smash.sim.externalsp.WeatherService;
import no.ntnu.item.smash.sim.model.DeviceModel;
import no.ntnu.item.smash.sim.model.ElectricWaterHeaterModel;
import no.ntnu.item.smash.sim.model.EVChargerModel;
import no.ntnu.item.smash.sim.model.WasherDryerModel;
import no.ntnu.item.smash.sim.structure.CLCRequest;
import no.ntnu.item.smash.sim.structure.ComfortMeasure;
import no.ntnu.item.smash.sim.structure.Device;
import no.ntnu.item.smash.sim.structure.ElectricWaterHeater;
import no.ntnu.item.smash.sim.structure.FeedbackList;
import no.ntnu.item.smash.sim.structure.PowerReductionRequest;
import no.ntnu.item.smash.sim.structure.Room;
import no.ntnu.item.smash.sim.structure.WashingMachineDryer;

public class SimulationModel {

	private int modelID;
	private int feederID;
	private SimEnvironment environment;
	private CEMS cems = null;
	private EventManager eventMan;
	private int contract;
	private SystemContext css;
	private RoomStatistics roomStat;
	private HouseholdStatistics monitor;
	private FeedbackList feedbacks = new FeedbackList();;
	private int simDone = 0;
	private int numSimModel = 0;
	private int hourDone = 0;
	private int monthDone = 0;
	public int runningHour;
	protected int runningDay;
	protected int runningMonth;
	protected int runningYear;
	private boolean cssSuspended = true;
	public boolean synced = false;
	private ExecutorService executorService;
	private boolean started = false;
	private SyncLock lock;
	private boolean standby = true;
	private int waitForCSS = -1; // -1=not waiting, 0=CLC decision made

	private int simTime;
	private int startDay;
	private int startMonth;
	private int startYear;
	private int syncInterval = 300;
	private String resultDir;

	public SimulationModel() {

	}

	public SimulationModel(SimEnvironment environment) {
		this.environment = environment;
	}

	public SimulationModel(SimEnvironment environment, int simTime,
			int syncInterval, int currentDay, int currentMonth,
			int currentYear, String resultDirectory) {
		this.environment = environment;
		this.simTime = simTime;
		this.startDay = currentDay;
		this.startMonth = currentMonth;
		this.startYear = currentYear;
		this.syncInterval = syncInterval;
		this.resultDir = resultDirectory;
	}

	public int getModelID() {
		return modelID;
	}

	public void setModelID(int id) {
		modelID = id;
	}
	
	public int getFeederID() {
		return feederID;
	}
	
	public void setFeederID(int id) {
		feederID = id;
	}

	public int getContract() {
		return contract;
	}

	public void setContract(int type) {
		contract = type;
	}

	public SimEnvironment getEnvironment() {
		return environment;
	}

	public void setEnvironment(SimEnvironment environment) {
		this.environment = environment;
	}

	public EventManager getEventManager() {
		return eventMan;
	}

	public void setEventManager(EventManager eventMan) {
		this.eventMan = eventMan;
	}

	public boolean getCSSSuspended() {
		return cssSuspended;
	}

	public SystemContext getCSS() {
		return css;
	}

	public void setCSS(SystemContext css) {
		this.css = css;
	}

	public void setExecutorService(ExecutorService ex) {
		executorService = ex;
	}

	public FeedbackList getFeedbacks() {
		return feedbacks;
	}

	public void setLock(SyncLock lock) {
		this.lock = lock;
	}

	public SyncLock getLock() {
		return lock;
	}

	public void setStandby(boolean standby) {
		this.standby = standby;
	}

	public boolean onStandby() {
		return standby;
	}

	public boolean isStarted() {
		return started;
	}

	public String getResultDir() {
		return resultDir;
	}

	public int getStartDay() {
		return startDay;
	}

	public int getStartMonth() {
		return startMonth;
	}

	public int getStartYear() {
		return startYear;
	}

	public int getSyncInterval() {
		return syncInterval;
	}

	public void startSimulation() {
		started = true;
		startSimulation(simTime, startDay, startMonth, startYear, resultDir);
	}

	public void startSimulation(int simTime, int currentDay, int currentMonth,
			int currentYear, String resultDirectory) {
		monitor = new HouseholdStatistics(resultDirectory);
		monitor.setStartDay(currentDay);
		roomStat = new RoomStatistics(resultDirectory);
		roomStat.setStartDay(currentDay);

		for (int i = 0; i < environment.getRooms().size(); i++) {
			roomStat.addRoom(environment.getRooms().get(i));
		}

		updateCurrentYear(currentYear);

		// start all DeviceModels
		HashMap<Device, DeviceModel> modelMap = environment.getDeviceModelMap();
		numSimModel = modelMap.size();
		hourDone = numSimModel;
		monthDone = numSimModel;
		simDone = numSimModel;

		runningDay = currentDay;
		runningMonth = currentMonth;
		runningYear = currentYear;

		Set<Device> keys = modelMap.keySet();
		for (Device key : keys) {
			DeviceModel dm = modelMap.get(key);

			dm.setSimParent(this);
			dm.setCurrentDay(currentDay);
			dm.setCurrentMonth(currentMonth);
			dm.setCurrentYear(currentYear);
			dm.setStatistics(resultDirectory, dm.getDevice().getName());
			dm.startSim(simTime, syncInterval, eventMan.getDSO()
					.getPriceChangeInterval(contract));
			executorService.execute(dm);
		}
	}

	public void updateCurrentYear(int year) {
		if (runningYear != year) {
			runningYear = year;
			monitor.writeYearHeadingToFiles(year);
			// roomStat.writeYearHeadingToFiles(year);
		}
	}

	public synchronized void informMonthReached(int month) { 
		if (--monthDone == 0) {
			monitor.writeDataToFiles(month);
			// roomStat.writeDataToFiles(month);
			monthDone = numSimModel;
		}
	}

	public synchronized void informSimDone(int day, int month, int year) {
		cssSuspended = true;

		// System.out.println("Compiling result data. " + simDone);

		if (--simDone == 0) {
			monitor.writeDataToFiles(month);
			// roomStat.writeDataToFiles(month);

			System.out.println("Simulation ended (" + day + "/" + month + "/"
					+ year + ")");

			eventMan.informHouseDone(this);
		}
	}

	public HouseholdStatistics getEnergyMonitor() {
		return monitor;
	}

	public RoomStatistics getRoomStat() {
		return roomStat;
	}

	public void setCSSSuspended(boolean suspended) {
		cssSuspended = suspended;
	}

	public synchronized void checkPowerUsage(int min, int hour) { 
		if((hour>0 || (hour==0 && min>0)) && !cssSuspended) {
			if (hour > 0 || (hour >= 0 && min > 0)) {
				double currentPeakLoad = getPeakload((hour * 12)
						+ (min / 5) - 1);
				double allowedPeakLoad = (contract == DSO.CONTRACT_RTPCPP
						|| contract == DSO.CONTRACT_RTPCPPEV ? 11000
						: 99999);
				
				if (currentPeakLoad > allowedPeakLoad) {
					Calendar cal = Calendar.getInstance();
					cal.set(Calendar.HOUR_OF_DAY, hour);
					cal.set(Calendar.MINUTE, min);
					cal.set(Calendar.DAY_OF_MONTH, runningDay);
					cal.set(Calendar.MONTH, runningMonth-1);
					cal.set(Calendar.YEAR, runningYear);
					
					css.getCss().notifyPowerLimitReached(
							currentPeakLoad - allowedPeakLoad,
							currentPeakLoad, cal.getTime().toString());
				}
			}
		}
	}
	
	public synchronized void synchTimeToCSS(Device dev, int min, int hour,
			int day, int month, int year) {
		//if(day==31) System.out.println("[Model# " + modelID + "] " + dev.getName() + " " + hour + " " + hourDone + " "
		 //+ synced + " " + numSimModel);
		if (--hourDone > 0) {
			synchronized (this) {
				while (!synced) {
					try {
						this.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

				if (++hourDone == numSimModel) {
					// System.out.println("Synced = false (" + hourDone + "/" +
					// numSimModel + ")");
					synced = false;
				}
				//if(day==31) System.out.println("[Model# " + modelID + "] " + dev.getName() + " OUT! (" + hourDone + " "
				 //+ ")");
			}
		} else {
			runningHour = hour;
			runningDay = day;
			runningMonth = month;
			runningYear = year;

			if (cssSuspended) {
				synced = true;
				++hourDone;
				if (numSimModel > 1)
					notifyAll();
				if (hourDone == numSimModel) {
					// System.out.println("Synced = false (" + hourDone + "/" +
					// numSimModel + ")");
					synced = false;
				}
			} else {
				css.getCss().syncTime(min, hour, day, month, year);
				if (hour > 0 || (hour >= 0 && min > 0)) {
					double currentPeakLoad = getPeakload((hour * 12)
							+ (min / 5) - 1);
					double allowedPeakLoad = (contract == DSO.CONTRACT_RTPCPP
							|| contract == DSO.CONTRACT_RTPCPPEV ? 15000
							: 99999); 
					if (currentPeakLoad > allowedPeakLoad) {
						Calendar cal = Calendar.getInstance();
						cal.set(Calendar.HOUR_OF_DAY, hour);
						cal.set(Calendar.MINUTE, min);
						cal.set(Calendar.DAY_OF_MONTH, day);
						cal.set(Calendar.MONTH, month-1);
						cal.set(Calendar.YEAR, year);
						
						css.getCss().notifyPowerLimitReached(
								currentPeakLoad - allowedPeakLoad,
								currentPeakLoad, cal.getTime().toString());
					}
				}
				synchronized (this) {
					while (!synced) {
						try {
							this.wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					if (++hourDone == numSimModel) {
						// System.out.println("Synced = false (" + hourDone +
						// "/" + numSimModel + ")");
						synced = false;
					}
					// System.out.println(dev.getName() + " OUT! (" + hourDone +
					// ")");
				}
			}
		}
	}

	// NOTE: it is assumed that each controlled entity is turned off the
	// entire control duration (not like entity1 is off for half the
	// duration and entity2 is off the other half)
	public double sendControlRequestToCSS(PowerReductionRequest req) { 		
		if (req.getId().startsWith("CLC")) {
			HashMap<String, Object> data = new HashMap<String, Object>();
			data.put("id", req.getId());
			data.put("startTime", req.getStartInterval()); // when this is 0 it
															// means that the
															// control starts at
															// 00:00 the next
															// day
			data.put("endTime", req.getEndInterval());
			data.put("powerToReduce", ((CLCRequest) req).getKW());
			data.put("timeToReduce", (req.getEndInterval()>req.getStartInterval()?
					(req.getEndInterval() - req.getStartInterval()) * 5:((req.getEndInterval()+288)-req.getStartInterval())*5));

			css.getCss().remoteEvent("DSO", req.getStartInterval(), data);
			synchronized (this) {
				while (waitForCSS != 0) {
					try {
						this.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			if (feedbacks.getFeedbacks().get(req.getId()) != null) {
				// log the request and the response
				String[] entities = ((String) feedbacks.getFeedback(req.getId())
						.getSpecificFeedback("entities")).split(";");
				logDSOControlRequest(req.getStartInterval(), req.getEndInterval(),
						runningDay, runningMonth, entities);

				return Double.parseDouble((String)feedbacks.getAndRemoveFeedback(req.getId())
						.getSpecificFeedback("amountReduced"));
			}
		} else {
			DeviceModel dm = environment.getDeviceModelByName(
					environment.getDLCDevice().getName());
			if(dm.getCurrentControlRequest()==null || req.getStartInterval()>dm.getCurrentControlRequest().getEndInterval()) {
				dm.setPowerReductionRequest(req);
				HashMap<String, Object> fb = new HashMap<String, Object>();
				
				String[] dlcDev = new String[1];
				dlcDev[0] = environment.getDLCDevice().getName();
				fb.put("entities", dlcDev);
				feedbacks.addFeedback(req.getId(), fb);
				
				// log the request and the response
				logDSOControlRequest(req.getStartInterval(), req.getEndInterval(),
						runningDay, runningMonth, (String[]) feedbacks
								.getAndRemoveFeedback(req.getId())
								.getSpecificFeedback("entities"));

				return environment.getDLCDevice().getWatt();
			} 
			
		}
		
		return 0;
	}

	public void taskDoneNotification(int taskCode) {
		if (taskCode == 1) {
			synchronized (this) {
				synced = true;
				notifyAll();
			}
		} else if (taskCode == 2) {
			synchronized (this) {
				waitForCSS = 0;
				notify();
			}
		}
	}

	public void startRescheduling(int deviceType, String entity, int newValue,
			int duration, int startHour, int startMin, Date startTime,
			int taskID) {
		Room room = getEnvironment().getRoom(entity);

		if (deviceType == Device.DEVTYPE_HEATER) {
			double[] sch = room.getSchedule().getSchedule();
			int begin = startHour * 12 + startMin / 5;
			int slot = duration / 5 + (duration % 5 == 0 ? 0 : 1);
			for (int i = 0; i < slot && begin + i< sch.length; i++) {
				sch[begin + i] = newValue;
			}

			for (Device dev : room.getDevicesByType(deviceType)) {
				DeviceModel devModel = getEnvironment().getDeviceModelMap()
						.get(dev);
				devModel.updateSchedule(room.getSchedule().getSchedule());
			}
		} else if (deviceType == Device.DEVTYPE_EWATERHEATER) {
			for (Device dev : room.getDevicesByType(deviceType)) {
				DeviceModel devModel = getEnvironment().getDeviceModelMap()
						.get(dev);
				double[] sch = ((ElectricWaterHeaterModel) devModel)
						.getSchedule();
				int begin = startHour * 12 + startMin / 5;
				for (int i = 0; i < duration / 5 && begin + i < sch.length; i++) {
					sch[begin + i] = newValue;
				}
				devModel.updateSchedule(sch);
			}
		} else if (deviceType == Device.DEVTYPE_EVCHARGER) { 
			//System.out.println("[Model " + this.getModelID() + "] " + "Reschedule " + startHour + " " + startMin + " " + duration + " (" + taskID + ")");
			// note that you can end up scheduling to start charging on the next
			// day
			// also you can really charge for a very long time
			// here we solve the problem by creating a schedule array with
			// length 576 (5-min resolution / 2 days)
			DeviceModel devModel = getEnvironment()
					.getDeviceModelByName(entity);
			double[] sch = devModel.getSchedule();

			double[] schLong = Arrays.copyOf(sch, sch.length);

			int begin = startHour * 12 + startMin / 5;
			Calendar cal = Calendar.getInstance();
			cal.setTime(startTime);
			if (cal.get(Calendar.DAY_OF_MONTH) > runningDay || cal.get(Calendar.MONTH)+1 > runningMonth)
				begin += 288;

			for (int i = 0; i <= (duration % 5 == 0 ? duration / 5
					: duration / 5 + 1); i++) {
				if (begin + i >= 576)
					break;
				schLong[begin + i] = taskID;
			}
			devModel.updateSchedule(schLong);
		} else { // generic devices
			DeviceModel devModel = getEnvironment()
					.getDeviceModelByName(entity);
			double[] sch = devModel.getSchedule();

			for (int i = 0; i < sch.length; i++) {
				if (sch[i] == taskID)
					sch[i] = 0;
			}

			int begin = startHour * 12 + startMin / 5;
			for (int i = 0; i < duration / 5; i++) {
				if (begin + i >= 288)
					break;
				sch[begin + i] = taskID;
			}
			devModel.updateSchedule(sch);
		}
	}

	// public void respondULCRequest(String id, int code, HashMap<String,Object>
	// data) {
	// switch(code) {
	// case 1: //OK
	// System.out.println("ULC request ID#" + id + " fulfilled");
	// break;
	// case 0: //REJECT
	// System.out.println("ULC request ID#" + id + " rejected");
	// double penalty = (Double)data.get("penalty");
	// double[] added = new double[24];
	// added[((ULCRequest)DSO.getControlRequest(id)).getStartHour()] = penalty;
	// getEnergyMonitor().addCostData(added);
	//
	// break;
	// case 2: //PARTIAL
	// System.out.println("ULC request ID#" + id + " partially fulfilled");
	// break;
	// }
	// }

	@SuppressWarnings("unchecked")
	public HashMap<String, Object> requestRemoteData(String provider,
			String operation, HashMap<String, Object> parameters) {
		HashMap<String, Object> returnVal = new HashMap<String, Object>();

		if (provider.equals("DSO")) {
			if (operation.equals("getEstElectricityPrice")) {
				int time = Integer.parseInt((String) parameters.get("time"));
				int day = Integer.parseInt((String) parameters.get("day"));
				int month = Integer.parseInt((String) parameters.get("month"));
				int year = Integer.parseInt((String) parameters.get("year"));
				returnVal.put("dso-price",
						eventMan.getDSO().getContract(contract)
								.getActGridPriceAtTime(time, day, month, year));
				returnVal
						.put("esp-price",
								eventMan.getDSO()
										.getContract(contract)
										.getEstEnergyPriceAtTime(time, day,
												month, year));
				returnVal.put("price", eventMan.getDSO().getContract(contract)
						.getActGridPriceAtTime(time, day, month, year) + eventMan.getDSO()
						.getContract(contract)
						.getEstEnergyPriceAtTime(time, day,
								month, year));
			} else if (operation.equals("getEstESPPrices")) {
				returnVal.put(
						"prices",
						eventMan.getDSO()
								.getContract(contract)
								.getEstEnergyPricesForDay(runningDay,
										runningMonth, runningYear));
			} else if (operation.equals("get2DayEstESPPrices")) {
				PricingContract c = eventMan.getDSO().getContract(contract);

				double[] prices = new double[48 * (60 / c
						.getPriceChangeInterval())];
				for (int i = 0; i < prices.length / 2; i++) {
					prices[i] = c.getEstElectricityPriceAtTime(
							i * c.getPriceChangeInterval() * 60, runningDay,
							runningMonth, runningYear);
				}
				int dupDay = runningDay;
				int dupMonth = runningMonth;
				int dupYear = runningYear;
				if (dupDay++ == Constants.getNumDaysInMonth(dupMonth, dupYear)) {
					dupDay = 1;
					if (dupMonth++ == 12) {
						dupMonth = 1;
						dupYear++;
					}
				}
				for (int i = prices.length / 2; i < prices.length; i++) {
					prices[i] = c.getEstElectricityPriceAtTime(
							i * c.getPriceChangeInterval() * 60, dupDay,
							dupMonth, dupYear);
				}
				returnVal.put("prices", prices);
			} else if (operation.equals("getEstElectricityPrices")) {
				double[] prices = new double[24 * (60 / eventMan.getDSO()
						.getContract(contract).getPriceChangeInterval())];
				for (int i = 0; i < prices.length; i++) {
					prices[i] = eventMan
							.getDSO()
							.getContract(contract)
							.getEstElectricityPriceAtTime(i * 1800, runningDay,
									runningMonth, runningYear);
				}
				returnVal.put("prices", prices);
			} else if (operation.equals("getEstElectricityPricesNextDay")) {
				double[] prices = new double[24 * (60 / eventMan.getDSO()
						.getContract(contract).getPriceChangeInterval())];
				int dupDay = runningDay;
				int dupMonth = runningMonth;
				int dupYear = runningYear;
				if (dupDay++ == Constants.getNumDaysInMonth(dupMonth, dupYear)) {
					dupDay = 1;
					if (dupMonth++ == 12) {
						dupMonth = 1;
						dupYear++;
					}
				}
				for (int i = 0; i < prices.length; i++) {
					prices[i] = eventMan
							.getDSO()
							.getContract(contract)
							.getEstElectricityPriceAtTime(i * 1800, dupDay,
									dupMonth, dupYear);
				}
				returnVal.put("prices", prices);
			} else if (operation.equals("getAvgEstEnergyPrice")) {
				int day = Integer.parseInt((String) parameters.get("day"));
				int month = Integer.parseInt((String) parameters.get("month"));
				int year = Integer.parseInt((String) parameters.get("year"));

				double[] prices = eventMan.getDSO().getContract(contract)
						.getEstEnergyPricesForDay(day, month, year);
				double accum = 0;
				for (int i = 0; i < prices.length; i++) {
					accum += prices[i];
				}
				returnVal.put("price", accum / prices.length);
			} else if (operation.equals("getNonPossibleEVChargeTime")) { 
				String name = (String)parameters.get("device");
				
				if(DSO.isEVContract(contract) && ((EVChargerModel)environment.getDeviceModelByName(name)).lv2Charging() &&
						cems.getEVChargeAlgorithm()!=99) {					 
					int curDay = Integer.parseInt((String) parameters.get("day"));
					String earliestStartTimeString = (String) parameters.get("earliestStart");
					int durationMin = Integer.parseInt((String) parameters.get("duration"));
					int watt = Integer.parseInt((String) parameters.get("power"));
					
					Calendar cal = Calendar.getInstance();
					DateFormat df = new SimpleDateFormat(
							"EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH); // Mon
																			// Feb
																			// 08
																			// 07:00:00
																			// CET
																			// 2010
					try {
						Date result = df.parse(earliestStartTimeString);
						cal.setTime(result);
					} catch (ParseException e) {
						e.printStackTrace();
					}
					
					int earliestStart = cal.get(Calendar.HOUR_OF_DAY)*3600 + cal.get(Calendar.MINUTE)*60 + (cal.get(Calendar.DAY_OF_MONTH)>curDay?0:86400);
					ArrayList<String> nonPossibleTimes = (ArrayList<String>)this.getCEMS().requestNonPossibleStartTime(runningDay, earliestStart, durationMin, watt, getFeederID())[1];
					String toSend = "[";
					for(int t=0; t<nonPossibleTimes.size(); t++) {
						toSend += nonPossibleTimes.get(t) + (t<nonPossibleTimes.size()-1?",":"");
					}
					//System.out.println(toSend + "]");
					returnVal.put("nonPossibleTimes", toSend + "]");
				} else {
					returnVal.put("nonPossibleTimes", new ArrayList<Double>().toArray());
				}
			}
		} 
		else if (provider.equals("WEATHER")) {
			if (operation.equals("getEstTemp")) {
				int hour = Integer.parseInt((String) parameters.get("time"));
				returnVal.put("temp",
						WeatherService.getForecastTempAtHour(hour));
			} else if (operation.equals("getActualTemp")) {
				int hour = Integer.parseInt((String) parameters.get("time"));
				int day = Integer.parseInt((String) parameters.get("day"));
				int month = Integer.parseInt((String) parameters.get("month"));
				int year = Integer.parseInt((String) parameters.get("year"));
				returnVal.put("temp", WeatherService.getActualTemp(hour * 3600,
						day, month, year));

			} else if (operation.equals("getActualWaterTemp")) {
				int hour = Integer.parseInt((String) parameters.get("time"));
				int day = Integer.parseInt((String) parameters.get("day"));
				int month = Integer.parseInt((String) parameters.get("month"));
				int year = Integer.parseInt((String) parameters.get("year"));
				returnVal.put("temp", WeatherService.getActualLakeTemp(hour * 3600,
						day, month, year));
			}
			else if (operation.equals("getAvgTemp")) {
				returnVal.put("temp", WeatherService.getForecastedAvgTemp());
			}
		} else if (provider.equals("SIMENV")) {
			if (operation.equals("getCurrentRTemp")) {
				String room = (String) parameters.get("room");
				returnVal.put("temp", getEnvironment().getRoom(room)
						.getCurrentTemp());
			} else if (operation.equals("getPlannedTempAtTime")) {
				String room = (String) parameters.get("room");
				String hour = (String) parameters.get("time");
				returnVal.put("plannedTemp",
						getEnvironment().getRoom(room).getPlannedSchedule()
								.getHourlyPlan()[Integer.parseInt(hour) % 24]);
			} else if (operation.equals("getScheduledTemps")) {
				String room = (String) parameters.get("room");
				double[] schedule = getEnvironment().getRoom(room)
						.getSchedule().getSchedule();
				returnVal.put("scheduledTemps", schedule);
			} else if (operation.equals("getScheduledTempAtHour")) {
				String room = (String) parameters.get("room");
				int hour = Integer.parseInt((String) parameters.get("hour"));

				hour = hour * 12;

				if (hour < 0)
					hour = 288 + hour;
				else
					hour = hour % 288;

				returnVal.put("scheduledTemp", getEnvironment().getRoom(room)
						.getSchedule().getSchedule()[hour]);
			} else if (operation.equals("getGenericSchedule")) {
				String device = (String) parameters.get("device");
				DeviceModel dm = environment.getDeviceModelByName(device);

				double[] sch = dm.getSchedule();
				returnVal.put("schedule", sch);
			} else if (operation.equals("getWMSchedule")) {
				String device = (String) parameters.get("device");
				DeviceModel dm = environment.getDeviceModelByName(device);

				int taskID = ((WasherDryerModel) dm).getCurrentTaskID();
				double[] task = ((WasherDryerModel) dm).getCurrentTask();
				double[] sch = dm.getSchedule();

				String startIntervalTime = "";
				String endIntervalTime = "";
				String latestFinishTime = "";
				int washProg = 0, dryProg = 0;

				if (taskID > 0) {
					int fromInterval = -1, toInterval = -1;
					for (int i = 0; i < sch.length; i++) {
						if (sch[i] == taskID && fromInterval == -1) {
							fromInterval = i;
							break;
						}
					}

					for (int i = sch.length - 1; i >= 0; i--) {
						if (sch[i] == taskID && fromInterval != -1) {
							toInterval = i + 1;
							break;
						}
					}

					Calendar cal = Calendar.getInstance();
					cal.set(Calendar.HOUR_OF_DAY, (fromInterval / 12) % 24);
					cal.set(Calendar.MINUTE, (fromInterval % 12) / 5);
					cal.set(Calendar.DAY_OF_MONTH, runningDay);
					cal.set(Calendar.MONTH, runningMonth-1);
					cal.set(Calendar.YEAR, runningYear);
					if (fromInterval >= 288)
						cal.add(Calendar.DAY_OF_MONTH, 1);
					startIntervalTime = cal.getTime().toString();

					Calendar cal2 = Calendar.getInstance();
					cal2.set(Calendar.HOUR_OF_DAY, (toInterval / 12) % 24);
					cal2.set(Calendar.MINUTE, (toInterval % 12) * 5);
					cal2.set(Calendar.DAY_OF_MONTH, runningDay);
					cal2.set(Calendar.MONTH, runningMonth-1);
					cal2.set(Calendar.YEAR, runningYear);
					if (toInterval >= 288)
						cal2.add(Calendar.DAY_OF_MONTH, 1);
					endIntervalTime = cal2.getTime().toString();

					int latestFinishInterval = (int) task[4] * 12;
					Calendar cal3 = Calendar.getInstance();
					cal3.set(Calendar.HOUR_OF_DAY,
							(latestFinishInterval / 12) % 24);
					cal3.set(Calendar.MINUTE, (latestFinishInterval % 12) / 5);
					cal3.set(Calendar.DAY_OF_MONTH, runningDay);
					cal3.set(Calendar.MONTH, runningMonth-1);
					cal3.set(Calendar.YEAR, runningYear);
					if (latestFinishInterval >= 288)
						cal3.add(Calendar.DAY_OF_MONTH, 1);
					latestFinishTime = cal3.getTime().toString();

					washProg = (int) task[0];
					dryProg = (int) task[1];
				}
				returnVal.put("schedule", sch);
				returnVal.put("startTime", startIntervalTime);
				returnVal.put("endTime", endIntervalTime);
				returnVal.put("latestFinish", latestFinishTime);
				returnVal.put("washProg", washProg);
				returnVal.put("dryProg", dryProg);
				returnVal.put("taskID", taskID);
			} else if (operation.equals("getEVSchedule")) {
				String device = (String) parameters.get("device");
				DeviceModel dm = environment.getDeviceModelByName(device);
				
				double[] sch = dm.getSchedule();
				
				int fromInterval = ((EVChargerModel)dm).getCurrentTaskStartInterval();
				int toInterval = ((EVChargerModel)dm).getCurrentTaskEndInterval();

				Calendar cal = Calendar.getInstance();
				cal.set(Calendar.HOUR_OF_DAY, (fromInterval / 12) % 24);
				cal.set(Calendar.MINUTE, ((fromInterval-((fromInterval/12)*12))*5)%60);
				cal.set(Calendar.DAY_OF_MONTH, runningDay);
				cal.set(Calendar.MONTH, runningMonth-1);
				cal.set(Calendar.YEAR, runningYear);
				if (fromInterval >= 288)
					cal.add(Calendar.DAY_OF_MONTH, 1);
				String startIntervalTime = cal.getTime().toString();

				Calendar cal2 = Calendar.getInstance();
				cal2.set(Calendar.HOUR_OF_DAY, (toInterval / 12) % 24);
				cal2.set(Calendar.MINUTE, ((toInterval-((toInterval/12)*12))*5)%60);
				cal2.set(Calendar.DAY_OF_MONTH, runningDay);
				cal2.set(Calendar.MONTH, runningMonth-1);
				cal2.set(Calendar.YEAR, runningYear);
				if (toInterval >= 288)
					cal2.add(Calendar.DAY_OF_MONTH, 1);
				String endIntervalTime = cal2.getTime().toString();

				int schDuration = ((EVChargerModel) dm)
						.getRequiredChargeDuration() / 60;
				int latestCharge = ((EVChargerModel) dm).getLatestStartSecond();

				int latestStartInterval = (latestCharge - ((runningDay - 1) * 24 * 3600)) / 300;
				int latestFinishInterval = latestStartInterval
						+ (schDuration / 5);
				Calendar cal3 = Calendar.getInstance();
				cal3.set(Calendar.HOUR_OF_DAY, (latestFinishInterval / 12) % 24);
				cal3.set(Calendar.MINUTE, ((latestFinishInterval-((latestFinishInterval/12)*12))*5)%60);
				cal3.set(Calendar.DAY_OF_MONTH, runningDay);
				cal3.set(Calendar.MONTH, runningMonth-1);
				cal3.set(Calendar.YEAR, runningYear);
				if (latestFinishInterval >= 288)
					cal3.add(Calendar.DAY_OF_MONTH, 1);
				String latestFinishTime = cal3.getTime().toString();

				returnVal.put("schedule", sch);
				returnVal.put("startTime", startIntervalTime);
				returnVal.put("endTime", endIntervalTime);
				returnVal.put("latestFinish", latestFinishTime);
			} else if (operation.equals("getExtendedSchedule")) {
				String r = (String) parameters.get("room");
				Room room = getEnvironment().getRoom(r);
				int extendedHours = Integer.parseInt((String) parameters
						.get("extendedHours"));

				double[] exschedule = room.getExtendedSchedule(extendedHours,
						runningDay, runningMonth, runningYear);

				returnVal.put("statusSchedule",room.getSchedule().getSchedule());
				returnVal.put("extendedSchedule", exschedule);
			} else if (operation.equals("getWHScheduledTempAtHour")) {
				int hour = Integer.parseInt((String) parameters.get("hour"));

				hour = hour * 12;

				if (hour < 0)
					hour = 288 + hour;
				else
					hour = hour % 288;

				ElectricWaterHeater wh = (ElectricWaterHeater) getEnvironment()
						.getRoom("Bathroom1")
						.getDevicesByType(Device.DEVTYPE_EWATERHEATER).get(0);
				returnVal.put("waterTemp", getEnvironment().getDeviceModelMap()
						.get(wh).getSchedule()[hour]);
			} else if (operation.equals("getWHExtendedSchedule")) {
				// find the water heater
				ElectricWaterHeater wh =  (ElectricWaterHeater)getEnvironment().getDeviceModelByName("wh_bathroom1").getDevice();
				int extendedHours = Integer.parseInt((String) parameters
						.get("extendedHours"));

				double[] exschedule = ((ElectricWaterHeaterModel) getEnvironment()
						.getDeviceModelMap().get(wh)).getExtendedSchedule(
						extendedHours, runningDay, runningMonth, runningYear);
				
				returnVal.put("statusSchedule", getEnvironment()
						.getDeviceModelMap().get(wh).getSchedule());
				returnVal.put("extendedSchedule", exschedule);
			} else if (operation.equals("getEstDailyEnergyConsumption")) {
				double otemp = Double.parseDouble((String) parameters
						.get("otemp"));
				int comfort = Integer.parseInt((String) parameters
						.get("comfort"));

				returnVal.put("kwh", HouseholdStatistics
						.getEstEnergyConsumption(comfort, (int) otemp));
			} else if (operation.equals("estimatedEnergyToMaintain")) {
				int otemp = (int) Double.parseDouble((String) parameters
						.get("otemp"));
				int temp = (int) Double.parseDouble((String) parameters
						.get("temp"));
				String entityID = (String) parameters.get("entity");

				returnVal
						.put("energy", EnergyUsageStatistics
								.getEstEnergyToMaintain(modelID, otemp, temp,
										entityID));
			} else if (operation.equals("estimateHighElectricityPriceCeiling")) {
				int day = Integer.parseInt((String) parameters.get("day"));
				int month = Integer.parseInt((String) parameters.get("month"));
				int year = Integer.parseInt((String) parameters.get("year"));

				// get estimated average and max electricity prices
				PricingContract c = eventMan.getDSO().getContract(contract);
				double accum = 0;
				double estMax = -10;
				for (int i = 0; i < 24 * 60 / c.getPriceChangeInterval(); i++) {
					double p = eventMan
							.getDSO()
							.getContract(contract)
							.getEstElectricityPriceAtTime(
									i * c.getPriceChangeInterval() * 60, day,
									month, year);
					accum += p;
					if (p > estMax)
						estMax = p;
				}
				double estAvg = accum / 24 * 60 / c.getPriceChangeInterval();

				// estimate ceiling
				double temp = (estMax - estAvg) / 2;
				// System.out.println(estMax-temp);
				returnVal.put("price", estMax - temp);
			} else if (operation.equals("checkTaskFixed")) {
				String device = (String) parameters.get("device");
				returnVal.put("taskFixed", ((EVChargerModel)environment.getDeviceModelByName(device)).getTaskFixed()?1:0);
			} else if (operation.equals("ewhWorking")) {
				String entity = (String) parameters.get("device");
				returnVal.put("ewhWorking",((ElectricWaterHeaterModel)environment.getDeviceModelByName(entity)).isWorking()?1:0);
			} else if (operation.equals("getEWHTankTemp")) {
				String entity = (String) parameters.get("device");
				returnVal.put("tankTemp", ((ElectricWaterHeaterModel)environment.getDeviceModelByName(entity)).getTankTemperature());
			} else if (operation.equals("checkShowerTime")) {
				String time = (String) parameters.get("time");
				Calendar cal = Calendar.getInstance();
				DateFormat df = new SimpleDateFormat(
						"EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH); // Mon
																		// Feb
																		// 08
																		// 07:00:00
																		// CET
																		// 2010
				returnVal.put("isShowerTime", 0);
				try {
					Date result = df.parse(time);
					cal.setTime(result);
					int hour = cal.get(Calendar.HOUR_OF_DAY);
					if ((hour >= 6 && hour < 10) || (hour >= 18 && hour < 22))
						returnVal.put("isShowerTime", 1);
				} catch (ParseException e) {
					e.printStackTrace();
				}
			} else if (operation.equals("askPostponeShower")) {
				// FOR TESTING
				int day = runningDay;
				if (day == 1 || day == 7 || day == 8 || day == 9)
					returnVal.put("postpone", 1);
				else
					returnVal.put("postpone", 0);

				// dummy method
				// double rand = Math.random();
				//
				// if(rand<0.70)
				// resObj.addReturnVal("postpone", 0);
				// else
				// resObj.addReturnVal("postpone", 1);
			} else if (operation.equals("checkPowerLimit")) {
				String deviceType = (String) parameters.get("deviceType");
				String schArray = (String) parameters.get("schedule");
				String startInt = (String) parameters.get("startInterval");
				String endInt = (String) parameters.get("endInterval");
				String limit = (String) parameters.get("powerLimit");
				String[] sch = schArray.substring(1, schArray.length() - 1)
						.split(",");
				double[] schedule = new double[sch.length];
				for (int i = 0; i < schedule.length; i++) {
					schedule[i] = Double.parseDouble(sch[i]);
				}

				boolean underLimit = checkPowerLimit(
						Integer.parseInt(deviceType), schedule,
						Integer.parseInt(startInt), Integer.parseInt(endInt),
						Double.parseDouble(limit));
				returnVal.put("underLimit", underLimit ? 1 : 0);
			} else if (operation.equals("createPowerSchedule")) {
				String device = (String) parameters.get("device");
				int program = Integer.parseInt((String) parameters
						.get("program"));
				String startTime = (String) parameters.get("startTime");
				int durationMin = (int) Double.parseDouble((String) parameters
						.get("duration"));

				double[] powerSchedule = new double[576];
				Calendar cal = Calendar.getInstance();
				DateFormat df = new SimpleDateFormat(
						"EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH); // Mon
																		// Feb
																		// 08
																		// 07:00:00
																		// CET
																		// 2010
				try {
					Date result = df.parse(startTime);
					cal.setTime(result);
				} catch (ParseException e) {
					e.printStackTrace();
				}
				int startInt = cal.get(Calendar.HOUR_OF_DAY) * 12
						+ (cal.get(Calendar.MINUTE) / 5);
				cal.add(Calendar.MINUTE, durationMin);
				int endInt = cal.get(Calendar.HOUR_OF_DAY) * 12
						+ (cal.get(Calendar.MINUTE) / 5);
				if(endInt<startInt) endInt += 288;

				Device dev = environment.getDeviceModelByName(device)
						.getDevice();
				for (int i = startInt; i <= endInt; i++) {
					powerSchedule[i] = dev.getEnergyConsumptionType() == Device.DEVENERGYTYPE_CONSTANT ? dev
							.getWatt() : dev.getWattByProgram(program);
				}

				String pwrSch = "[";
				for (int i = 0; i < powerSchedule.length; i++) {
					pwrSch += powerSchedule[i];
					if (i < powerSchedule.length - 1)
						pwrSch += ",";
				}
				returnVal.put("startInterval", startInt);
				returnVal.put("endInterval", endInt);
				returnVal.put("powerSchedule", pwrSch + "]");
			} else if (operation.equals("createWasherDryerPowerSchedule")) {
				int washProg = Integer.parseInt((String) parameters
						.get("washProg"));
				int dryProg = Integer.parseInt((String) parameters
						.get("dryProg"));
				String startW = (String) parameters.get("startW");
				String startD = (String) parameters.get("startD");
				double[] powerSchedule = new double[288];
				Calendar cal = Calendar.getInstance();
				DateFormat df = new SimpleDateFormat(
						"EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH); // Mon
																		// Feb
																		// 08
																		// 07:00:00
																		// CET
																		// 2010
				int startInt = 0, endInt = 0;
				if (washProg > 0) {
					try {
						Date result = df.parse(startW);
						cal.setTime(result);
						int hour = cal.get(Calendar.HOUR_OF_DAY);
						int min = cal.get(Calendar.MINUTE);
						startInt = hour * 12 + (min / 5);
						returnVal.put("startInterval", startInt);
						endInt = startInt
								+ (int) (((WashingMachineDryer) environment
										.getDeviceModelByName("WasherDryer")
										.getDevice())
										.getWashDurationFromProgram(washProg))
								/ 5;
						returnVal.put("endInterval", endInt);

						for (int i = startInt; i < endInt; i++) {
							powerSchedule[i] = ((WashingMachineDryer) environment
									.getDeviceModelByName("WasherDryer")
									.getDevice())
									.getWWattFromProgramAndTime(washProg, (i
											- startInt + 1)
											* syncInterval);
						}
					} catch (ParseException e) {
						e.printStackTrace();
					}
				}
				if (dryProg > 0) {
					try {
						Date result = df.parse(startD);
						cal.setTime(result);
						int hour = cal.get(Calendar.HOUR_OF_DAY);
						int min = cal.get(Calendar.MINUTE);
						startInt = hour * 12 + (min / 5);
						if (washProg == 0)
							returnVal.put("startInterval", startInt);
						endInt = startInt
								+ (int) (((WashingMachineDryer) environment
										.getDeviceModelByName("WasherDryer")
										.getDevice())
										.getDryDurationFromProgram(dryProg))
								/ 5;
						returnVal.put("endInterval", endInt);

						for (int i = startInt; i < endInt; i++) {
							powerSchedule[i] = ((WashingMachineDryer) environment
									.getDeviceModelByName("WasherDryer")
									.getDevice()).getDWattFromProgramAndTime(
									dryProg, (i - startInt + 1) * syncInterval);
						}
					} catch (ParseException e) {
						e.printStackTrace();
					}
				}

				String pwrSch = "[";
				for (int i = 0; i < powerSchedule.length; i++) {
					pwrSch += powerSchedule[i];
					if (i < powerSchedule.length - 1)
						pwrSch += ",";
				}
				returnVal.put("powerSchedule", pwrSch + "]");
			} else if (operation.equals("calculateCost")) {
				String device = (String) parameters.get("device");
				String program = (String) parameters.get("program");
				String startTimeString = (String) parameters
						.get("startTimeString");
				String durationMin = (String) parameters.get("durationMin");

				DeviceModel dm = environment.getDeviceModelByName(device);
				Device d = dm.getDevice();

				double watt = 0;
				if (d.getEnergyConsumptionType() == Device.DEVENERGYTYPE_CONSTANT) {
					watt = d.getWatt();
				} else if (d.getEnergyConsumptionType() == Device.DEVENERGYTYPE_PROGRAM) {
					if (d.getType() == Device.DEVTYPE_EVCHARGER) {
						if (Integer.parseInt(program) == 2)
							watt = d.getWatt();
						else
							watt = 1100;
					} else if (d.getType() == Device.DEVTYPE_WASHINGMACHINEDRYER) {
						String type = program.substring(0, 1);
						String prog = program.substring(1, program.length());
						WashingMachineDryer wm = (WashingMachineDryer) d;
						if (type.equals("W"))
							watt = wm.getAvgWattFromWProgram(Integer
									.parseInt(prog));
						else
							watt = wm.getAvgWattFromDProgram(Integer
									.parseInt(prog));
					}

				}

				Calendar cal = Calendar.getInstance();
				Calendar cal2 = Calendar.getInstance();
				DateFormat df = new SimpleDateFormat(
						"EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH); // Mon
																		// Feb
																		// 08
																		// 07:00:00
																		// CET
																		// 2010
				try {
					Date result = df.parse(startTimeString);
					cal.setTime(result);
					cal2.setTime(result);
					cal2.add(Calendar.MINUTE,
							(int) Double.parseDouble(durationMin));
				} catch (ParseException e) {
					e.printStackTrace();
				}

				int sHour = cal.get(Calendar.HOUR_OF_DAY);
				int eHour = cal2.get(Calendar.HOUR_OF_DAY);
				int sMin = cal.get(Calendar.MINUTE);
				int eMin = cal2.get(Calendar.MINUTE);
				if (eHour < sHour)
					eHour += 24;

				double cost = 0;
				for (int i = sHour; i <= eHour; i++) {
					int hourToUse = i;
					Calendar calToUse = cal;
					if (i > 23) {
						hourToUse = hourToUse - 24;
						calToUse = cal2;
					}

					if (sMin < 30) {
						int durationToUse = 30;
						if (i == eHour && sMin < eMin)
							durationToUse = Math.min(30, eMin);
						else if (i == eHour)
							break;
						cost += watt
								* 0.001
								* ((durationToUse - sMin) / 60.0)
								* eventMan
										.getDSO()
										.getContract(contract)
										.getEstElectricityPriceAtTime(
												hourToUse * 3600,
												calToUse.get(Calendar.DAY_OF_MONTH),
												calToUse.get(Calendar.MONTH),
												calToUse.get(Calendar.YEAR));
						sMin = 30;
						i--;
					} else {
						int durationToUse = 60;
						if (i == eHour && sMin < eMin)
							durationToUse = Math.min(60, eMin);
						else if (i == eHour)
							break;
						cost += watt
								* 0.001
								* ((durationToUse - sMin) / 60.0)
								* eventMan
										.getDSO()
										.getContract(contract)
										.getEstElectricityPriceAtTime(
												(hourToUse * 3600) + 1800,
												calToUse.get(Calendar.DAY_OF_MONTH),
												calToUse.get(Calendar.MONTH),
												calToUse.get(Calendar.YEAR));
						sMin = 0;
					}

				}

				returnVal.put("cost", cost);
			} else if (operation.equals("calculateHeatingCost")) {
				String oSch = (String) parameters.get("originalSch");
				String nSch = (String) parameters.get("newSch");
				int fromInterval = Integer.parseInt((String) parameters
						.get("fromInterval"));
				int toInterval = Integer.parseInt((String) parameters
						.get("toInterval"));
				String entityID = (String) parameters.get("entityID");

				String[] arrayStr = oSch.substring(1, oSch.length() - 1).split(
						",");
				double[] originalSch = new double[arrayStr.length];
				for (int i = 0; i < originalSch.length; i++) {
					originalSch[i] = Double.parseDouble(arrayStr[i]);
				}
				arrayStr = nSch.substring(1, nSch.length() - 1).split(",");
				double[] newSch = new double[arrayStr.length];
				for (int i = 0; i < newSch.length; i++) {
					newSch[i] = Double.parseDouble(arrayStr[i]);
				}

				double price_t = eventMan
						.getDSO()
						.getContract(contract)
						.getEstElectricityPriceAtTime(fromInterval * 300,
								runningDay, runningMonth, runningYear);
				double price_tPlus1 = eventMan
						.getDSO()
						.getContract(contract)
						.getEstElectricityPriceAtTime(fromInterval * 300 + 1800,
								runningDay, runningMonth, runningYear);
				double otemp_t = WeatherService.getActualTemp(
						fromInterval * 300, runningDay, runningMonth,
						runningYear);
				double otemp_tPlus1 = WeatherService.getActualTemp(
						fromInterval * 300 + 1800, runningDay, runningMonth,
						runningYear);
				Room room = environment.getDeviceModelByName(entityID)
						.getRoom();

				double cost = 0;
				for (int i = fromInterval; i <= toInterval; i = i + 6) {
					if (originalSch[i] != newSch[i]) {
						double originalTemp_t = originalSch[i];
						double newTemp_t = newSch[i];

						double originalTemp_tPlus1 = originalSch[i + 6];

						// Saved: estimate energy used to maintain the
						// temperature for half an hour
						double energySaved = EnergyUsageStatistics
								.getEstEnergyToMaintain(modelID, otemp_t,
										originalTemp_t, entityID) * 0.5;

						cost -= energySaved * price_t;
						
						if(energySaved==0) {
							returnVal.put("cost", -0.05); return returnVal;
						}
						
						if (originalTemp_tPlus1 == originalTemp_t) {
							// Cost: estimate energy needed to heat the room
							// back up
							double energySaved2 = EnergyUsageStatistics
									.getEstEnergyToMaintain(modelID,
											otemp_tPlus1, originalTemp_tPlus1,
											entityID); 
							double energyNeeded = EnergyManagement.estimatedEnergy(entityID,
									otemp_tPlus1, newTemp_t, originalTemp_t
											- newTemp_t, room.getWidth(),
									room.getLength(), room.getHeight(), room.getDoorArea(),
									room.getWindowNo(), room.getWindowL(), room.getWindowW(),
									environment.getDeviceModelByName(entityID)
											.getDevice().getWatt());
							double timeNeeded = energyNeeded
									/ (environment
											.getDeviceModelByName(entityID)
											.getDevice().getWatt()*0.001);
							
							if (timeNeeded < 0.5) {
								cost += (energyNeeded - (energySaved2 * timeNeeded))
										* price_tPlus1;
							} else {
								cost += (energyNeeded - (energySaved2 * 0.5))
										* price_tPlus1;
							} 
						} else if (originalTemp_tPlus1 < originalTemp_t) {
							// Cost: estimate energy needed to heat the room
							// back up
							double coolDownTime = EnergyManagement
									.estimatedTime(entityID,otemp_tPlus1,
											originalTemp_t, originalTemp_tPlus1,
											room.getWidth(), room.getLength(),
											room.getHeight(), room.getDoorArea(), 
											room.getWindowNo(), room.getWindowL(),
											room.getWindowW(), 0, false) / 60;
							
							double energyMaintain = EnergyUsageStatistics
									.getEstEnergyToMaintain(modelID,
											otemp_tPlus1, originalTemp_tPlus1,
											entityID)
									* (0.5 - Math.min(coolDownTime, 0.5)); 
							double energyNeeded = EnergyManagement.estimatedEnergy(entityID,
									otemp_tPlus1, newTemp_t,
									originalTemp_tPlus1 - newTemp_t,
									room.getWidth(), room.getLength(),
									room.getHeight(), room.getDoorArea(),
									room.getWindowNo(), room.getWindowL(),
									room.getWindowW(), environment
											.getDeviceModelByName(entityID)
											.getDevice().getWatt());
							cost += (energyNeeded - energyMaintain)
									* price_tPlus1;
							
						} else {
							// Cost: estimate energy needed to heat the room
							// back up
							double energyNeeded = EnergyManagement.estimatedEnergy(entityID,
									otemp_tPlus1, newTemp_t,
									originalTemp_tPlus1 - newTemp_t,
									room.getWidth(), room.getLength(),
									room.getHeight(), room.getDoorArea(),
									room.getWindowNo(), room.getWindowL(),
									room.getWindowW(), environment
											.getDeviceModelByName(entityID)
											.getDevice().getWatt());
							cost += energyNeeded * price_tPlus1;
						}
					}
				}
				returnVal.put("cost", cost);
			} else if (operation.equals("calculateWHHeatingCost")) {
				String oSch = (String) parameters.get("originalSch");
				String nSch = (String) parameters.get("newSch");
				int fromInterval = Integer.parseInt((String) parameters
						.get("fromInterval"));
				int toInterval = Integer.parseInt((String) parameters
						.get("toInterval"));
				String entityID = (String) parameters.get("entityID");
				double ambient = Double.parseDouble(((String) parameters
						.get("ambientT")));
				double tHeight = Double.parseDouble(((String) parameters
						.get("tankHeight")));
				double tDiameter = Double.parseDouble(((String) parameters
						.get("tankDiameter")));
				double tVolume = Double.parseDouble(((String) parameters
						.get("tankVolume")));
				double power = Double.parseDouble(((String) parameters
						.get("power")));
				double rVal = Double.parseDouble(((String) parameters
						.get("tankRVal")));

				String[] arrayStr = oSch.substring(1, oSch.length() - 1).split(
						",");
				double[] originalSch = new double[arrayStr.length];
				for (int i = 0; i < originalSch.length; i++) {
					originalSch[i] = Double.parseDouble(arrayStr[i]);
				}
				arrayStr = nSch.substring(1, nSch.length() - 1).split(",");
				double[] newSch = new double[arrayStr.length];
				for (int i = 0; i < newSch.length; i++) {
					newSch[i] = Double.parseDouble(arrayStr[i]);
				}

				double price_t = eventMan
						.getDSO()
						.getContract(contract)
						.getEstElectricityPriceAtTime(fromInterval * 300,
								runningDay, runningMonth, runningYear);
				double price_tPlus1 = eventMan
						.getDSO()
						.getContract(contract)
						.getEstElectricityPriceAtTime(fromInterval * 300 + 1800,
								runningDay, runningMonth, runningYear);
				double otemp_tPlus1 = WeatherService.getActualLakeTemp(
						fromInterval * 300 + 1800, runningDay, runningMonth,
						runningYear);
				//System.out.println(fromInterval + " " + toInterval + " " + price_t + " " + price_tPlus1);
				double cost = 0;
				for (int i = fromInterval; i <= toInterval && i<originalSch.length; i = i + 6) { //TODO: the i<originalSch.length part is cheating
					if (originalSch[i] != newSch[i]) {
						double originalTemp_t = originalSch[i];
						double newTemp_t = newSch[i];

						double originalTemp_tPlus1 = originalSch[(i + 6)%originalSch.length];

						// Saved: estimate energy used to maintain the
						// temperature for half an hour
						double energySaved = 2 * 0.5;

						cost -= energySaved * price_t;

						if (originalTemp_tPlus1 == originalTemp_t) {
							// Cost: estimate energy needed to heat the room
							// back up
							double energySaved2 = 2;
							double energyNeeded = EnergyManagement.estimatedWHEnergy(
									otemp_tPlus1, ambient, tHeight, tDiameter,
									tVolume, power, rVal, newTemp_t,
									originalTemp_tPlus1);

							double timeNeeded = energyNeeded
									/ environment
											.getDeviceModelByName(entityID)
											.getDevice().getWatt();

							if (timeNeeded < 0.5) {
								cost += (energyNeeded - (energySaved2 * (0.5 - timeNeeded)))
										* 0.001 * price_tPlus1;
							} else {
								cost += (energyNeeded - (energySaved2 * (0.5)))
										* 0.001 * price_tPlus1;
							}
						} else if (originalTemp_tPlus1 < originalTemp_t) {
							// Cost: estimate energy needed to heat the room
							// back up
							double coolDownTime = EnergyManagement
									.estimatedWaterTempDropTime(otemp_tPlus1,
											ambient, tHeight, tDiameter,
											tVolume, rVal, originalTemp_t,
											originalTemp_tPlus1) / 3600;

							double energyMaintain = 2 * (0.5 - Math.min(
									coolDownTime, 0.5));
							double energyNeeded = EnergyManagement.estimatedWHEnergy(
									otemp_tPlus1, ambient, tHeight, tDiameter,
									tVolume, power, rVal, newTemp_t,
									originalTemp_tPlus1);
							cost += (energyNeeded - energyMaintain)
									* price_tPlus1;

						} else {
							// Cost: estimate energy needed to heat the room
							// back up
							double energyNeeded = EnergyManagement.estimatedWHEnergy(
									otemp_tPlus1, ambient, tHeight, tDiameter,
									tVolume, power, rVal, newTemp_t,
									originalTemp_tPlus1);
							cost += energyNeeded * price_tPlus1;
						}
					}
				}
				returnVal.put("cost", cost);
			} else if (operation.equals("getEVSoC")) {
				String device = (String) parameters.get("device");
				DeviceModel dm = environment.getDeviceModelByName(device);
				returnVal.put("SoC", ((EVChargerModel)dm).getCurrentSoc());
			}
		}

		return returnVal;
	}

	public double getExpectedPeakLoad(int atInterval, int updateInterval) {
		HashMap<Device, DeviceModel> modelMap = environment.getDeviceModelMap();
		double load = 0;
		double[] halfHourPeak = new double[updateInterval * 60 / syncInterval];

		Set<Device> keys = modelMap.keySet();
		for (Device key : keys) {
			DeviceModel dm = modelMap.get(key);

			double[] exp = dm.getExpectedPeakLoad(atInterval, updateInterval,
					syncInterval);
			for (int i = 0; i < halfHourPeak.length; i++) {
				halfHourPeak[i] += exp[i];
			}
		}

		for (int i = 0; i < halfHourPeak.length; i++) {
			if (halfHourPeak[i] > load)
				load = halfHourPeak[i];
		}

		return load;
	}

	public double getEnergyConsumption(int atInterval, int updateInterval) {
		HashMap<Device, DeviceModel> modelMap = environment.getDeviceModelMap();
		double energy = 0;

		Set<Device> keys = modelMap.keySet();
		for (Device key : keys) {
			DeviceModel dm = modelMap.get(key);

			for (int i = 0; i < updateInterval * 60 / syncInterval; i++) {
				energy += dm.getEnergyInfo()[atInterval
						% (24 * 3600 / syncInterval) + i];
			}
		}

		return energy;
	}

	public int getPeakload(int atInterval) { 
		HashMap<Device, DeviceModel> modelMap = environment.getDeviceModelMap();
		int load = 0;
		
		Set<Device> keys = modelMap.keySet();
		for (Device key : keys) {
			DeviceModel dm = modelMap.get(key);
			
			load += dm.getPeakLoadInfo()[atInterval
					% (24 * 3600 / syncInterval)];
		}
		
		return load;
	}

	public int getPeakloadWithoutEV(int atInterval) {
		HashMap<Device, DeviceModel> modelMap = environment.getDeviceModelMap();
		int load = 0;

		Set<Device> keys = modelMap.keySet();
		for (Device key : keys) {
			DeviceModel dm = modelMap.get(key);

			if (dm.getDevice().getType() != Device.DEVTYPE_EVCHARGER)
				load += dm.getPeakLoadInfo()[atInterval
						% (24 * 3600 / syncInterval)];
		}

		return load;
	}

	public void setCEMS(CEMS cems) {
		this.cems = cems;
	}

	public CEMS getCEMS() {
		return cems;
	}

	private void logDSOControlRequest(int startInterval, int endInterval,
			int day, int month, String[] entities) {
		String toWrite = month + "," + day + "," + startInterval + ","
				+ endInterval + ",";
		for (String entity : entities) {
			toWrite += entity + ";";
		}

		EntityStatistics.writeStringToFile(toWrite + "\n", resultDir
				+ "/DSOControl.log");
	}

	public ArrayList<double[]> retrieveDiscomfortDuration(int month) {
		ArrayList<double[]> stat = new ArrayList<double[]>();
		for(int i=1; i<=Constants.getNumDaysInMonth(month, runningYear); i++) {
			stat.add(retrieveDiscomfortDuration(i, month));
		}
		
		return stat;
	}
	
	public double[] retrieveDiscomfortDuration(int day, int month) {
		double[] duration = new double[2]; // 0-reduced, 1-unacceptable

		File f = new File(resultDir + "/DSOControl.log");
		if(!f.exists()) return duration;
		
		BufferedReader br = null;
		boolean found = false;
		try {
			String sCurrentLine;
			br = new BufferedReader(new FileReader(resultDir
					+ "/DSOControl.log"));

			while ((sCurrentLine = br.readLine()) != null
					&& !sCurrentLine.isEmpty()) {
				if (sCurrentLine.startsWith(month + "," + day)) { 
					found = true;

					String[] entry = sCurrentLine.split(",");
					int startInterval = Integer.parseInt(entry[2])%288;
					int endInterval = Integer.parseInt(entry[3])%288;
					String[] entities = entry[4].split(";");
					for (String entity : entities) { 
						if(entity.length()==0 || entity.startsWith("EV")) continue; 
						HashMap<ComfortMeasure, ArrayList<String[]>> stats = environment.getDeviceModelByName(
								entity).getComfortStats().getComfortLVStats();
						for(ComfortMeasure m: stats.keySet()) { 
							ArrayList<String[]> levels = stats.get(m);
							String[] stat = levels.get(day-1);
							for (int i = startInterval; i <= endInterval; i++) { 
								if (stat[i].equals("R")) {
									duration[0] = duration[0] + 5; // minutes
								} else if (stat[i].equals("U")) {
									duration[1] = duration[1] + 5; 
								}
							}
						}
					}
				} else if (found)
					break;
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}

		return duration;
	}

	public String retrieveEVDiscomfort(int day, int month) {
		double soc = 0;
		ArrayList<double[]> socs = ((EVStatistics) environment
				.getDeviceModelByName("EV").getStatistics()).getSoCData();
		if (socs == null || socs.size()<day)
			return "N";
		else if(socs.size()>day)
			soc = Math.min(socs.get(day-1)[1],socs.get(day-2)[1]);
		else
			soc = socs.get(day-1)[1];

		if (soc < 72 && soc >= 50)
			return "R";
		else if (soc < 50)
			return "U";

		return "N";
	}
//TODO: how can we know how much power will be used at an interval??
	private boolean checkPowerLimit(int deviceType, double[] powerSch,
			int startInterval, int endInterval, double limit) { 
		HashMap<Device, DeviceModel> modelMap = environment.getDeviceModelMap();
		return true;
		/*if (limit == 99999 || limit == -1)
			return true;

		for (int interval = startInterval; interval <= endInterval; interval++) {
			double totalWatt = 0;
			Set<Device> keys = modelMap.keySet();
			for (Device key : keys) {
				DeviceModel dm = modelMap.get(key);
				if (dm.getDevice().getType() == deviceType)
					continue;
				
				// get schedule of that device
				totalWatt += dm.getPowerSchedule()[interval];
				
				if (totalWatt > limit) {
					return false;
				}
			}
			
			if (powerSch[interval] + totalWatt > limit) { 
				return false;
			} 
		}
		
		return true;*/
	}

}
