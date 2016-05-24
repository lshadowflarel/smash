package no.ntnu.item.smash.sim.externalsp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import no.ntnu.item.smash.sim.core.SimulationModel;
import no.ntnu.item.smash.sim.model.EVChargerModel;
import no.ntnu.item.smash.sim.pss.loadflow.LVNetwork;

/**
 * Use for anything that requires asking CEMS for permission
 */
public class CEMS {

	private boolean DEBUG = false;
	
	private ArrayList<int[]> evMax = new ArrayList<int[]>();
	private double[] evWeight = new double[8];
	double[][] historicVoltages;
	private int[] numHousesInFeeders;
	
	private String historicLoadPath = "data/cems/historical/loadflow.txt";
	private int numHouse;	
	private int curDay;
	
	private ArrayList<int[]> evQueue = new ArrayList<int[]>();
	private int[] circuitEVQueue;
	
	private boolean waitForCSS = false;
	private boolean waitForOtherEVs = true;
	
	private ArrayList<SimulationModel> customers = new ArrayList<SimulationModel>();
	
	// parameters
	private int evChargeAlgorithm = 2; // 1=local first then CEMS, 2=CEMS first then local
	
	public CEMS(int numHouse, int[] numHousesInFeeders) {
		this.numHouse = numHouse;
		this.numHousesInFeeders = numHousesInFeeders;
		curDay = 1;
		
		for(int i=0; i<numHousesInFeeders.length; i++) {
			evMax.add(new int[576]);
			evQueue.add(new int[576]);
		}
		circuitEVQueue = new int[576];
		historicVoltages = new double[576][numHouse+2];
		
		initializeEVMax(curDay);
		createQueueFiles();
		
		// dummy weight for now
		evWeight[0] = 1.3; evWeight[1] = 1.3; evWeight[2] = 1.2; evWeight[3] = 1; 
		evWeight[4] = 1; evWeight[5] = 1; evWeight[6] = 1; evWeight[7] = 2.4;
	}
	
	public CEMS(int numHouse, int[] numHousesInFeeders, String historicLoadPath) {	
		this.numHouse = numHouse;
		this.numHousesInFeeders = numHousesInFeeders;
		curDay = 1;
		
		for(int i=0; i<numHousesInFeeders.length; i++) {
			evMax.add(new int[576]);
			evQueue.add(new int[576]);
		}
		circuitEVQueue = new int[576];
		historicVoltages = new double[576][numHouse+2];
		
		this.historicLoadPath = historicLoadPath;
		initializeEVMax(curDay);
		createQueueFiles();
		
		// dummy weight for now
		evWeight[0] = 1.3; evWeight[1] = 1.3; evWeight[2] = 1.2; evWeight[3] = 1; 
		evWeight[4] = 1; evWeight[5] = 1; evWeight[6] = 1; evWeight[7] = 2.4;
	}
	
	public void setCustomers(ArrayList<SimulationModel> customers) {
		this.customers = customers;
	}
	
	public void resetQueue() {
		for(int i=0; i<evQueue.size(); i++) {
			evQueue.set(0, new int[576]);
		}
		circuitEVQueue = new int[576];
	}
	
	public void resetEVMax() {
		evMax.clear();
		for(int i=0; i<numHousesInFeeders.length; i++) {
			evMax.add(new int[576]);
		}
	}
	
	public void initializeEVMax(int simDay) {		
		// read the historical voltages
		BufferedReader br = null;
		
		try {
			String sCurrentLine;
			br = new BufferedReader(new FileReader(historicLoadPath));
 
			int line = 1;
			int reqLine = ((simDay-1) * 288) + 1; 
			while ((sCurrentLine = br.readLine()) != null && !sCurrentLine.isEmpty()) { 
				if(line==reqLine) {	
					for(int interval=0; interval<576; interval++) {
						if(sCurrentLine == null || sCurrentLine.isEmpty()) {
							for(int j=0; j<numHouse+2; j++) { 
								historicVoltages[interval][j] = historicVoltages[0][j]; // kind of a hack because it's not really important here
							}
						} else {					
							// check min voltage of the entire circuit
							String[] split = sCurrentLine.split(",");
							for(int j=0; j<Math.min(numHouse+2, split.length); j++) { 
								double v = Double.parseDouble(split[j]);
								historicVoltages[interval][j] = v;
							}
						}
						sCurrentLine = br.readLine();
					}
					break;
				}
				line++;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null) br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}

		resetEVMax();
		initEVMax(historicVoltages, 0, 575, false);
	}
	
