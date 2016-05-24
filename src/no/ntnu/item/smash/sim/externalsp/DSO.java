package no.ntnu.item.smash.sim.externalsp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;

import no.ntnu.item.smash.sim.core.SimulationModel;
import no.ntnu.item.smash.sim.data.Constants;
import no.ntnu.item.smash.sim.data.parser.PriceDataParser;
import no.ntnu.item.smash.sim.data.utility.LoadDataUtility;
import no.ntnu.item.smash.sim.data.utility.PeakloadDataUtility;
import no.ntnu.item.smash.sim.pss.loadflow.LVNetwork;
import no.ntnu.item.smash.sim.structure.CLCRequest;
import no.ntnu.item.smash.sim.structure.DLCRequest;
import no.ntnu.item.smash.sim.structure.Device;

public class DSO {
	public static final int CONTRACT_RTP = 0;
	public static final int CONTRACT_RTPCPP = 1;
	public static final int CONTRACT_RTPCLC = 2;
	public static final int CONTRACT_RTPDLC = 3;
	public static final int CONTRACT_RTPEV = 4;
	public static final int CONTRACT_RTPCPPEV = 5;
	public static final int CONTRACT_RTPCLCEV = 6;
	public static final int CONTRACT_RTPDLCEV = 7;
	public static final int CONTRACT_FLAT = 8;

	private ArrayList<SimulationModel> customers = new ArrayList<SimulationModel>();
	private HashMap<Integer, LinkedList<SimulationModel>> customerContracts = new HashMap<Integer, LinkedList<SimulationModel>>();
	private HashMap<Integer, PricingContract> offeredContracts = new HashMap<Integer, PricingContract>();
	private LinkedList<SimulationModel> dlcTurn = new LinkedList<SimulationModel>();
	
	private int lastControlDay = 0;
	private int[] numControlPerCust;

	public DSO(ArrayList<SimulationModel> customers) {
		for (int i = 0; i < 9; i++) {
			LinkedList<SimulationModel> newCustomers = new LinkedList<SimulationModel>();
			customerContracts.put(i, newCustomers);
		}

		this.customers = customers;
		numControlPerCust = new int[customers.size()];
		
		for (SimulationModel customer : customers) {
			customerContracts.get(customer.getContract()).add(customer);
			if (customer.getContract() == CONTRACT_RTPCLC
					|| customer.getContract() == CONTRACT_RTPDLC
					|| customer.getContract() == CONTRACT_RTPCLCEV
					|| customer.getContract() == CONTRACT_RTPDLCEV)
				dlcTurn.add(customer);
		}

//		offeredContracts.put(CONTRACT_FLAT, new FlatRateContract(30));
		offeredContracts.put(CONTRACT_RTP, new StandardRTPContract(30));
//		offeredContracts.put(CONTRACT_RTPCPP, new RTPwithCPPContract(30));
		offeredContracts.put(CONTRACT_RTPDLC, new RTPwithDLCContract(30));
		offeredContracts.put(CONTRACT_RTPCLC, new RTPwithCLCContract(30));
		offeredContracts.put(CONTRACT_RTPEV, new RTPwithEVContract(30));
//		offeredContracts.put(CONTRACT_RTPCPPEV, new RTPwithCPPEVContract(30));
		offeredContracts.put(CONTRACT_RTPCLCEV, new
		 RTPwithCLCEVContract(30));
		offeredContracts.put(CONTRACT_RTPDLCEV, new RTPwithDLCEVContract(30));
	}

	public void assignEnergyPrices(int interval, int day, int month, int year) {
		for (Integer t : offeredContracts.keySet()) {
			offeredContracts.get(t).assignEnergyPrices(interval, day, month,
					year);
		}
	}

	public void assignGridPrices(int interval, int day, int month, int year) {
		for (Integer t : offeredContracts.keySet()) {
			offeredContracts.get(t)
					.assignGridPrices(interval, day, month, year);
		}
	}

	public void resolveEnergyPriceAtTime(int interval, int day, int month,
			int year) {
		for (Integer t : offeredContracts.keySet()) {
			offeredContracts.get(t).resolveEnergyPriceAtTime(interval, day,
					month, year);
		}
	}

	public void resolveGridPriceAtTime(int interval, int day, int month,
			int year) {
		for (Integer t : offeredContracts.keySet()) {
			offeredContracts.get(t).resolveGridPriceAtTime(interval, day,
					month, year);
		}
	}

	public void assignExtraCostAtTime(int interval, int day, int month, int year) {
		for (Integer t : offeredContracts.keySet()) {
			offeredContracts.get(t)
					.assignExtraCosts(interval, day, month, year);
		}
	}

	public static boolean isEVContract(int type) {
		int[] evContracts = { CONTRACT_RTPCLCEV, CONTRACT_RTPCPPEV,
				CONTRACT_RTPDLCEV, CONTRACT_RTPEV };

		for (int i = 0; i < evContracts.length; i++) {
			if (type == evContracts[i])
				return true;
		}

		return false;
	}

	public static boolean isControlContract(int type) {
		int[] controlContract = { CONTRACT_RTPDLC, CONTRACT_RTPCLC,
				CONTRACT_RTPDLCEV, CONTRACT_RTPCLCEV };

		for (int i = 0; i < controlContract.length; i++) {
			if (type == controlContract[i])
				return true;
		}

		return false;
	}

	public HashMap<SimulationModel, Double> sendDRSignal(double minVoltage, double overloadAmount,
			int interval, int day, int month, int year) {
		if(day!=lastControlDay) {
			lastControlDay = day;
			
			for (SimulationModel customer : customers) {
				if (customer.getContract() == CONTRACT_RTPCLC
						|| customer.getContract() == CONTRACT_RTPDLC
						|| customer.getContract() == CONTRACT_RTPCLCEV
						|| customer.getContract() == CONTRACT_RTPDLCEV)
					if(!dlcTurn.contains(customer)) dlcTurn.add(customer);
			}
			
			numControlPerCust = new int[customers.size()];
		} 
		
		HashMap<SimulationModel, Double> totalResponses = new HashMap<SimulationModel, Double>();
		
		ArrayList<Integer> controlled = new ArrayList<Integer>();
		int dlcCusts = customerContracts.get(CONTRACT_RTPDLC).size() + customerContracts.get(CONTRACT_RTPDLCEV).size();
		int clcCusts = customerContracts.get(CONTRACT_RTPCLC).size() + customerContracts.get(CONTRACT_RTPCLCEV).size();
		for(int i=0; i<dlcTurn.size(); i++) {
			if(overloadAmount<=0) break;
			
			SimulationModel customer = dlcTurn.get(i);
			
			if(customer.getContract()==CONTRACT_RTPDLC || customer.getContract()==CONTRACT_RTPDLCEV) { 
				DLCRequest req = new DLCRequest(DLCContract.controlledDeviceType,
						interval, interval+12, 30, 10);
				req.setId("DLC" + interval); 
				double amount = customer.sendControlRequestToCSS(req);
				if(amount>0) {
					System.out.println("[DSO] Control signal " + req.getId() + " sent to House #" + customer.getModelID() + ".");
					totalResponses.put(customer, amount);
					overloadAmount -= amount;
					controlled.add(i);
				}
				dlcCusts--;
			} else {
				double each = (overloadAmount - (dlcCusts*2000))/clcCusts;
				CLCRequest req = new CLCRequest(interval % 288,
						(interval+12) % 288, Math.min(each, 2000));
				req.setId("CLC" + interval);
				double amount = customer.sendControlRequestToCSS(req);
				if(amount>0) {
					System.out.println("[DSO] Control signal " + req.getId() + " sent to House #" + customer.getModelID() + ".");
					totalResponses.put(customer, amount);
					overloadAmount -= amount;
					controlled.add(i);
				}
				clcCusts--;
			}
		}
		
		int removed = 0;
		for(Integer index:controlled) {
			numControlPerCust[dlcTurn.get(index - removed).getModelID()]++;
			
			if(numControlPerCust[dlcTurn.get(index - removed).getModelID()]<5)
				dlcTurn.add(dlcTurn.remove(index - removed));
			else
				dlcTurn.remove(index - removed);
			removed++;
		}
		
		return totalResponses;
	}
	
