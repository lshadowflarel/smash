package no.ntnu.item.smash.sim.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import no.ntnu.item.smash.sim.data.stats.FullSimulationStatistics;
import no.ntnu.item.smash.sim.externalsp.DSO;
import no.ntnu.item.smash.sim.model.EVChargerModel;
import no.ntnu.item.smash.sim.pss.loadflow.LVNetwork;

import org.eclipse.emf.common.util.EList;

import com.interpss.core.aclf.AclfBus;

public class EventManager {

	private LinkedList<SimulationModel> eventList = new LinkedList<SimulationModel>();
	private LinkedList<SimulationModel> doneList = new LinkedList<SimulationModel>();
	private double[] historicPeakLoad;
	private LVNetwork network;
	private DSO dso;
	private int houseDone = 0;
	private int numHouse;
	private int numConcurrent;
	private int atInterval = 0;
	private int simIntervals = 0;
	private String resultDir;
	private boolean priceInit = false;

	public EventManager(ArrayList<SimulationModel> houses, int simIntervals, int numConcurrent,
			LVNetwork network, DSO dso, String resultDir) {
		numHouse = houses.size();
		this.network = network;
		this.dso = dso;
		this.numConcurrent = numConcurrent;
		this.resultDir = resultDir;
		this.simIntervals = simIntervals;
		this.historicPeakLoad = null;
		
		// put houses in the event list
		for (SimulationModel h : houses) {
			SyncLock l = new SyncLock(this);
			h.setEventManager(this);
			l.setNumSync(h.getEnvironment().getDeviceModelMap().size());
			h.setLock(l);
			eventList.add(h);
		}
	}
	
	public EventManager(ArrayList<SimulationModel> houses, int simIntervals, int numConcurrent,
			LVNetwork network, DSO dso, String resultDir, double[] historicPeakLoad) {
		numHouse = houses.size();
		this.network = network;
		this.dso = dso;
		this.numConcurrent = numConcurrent;
		this.resultDir = resultDir;
		this.simIntervals = simIntervals;
		this.historicPeakLoad = historicPeakLoad;
		
		// put houses in the event list
		for (SimulationModel h : houses) {
			SyncLock l = new SyncLock(this);
			h.setEventManager(this);
			l.setNumSync(h.getEnvironment().getDeviceModelMap().size());
			h.setLock(l);
			eventList.add(h);
		}
	}
	