	public void initEVMax(double[][] comparingVoltages, int startInterval, int endInterval, boolean wipe) {
		double LIMIT = 207;
		for(int i=startInterval; i<=endInterval; i++) {			
			double minVoltage = 230;
			for(int j=0; j<comparingVoltages[i].length; j++) {
				if(comparingVoltages[i][j]<minVoltage) minVoltage = comparingVoltages[i][j];
			}
			
			if(wipe) { for(int f=0; f<numHousesInFeeders.length; f++) evMax.get(f)[i] = 0; }

			int feederDone = 0; double[] busMidEffect = new double[numHousesInFeeders.length];
			while(minVoltage>LIMIT && feederDone<numHousesInFeeders.length) {
				int compensation = 0;
				
				double[][] circuitVoltage = new double[comparingVoltages.length][comparingVoltages[0].length];
				for (int a = 0; a < circuitVoltage.length; a++) {
				     circuitVoltage[a] = Arrays.copyOf(comparingVoltages[a], comparingVoltages[a].length);
				}
				
				for(int feeder=0; feeder<numHousesInFeeders.length; feeder++) {
					if(feeder>0) compensation += numHousesInFeeders[feeder-1];
					for(int h=0; h<numHousesInFeeders[feeder]; h++) { 
						circuitVoltage[i][h+2+compensation] -= busMidEffect[feeder];
					}
				}
				
				compensation = 0;
				for(int feeder=0; feeder<numHousesInFeeders.length; feeder++) {					
					// see if we can increase evMax by 1
					int testEVMax = evMax.get(feeder)[i] + 1; 
					if(testEVMax>numHousesInFeeders[feeder]) {
						feederDone++; 
						break; 
					}
					
					if(feeder>0) compensation += numHousesInFeeders[feeder-1];

					// estimate voltage drop at the end of feeder
					double accumDrop = 0;
					for(int loadLoc=numHousesInFeeders[feeder]-testEVMax+1; loadLoc<=numHousesInFeeders[feeder]; loadLoc++) {
						double prevLocVoltage = circuitVoltage[i][testEVMax<numHousesInFeeders[feeder]?loadLoc+compensation:1]; 
						
						accumDrop += (2/(prevLocVoltage-accumDrop))*(6600*LVNetwork.resistancePerKm*LVNetwork.km[loadLoc+compensation-1] 
								+ 6600*0.9*LVNetwork.reactancePerKm*LVNetwork.km[loadLoc+compensation-1]);
					}
					double newVoltageAtEnd = circuitVoltage[i][numHousesInFeeders[feeder]+compensation+1] - accumDrop;
					if(newVoltageAtEnd<minVoltage) minVoltage = newVoltageAtEnd;

					// estimate voltage drop that will propagate to other feeders
					double dropAtBusMid = (2/(circuitVoltage[i][0]))*(6600*LVNetwork.resistancePerKm*LVNetwork.kmMid + 6600*0.9*LVNetwork.reactancePerKm*LVNetwork.kmMid);
					double[] newVoltageAtEnd2 = new double[numHousesInFeeders.length]; int compensation2 = 0;
					for(int otherFeeder=0; otherFeeder<numHousesInFeeders.length; otherFeeder++) {
						if(otherFeeder>0) compensation2 += numHousesInFeeders[otherFeeder-1];
						if(otherFeeder==feeder) continue;
						newVoltageAtEnd2[otherFeeder] = circuitVoltage[i][numHousesInFeeders[otherFeeder]+1+compensation2] - dropAtBusMid;
						if(newVoltageAtEnd2[otherFeeder]<minVoltage) {
							minVoltage = newVoltageAtEnd2[otherFeeder];
						}
					}
					
					if(minVoltage>=LIMIT) {
						evMax.get(feeder)[i] += 1;
						circuitVoltage[i][numHousesInFeeders[feeder]+1+compensation] = newVoltageAtEnd;
						compensation2 = 0;
						for(int otherFeeder=0; otherFeeder<numHousesInFeeders.length; otherFeeder++) {
							if(otherFeeder>0) compensation2 += numHousesInFeeders[otherFeeder-1];
							if(otherFeeder==feeder) continue;
							for(int h=0; h<numHousesInFeeders[otherFeeder]; h++) { 
								circuitVoltage[i][h+2+compensation2] -= dropAtBusMid;
							}
							busMidEffect[otherFeeder] += dropAtBusMid;
						}
					}
				}
			}
		}
		
		// DEBUG
//		if(DEBUG) {
//		System.out.println("EVMAX for all feeders");
//			for(int i=0; i<numHousesInFeeders.length; i++) {
//				System.out.println("Feeder " + i + ":");
//				for(int j=0; j<evMax.get(i).length; j++) {
//					System.out.print(evMax.get(i)[j] + " ");
//				}
//				System.out.println();
//			}
//		}
	}

	public HashMap<EVChargerModel,Integer> recalculateEVMax(double newMinV, int atInterval, double[][] houseVoltages, HashMap<EVChargerModel, Double> chargingCars) {
		int interval = atInterval % 288;
		HashMap<EVChargerModel,Integer> rescheduledCars = new HashMap<EVChargerModel,Integer>();
		
		double oldMinVoltage = 230;
		for(int i=0; i<historicVoltages[interval].length; i++) {
			if(historicVoltages[interval][i]<oldMinVoltage) oldMinVoltage = historicVoltages[interval][i];
		}
		
		if((int)newMinV < (int)oldMinVoltage || (newMinV - oldMinVoltage) > 2) {
			if(DEBUG)System.out.println("Recalculating EVMax at " + interval + " (newMinV=" + newMinV + " oldMinV=" + oldMinVoltage);
			initEVMax(houseVoltages, interval, interval, true);
		}
		
		for(int i=0; i<numHousesInFeeders.length; i++) {
			// get only the cars in this feeders
			HashMap<EVChargerModel, Double> cars = new HashMap<EVChargerModel, Double>();
			for(EVChargerModel m:chargingCars.keySet()) {
				if(m.getSimParent().getFeederID()==i) {
					cars.put(m, chargingCars.get(m));
				}
			}
			
			int toPostpone = cars.size() - evMax.get(i)[interval];
			if(DEBUG && toPostpone>0) {
				System.out.println("Feeder #" + i + " RECALCULATE AT" + atInterval + "(" + interval + "): "+ newMinV + "/" + oldMinVoltage);
				System.out.println("Need to postpone " + toPostpone + " EV(s). -> evMax=" + evMax.get(i)[interval]);
			}
			HashMap<EVChargerModel,Integer> rescheduled = rescheduleEVs(atInterval, interval, toPostpone, cars, new HashMap<EVChargerModel,Integer>(), evMax.get(i)[interval], i);
			
			for(EVChargerModel ev:rescheduled.keySet()) {
				rescheduledCars.put(ev, rescheduled.get(ev));
			}
		}
		
		
		return rescheduledCars;
	}
	