	public HashMap<SimulationModel, Double> sendDRSignal(double minVoltage, int interval, int day,
			int month, int year) {
		HashMap<SimulationModel, Double> totalResponses = new HashMap<SimulationModel, Double>();
		LVNetwork network = customers.get(0).getEventManager().getNetwork();
		int[] numHousesInFeeders = network.getNumHousesInFeeders();

		// find out which feeders have too low voltages
		double[] houseVoltages = new double[customers.size()];
		for (int i = 2; i < network.getAclfNetwork().getBusList().size(); i++) {
			double v = network.getAclfNetwork().getBus(i).getVoltage()
					.getReal()
					* network.getAclfNetwork().getBus(0).getBaseVoltage();
			houseVoltages[i - 2] = v;
		}
		int compensation = 0;
		
		for (int feeder = 0; feeder < numHousesInFeeders.length; feeder++) {
			if (feeder > 0)
				compensation += numHousesInFeeders[feeder - 1];

			// check voltage of house at end of feeder
			double minFeederVoltage = houseVoltages[compensation
					+ numHousesInFeeders[feeder] - 1];
			ArrayList<SimulationModel> dlcCustomers = new ArrayList<SimulationModel>();
			ArrayList<SimulationModel> clcCustomers = new ArrayList<SimulationModel>();
			ArrayList<SimulationModel> dlcEVCustomers = new ArrayList<SimulationModel>();
			ArrayList<SimulationModel> clcEVCustomers = new ArrayList<SimulationModel>();
			if (minFeederVoltage < 211) {
				// some houses in this feeder must reduce energy usage
				int refLocation = 9999;
				for (int customer = compensation; customer < compensation
						+ numHousesInFeeders[feeder]; customer++) {
					int contract = customers.get(customer).getContract();
					if (isControlContract(contract)) {
						if (refLocation == 9999) {
							refLocation = customer;
						}
						if (contract == DSO.CONTRACT_RTPCLC)
							clcCustomers.add(customers.get(customer));
						else if (contract == DSO.CONTRACT_RTPCLCEV)
							clcEVCustomers.add(customers.get(customer));
						else if (contract == DSO.CONTRACT_RTPDLC)
							dlcCustomers.add(customers.get(customer));
						else if (contract == DSO.CONTRACT_RTPDLCEV) {
							dlcEVCustomers.add(customers.get(customer));
						}
					}
				}
				if (refLocation == 9999)
					continue;

				// use refLocation to determine how much
				// kW needs to be shaved
				double amount = ((211 - minFeederVoltage)
						* customers.get(0).getEventManager().getNetwork()
								.getAclfNetwork().getBus(refLocation + 1)
								.getVoltage().getReal() * 230)
						/ (2 * (LVNetwork.resistancePerKm
								* LVNetwork.km[refLocation] + (0.9 * LVNetwork.reactancePerKm * LVNetwork.km[refLocation])));
				System.out.println("[DSO] Feeder " + feeder + " need to shave " + amount + ".");
				// TODO: calculate how long to perform load shedding for?
				int numInterval = 12;

				// we will start with DLC customers first because it's more
				// predictable
				// since they can't refuse
				// assume that all DLC customers have a specially equipped water
				// heater
				// installed and it uses 2 kW power
				
				double remaining = amount;
				HashMap<SimulationModel, Double> dlcResponses = offeredContracts.get(CONTRACT_RTPDLC).sendDRSignal(
						interval, interval + numInterval,
						remaining, dlcCustomers);
				for(SimulationModel c:dlcResponses.keySet()) {
					remaining -= dlcResponses.get(c);
					totalResponses.put(c, dlcResponses.get(c));
				}
				HashMap<SimulationModel, Double> dlcevResponses = offeredContracts.get(CONTRACT_RTPDLCEV).sendDRSignal(
						interval, interval + numInterval,
						remaining, dlcEVCustomers);
				for(SimulationModel c:dlcevResponses.keySet()) {
					remaining -= dlcevResponses.get(c);
					totalResponses.put(c, dlcevResponses.get(c));
				}
				
				if(remaining>0) {
					double forEach = Math.min(2000, (clcCustomers.size() + clcEVCustomers.size())==0?remaining:remaining
							/ (clcCustomers.size() + clcEVCustomers.size())); // must have a limit of how much can be shaved
	
					HashMap<SimulationModel, Double> clcResponses = offeredContracts
							.get(CONTRACT_RTPCLC)
							.sendDRSignal(interval, interval + numInterval,
									forEach * clcCustomers.size(), clcCustomers);
					HashMap<SimulationModel, Double> clcevResponses = offeredContracts.get(CONTRACT_RTPCLCEV)
							.sendDRSignal(interval, interval + numInterval,
									forEach * clcEVCustomers.size(),
									clcEVCustomers);
					for(SimulationModel c:clcResponses.keySet()) {
						totalResponses.put(c, clcResponses.get(c));
					}
					for(SimulationModel c:clcevResponses.keySet()) {
						totalResponses.put(c, clcevResponses.get(c));
					}					
				}
			}
		}

		return totalResponses;
	}

	public PricingContract getContract(int type) {
		return offeredContracts.get(type);
	}

	public int getPriceChangeInterval(int type) {
		return offeredContracts.get(type).getPriceChangeInterval();
	}

	public int getNumCustomerByContract(int type) {
		return customerContracts.get(type) != null ? customerContracts
				.get(type).size() : 0;
	}

	public boolean hasLoadControlCustomers() {
		for (SimulationModel c : customers) {
			if (c.getContract() == CONTRACT_RTPDLC
					|| c.getContract() == CONTRACT_RTPCLC
					|| c.getContract() == CONTRACT_RTPDLCEV
					|| c.getContract() == CONTRACT_RTPCLCEV)
				return true;
		}

		return false;
	}

	/*
	 * All contract types
	 */
	// Flat rate
	// work-around for flat rate contract (a proper one should be created later on that doesn't
	// extend RTPContract)
	public class FlatRateContract extends RTPContract {
		public FlatRateContract(int updateInterval) {
			super(updateInterval);
		}
		
		public double[] getEstGridPricesForDay(int day, int month, int year) {
			return new double[24 * 60 / updateInterval];
		}
		
		public double[] getActGridPricesForDay(int day, int month, int year) {
			return new double[24 * 60 / updateInterval];
		}
		
		public double getEstElectricityPriceAtTime(int time, int day,
				int month, int year) {
			return super.getEstElectricityPriceAtTime(time, day, month, year) + 0.3;
		}
		
		public double getActElectricityPriceAtTime(int time, int day,
				int month, int year) {
			return super.getActElectricityPriceAtTime(time, day, month, year) + 0.3;
		}
		
		public HashMap<SimulationModel, Double> sendDRSignal(int startInterval, int endInterval,
				double totalAmount, ArrayList<SimulationModel> targetCustomers) {
			return new HashMap<SimulationModel, Double>();
		}
	}
	
	// Standard RTP
	public class StandardRTPContract extends RTPContract {
		public StandardRTPContract(int updateInterval) {
			super(updateInterval);
		}

		public HashMap<SimulationModel, Double> sendDRSignal(int startInterval, int endInterval,
				double totalAmount, ArrayList<SimulationModel> targetCustomers) {
			return new HashMap<SimulationModel, Double>();
		}
	}

	// RTP with CPP
	public class RTPwithCPPContract extends CPPContract {
		public RTPwithCPPContract(int updateInterval) {
			super(updateInterval);
		}

