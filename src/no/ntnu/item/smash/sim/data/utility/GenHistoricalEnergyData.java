package no.ntnu.item.smash.sim.data.utility;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import no.ntnu.item.smash.sim.core.EventManager;
import no.ntnu.item.smash.sim.core.SimEnvironment;
import no.ntnu.item.smash.sim.core.SimulationModel;
import no.ntnu.item.smash.sim.data.stats.HouseholdStatistics;
import no.ntnu.item.smash.sim.externalsp.CEMS;
import no.ntnu.item.smash.sim.externalsp.DSO;
import no.ntnu.item.smash.sim.model.HeaterModel;
import no.ntnu.item.smash.sim.pss.loadflow.LVNetwork;
import no.ntnu.item.smash.sim.structure.BuildingThermalIntegrity;
import no.ntnu.item.smash.sim.structure.Device;
import no.ntnu.item.smash.sim.structure.Heater;
import no.ntnu.item.smash.sim.structure.Plan;
import no.ntnu.item.smash.sim.structure.Room;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class GenHistoricalEnergyData {

	// simulation
	private int simulationTime;
	private int startDay;
	private int startMonth;
	private int startYear;

	private EventManager eventMan;
	private ExecutorService executorService;

	// environment
	private ArrayList<SimulationModel> houseModels;

	public static void main(String[] args) {
		GenHistoricalEnergyData sim = new GenHistoricalEnergyData(
				12 * 24 * 60 * 60, 1, 1, 2006, 1, 1, 5 * 60, "histEnergy",
				false, false, null);
		sim.runHouse();
	}

	public GenHistoricalEnergyData(int simTime, int startDay, int startMonth,
			int startYear, int numHouse, int concurrentHouses,
			int syncInterval, String resultDir, boolean useCEMS,
			boolean useCSS, LVNetwork network) {
		this.simulationTime = simTime;
		this.startDay = startDay;
		this.startMonth = startMonth;
		this.startYear = startYear;

		executorService = Executors.newFixedThreadPool(20);

		HouseholdStatistics.emptyDirectory(resultDir);

		houseModels = new ArrayList<SimulationModel>();

		setupEnvironment(null, useCSS, syncInterval, resultDir, 4, 25);

		DSO dso = new DSO(houseModels);
		eventMan = new EventManager(houseModels, simTime / syncInterval, concurrentHouses, network, dso,
				resultDir, null);
	}

	public void setupEnvironment(CEMS cems, boolean useCSS, int syncInterval,
			String resultDir, int houseTemplateID, double setpoint) {
		SimulationModel houseModel = new SimulationModel(specifyEnvironment(houseTemplateID, setpoint),
				simulationTime, syncInterval, startDay, startMonth, startYear,
				resultDir + "/H1");
		houseModel.setModelID(0);
		houseModel.setCEMS(cems);
		houseModel.setCSSSuspended(true);
		houseModel.setExecutorService(executorService);
		houseModel.setContract(DSO.CONTRACT_RTP);

		houseModels.add(houseModel);
	}

	public synchronized void runHouse() {
		eventMan.start();
	}

	public SimEnvironment specifyEnvironment(int houseTemplateID,
			double planSetpoint) {
		Document spec = EntitySpecUtility.readSpecFromPattern(houseTemplateID, "lastpaper");

		SimEnvironment env = new SimEnvironment();
		ArrayList<Device> devices = new ArrayList<Device>();

		spec.getDocumentElement().normalize();
		NodeList entities = spec.getElementsByTagName("smash:Entity");

		// Configure house thermal property
		double[] thermalProp = BuildingThermalIntegrity.NORWAY_HEAVYINSULATED;
		env.setThermalProperty(thermalProp);

		int devId = 1;

		for (int i = 0; i < entities.getLength(); i++) {
			Element entity = (Element) entities.item(i);
			String entityType = ((Element) (entity
					.getElementsByTagName("smash:entityType").item(0)))
					.getTextContent();

			if (entityType.equals("Room")) {
				double length = Double.parseDouble(((Element) (entity
						.getElementsByTagName("smash:PhyscialSpec").item(0)))
						.getElementsByTagName("smash:length").item(0)
						.getTextContent());
				double width = Double.parseDouble(((Element) (entity
						.getElementsByTagName("smash:PhyscialSpec").item(0)))
						.getElementsByTagName("smash:width").item(0)
						.getTextContent());
				double height = Double.parseDouble(((Element) (entity
						.getElementsByTagName("smash:PhyscialSpec").item(0)))
						.getElementsByTagName("smash:height").item(0)
						.getTextContent());
				double doorArea = Double.parseDouble(((Element) (entity
						.getElementsByTagName("smash:PhyscialSpec").item(0)))
						.getElementsByTagName("smash:doorArea").item(0)
						.getTextContent());
				int numWin = Integer.parseInt(((Element) (entity
						.getElementsByTagName("smash:PhyscialSpec").item(0)))
						.getElementsByTagName("smash:numWindow").item(0)
						.getTextContent());
				double winHeight = Double.parseDouble(((Element) (entity
						.getElementsByTagName("smash:PhyscialSpec").item(0)))
						.getElementsByTagName("smash:winHeight").item(0)
						.getTextContent());
				double winWidth = Double.parseDouble(((Element) (entity
						.getElementsByTagName("smash:PhyscialSpec").item(0)))
						.getElementsByTagName("smash:winWidth").item(0)
						.getTextContent());
				double heaterWatt = Double.parseDouble(((Element) (entity
						.getElementsByTagName("smash:DeviceSpec").item(0)))
						.getElementsByTagName("smash:power").item(0)
						.getTextContent());
				String entityName = entity.getAttribute("rdf:about");

				// always have a heater
				Device dev = new Heater();
				dev.setId(devId);
				dev.setName(entityName);
				dev.setWatt(heaterWatt);
				dev.setType(Device.DEVTYPE_HEATER);
				devices.add(dev);

				ArrayList<Device> roomDevices = new ArrayList<Device>();
				roomDevices.add(dev);

				// instantiate
				Room room = new Room(entityName, null, null, height, width,
						length, numWin, winWidth, winHeight, doorArea, 0,
						roomDevices, new Plan(
								genPlanFromSingleSetpoint(planSetpoint)));

				for (Device device : roomDevices) {
					device.setRoom(room);
				}

				env.addRoom(room);
				env.addDeviceModel(dev, new HeaterModel(dev, room, thermalProp));
			}
		}

		return env;
	}

	private double[] genPlanFromSingleSetpoint(double setpoint) {
		double[] plan = new double[24];
		for (int i = 0; i < plan.length; i++) {
			plan[i] = setpoint;
		}
		return plan;
	}

}