	public void start() {
		// if event list is empty, it means that a full round has been completed
		// 1) perform load flow, other feeder-level stats, other global
		// rescheduling
		// 2) put all houses from the done list into the event list and start a
		// new round

		if (eventList.isEmpty()) { 
			// 1)
			if (network != null) {
				EList<AclfBus> buses = network.getAclfNetwork().getBusList();
				double[] houseLoad = new double[numHouse];
				double minVoltage = 230;
				double curIntPeakLoad = 0;

				HashMap<EVChargerModel, Double> chargingCars = new HashMap<EVChargerModel, Double>();

				for (int i = 0; i < doneList.size(); i++) {
					SimulationModel house = doneList.get(i);
					int load = house.getPeakload(atInterval-1);
					curIntPeakLoad += load;
					houseLoad[house.getModelID()] = load;
					AclfBus hNode = buses.get(house.getModelID() + 2); // 0=Transformer, 1=BusMid
					double baseKva = network.getAclfNetwork().getBaseKva();
					hNode.setLoadP(load * 0.001 / baseKva);
					hNode.setLoadQ(load * 0.001 * 0.9 / baseKva);

					// check which EVs are scheduled to charge in the next
					// interval
					EVChargerModel ev = (EVChargerModel) house.getEnvironment()
							.getDeviceModelByName("EV");
					if (ev != null) {
						int toCheck = 0;
						if (atInterval % 288 == 0)
							toCheck = 288;
						else if (atInterval < 288)
							toCheck = atInterval;
						else
							toCheck = atInterval % 288;
						if (ev.getSchedule()[toCheck] != 0 && ev.lv2Charging()) {
							chargingCars.put(ev, ev.getCurrentSoc()); 
						}
					}
				}
				LVNetwork.runLoadFlow(network.getAclfNetwork());

				double minVoltageWithEVs = 230;
				double[][] houseVoltages = new double[576][numHouse+2];
				for (int i = 0; i < buses.size(); i++) {
					double v = buses.get(i).getVoltage().getReal()
							* buses.get(0).getBaseVoltage();
					houseVoltages[atInterval%288][i] = v;
					if (v < minVoltageWithEVs)
						minVoltageWithEVs = v;
				}
				
				// write load flow results to file
				LVNetwork.writeLoadFlowSummaryToFile(network.getAclfNetwork(),
						resultDir + "/loadflow.txt");
				LVNetwork.writeLoadsToFile(houseLoad, resultDir
						+ "/loaddata.txt");
				LVNetwork.writeGridPeakLoadToFile(houseLoad, resultDir
						+ "/full_peakload.txt");
				
				HashMap<EVChargerModel, Integer> postpone = null;
				for (int i = 0; i < doneList.size(); i++) {
					SimulationModel house = doneList.get(i);
					int load = house.getPeakloadWithoutEV(atInterval-1);
					houseLoad[house.getModelID()] = load;
					AclfBus hNode = buses.get(house.getModelID() + 2); // 0=Transformer, 1=BusMid
					double baseKva = network.getAclfNetwork().getBaseKva();
					hNode.setLoadP(load * 0.001 / baseKva);
					hNode.setLoadQ(load * 0.001 * 0.9 / baseKva);
				}
				LVNetwork.runLoadFlow(network.getAclfNetwork()); // load
																	// flow
																	// is
																	// done
																	// again
																	// but
																	// this
																	// time
																	// without
																	// EVs
																	// charging
																	// it's
																	// used
																	// only
																	// to
																	// check
																	// if
																	// evMax
																	// must
																	// be
																	// recalculated
				LVNetwork.writeLoadFlowSummaryToFile(network.getAclfNetwork(),
						resultDir + "/loadflow-noEV.txt");
					
				if (doneList.peek().getCEMS() != null
						|| dso.hasLoadControlCustomers()) {
					minVoltage = 230;
					houseVoltages = new double[576][numHouse+2];
					for (int i = 0; i < buses.size(); i++) {
						double v = buses.get(i).getVoltage().getReal()
								* buses.get(0).getBaseVoltage();
						houseVoltages[atInterval%288][i] = v;
						if (v < minVoltage)
							minVoltage = v;
					}

					// if the voltage without EVs is lower than 212, we consider
					// it as having too much load
					// DLC and/or CLC will be performed
					HashMap<SimulationModel, Double> controlResponses = new HashMap<SimulationModel, Double>();
					//if (minVoltage < 213) {System.out.println(curIntPeakLoad + " " + historicPeakLoad[atInterval-1]);}
					if (historicPeakLoad!=null && (curIntPeakLoad - historicPeakLoad[atInterval-1]>50000 && minVoltage < 213)){
						controlResponses = dso.sendDRSignal(minVoltage, curIntPeakLoad - historicPeakLoad[atInterval-1] + 50000, atInterval,
								doneList.peek().runningDay, doneList.peek()
										.runningMonth, doneList.peek()
										.runningYear); 
						System.out.println("DR signal sent. Peak load was " + curIntPeakLoad + " (" + historicPeakLoad[atInterval-1] + "). Min voltage was " + minVoltage);
						
						// perform yet another load flow to see how high voltage we managed to increase
						for (int i = 0; i < doneList.size(); i++) {
							SimulationModel house = doneList.get(i);
							int load = house.getPeakloadWithoutEV(atInterval-1);
							if(controlResponses.get(house)!=null) load -= controlResponses.get(house);
							houseLoad[house.getModelID()] = load;
							AclfBus hNode = buses.get(house.getModelID() + 2); // 0=Transformer, 1=BusMid
							double baseKva = network.getAclfNetwork().getBaseKva();
							hNode.setLoadP(load * 0.001 / baseKva);
							hNode.setLoadQ(load * 0.001 * 0.9 / baseKva);
						}
						LVNetwork.runLoadFlow(network.getAclfNetwork());
						
						minVoltage = 230;
						for (int i = 0; i < buses.size(); i++) {
							double v = buses.get(i).getVoltage().getReal()
									* buses.get(0).getBaseVoltage();
							houseVoltages[atInterval%288][i] = v;
							if (v < minVoltage)
								minVoltage = v;
						}
						System.out.println("After DR signal. Min voltage is expected to be " + minVoltage);
					} else if(minVoltageWithEVs < 207){
						controlResponses = dso.sendDRSignal(minVoltageWithEVs, (207 - minVoltageWithEVs)*20000, atInterval,
								doneList.peek().runningDay, doneList.peek()
										.runningMonth, doneList.peek()
										.runningYear); 
						System.out.println("DR signal sent. Peak load was " + curIntPeakLoad + " (" + historicPeakLoad[atInterval-1] + "). " +
								"Min voltage without EVs was " + minVoltage + " / with EVs was " + minVoltageWithEVs);
						
						// perform yet another load flow to see how high voltage we managed to increase
						for (int i = 0; i < doneList.size(); i++) {
							SimulationModel house = doneList.get(i);
							int load = house.getPeakloadWithoutEV(atInterval-1);
							if(controlResponses.get(house)!=null) load -= controlResponses.get(house);
							houseLoad[house.getModelID()] = load;
							AclfBus hNode = buses.get(house.getModelID() + 2); // 0=Transformer, 1=BusMid
							double baseKva = network.getAclfNetwork().getBaseKva();
							hNode.setLoadP(load * 0.001 / baseKva);
							hNode.setLoadQ(load * 0.001 * 0.9 / baseKva);
						}
						LVNetwork.runLoadFlow(network.getAclfNetwork());
						
						minVoltage = 230;
						for (int i = 0; i < buses.size(); i++) {
							double v = buses.get(i).getVoltage().getReal()
									* buses.get(0).getBaseVoltage();
							houseVoltages[atInterval%288][i] = v;
							if (v < minVoltage)
								minVoltage = v;
						}
						System.out.println("After DR signal. Min voltage without EVs is expected to be " + minVoltage);
					}
					
					if (doneList.peek().getCEMS() != null
							&& chargingCars.size() > 0) {
						postpone = doneList
								.peek()
								.getCEMS()
								.recalculateEVMax(minVoltage, atInterval,
										houseVoltages, chargingCars);

						if (postpone != null && postpone.size() > 0) {
							for (EVChargerModel ev : postpone.keySet()) {
								ev.shiftSchedule(atInterval % 288
												, postpone
												.get(ev));
							}
						}
					}
				}
			}
			// 2)
			while (!doneList.isEmpty()) {
				eventList.add(doneList.pollFirst());
			}
		}

		// if it's not started, start the entire set
		if (!eventList.peek().isStarted()) {			
			// before doing anything, DSO assign prices for the next interval
			// here
			// DSO 1 - assign forecast energy price for the next interval using
			// the day-ahead market price
			if(!priceInit) {
				dso.assignEnergyPrices(atInterval, eventList.peek().getStartDay(),
					eventList.peek().getStartMonth(), eventList.peek()
							.getStartYear());
				dso.assignGridPrices(atInterval, eventList.peek().getStartDay(), 
						eventList.peek().getStartMonth(), eventList.peek()
							.getStartYear());
			}
			// DSO 2 - calculate grid price for the next interval based on
			// historical peak load data and expected peak load
			if(!priceInit) dso.resolveGridPriceAtTime(atInterval, eventList.peek()
					.getStartDay(), eventList.peek().getStartMonth(), eventList
					.peek().getStartYear());

			priceInit = true;
			int remaining = eventList.size();
			for (int i = 0; i < Math.min(numConcurrent, remaining); i++) {
				SimulationModel house = eventList.pollFirst();
				// System.out.println(house.getModelID() + " is now started.");
				house.startSimulation();
			}
		}
		// if it's started, continue the set
		else {
			int remaining = eventList.size();
			for (int i = 0; i < Math.min(numConcurrent, remaining); i++) {
				SimulationModel house = eventList.pollFirst();
				// System.out.println(house.getModelID() +
				// " wakes up and continues.");
				synchronized (house.getLock()) {
					house.setStandby(false);
					house.getLock().notifyAll();
				}
			}
		}
	}