		public double[] getEstGridPricesForDay(int day, int month, int year) {
			double[] prices = new double[24 * 60 / updateInterval];

			for (int i = 0; i < prices.length; i++) {
				prices[i] = GRID_EST_PRICES[day - 1][i] - 0.2;
			}

			return prices;
		}

		public double[] getActGridPricesForDay(int day, int month, int year) {
			double[] prices = new double[24 * 60 / updateInterval];

			for (int i = 0; i < prices.length; i++) {
				prices[i] = GRID_ACT_PRICES[day - 1][i] - 0.2;
			}

			return prices;
		}

		public double getEstElectricityPriceAtTime(int time, int day,
				int month, int year) {
			return super.getEstElectricityPriceAtTime(time, day, month, year) - 0.2;
		}
		
		public HashMap<SimulationModel, Double> sendDRSignal(int startInterval, int endInterval,
				double totalAmount, ArrayList<SimulationModel> targetCustomers) {
			return new HashMap<SimulationModel, Double>();
		}
	}

	// RTP with DLC
	public class RTPwithDLCContract extends RTPContract implements DLCContract {
		public RTPwithDLCContract(int updateInterval) {
			super(updateInterval);
		}

		public HashMap<SimulationModel, Double> sendDRSignal(int startInterval, int endInterval,
				double totalAmount, ArrayList<SimulationModel> targetCustomers) {
			HashMap<SimulationModel, Double> responses = new HashMap<SimulationModel, Double>();

			ArrayList<Integer> controlled = new ArrayList<Integer>();
			for (int i = 0; i < dlcTurn.size(); i++) {
				if (totalAmount <= 0)
					break;

				if (dlcTurn.get(i).getContract() == CONTRACT_RTPDLC
						&& (targetCustomers.size() == 0 || targetCustomers
								.contains(dlcTurn.get(i)))) {
					SimulationModel customer = dlcTurn.get(i);
					DLCRequest req = new DLCRequest(controlledDeviceType,
							startInterval, endInterval, 30, 10);
					req.setId("DLC" + startInterval);
					double amount = customer.sendControlRequestToCSS(req);
					if(amount>0) {
						System.out.println("[DSO] Control signal " + req.getId() + " sent to House #" + customer.getModelID() + ".");
						responses.put(customer, amount);
						totalAmount -= amount;
						controlled.add(i);
					}
				}
			}
			
			for(Integer index:controlled) {
				dlcTurn.add(dlcTurn.remove((int)index));
			}
			
			return responses;
		}

		public void assignExtraCosts(int interval, int day, int month, int year) {
			super.assignExtraCosts(interval, day, month, year);

			if(day!=no.ntnu.item.smash.sim.data.Constants.getNumDaysInMonth(month, year))
				return;

			// pay fixed compensation to customers of 100kr per month
			for (SimulationModel customer : customerContracts
					.get(CONTRACT_RTPDLC)) {
				double toPay = 100;
				double[] toAdd = new double[24];
				toAdd[toAdd.length - 1] = -toPay;
				customer.getEnergyMonitor().addCostDataAtDay(day, toAdd);
				customer.getEnergyMonitor().writeCompentation(customer.getModelID(), -toPay, customer.getEventManager().getResultDirectory(), "DLC");
			}
		}
	}

	// RTP with CLC
	public class RTPwithCLCContract extends RTPContract implements CLCContract {
		public RTPwithCLCContract(int updateInterval) {
			super(updateInterval);
		}

		public HashMap<SimulationModel, Double> sendDRSignal(int startInterval, int endInterval,
				double totalAmount, ArrayList<SimulationModel> targetCustomers) {
			HashMap<SimulationModel, Double> responses = new HashMap<SimulationModel, Double>();
			
			CLCRequest req = new CLCRequest(startInterval % 288,
					endInterval % 288, totalAmount
							/ (targetCustomers == null ? customerContracts.get(
									CONTRACT_RTPCLC).size()
									: targetCustomers.size()));
			req.setId("CLC" + startInterval);

			for (SimulationModel customer : (targetCustomers == null ? customerContracts
					.get(CONTRACT_RTPCLC) : targetCustomers)) {
				double amount = customer.sendControlRequestToCSS(req);
				if(amount>0) responses.put(customer, amount);
			}

			return responses;
		}

		public void assignExtraCosts(int interval, int day, int month, int year) {
			super.assignExtraCosts(interval, day, month, year);

			// pay compensation to customers for DoD
			for (SimulationModel customer : customerContracts
					.get(CONTRACT_RTPCLC)) {
				// read the DSOControl.log file to identify the affected devices
				// and time intervals
				double[] duration = customer.retrieveDiscomfortDuration(day,
						month);
				double toPay = (duration[0] * 1) + (duration[1] * 2);
				System.out.println("Compensate " + toPay + " on day " + day + " to Customer# " + customer.getModelID());
				double[] toAdd = new double[24];
				toAdd[toAdd.length - 1] = -toPay;
				customer.getEnergyMonitor().addCostDataAtDay(day, toAdd);
				customer.getEnergyMonitor().writeCompentation(customer.getModelID(), -toPay, customer.getEventManager().getResultDirectory(), "CLCDoD");
			}
			
			// pay fixed compensation to customers of 50kr per month
			if(day!=no.ntnu.item.smash.sim.data.Constants.getNumDaysInMonth(month, year))
				return;
			for (SimulationModel customer : customerContracts
					.get(CONTRACT_RTPCLC)) {
				double toPay = 50;
				double[] toAdd = new double[24];
				toAdd[toAdd.length - 1] = -toPay;
				customer.getEnergyMonitor().addCostDataAtDay(day, toAdd);
				customer.getEnergyMonitor().writeCompentation(customer.getModelID(), -toPay, customer.getEventManager().getResultDirectory(), "CLC");
			}
		}
	}

	// RTP with EV scheduling
	public class RTPwithEVContract extends RTPContract implements EVContract {
		public RTPwithEVContract(int updateInterval) {
			super(updateInterval);
		}

		public HashMap<SimulationModel, Double> sendDRSignal(int startInterval, int endInterval,
				double totalAmount, ArrayList<SimulationModel> targetCustomers) {
			return new HashMap<SimulationModel, Double>();
		}

		public void assignExtraCosts(int interval, int day, int month, int year) {
			super.assignExtraCosts(interval, day, month, year);

			// pay compensation to customers for DoD
			for (SimulationModel customer : customerContracts
					.get(CONTRACT_RTPEV)) {
				double toPay = 0;
				String comfort = customer.retrieveEVDiscomfort(day, month);
				if (comfort.equals("R"))
					toPay = 20;
				else if (comfort.equals("U"))
					toPay = 100;
				double[] toAdd = new double[24];
				toAdd[toAdd.length - 1] = -toPay;
				if (toPay > 0)
					customer.getEnergyMonitor().writeCompentation(customer.getModelID(), -toPay, customer.getEventManager().getResultDirectory(), "EVDoD");
				customer.getEnergyMonitor().addCostDataAtDay(day, toAdd);
			}

			// pay fixed compensation to customers of 50kr per month
			if(day!=no.ntnu.item.smash.sim.data.Constants.getNumDaysInMonth(month, year))
				return;

			for (SimulationModel customer : customerContracts
					.get(CONTRACT_RTPEV)) {
				double toPay = 50;
				double[] toAdd = new double[24];
				toAdd[toAdd.length - 1] = -toPay;
				customer.getEnergyMonitor().addCostDataAtDay(day, toAdd);
				customer.getEnergyMonitor().writeCompentation(customer.getModelID(), -toPay, customer.getEventManager().getResultDirectory(), "EV");
			}
		}
	}

	// RTP with CPP and EV scheduling
	public class RTPwithCPPEVContract extends CPPContract implements EVContract {
		public RTPwithCPPEVContract(int updateInterval) {
			super(updateInterval);
		}