	private HashMap<EVChargerModel,Integer> rescheduleEVs(int accumInterval, int dayInterval, int toPostpone, HashMap<EVChargerModel,Double> chargingCars, HashMap<EVChargerModel,Integer> rescheduledCars, int maxEV, int feeder) {
		if((toPostpone<=0 && maxEV>0) || chargingCars.size()==0) return rescheduledCars;
		
		double maxSoC = 0; EVChargerModel maxSoCEV = null;
		for(EVChargerModel ev: chargingCars.keySet()) {
			int contract = ev.getSimParent().getContract();
			if(ev.getSimParent().getFeederID()==feeder && ev.getCurrentSoc()>maxSoC 
					&& (DSO.isEVContract(contract))) {
				maxSoC = ev.getCurrentSoc();
				maxSoCEV = ev;
			}
		}
		
		if(maxSoCEV==null) return rescheduledCars;
		
		chargingCars.remove(maxSoCEV);
	
		// find out when the EV can charge
		boolean nextDayReschedule = false;
		int nextInterval = dayInterval+1;
		if(((double)accumInterval)/288 > curDay) {
			// evNextDayQueue then
			nextDayReschedule = true;
			nextInterval += 288;
		} 		

		boolean canCharge = false;
		int latestStartInterval = (maxSoCEV.getLatestEndSecond()/300)%288; 
		if(latestStartInterval<dayInterval) { // means that atInterval is on the current day while latestStartInterval is on the next day
			latestStartInterval += 288;
		}
		if(DEBUG) System.out.println("Attempting to reschedule, on same feeder, " + maxSoCEV.getModelID() + " at " + dayInterval + ". (" + nextDayReschedule + ") Finding from " + nextInterval + " to " + latestStartInterval);
		for(int s=nextInterval; s<latestStartInterval; s++) {
			if(maxSoCEV.getSchedule()[s]!=0) continue;
			if(evQueue.get(feeder)[s]<(maxSoCEV.getDevice().getWatt()>6600?evMax.get(feeder)[s]-1:evMax.get(feeder)[s])) {
				evQueue.get(feeder)[s] = evQueue.get(feeder)[s]+(maxSoCEV.getDevice().getWatt()>6600?2:1);
				circuitEVQueue[s] += maxSoCEV.getDevice().getWatt()>6600?2:1;
				rescheduledCars.put(maxSoCEV, s); 
				if(!nextDayReschedule) {
					evQueue.get(feeder)[dayInterval] = Math.max(0, evQueue.get(feeder)[dayInterval]-(maxSoCEV.getDevice().getWatt()>6600?2:1));
					circuitEVQueue[dayInterval] = Math.max(0, circuitEVQueue[dayInterval] - (maxSoCEV.getDevice().getWatt()>6600?2:1));
				}
				else {
					evQueue.get(feeder)[dayInterval-288] = Math.max(0, evQueue.get(feeder)[dayInterval-288]-(maxSoCEV.getDevice().getWatt()>6600?2:1));
					circuitEVQueue[dayInterval-288] = Math.max(0, circuitEVQueue[dayInterval-288] - (maxSoCEV.getDevice().getWatt()>6600?2:1));
				}
				canCharge = true;
				if(DEBUG) System.out.println("Rescheduling of " + maxSoCEV.getModelID() + " successful. Charge at " + s);
				break;
			}
		}
		
		if(!canCharge) {
			if(DEBUG) System.out.println("Attempting to reschedule, on different feeder, " + maxSoCEV.getModelID() + " at " + dayInterval + ". (" + nextDayReschedule + ") Finding from " + nextInterval + " to " + latestStartInterval);
			for(int s=nextInterval; s<latestStartInterval; s++) {
				if(maxSoCEV.getSchedule()[s]!=0) continue;
				int[] assignedFeeders = assignEVToFeeder(feeder, s, maxSoCEV.getDevice().getWatt());
				if(assignedFeeders!=null && assignedFeeders.length==1) {					
					evQueue.get(feeder)[s] = evQueue.get(feeder)[s]+(maxSoCEV.getDevice().getWatt()>6600?2:1);
					if(assignedFeeders[0]!=feeder) {
						evMax.get(feeder)[s] = (maxSoCEV.getDevice().getWatt()>6600?evMax.get(feeder)[s]+2:evQueue.get(feeder)[s]+1);
						int weight = (int)(evWeight[feeder]/evWeight[assignedFeeders[0]]);
						evMax.get(assignedFeeders[0])[s] = (maxSoCEV.getDevice().getWatt()>6600?evMax.get(assignedFeeders[0])[s]-(2*weight):evQueue.get(assignedFeeders[0])[s]-(1*weight));
					}
					circuitEVQueue[s] += maxSoCEV.getDevice().getWatt()>6600?2:1;
					rescheduledCars.put(maxSoCEV, s); 
					if(!nextDayReschedule) {
						evQueue.get(feeder)[dayInterval] = Math.max(0, evQueue.get(feeder)[dayInterval]-(maxSoCEV.getDevice().getWatt()>6600?2:1));
						circuitEVQueue[dayInterval] = Math.max(0, circuitEVQueue[dayInterval] - (maxSoCEV.getDevice().getWatt()>6600?2:1));
					}
					else {
						evQueue.get(feeder)[dayInterval-288] = Math.max(0, evQueue.get(feeder)[dayInterval-288]-(maxSoCEV.getDevice().getWatt()>6600?2:1));
						circuitEVQueue[dayInterval-288] = Math.max(0, circuitEVQueue[dayInterval-288] - (maxSoCEV.getDevice().getWatt()>6600?2:1));
					}
					canCharge = true;
					if(DEBUG) System.out.println("Rescheduling of " + maxSoCEV.getModelID() + " successful. Charge at " + s + " using feeder " + assignedFeeders[0] + " quota.");
					break;
				} else if(assignedFeeders!=null && assignedFeeders.length==2) {
					evQueue.get(feeder)[s] = evQueue.get(feeder)[s]+2;
					for(int a=0; a<assignedFeeders.length; a++) {
						evMax.get(feeder)[s] = evMax.get(feeder)[s]+1;
						int weight = (int)(evWeight[feeder]/evWeight[assignedFeeders[a]]);
						evMax.get(assignedFeeders[a])[s] = evMax.get(assignedFeeders[a])[s]-weight;
					}
					circuitEVQueue[s] += 2;
					rescheduledCars.put(maxSoCEV, s);
					if(!nextDayReschedule) {
						evQueue.get(feeder)[dayInterval] = Math.max(0, evQueue.get(feeder)[dayInterval]-(maxSoCEV.getDevice().getWatt()>6600?2:1));
						circuitEVQueue[dayInterval] = Math.max(0, circuitEVQueue[dayInterval] - (maxSoCEV.getDevice().getWatt()>6600?2:1));
					}
					else {
						evQueue.get(feeder)[dayInterval-288] = Math.max(0, evQueue.get(feeder)[dayInterval-288]-(maxSoCEV.getDevice().getWatt()>6600?2:1));
						circuitEVQueue[dayInterval-288] = Math.max(0, circuitEVQueue[dayInterval-288] - (maxSoCEV.getDevice().getWatt()>6600?2:1));
					}
					canCharge = true;
					if(DEBUG) System.out.println("Rescheduling of " + maxSoCEV.getModelID() + " successful. Charge at " + s + " using feeder " 
							+ assignedFeeders[0] + " and feeder " + assignedFeeders[1] + " quota.");
					break;
				}
			}
			if(!canCharge) {
				System.out.println("NO SLOT TO RESCHEDULE " + maxSoCEV.getModelID() + " (Feeder #" + feeder + ") at " + accumInterval + " (" + nextInterval + " to " + latestStartInterval + ")");
				evQueue.get(feeder)[dayInterval] = Math.max(0, evQueue.get(feeder)[dayInterval]-(maxSoCEV.getDevice().getWatt()>6600?2:1));
				circuitEVQueue[dayInterval] -= Math.max(0, circuitEVQueue[dayInterval] - (maxSoCEV.getDevice().getWatt()>6600?2:1));
				rescheduledCars.put(maxSoCEV, -1);
			}
		}
		
		return rescheduleEVs(accumInterval, dayInterval, toPostpone-(maxSoCEV.getDevice().getWatt()>6600?2:1), chargingCars, rescheduledCars, maxEV, feeder);
	}
	