	public synchronized void periodicSync(SimulationModel house, int atInterval) {
		this.atInterval = atInterval;

		// System.out.println(house.getModelID() + " syncs and waits.");
		// put the house in the doneList
		doneList.add(house);

		if (doneList.size() % numConcurrent == 0 || doneList.size() == numHouse) {
			if (atInterval % 6 == 0) {
				// DSO assigns real price of current interval and forecast price
				// of next interval
				// DSO 1 - calculate real energy price for the ended interval
				// based on mismatch of expected load volume and real load
				// volume
				if (doneList.size() == numHouse)
					dso.resolveEnergyPriceAtTime(atInterval, house.runningDay,
							house.runningMonth, house.runningYear);
				// DSO 2 - assign forecast energy price for the next interval
				dso.assignEnergyPrices(atInterval, house.runningDay,
						house.runningMonth, house.runningYear);
				// DSO 3 - calculate grid price for the next interval based on
				// historical peak load data and expected peak load
				if (doneList.size() == numHouse
						&& atInterval < simIntervals - 6)
					dso.resolveGridPriceAtTime(atInterval, house.runningDay,
							house.runningMonth, house.runningYear);
			}
			if (atInterval % 288 == 0) {
				System.out.println("[GROUP] DAY START (" + atInterval + ")");
				if (house.getCEMS() != null) {
					house.getCEMS().initializeEVMax(atInterval / 288 + 1);
					house.getCEMS().prepareNewDayContext(atInterval / 288 + 1);
				}
				if (doneList.size() == numHouse)
					dso.assignExtraCostAtTime(atInterval, house.runningDay,
							house.runningMonth, house.runningYear);
			}

			start();
		}
	}