		public double[] getEstGridPricesForDay(int day, int month, int year) {
			double[] prices = new double[24 * 60 / updateInterval];

			for (int i = 0; i < prices.length; i++) {
				prices[i] = GRID_EST_PRICES[day - 1][i] - 0.2;
			}

			return prices;
		}

		public double[] getActGridPricesForDay(int day, int month, int year) {
			double[] prices = new double[24 * 60 / updateInterval];

			for (int i = 0; i < prices.length; i++) {
				prices[i] = GRID_ACT_PRICES[day - 1][i] - 0.2;
			}

			return prices;
		}

		public double getEstElectricityPriceAtTime(int time, int day,
				int month, int year) {
			return super.getEstElectricityPriceAtTime(time, day, month, year) - 0.2;
		}
		
		public HashMap<SimulationModel, Double> sendDRSignal(int startInterval, int endInterval,
				double totalAmount, ArrayList<SimulationModel> targetCustomers) {
			return new HashMap<SimulationModel, Double>();
		}

		public void assignExtraCosts(int interval, int day, int month, int year) {
			if (getNumCustomerByContract(CONTRACT_RTPCPP) == 0)
				super.assignExtraCosts(interval, day, month, year);

			// pay compensation to customers for DoD
			for (SimulationModel customer : customerContracts
					.get(CONTRACT_RTPCPPEV)) {
				double toPay = 0;
				String comfort = customer.retrieveEVDiscomfort(day, month);
				if (comfort.equals("R"))
					toPay = 20;
				else if (comfort.equals("U"))
					toPay = 100;
				double[] toAdd = new double[24];
				toAdd[toAdd.length - 1] = -toPay;
				customer.getEnergyMonitor().addCostDataAtDay(day, toAdd);
				customer.getEnergyMonitor().writeCompentation(customer.getModelID(), -toPay, customer.getEventManager().getResultDirectory(), "EVDoD");
			}
			
			// pay fixed compensation to customers of 50kr per month
			if(day!=no.ntnu.item.smash.sim.data.Constants.getNumDaysInMonth(month, year))
				return;

			for (SimulationModel customer : customerContracts
					.get(CONTRACT_RTPCPPEV)) {
				double toPay = 50;
				double[] toAdd = new double[24];
				toAdd[toAdd.length - 1] = -toPay;
				customer.getEnergyMonitor().addCostDataAtDay(day, toAdd);
				customer.getEnergyMonitor().writeCompentation(customer.getModelID(), -toPay, customer.getEventManager().getResultDirectory(), "EV");
			}
		}
	}

	// RTP with DLC and EV scheduling
	public class RTPwithDLCEVContract extends RTPContract implements
			DLCContract, EVContract {
		public RTPwithDLCEVContract(int updateInterval) {
			super(updateInterval);
		}

		public HashMap<SimulationModel, Double> sendDRSignal(int startInterval, int endInterval,
				double totalAmount, ArrayList<SimulationModel> targetCustomers) {
			HashMap<SimulationModel, Double> responses = new HashMap<SimulationModel, Double>();

			for (int i = 0; i < dlcTurn.size(); i++) {
				if (totalAmount <= 0)
					break;

				if (dlcTurn.get(i).getContract() == CONTRACT_RTPDLCEV
						&& (targetCustomers.size() == 0 || targetCustomers
								.contains(dlcTurn.get(i)))) {
					SimulationModel customer = dlcTurn.remove(i);
					DLCRequest req = new DLCRequest(controlledDeviceType,
							startInterval, endInterval, 30, 10);
					req.setId("DLC" + startInterval);
					double amount = customer.sendControlRequestToCSS(req);
					if(amount>0) {
						responses.put(customer, amount);
						totalAmount -= amount;
						System.out.println("[DSO] Control signal " + req.getId() + " sent to House #" + customer.getModelID() + ".");
					}
					dlcTurn.add(customer);
				}
			}

			return responses;
		}

		public void assignExtraCosts(int interval, int day, int month, int year) {
			super.assignExtraCosts(interval, day, month, year);

			for (SimulationModel customer : customerContracts
					.get(CONTRACT_RTPDLCEV)) { 
				// DLC part - pay fixed compensation to customers of 100kr per
				// month
				// EV part - pay for DoD
				double toPay = 0;
				String comfort = customer.retrieveEVDiscomfort(day, month);
				if (comfort.equals("R"))
					toPay = 20;
				else if (comfort.equals("U"))
					toPay = 100;
				double[] toAdd = new double[24];
				toAdd[toAdd.length - 1] = -toPay;
				customer.getEnergyMonitor().addCostDataAtDay(day, toAdd);
				customer.getEnergyMonitor().writeCompentation(customer.getModelID(), -toPay, customer.getEventManager().getResultDirectory(), "DLCEVDoD");

				if(day==no.ntnu.item.smash.sim.data.Constants.getNumDaysInMonth(month,
						year)) {
					toPay = 100 + 50;
					double[] toAddDLC = new double[24];
					toAddDLC[toAdd.length - 1] = -toPay;
					customer.getEnergyMonitor().addCostDataAtDay(day, toAddDLC);
					customer.getEnergyMonitor().writeCompentation(customer.getModelID(), -toPay, customer.getEventManager().getResultDirectory(), "DLCEV");
				}
			}
		}
	}

	// RTP with CLC and EV scheduling
	public class RTPwithCLCEVContract extends RTPContract implements
			CLCContract, EVContract {
		public RTPwithCLCEVContract(int updateInterval) {
			super(updateInterval);
		}

		public HashMap<SimulationModel, Double> sendDRSignal(int startInterval, int endInterval,
				double totalAmount, ArrayList<SimulationModel> targetCustomers) {
			HashMap<SimulationModel, Double> responses = new HashMap<SimulationModel, Double>();
			CLCRequest req = new CLCRequest(startInterval % 288,
					endInterval % 288, totalAmount
							/ (targetCustomers == null ? customerContracts.get(
									CONTRACT_RTPCLCEV).size()
									: targetCustomers.size()));
			req.setId("CLC" + startInterval);

			for (SimulationModel customer : (targetCustomers == null ? customerContracts
					.get(CONTRACT_RTPCLCEV) : targetCustomers)) {
				double amount = customer.sendControlRequestToCSS(req);
				if(amount>0) responses.put(customer, amount);
			}

			return responses;
		}

		public void assignExtraCosts(int interval, int day, int month, int year) {
			super.assignExtraCosts(interval, day, month, year);

			// pay compensation to customers for DoD from CLC and EV
			for (SimulationModel customer : customerContracts
					.get(CONTRACT_RTPCLCEV)) {
				// read the DSOControl.log file to identify the affected devices
				// and time intervals
				double[] duration = customer.retrieveDiscomfortDuration(day,
						month);
				double toPay = duration[0] * 1 + duration[1] * 2;

				// EV part
				String comfort = customer.retrieveEVDiscomfort(day, month);
				if (comfort.equals("R"))
					toPay += 20;
				else if (comfort.equals("U"))
					toPay += 100;
				System.out.println("Compensate " + toPay + " on day " + day + " to Customer# " + customer.getModelID());
				double[] toAdd = new double[24];
				toAdd[toAdd.length - 1] = -toPay;
				customer.getEnergyMonitor().addCostDataAtDay(day, toAdd);
				customer.getEnergyMonitor().writeCompentation(customer.getModelID(), -toPay, customer.getEventManager().getResultDirectory(), "CLCEVDoD");
			}
			
			// pay fixed compensation to customers of 100kr per month (50+50)
			if(day!=no.ntnu.item.smash.sim.data.Constants.getNumDaysInMonth(month, year))
				return;

			for (SimulationModel customer : customerContracts
					.get(CONTRACT_RTPCLCEV)) {
				double toPay = 100;
				double[] toAdd = new double[24];
				toAdd[toAdd.length - 1] = -toPay;
				customer.getEnergyMonitor().addCostDataAtDay(day, toAdd);
				customer.getEnergyMonitor().writeCompentation(customer.getModelID(), -toPay, customer.getEventManager().getResultDirectory(), "CLCEV");
			}
		}
	}