	/**
	 * This method assumes that the starting charge time is on the current day (at night) and not the next day (next morning (after midnight)).
	 * (algorithm 1)
	 * @param curDay
	 * @param curMonth
	 * @param curYear
	 * @param earliestChargeSecond
	 * @param plannedChargeSecond
	 * @param duration
	 * @return
	 */
	public synchronized double[] requestChargeTime(int simDay, int earliestChargeSecond, int plannedChargeSecond, int duration, int latestChargeSecond, double watt, int feeder) {
		if(DEBUG) System.out.println("CEMS receives request on day " + simDay + " to charge at " + plannedChargeSecond 
				+ " for " + duration + " minutes.");
				
		double[] suggestion = new double[576];
		
		// check whether the EV in question is able to charge at its desired time slot
		int earliestStartSlot = (earliestChargeSecond/60)/5;
		int prefStartSlot = (plannedChargeSecond/60)/5; 
		int latestStartSlot = (latestChargeSecond/60)/5;
		int numSlot = duration%5==0?duration/5:duration/5+1;
		//System.out.println(earliestStartSlot + " " + prefStartSlot + " " + numSlot);
		boolean canCharge = true;
		
		for(int i=prefStartSlot; i<=prefStartSlot+numSlot; i++) {
			if(evQueue.get(feeder)[i]>=(watt>6600?evMax.get(feeder)[i]-1:evMax.get(feeder)[i])) {
				canCharge = false;
				break;
			}
		}

		if(DEBUG) System.out.println("CEMS decision: " + (canCharge?"allow":"deny"));
		
		// can it charge when it want to?
		if(canCharge) {
			// update the queues and construct the suggestion
			for(int i=0; i<576; i++) {
				if(i>=prefStartSlot && i<prefStartSlot+numSlot) {
					suggestion[i] = 1;
					evQueue.get(feeder)[i] = (watt>6600?evQueue.get(feeder)[i]+2:evQueue.get(feeder)[i]+1);
				}
			}
		} else {
			int startAt = earliestStartSlot;
			int minNum = 9999;
			int free = 0; int bestFreeSoFar = 0; int bestStartSlotSoFar = startAt; int numSlotSoFar = 0;
			int minStartSlot = earliestStartSlot;
			
			// find out when the EV can charge --> start from the earliest possible charge time
			for(int s=earliestStartSlot; s<latestStartSlot; s++) {
				startAt = s; 
				int tempNum = 0; int freeNum = 0;
				for(int c=s; c<s+numSlot; c++) {
					if(evQueue.get(feeder)[c]<(watt>6600?evMax.get(feeder)[c]-1:evMax.get(feeder)[c])) {
						canCharge = true;
						tempNum+=circuitEVQueue[c];
						freeNum+=(evMax.get(feeder)[c] - evQueue.get(feeder)[c]);
					} else {
						canCharge = false;
						s=c;
						if(freeNum>bestFreeSoFar) {
							bestFreeSoFar = freeNum;
							bestStartSlotSoFar = startAt;
							numSlotSoFar = c - startAt;
						}
						break;
					}
				}
				if(canCharge) {
					if(freeNum>free || (freeNum==free && tempNum<minNum)) {
						minNum = tempNum;
						free = freeNum;
						minStartSlot = startAt;
					}
				}
			}
			
			if(minNum!=9999) {
				// found it!
				// update the queues and construct the suggestion
				for(int i=0; i<576; i++) {
					if(i>=minStartSlot && i<=minStartSlot+numSlot) { 
						suggestion[i] = 1;
						evQueue.get(feeder)[i] = (watt>6600?evQueue.get(feeder)[i]+2:evQueue.get(feeder)[i]+1);
						circuitEVQueue[i] += watt>6600?2:1;
					}
				}
				if(DEBUG) System.out.println("Suggestion>" + minStartSlot + "-" + (minStartSlot+numSlot));
			} else{ // can't find any consecutive slots that works
				if(DEBUG) System.out.print("NO OPTIMAL SOLUTION. Suggestion> ");
				for(int s=bestStartSlotSoFar; s<bestStartSlotSoFar+numSlotSoFar; s++){
					suggestion[s] = 1;
					evQueue.get(feeder)[s] = (watt>6600?evQueue.get(feeder)[s]+2:evQueue.get(feeder)[s]+1);
					circuitEVQueue[s] += watt>6600?2:1;
					if(DEBUG) System.out.print(s + " ");
				}
				for(int s=earliestStartSlot; s<latestStartSlot; s++) {
					if(numSlotSoFar>=numSlot) break;
					if(s<bestStartSlotSoFar || s>bestStartSlotSoFar+numSlotSoFar) {					
						int[] assignedFeeders = assignEVToFeeder(feeder, s, watt);
						if(assignedFeeders!=null && assignedFeeders.length==1) {
							suggestion[s] = 1;
							evQueue.get(feeder)[s] = (watt>6600?evQueue.get(feeder)[s]+2:evQueue.get(feeder)[s]+1);
							if(assignedFeeders[0]!=feeder) {
								evMax.get(feeder)[s] = (watt>6600?evMax.get(feeder)[s]+2:evMax.get(feeder)[s]+1);
								int weight = (int)(evWeight[feeder]/evWeight[assignedFeeders[0]]);
								evMax.get(assignedFeeders[0])[s] = (watt>6600?evMax.get(assignedFeeders[0])[s]-(2*weight):evMax.get(assignedFeeders[0])[s]-(1*weight));
							}
							circuitEVQueue[s] += watt>6600?2:1;
							numSlotSoFar++;
							if(DEBUG) System.out.print(s + " ");
						} else if(assignedFeeders!=null && assignedFeeders.length==2) {
							suggestion[s] = 1;
							evQueue.get(feeder)[s] = evQueue.get(feeder)[s]+2;
							for(int a=0; a<assignedFeeders.length; a++) {
								if(assignedFeeders[a]!=feeder) {
									evMax.get(feeder)[s] = evQueue.get(feeder)[s]+1;
									int weight = (int)(evWeight[feeder]/evWeight[assignedFeeders[a]]);
									evMax.get(assignedFeeders[a])[s] = evMax.get(assignedFeeders[a])[s]-weight;
								}
							}
							circuitEVQueue[s] += 2;
							numSlotSoFar++;
							if(DEBUG) System.out.print(s + " ");
						}	
					}
				}
				if(DEBUG) System.out.println();
				
				if(DEBUG && numSlotSoFar<numSlot) System.out.println("LACKING " + (numSlot-numSlotSoFar) + " SLOTS.");
			}
		}
		return suggestion;
	}
	