	public synchronized void informHouseDone(SimulationModel house) {
		System.out.println(house.getModelID() + " finishes simulation.");
		if (DSO.isControlContract(house.getContract()))	
			FullSimulationStatistics.writeComfortStat(resultDir, house.getModelID(), house.retrieveDiscomfortDuration(house.runningMonth));
		if (house.getCEMS() != null)
			house.getCEMS().writeEVQueueToFile();

		if (++houseDone == numHouse) {
			System.out.println("Aggregating all house data.");
			FullSimulationStatistics.aggregateStats(resultDir, numHouse);
			System.exit(0);
		} else {
			if (eventList.size() > 0) {
				int remaining = eventList.size();
				for (int i = 0; i < Math.min(numConcurrent, remaining); i++) {
					SimulationModel h = eventList.pollFirst();
					// System.out.println(h.getModelID() +
					// " wakes up and continues.");
					synchronized (h.getLock()) {
						h.setStandby(false);
						h.getLock().notifyAll();
					}
				}
			}
		}
	}

	public String getResultDirectory() {
		return resultDir;
	}
	
	public DSO getDSO() {
		return dso;
	}

	public LVNetwork getNetwork() {
		return network;
	}

	public class SyncLock {
		EventManager eventMan;
		int numSync = 0;
		int numModel = 0;

		public SyncLock(EventManager eventMan) {
			this.eventMan = eventMan;
		}

		public void setNumSync(int num) {
			numSync = num;
			numModel = num;
		}

		public synchronized void periodicSync(SimulationModel house,
				String name, int atInterval) {// System.out.println("Num sync: "
												// + numSync + " " + name);
			if (--numSync == 0) {
				house.checkPowerUsage(((atInterval-((atInterval/12)*12))*5)%60, (atInterval/12)%24);
				eventMan.periodicSync(house, atInterval);
			}

			synchronized (this) {
				while (house.onStandby()) {
					try {// System.out.println("Waiting... " + name);
						this.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}// System.out.println("Continue " + name + " " + numSync);

				if (++numSync == numModel) {
					house.setStandby(true);
				}
			}

		}
	}
}