	/*
	 * Definitions of pricing contracts
	 */
	public abstract class PricingContract implements ControlContract {
		protected double[][] ENERGY_EST_PRICES;
		protected double[][] ENERGY_ACT_PRICES;
		protected double[][] GRID_EST_PRICES;
		protected double[][] GRID_ACT_PRICES;
		protected boolean[][] GRID_PRICES_LOCK;

		protected int[][] dataPeriod = new int[4][2];

		protected int dloadDay = -1, boughtDay = -1;
		protected int dloadMonth = -1, boughtMonth = -1;
		protected int dloadYear = -1, boughtYear = -1;

		protected abstract void assignEnergyPrices(int interval, int day,
				int month, int year);

		protected abstract void assignGridPrices(int interval, int day,
				int month, int year);

		protected abstract void resolveEnergyPriceAtTime(int interval, int day,
				int month, int year);

		protected abstract void resolveGridPriceAtTime(int interval, int day,
				int month, int year);

		protected abstract void assignExtraCosts(int interval, int day,
				int month, int year);

		public abstract double[] getEstEnergyPricesForDay(int day, int month,
				int year);

		public abstract double[] getActEnergyPricesForDay(int day, int month,
				int year);

		public abstract double[] getActGridPricesForDay(int day, int month,
				int year);

		public abstract double getEstEnergyPriceAtTime(int time, int day,
				int month, int year);

		public abstract double getActEnergyPriceAtTime(int time, int day,
				int month, int year);

		public abstract double getActGridPriceAtTime(int time, int day,
				int month, int year);

		public abstract double getEstElectricityPriceAtTime(int time, int day,
				int month, int year);

		public abstract double getActElectricityPriceAtTime(int time, int day,
				int month, int year);

		public abstract int getPriceChangeInterval();
	}

	public abstract class RTPContract extends PricingContract {
		private static final double LOWEST_GRID_PRICE = 0.15;
		protected int updateInterval = 30;
		private double[] estDailyLoad;
		private double[] boughtDailyEnergy;
		private double baseLineGridPrice = 0.3;

		public RTPContract(int updateInterval) {
			this.updateInterval = updateInterval;
			estDailyLoad = new double[24 * 60 / updateInterval];
			boughtDailyEnergy = new double[24 * 60 / updateInterval];

			Arrays.fill(dataPeriod[0], -1);
			Arrays.fill(dataPeriod[1], -1);
			Arrays.fill(dataPeriod[2], -1);
			Arrays.fill(dataPeriod[3], -1);
		}

		/*
		 * In reality, assign one day at a time; here we just assign all days
		 * because we know in advance. Here, we also assign starting at 0:00
		 * instead of after 12:00 when the market clears since it doesn't make
		 * any difference. Prices here are consumer prices with 10% markup.
		 */
		public void assignEnergyPrices(int interval, int day, int month,
				int year) {
			if ((month != dataPeriod[0][0] || year != dataPeriod[0][1])
					&& (interval == 0 || interval % 288 == 0)) {
				ENERGY_EST_PRICES = PriceDataParser.readPriceForMonth(
						PriceDataParser.ESP, month, year, updateInterval);

				dataPeriod[0][0] = month;
				dataPeriod[0][1] = year;
			}
		}

		/*
		 * Grid prices are estimated one day ahead based on the forecasted
		 * temperatures. Again, we assign for all days because we know the
		 * temperatures in advance 
		 * 
		 * NOTE: I commented the adjustment part out because I forgot that
		 * the spot prices already took into account weather and then
		 * prices become too high with this algorithm
		 */
		public void assignGridPrices(int interval, int day, int month, int year) {
			if ((month != dataPeriod[1][0] || year != dataPeriod[1][1])
					&& (interval == 0 || interval % 288 == 0)) {
				GRID_EST_PRICES = new double[Constants.getNumDaysInMonth(month,
						year)][24 * 60 / updateInterval];

				// calculate a base line grid price so that it's around 30% of
				// the total price
				// energy price is roughly 40% / we know the energy prices
				double totalEnergy = 0;
				for (int d = 0; d < ENERGY_EST_PRICES.length; d++) {
					for (int h = 0; h < ENERGY_EST_PRICES[d].length; h++) { 
						totalEnergy += ENERGY_EST_PRICES[d][h];
					}
				}
				double baseLineGridPrice = (0.3 * (totalEnergy * 100 / 40))
						/ (ENERGY_EST_PRICES.length * ENERGY_EST_PRICES[0].length);

				for (int d = 1; d <= Constants.getNumDaysInMonth(month, year); d++) {
					double accumTemp = 0;
					// read temperatures
					for (int i = 0; i < 24; i++) {
						accumTemp += WeatherService.getActualTemp(i * 3600, d,
								month, year);
						GRID_EST_PRICES[d - 1][i * 2] = baseLineGridPrice;
						GRID_EST_PRICES[d - 1][i * 2 + 1] = baseLineGridPrice;
					}

					double avgTemp = accumTemp / 24;
					int noColdHours = 0;
					int noAboveAvg = 0;
					for (int i = 0; i < 24; i++) {
						double temp = WeatherService.getActualTemp(i * 3600, d,
								month, year);
						if (temp - avgTemp <= -2) {
							GRID_EST_PRICES[d - 1][i * 2] = baseLineGridPrice + 0.2;
							GRID_EST_PRICES[d - 1][i * 2 + 1] = baseLineGridPrice + 0.2;
							// System.out.println((i*2) + "/" + (d-1) +
							// " GRIDEST " + GRID_EST_PRICES[d-1][i*2]);
							// System.out.println((i*2+1) + "/" + (d-1) +
							// " GRIDEST " + GRID_EST_PRICES[d-1][i*2+1]);
							noColdHours++;
						} else if (temp > avgTemp) {
							noAboveAvg++;
						}
					}
					double increasedCost = noColdHours * 0.2;
					double toReducePerDay = increasedCost / noAboveAvg;
					for (int i = 0; i < 24; i++) {
						double temp = WeatherService.getActualTemp(i * 3600, d,
								month, year);
						if (temp > avgTemp) {
							GRID_EST_PRICES[d - 1][i * 2] = baseLineGridPrice
									- toReducePerDay > LOWEST_GRID_PRICE ? baseLineGridPrice
									- toReducePerDay
									: LOWEST_GRID_PRICE;
							GRID_EST_PRICES[d - 1][i * 2 + 1] = baseLineGridPrice
									- toReducePerDay > LOWEST_GRID_PRICE ? baseLineGridPrice
									- toReducePerDay
									: LOWEST_GRID_PRICE;
							// System.out.println((i*2) + "/" + (d-1) +
							// " GRIDEST " + GRID_EST_PRICES[d-1][i*2]);
							// System.out.println((i*2+1) + "/" + (d-1) +
							// " GRIDEST " + GRID_EST_PRICES[d-1][i*2+1]);
						}
					} 
				}

				dataPeriod[1][0] = month;
				dataPeriod[1][1] = year;
			}
		}