	/*
	 * second algorithm
	 */
	public synchronized Object[] requestNonPossibleStartTime(int simDay, double earliestChargeSecond, int duration, double watt, int feeder) {		
		while(waitForCSS) {
			synchronized(this) {
				try {
					this.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
	        }
		}
		waitForCSS = true;
		if(DEBUG) System.out.println("CEMS receives request on day " + simDay + " for the possible charge times starting from " + earliestChargeSecond + " " + duration);
		
		Object[] toReturn = new Object[2];
		ArrayList<String> nonPossible = new ArrayList<String>();
		ArrayList<String> possible = new ArrayList<String>();
		int earliestStartSlot = (int)((earliestChargeSecond/60)/5);
		int numSlot = (duration%5==0?duration/5:duration/5+1)+1;
		boolean canCharge = true;
		
		int startAt = 0;
		// find out when the EV can charge
		for(int s=earliestStartSlot; s<=576-numSlot; s=s+6) {
			startAt = s; 
			for(int c=s; c<s+numSlot; c++) {
				if(evQueue.get(feeder)[c]<(watt>6600?evMax.get(feeder)[c]-1:evMax.get(feeder)[c])) {
					canCharge = true;
				} else {
					canCharge = false;
					break;
				}
			}
			String toAdd = "" + ((startAt/12)%24) + ":" + ((startAt%12)*5);
			if(!canCharge) {
				nonPossible.add(toAdd);
			} else {
				possible.add(toAdd);
			}
		} 
		
		toReturn[0] = possible;
		toReturn[1] = nonPossible;
		
		return toReturn;
	}
	
	/**
	 * This method assumes that the starting charge time is on the current day (at night) and not the next day (next morning (after midnight)).
	 * (algorithm 3)
	 * @param curDay
	 * @param curMonth
	 * @param curYear
	 * @param earliestChargeSecond
	 * @param plannedChargeSecond
	 * @param duration
	 * @return
	 */
	public synchronized double[] requestStrictChargeTime(int simDay, int earliestChargeSecond, int duration, int latestChargeSecond, double watt, int feeder) {
		if(DEBUG) System.out.println("CEMS receives request on day " + simDay + " to charge at " + earliestChargeSecond);
		
		double[] suggestion = new double[576];
		
		// check whether the EV in question is able to charge at its desired time slot
		int earliestStartSlot = (earliestChargeSecond/60)/5;
				int latestStartSlot = (latestChargeSecond/60)/5;
		int numSlot = duration%5==0?duration/5:duration/5+1;
		if (DEBUG) System.out.println(earliestStartSlot + " " + numSlot);
		boolean canCharge = true;
		
		int startAt = earliestStartSlot;
		int minNum = 9999;
		int free = 0; int bestFreeSoFar = 0; int bestStartSlotSoFar = startAt; int numSlotSoFar = 0;
		int minStartSlot = earliestStartSlot;
		
		// find out when the EV can charge --> start from the earliest possible charge time
		for(int s=earliestStartSlot; s<latestStartSlot; s++) {
			startAt = s; 
			int tempNum = 0; int freeNum = 0; 
			for(int c=s; c<s+numSlot; c++) {
				if(evQueue.get(feeder)[c]<(watt>6600?evMax.get(feeder)[c]-1:evMax.get(feeder)[c])) {
					canCharge = true;
					tempNum+=circuitEVQueue[c];
					freeNum+=(evMax.get(feeder)[c] - evQueue.get(feeder)[c]);
				} else {
					canCharge = false;
					s=c;
					if(freeNum>bestFreeSoFar) {
						bestFreeSoFar = freeNum;
						bestStartSlotSoFar = startAt;
						numSlotSoFar = c - startAt;
					}
					break;
				}
			}
			if(canCharge) {
				if(freeNum>free || (freeNum==free && tempNum<minNum)) {
					minNum = tempNum;
					free = freeNum;
					minStartSlot = startAt;
				}
			}
		}
		
		if(minNum!=9999) { 
			// found it!
			// update the queues and construct the suggestion
			for(int i=0; i<576; i++) {
				if(i>=minStartSlot && i<minStartSlot+numSlot) { 
					suggestion[i] = 1; 
					evQueue.get(feeder)[i] = (watt>6600?evQueue.get(feeder)[i]+2:evQueue.get(feeder)[i]+1);
					circuitEVQueue[i] += watt>6600?2:1;
				}
			}
			if(DEBUG) System.out.println("Decision>" + minStartSlot + "-" + (minStartSlot+numSlot));
		} else{ // can't find any consecutive slots that works
			if(DEBUG) System.out.print("NO OPTIMAL SOLUTION. Suggestion> ");
			for(int s=bestStartSlotSoFar; s<bestStartSlotSoFar+numSlotSoFar; s++){
				suggestion[s] = 1;
				evQueue.get(feeder)[s] = (watt>6600?evQueue.get(feeder)[s]+2:evQueue.get(feeder)[s]+1);
				circuitEVQueue[s] += watt>6600?2:1;
				if(DEBUG) System.out.print(s + " ");
			}
			for(int s=earliestStartSlot; s<latestStartSlot; s++) {
				if(numSlotSoFar>=numSlot) break;
				if(s<bestStartSlotSoFar || s>bestStartSlotSoFar+numSlotSoFar) {					
					int[] assignedFeeders = assignEVToFeeder(feeder, s, watt);
					if(assignedFeeders!=null && assignedFeeders.length==1) {
						suggestion[s] = 1;
						evQueue.get(feeder)[s] = (watt>6600?evQueue.get(feeder)[s]+2:evQueue.get(feeder)[s]+1);
						if(assignedFeeders[0]!=feeder) {
							evMax.get(feeder)[s] = (watt>6600?evMax.get(feeder)[s]+2:evMax.get(feeder)[s]+1);
							int weight = (int)(evWeight[feeder]/evWeight[assignedFeeders[0]]);
							evMax.get(assignedFeeders[0])[s] = (watt>6600?evMax.get(assignedFeeders[0])[s]-(2*weight):evMax.get(assignedFeeders[0])[s]-(1*weight));
						}
						circuitEVQueue[s] += watt>6600?2:1;
						numSlotSoFar++;
						if(DEBUG) System.out.print(s + " ");
					} else if(assignedFeeders!=null && assignedFeeders.length==2) {
						suggestion[s] = 1;
						evQueue.get(feeder)[s] = evQueue.get(feeder)[s]+2;
						for(int a=0; a<assignedFeeders.length; a++) {
							if(assignedFeeders[a]!=feeder) {
								evMax.get(feeder)[s] = evQueue.get(feeder)[s]+1;
								int weight = (int)(evWeight[feeder]/evWeight[assignedFeeders[a]]);
								evMax.get(assignedFeeders[a])[s] = evMax.get(assignedFeeders[a])[s]-weight;
							}
						}
						circuitEVQueue[s] += 2;
						numSlotSoFar++;
						if(DEBUG) System.out.print(s + " ");
					}	
				}
			}
			if(DEBUG) System.out.println();
			
			if(DEBUG && numSlotSoFar<numSlot) System.out.println("LACKING " + (numSlot-numSlotSoFar) + " SLOTS.");
		}
		
		return suggestion;
	}
	
	/*
	 * ONLY USED THIS WHEN WE DON'T WANT TO USE CEMS BUT NEEDS STATISTICS ON NUMBER OF SCHEDULED EVS
	 */
	/*public synchronized double[] requestStrictChargeTime(int simDay, int earliestChargeSecond, int duration, int latestChargeSecond, double watt) {
		//System.out.println("CEMS receives request on day " + simDay + " to charge at " + plannedChargeSecond);
		
		double[] suggestion = new double[576];
		
		// check whether the EV in question is able to charge at its desired time slot
		int earliestStartSlot = (earliestChargeSecond/60)/5;
		int numSlot = duration%5==0?duration/5:duration/5+1;
		
		int startAt = earliestStartSlot;

		for(int i=0; i<576; i++) {
			if(i>=startAt && i<=startAt+numSlot) { 
				suggestion[i] = 1;
				if(i<288)
					evCurDayQueue[i] = (watt>6600?evCurDayQueue[i]+2:evCurDayQueue[i]+1);
				else
					evNextDayQueue[i-288] = (watt>6600?evNextDayQueue[i-288]+2:evNextDayQueue[i-288]+1);
			}
		}	
		
		return suggestion;
	}*/
	
	public synchronized void informCSSDecision(int simDay, ArrayList<Integer> intervals, double watt, boolean chargeMode, int feeder, int id) {
		if(DEBUG) System.out.println("[Model ID#" + id + "] CEMS is informed of CSS decision on day " + simDay + " to charge (" + chargeMode + ").");

		// update the queues
		if(chargeMode) {
			for(int in=0; in<intervals.size(); in++) {
				int i = intervals.get(in);
				evQueue.get(feeder)[intervals.get(i)] = watt>6600?evQueue.get(feeder)[i]+2:evQueue.get(feeder)[i]+1;
				circuitEVQueue[i] += watt>6600?2:1;
			}
			
			waitForCSS = false;
			notify();
		} else {
			for(int in=0; in<intervals.size(); in++) {
				int i = intervals.get(in);
				evQueue.get(feeder)[i] = Math.max(0, watt>6600?evQueue.get(feeder)[i]-2:evQueue.get(feeder)[i]-1);
				circuitEVQueue[i] = Math.max(0, watt>6600?circuitEVQueue[i]-2:circuitEVQueue[i]-1);
			}
		}
	}
	
	/* 
	 * For non-EVContract customers who get priorities
	 */
	public synchronized void informNonEVCustChargeTime(int simDay, ArrayList<Integer> intervals, double watt, boolean chargeMode, int feeder) { 	
		// non-EVContract customers can always charge their cars whenever they want to
		// if the slot is full, EVContract customers have to reschedule their EV charging
		// non-EVContract customers may also change their minds and decide to not charge
		// at previous informed time slots and charge at other slots

		
		// update the queues
		for(int in=0; in<intervals.size(); in++) {
			int i = intervals.get(in);
			if(chargeMode) { // inform to charge
				evQueue.get(feeder)[i] = watt>6600?evQueue.get(feeder)[i]+2:evQueue.get(feeder)[i]+1;
				circuitEVQueue[i] += watt>6600?2:1;
				if(evQueue.get(feeder)[i]>evMax.get(feeder)[i]) {
					// check which EVs are scheduled to charge in this interval
					HashMap<EVChargerModel,Double> chargingCars = new HashMap<EVChargerModel,Double>();
					HashMap<EVChargerModel,Integer> rescheduledCars = new HashMap<EVChargerModel,Integer>();
					for(SimulationModel house:customers) {
						if(house.getFeederID()!=feeder) continue;
						EVChargerModel ev = (EVChargerModel)house.getEnvironment().getDeviceModelByName("EV");
						if(ev!=null) {
							int toCheck = 0;
							if(i%288==0) toCheck = 288;
							else if(i<288) toCheck = i;
							else toCheck = i%288;
							if(ev.getSchedule()[toCheck]!=0 && ev.lv2Charging()) { 
								chargingCars.put(ev, ev.getCurrentSoc());
							}
						}
					}
					rescheduleEVs(((curDay-1)*288)+i, i, evMax.get(feeder)[i]-evQueue.get(feeder)[i], chargingCars, rescheduledCars, evMax.get(feeder)[i], feeder);
				}
			} else { // inform to free up slots
				evQueue.get(feeder)[i] = Math.max(0, watt>6600?evQueue.get(feeder)[i]-2:evQueue.get(feeder)[i]-1);
				circuitEVQueue[i] = Math.max(0, watt>6600?circuitEVQueue[i]-2:circuitEVQueue[i]-1);
			}
		}
		
	}
	
	private int[] assignEVToFeeder(int intendedFeeder, int slot, double watt) {
		if(evQueue.get(intendedFeeder)[slot] < (watt>6600?evMax.get(intendedFeeder)[slot]-2:evMax.get(intendedFeeder)[slot]-1)) {
			int[] feeders = new int[1]; feeders[0] = intendedFeeder;
			return feeders;
		} else {
			int bestFeeder = -1; int bestDifference = 0;
			for(int f=0; f<numHousesInFeeders.length; f++) {
				if(f==intendedFeeder) continue;
				
				int difference = evMax.get(f)[slot] - evQueue.get(f)[slot];
				if(difference>(watt>6600?1:0) && difference>bestDifference) {
					bestDifference = difference;
					bestFeeder = f;
				} 
			}
			if(bestFeeder!=-1) {
				int[] feeders = new int[1]; feeders[0] = bestFeeder;
				return feeders;
			} else if(watt>6600){
				int[] feeders = new int[2]; int index = 0;
				for(int f=0; f<numHousesInFeeders.length; f++) {					
					int difference = evMax.get(f)[slot] - evQueue.get(f)[slot];
					if(difference>0 && index<2) {
						feeders[index++] = f;
					} 
				}
				if(index<1) return null;
				else return feeders;
			} else return null;
		}
	}
	
	public void prepareNewDayContext(int simDay) {
		if(simDay!=curDay) {			
			// write queue to file first
			writeEVQueueToFile();
			
			for(int i=0; i<evQueue.size(); i++) {
				int[] newQueue = new int[576];
				System.arraycopy(evQueue.get(i), 288, newQueue, 0, 288);
				evQueue.set(i, newQueue);
			}
			
			curDay = simDay;
		}
	}
	
	private void createQueueFiles() {
		for(int feeder=0; feeder<numHousesInFeeders.length; feeder++) {
			String path = "data/cems/ev-charge-" + feeder + ".txt";
			
			FileWriter fw = null;
			try {
				fw = new FileWriter(path,false);
				
				PrintWriter pw = new PrintWriter(fw);
				
				String daily = "0";
				
				for(int i=1; i<288; i++) {
					daily = daily + " 0";
				}
				
				for(int l=0; l<=1; l++) { // we need the next day also even though we don't simulate that day
					pw.println(daily);
				}
				
				pw.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void writeEVQueueToFile() {
		for(int i=0; i<numHousesInFeeders.length; i++) {
			writeEVQueueToFile(i);
		}
	}
	
	public void writeEVQueueToFile(int feeder) {
		String random = "data/cems/ev-charge-temp" + System.currentTimeMillis() + "-" + feeder +".txt";
		
		String path = "data/cems/ev-charge-" + feeder + ".txt";
		File oldfile = new File(path);
		File newfile = new File(random);
		
		oldfile.renameTo(newfile);
		
		BufferedReader br = null;
		
		FileWriter fw = null;
		PrintWriter pw = null;
		try {
			fw = new FileWriter(path,true);
			pw = new PrintWriter(fw);
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		try {
			String sCurrentLine;
			br = new BufferedReader(new FileReader(random));
 
			int day = 1;
			while ((sCurrentLine = br.readLine()) != null && !sCurrentLine.isEmpty()) {
				if(day==curDay) {
					String newLineData = "";
					for(int i=0; i<576; i++) {
						newLineData += evQueue.get(feeder)[i];
						if(i+1==288) {
							pw.println(newLineData);
							newLineData = "";
						} else if(i+1<576) newLineData += " "; 
					}
					pw.println(newLineData);
					day++;
					break;
				} else {
					pw.println(sCurrentLine);
				}
				day++;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null) br.close();
				pw.close();
				newfile.delete();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	public void setEVChargeAlgorithm(int algorithm) {
		evChargeAlgorithm = algorithm;
	}
	
	public int getEVChargeAlgorithm() {
		return evChargeAlgorithm;
	}
	
	public boolean isWaiting() {
		return waitForCSS;
	}
	
	public boolean isSyncing() {
		return waitForOtherEVs;
	}
}