		/*
		 * Energy price is resolved at the end of each interval based on the
		 * mismatch between the bought volume and supplied volume
		 */
		public void resolveEnergyPriceAtTimeBackup(int interval, int day,
				int month, int year) {
			if (month != dataPeriod[2][0] || year != dataPeriod[2][1]) {
				ENERGY_ACT_PRICES = new double[no.ntnu.item.smash.sim.data.Constants
						.getNumDaysInMonth(month, year)][24 * 60 / updateInterval];
				dataPeriod[2][0] = month;
				dataPeriod[2][1] = year;
			}

			if (interval > 0 && interval % 6 == 0) {
				// retrieve the bought energy
				if (dloadDay != day || dloadMonth != month || dloadYear != year) {
					boughtDailyEnergy = Arrays.copyOf(LoadDataUtility
							.readDailyForecastEnergy(day, month, year,
									updateInterval), boughtDailyEnergy.length);
					dloadDay = day;
					dloadMonth = month;
					dloadYear = year;
				}
				double boughtEnergy = boughtDailyEnergy[((interval - 6) / 6)
						% boughtDailyEnergy.length];
				// retrieve the real energy consumption
				double consumedEnergy = 0;
				for (SimulationModel customer : customers) {
					consumedEnergy += customer.getEnergyConsumption(
							interval - 6, updateInterval);
				}

				// calculate mismatch and add that to the day-ahead price
				double mismatch = boughtEnergy - consumedEnergy;
				double adjustment = 0;

				if (mismatch < 0)
					adjustment += (-mismatch) / boughtEnergy;
				else if (mismatch > 0)
					adjustment -= mismatch / boughtEnergy;

				ENERGY_ACT_PRICES[day - 1][((interval - 6) / 6)
						% boughtDailyEnergy.length] = ENERGY_EST_PRICES[day - 1][((interval - 6) / 6)
						% boughtDailyEnergy.length]
						+ adjustment;
				// System.out.println((((interval-6)/6)%boughtDailyEnergy.length)
				// + " ENERGY " + boughtEnergy + "-" + consumedEnergy + "=" +
				// mismatch + " -> " + adjustment + " -> " +
				// (ENERGY_ACT_PRICES[day-1][((interval-6)/6)%boughtDailyEnergy.length]));
			}
		}

		/*
		 * Consumers pay spot prices plus 10% markup (markup included in the
		 * estimated prices) DSOs pay the intra-day and balancing costs (no
		 * statistics kept here)
		 */
		public void resolveEnergyPriceAtTime(int interval, int day, int month,
				int year) {
			if (month != dataPeriod[2][0] || year != dataPeriod[2][1]) {
				ENERGY_ACT_PRICES = new double[no.ntnu.item.smash.sim.data.Constants
						.getNumDaysInMonth(month, year)][24 * 60 / updateInterval];
				dataPeriod[2][0] = month;
				dataPeriod[2][1] = year;
			}

			if (interval > 0 && interval % 6 == 0) {
				ENERGY_ACT_PRICES[day - 1][((interval - 6) / 6)
						% boughtDailyEnergy.length] = ENERGY_EST_PRICES[day - 1][((interval - 6) / 6)
						% boughtDailyEnergy.length];
			}
		}

		/*
		 * Grid price is assigned at the next interval based on the mismatch
		 * between the forecast load and known scheduled load
		 */
		public void resolveGridPriceAtTimeBackup(int interval, int day,
				int month, int year) {

			if (month != dataPeriod[3][0] || year != dataPeriod[3][1]) {
				GRID_ACT_PRICES = new double[no.ntnu.item.smash.sim.data.Constants
						.getNumDaysInMonth(month, year)][24 * 60 / updateInterval];
				dataPeriod[3][0] = month;
				dataPeriod[3][1] = year;
			}

			if (interval == 0) {
				GRID_ACT_PRICES[day - 1][0] = 0.3;
				GRID_ACT_PRICES[day - 1][1] = 0.3;
			} else if ((interval + 6) % 288 == 0 || interval % 288 == 0)
				day = day + 1;

			if (interval > 0 && interval % 6 == 0) {
				// retrieve the forecast load
				if (boughtDay != day || boughtMonth != month
						|| boughtYear != year) {
					estDailyLoad = Arrays.copyOf(PeakloadDataUtility
							.readDailyForecastPeakLoad(day, month, year),
							estDailyLoad.length);
					boughtDay = day;
					boughtMonth = month;
					boughtYear = year;
				}
				double forecastLoad = estDailyLoad[((interval + 6) / 6)
						% estDailyLoad.length];
				double forecastLoad_curInterval = estDailyLoad[(interval / 6)
						% estDailyLoad.length];
				// retrieve the known scheduled load
				double expectedKnownLoad = 0, realLoad_curInterval = 0;
				for (SimulationModel customer : customers) {
					realLoad_curInterval += customer.getPeakload(interval * 6);
					expectedKnownLoad += customer.getExpectedPeakLoad(
							interval + 6, updateInterval);
				}
				// calculate mismatch and adjust grid prices from the base of
				// 0.3
				double mismatch = forecastLoad - expectedKnownLoad;
				double drLoad = forecastLoad_curInterval - realLoad_curInterval;
				double adjustment = 0;

				// adjustment up or down based on the difference between the
				// forecast load
				// and known scheduled load
				if (mismatch < -customers.size() * 3000)
					adjustment -= (mismatch + (customers.size() * 3000))
							/ forecastLoad;
				else if (mismatch > customers.size() * 3000)
					adjustment -= (mismatch - (customers.size() * 3000))
							/ forecastLoad;

				// in the case of upward adjustment, compensate at the next next
				// interval (from current)
				GRID_ACT_PRICES[day - 1][((interval + 12) / 6)
						% estDailyLoad.length] = 0.3 - adjustment;

				// // adjustment up or down based on the demand response
				// achieved in the current interval
				// if (drLoad > customers.size() * 3000)
				// adjustment -= (drLoad - (customers.size() * 3000)) /
				// forecastLoad_curInterval;
				// else if (drLoad < -customers.size() * 3000)
				// adjustment -= (drLoad - (customers.size() * 3000)) /
				// forecastLoad_curInterval;

				// System.out.println((((interval+6)/6)%estDailyLoad.length)
				// +"/"+day+ " GRID " + forecastLoad + "-" + expectedKnownLoad +
				// "=" + mismatch + " -> " + adjustment + " -> " + (0.3 +
				// adjustment));
				GRID_ACT_PRICES[day - 1][((interval + 6) / 6)
						% estDailyLoad.length] = 0.3 + adjustment;
			}
		}

		/*
		 * For when we don't want to adjust prices in real-time
		 */
		public void resolveGridPriceAtTime_fake(int interval, int day,
				int month, int year) {

			if (month != dataPeriod[3][0] || year != dataPeriod[3][1]) {
				GRID_ACT_PRICES = new double[no.ntnu.item.smash.sim.data.Constants
						.getNumDaysInMonth(month, year)][24 * 60 / updateInterval];
				dataPeriod[3][0] = month;
				dataPeriod[3][1] = year;
			}

			if (interval == 0) {
				GRID_ACT_PRICES[day - 1][0] = GRID_EST_PRICES[day - 1][0];
				GRID_ACT_PRICES[day - 1][1] = GRID_EST_PRICES[day - 1][1];
			} else if ((interval + 6) % 288 == 0 || interval % 288 == 0)
				day = day + 1;

			if (interval > 0 && interval % 6 == 0) {
				GRID_ACT_PRICES[day - 1][((interval + 6) / 6)
						% estDailyLoad.length] = GRID_EST_PRICES[day - 1][((interval + 6) / 6)
						% estDailyLoad.length];
				GRID_ACT_PRICES[day - 1][(((interval + 12) / 6) % estDailyLoad.length)] = GRID_EST_PRICES[day - 1][(((interval + 12) / 6) % estDailyLoad.length)];
			}
		}

		/*
		 * For this algorithm, once adjusted it won't be adjusted again because
		 * we can't keep adjusting things or customers won't get any savings
		 */
		public void resolveGridPriceAtTime(int interval, int day, int month,
				int year) {
			if (month != dataPeriod[3][0] || year != dataPeriod[3][1]) {
				GRID_ACT_PRICES = new double[no.ntnu.item.smash.sim.data.Constants
						.getNumDaysInMonth(month, year)][24 * 60 / updateInterval];
				GRID_PRICES_LOCK = new boolean[no.ntnu.item.smash.sim.data.Constants
						.getNumDaysInMonth(month, year)][24 * 60 / updateInterval];
				dataPeriod[3][0] = month;
				dataPeriod[3][1] = year;
			}

			if (interval == 0) {
				GRID_ACT_PRICES[day - 1][0] = GRID_EST_PRICES[day - 1][0];
			} else if (interval % 6 == 0) {
				if ((interval + 6) % 288 == 0 || interval % 288 == 0)
					day = day + 1;
				
				// we're actually adjusting the price of the next interval
				interval += 6;
				int intervalIn = (interval / 6) % 48;
				
				// assign to the estimated first then we can adjust as necessary (the + grid_act_prices is because it may have been adjusted some time ago)
				GRID_ACT_PRICES[day-1][intervalIn] = GRID_EST_PRICES[day-1][intervalIn] + GRID_ACT_PRICES[day-1][intervalIn];
			
				// if we've adjusted this interval, we will leave it alone
				if (GRID_PRICES_LOCK[day - 1][intervalIn]) {
					return;
				}

				// retrieve the forecast load
				if (boughtDay != day || boughtMonth != month
						|| boughtYear != year) {
					estDailyLoad = Arrays.copyOf(PeakloadDataUtility
							.readDailyForecastPeakLoad(day, month, year),
							estDailyLoad.length);
					boughtDay = day;
					boughtMonth = month;
					boughtYear = year;
				}
				double forecastLoad = estDailyLoad[intervalIn];
				// retrieve the known scheduled load
				double expectedKnownLoad = 0;
				for (SimulationModel customer : customers) {
					expectedKnownLoad += customer.getExpectedPeakLoad(
							interval, updateInterval);
				}
				// calculate mismatch and adjust grid prices
				double mismatch = forecastLoad - expectedKnownLoad;
				double adjustment = 0;

				// adjustment up based on the difference between the forecast
				// load
				// and known scheduled load
				if (Math.abs(mismatch) > customers.size() * 2000)
					adjustment += (((Math.abs(mismatch) - (customers.size() * 2000)) / forecastLoad) * (mismatch > 0 ? -1
							: 1)) * 0.5;
				
				if (adjustment < 0
						&& (GRID_EST_PRICES[day - 1][intervalIn]
								- adjustment < LOWEST_GRID_PRICE)) {
					adjustment = -(GRID_EST_PRICES[day - 1][intervalIn] - LOWEST_GRID_PRICE);
				}

				// compensate in another interval (check the next half day too)
				int compensateIn = -1; double p=mismatch<0?999:0;
				for (int i=72; i>intervalIn; i--) {
					int ii = i; int dd = day;
					if (i>47) {
						dd+=1;
						if(dd>Constants.getNumDaysInMonth(month, year)) break;
						ii-=48;
					}
					
					if(mismatch<0 && GRID_EST_PRICES[dd-1][ii]<p && !GRID_PRICES_LOCK[dd-1][ii]) {
						p = GRID_EST_PRICES[dd-1][ii];
						compensateIn = i;
					} else if(mismatch>0 && GRID_EST_PRICES[dd-1][ii]>p && !GRID_PRICES_LOCK[dd-1][ii]) {
						p = GRID_EST_PRICES[dd-1][ii];
						compensateIn = i;
					}
				}
				
				if(compensateIn!=-1) { // successful in finding 2 slots to adjust (one up, one down)
					//System.out.println(GRID_ACT_PRICES[day-1][intervalIn] + "->" + (GRID_ACT_PRICES[day-1][intervalIn]-adjustment));
					GRID_ACT_PRICES[day-1][intervalIn] -= adjustment;
					GRID_PRICES_LOCK[day-1][intervalIn] = true;
					if(compensateIn < 48) {
						//System.out.println(GRID_ACT_PRICES[day-1][compensateIn] + "->" + (GRID_ACT_PRICES[day-1][compensateIn]+adjustment));
						GRID_ACT_PRICES[day-1][compensateIn] += adjustment;
						GRID_PRICES_LOCK[day-1][compensateIn] = true;
					} else {
						//System.out.println(GRID_ACT_PRICES[day][compensateIn-48] + "->" + (GRID_ACT_PRICES[day][compensateIn-48]+adjustment));
						GRID_ACT_PRICES[day][compensateIn-48] += adjustment;
						GRID_PRICES_LOCK[day][compensateIn-48] = true;
					}
				}
			}
		}

		/*
		 * Grid price is assigned at the next interval based on the mismatch
		 * between the forecast load and known scheduled load
		 */
		public void resolveGridPriceAtTime2(int interval, int day, int month,
				int year) {

			if (month != dataPeriod[3][0] || year != dataPeriod[3][1]) {
				GRID_ACT_PRICES = new double[no.ntnu.item.smash.sim.data.Constants
						.getNumDaysInMonth(month, year)][24 * 60 / updateInterval];
				dataPeriod[3][0] = month;
				dataPeriod[3][1] = year;
			}

			if (interval == 0) {
				GRID_ACT_PRICES[day - 1][0] = GRID_EST_PRICES[day - 1][0];
				GRID_ACT_PRICES[day - 1][1] = GRID_EST_PRICES[day - 1][1];
			} else if ((interval + 6) % 288 == 0 || interval % 288 == 0)
				day = day + 1;

			if (interval > 0 && interval % 6 == 0) {
				// retrieve the forecast load
				if (boughtDay != day || boughtMonth != month
						|| boughtYear != year) {
					estDailyLoad = Arrays.copyOf(PeakloadDataUtility
							.readDailyForecastPeakLoad(day, month, year),
							estDailyLoad.length);
					boughtDay = day;
					boughtMonth = month;
					boughtYear = year;
				}
				double forecastLoad = estDailyLoad[((interval + 6) / 6)
						% estDailyLoad.length];
				// retrieve the known scheduled load
				double expectedKnownLoad = 0;
				for (SimulationModel customer : customers) {
					expectedKnownLoad += customer.getExpectedPeakLoad(
							interval + 6, updateInterval);
				}
				// calculate mismatch and adjust grid prices
				double mismatch = forecastLoad - expectedKnownLoad;
				double adjustment = 0;

				// adjustment up based on the difference between the forecast
				// load
				// and known scheduled load
				if (Math.abs(mismatch) > customers.size() * 1000)
					adjustment += (((Math.abs(mismatch) - (customers.size() * 1000)) / forecastLoad) * (mismatch > 0 ? -1
							: 1));

				if ((((interval + 6) / 6) % estDailyLoad.length) + 1 < estDailyLoad.length - 1) {
					if (adjustment < 0
							&& (GRID_EST_PRICES[day - 1][((interval + 6) / 6)
									% estDailyLoad.length]
									- (0.5 * adjustment) < LOWEST_GRID_PRICE || GRID_EST_PRICES[day - 1][(((interval + 6) / 6) % estDailyLoad.length) + 1]
									- (0.5 * adjustment) < LOWEST_GRID_PRICE)) {
						adjustment = -(Math
								.min(GRID_EST_PRICES[day - 1][((interval + 6) / 6)
										% estDailyLoad.length],
										GRID_EST_PRICES[day - 1][(((interval + 6) / 6) % estDailyLoad.length) + 1]) - LOWEST_GRID_PRICE);
					}
					GRID_ACT_PRICES[day - 1][((interval + 6) / 6)
							% estDailyLoad.length] = GRID_EST_PRICES[day - 1][((interval + 6) / 6)
							% estDailyLoad.length]
							+ (0.5 * adjustment);
					GRID_ACT_PRICES[day - 1][(((interval + 6) / 6) % estDailyLoad.length) + 1] = GRID_EST_PRICES[day - 1][(((interval + 6) / 6) % estDailyLoad.length) + 1]
							+ (0.5 * adjustment);

					// compensate in other intervals
					int compensateIn = 0;
					double compareWith = 99999999;
					for (int i = (((interval + 6) / 6) + 2)
							% estDailyLoad.length; i < 24 * 60 / updateInterval; i++) {
						if (mismatch < 0
								&& estDailyLoad[i] < compareWith
								&& GRID_EST_PRICES[day - 1][i]
										- (0.5 * adjustment) > LOWEST_GRID_PRICE
								&& (i + 1 >= estDailyLoad.length ? GRID_EST_PRICES[day][0]
										: GRID_EST_PRICES[day - 1][i + 1])
										- (0.5 * adjustment) > LOWEST_GRID_PRICE) {
							compareWith = estDailyLoad[i];
							compensateIn = i;
						} else if (mismatch > 0
								&& estDailyLoad[i] > compareWith) {
							compareWith = estDailyLoad[i];
							compensateIn = i;
						}
					}

					GRID_ACT_PRICES[day - 1][compensateIn] = GRID_EST_PRICES[day - 1][compensateIn]
							- (0.5 * adjustment);
					if (compensateIn + 1 < estDailyLoad.length)
						GRID_ACT_PRICES[day - 1][compensateIn + 1] = GRID_EST_PRICES[day - 1][compensateIn + 1]
								- (0.5 * adjustment);
					else
						GRID_ACT_PRICES[day][0] = GRID_EST_PRICES[day][0]
								- (0.5 * adjustment);
				} else {
					GRID_ACT_PRICES[day - 1][((interval + 6) / 6)
							% estDailyLoad.length] = GRID_EST_PRICES[day - 1][((interval + 6) / 6)
							% estDailyLoad.length];
				}

				// System.out.println((((interval+6)/6)%estDailyLoad.length)
				// +"/"+day+ " GRID " + forecastLoad + "-" + expectedKnownLoad +
				// "=" + mismatch + " -> " + adjustment + " -> " +
				// GRID_ACT_PRICES[day - 1][((interval + 6) / 6)
				// % estDailyLoad.length]);
			}
		}

		public void assignExtraCosts(int interval, int day, int month, int year) {

		}

		public double[] getEstEnergyPricesForDay(int day, int month, int year) {
			double[] prices = new double[24 * 60 / updateInterval];

			for (int i = 0; i < prices.length; i++) {
				prices[i] = ENERGY_EST_PRICES[day - 1][i];
			}

			return prices;
		}

		public double getEstEnergyPriceAtTime(int time, int day, int month,
				int year) { // time in seconds
			int interval = (time / (60 * updateInterval))
					% (24 * 60 / updateInterval);

			return getEstEnergyPricesForDay(day, month, year)[interval];
		}

		public double[] getEstGridPricesForDay(int day, int month, int year) {
			double[] prices = new double[24 * 60 / updateInterval];

			for (int i = 0; i < prices.length; i++) {
				prices[i] = GRID_EST_PRICES[day - 1][i];
			}

			return prices;
		}

		public double getEstGridPriceAtTime(int time, int day, int month,
				int year) { // time in seconds
			int interval = (time / (60 * updateInterval))
					% (24 * 60 / updateInterval);

			return getEstGridPricesForDay(day, month, year)[interval];
		}

		public double[] getActEnergyPricesForDay(int day, int month, int year) {
			double[] prices = new double[24 * 60 / updateInterval];

			for (int i = 0; i < prices.length; i++) {
				prices[i] = ENERGY_ACT_PRICES[day - 1][i];
			}

			return prices;
		}

		public double getActEnergyPriceAtTime(int time, int day, int month,
				int year) { // time in seconds
			int interval = (time / (60 * updateInterval))
					% (24 * 60 / updateInterval);

			return getActEnergyPricesForDay(day, month, year)[interval];
		}

		public double[] getActGridPricesForDay(int day, int month, int year) {
			double[] prices = new double[24 * 60 / updateInterval];

			for (int i = 0; i < prices.length; i++) {
				prices[i] = GRID_ACT_PRICES[day - 1][i];
			}

			return prices;
		}

		public double getActGridPriceAtTime(int time, int day, int month,
				int year) { // time in seconds
			int interval = (time / (60 * updateInterval))
					% (24 * 60 / updateInterval);

			return getActGridPricesForDay(day, month, year)[interval];
		}

		public double getEstElectricityPriceAtTime(int time, int day,
				int month, int year) {
			if (month == dataPeriod[3][0] || year == dataPeriod[3][1]) {
				return getEstEnergyPriceAtTime(time, day, month, year)
						+ getEstGridPriceAtTime(time, day, month, year);
			} else {
				assignEnergyPrices(0, day, month, year);
				assignGridPrices(0, day, month, year);
				return getEstEnergyPriceAtTime(time, day, month, year)
						+ getEstGridPriceAtTime(time, day, month, year);
			}

		}

		public double getActElectricityPriceAtTime(int time, int day,
				int month, int year) {
			// System.out.println(((time/(60*updateInterval))%(24*60/updateInterval))
			// + "/"+day+ " Energy=" + getActEnergyPriceAtTime(time, day, month,
			// year) + " Grid=" + getActGridPriceAtTime(time, day, month,
			// year));
			return getActEnergyPriceAtTime(time, day, month, year)
					+ getActGridPriceAtTime(time, day, month, year);
		}

		public int getPriceChangeInterval() {
			return updateInterval;
		}
	}

	public abstract class CPPContract extends RTPContract {
		public CPPContract(int updateInterval) {
			super(updateInterval);
		}

		public void assignExtraCosts(int interval, int day, int month, int year) {
			if (day == no.ntnu.item.smash.sim.data.Constants
			 .getNumDaysInMonth(month, year))
				assignMonthlyPeakPricesToCustomers(day, month, year);
		}

		/*
		 * This method must be called when a month is over. It adds extra costs
		 * to customers based on their peak load after 10kW. 1kW costs 50kr extra
		 */
		private void assignMonthlyPeakPricesToCustomers(int day, int month,
				int year) {
			for (SimulationModel customer : customers) {
				if (customer.getContract() == CONTRACT_RTPCPP
						|| customer.getContract() == CONTRACT_RTPCPPEV) {
					double peak = 0;
					for (double[] d : customer.getEnergyMonitor()
							.getMonthlyPeakLoad()) {
						for (int i = 0; i < d.length; i++) {
							if (peak < d[i])
								peak = d[i];
						}
					}
					if (customer.getEnergyMonitor().getMonthlyPeakLoad().size() < no.ntnu.item.smash.sim.data.Constants
							.getNumDaysInMonth(month, year)) {
						for (int i = 0; i < 24 * 3600 / customer
								.getSyncInterval(); i++) {
							if (peak < customer.getPeakload(i))
								peak = customer.getPeakload(i);
						}
					}

					double extraCost = Math.max(0, ((peak * 0.001) - 10)) * 50;
					double[] toAdd = new double[24];
					toAdd[toAdd.length - 1] = extraCost;
					customer.getEnergyMonitor().addCostDataAtDay(day, toAdd);
					customer.getEnergyMonitor().writeCompentation(customer.getModelID(), extraCost, customer.getEventManager().getResultDirectory(), "CPP");
					System.out.println("CPP [Model ID#" + customer.getModelID() + "] peak=" + peak + " cost=" + extraCost);
				}
			}
		}
	}

	/*
	 * Definitions of control contracts
	 */

	public interface ControlContract {
		/*
		 * return the amount that will actually be reduced as a result of the
		 * control
		 */
		public HashMap<SimulationModel, Double> sendDRSignal(int startInterval, int endInterval,
				double totalAmount, ArrayList<SimulationModel> targetCustomers);
	}

	public interface DLCContract extends ControlContract {
		public int controlledDeviceType = Device.DEVTYPE_EWATERHEATER;
	}

	public interface CLCContract extends ControlContract {

	}

	public interface EVContract extends ControlContract {

	}
}
