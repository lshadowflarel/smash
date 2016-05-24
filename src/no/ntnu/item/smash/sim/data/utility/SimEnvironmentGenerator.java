package no.ntnu.item.smash.sim.data.utility;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;

import no.ntnu.item.smash.sim.core.SimEnvironment;
import no.ntnu.item.smash.sim.model.BulkLoadModel;
import no.ntnu.item.smash.sim.model.DeviceModel;
import no.ntnu.item.smash.sim.model.EVChargerModel;
import no.ntnu.item.smash.sim.model.ElectricWaterHeaterModel;
import no.ntnu.item.smash.sim.model.HeaterModel;
import no.ntnu.item.smash.sim.model.WasherDryerModel;
import no.ntnu.item.smash.sim.structure.BuildingThermalIntegrity;
import no.ntnu.item.smash.sim.structure.BulkLoad;
import no.ntnu.item.smash.sim.structure.ComfortMeasure;
import no.ntnu.item.smash.sim.structure.ComfortPreference;
import no.ntnu.item.smash.sim.structure.Device;
import no.ntnu.item.smash.sim.structure.EVLiionBattery;
import no.ntnu.item.smash.sim.structure.ElectricWaterHeater;
import no.ntnu.item.smash.sim.structure.Heater;
import no.ntnu.item.smash.sim.structure.Plan;
import no.ntnu.item.smash.sim.structure.Room;
import no.ntnu.item.smash.sim.structure.WashingMachineDryer;
import no.ntnu.item.smash.sim.structure.logic.LogicExpression;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class SimEnvironmentGenerator {

	public static String EXECUTION_PATH = "";
	private static Random generator = new Random(78);
	private static String[] houseInsulation;

	public static SimEnvironment generate(int houseID, int simTime,
			int startDay, int startMonth, int startYear, int patternID,
			String folder, String policyPath) {
		return SimEnvironmentGenerator.generate(houseID, simTime, startDay, startMonth, startYear, patternID, folder, policyPath, null);
	}
	
	@SuppressWarnings("unchecked")
	public static SimEnvironment generate(int houseID, int simTime,
			int startDay, int startMonth, int startYear, int patternID,
			String folder, String policyPath,
			HashMap<SimConfigUtility.UseDefault, Boolean> useDefault) {
		SimEnvironment env = new SimEnvironment();

		// Assign house insulation (either randomize or read from input file)
		String thermalPropCode = "NORWAY_MEDINSULATED";
		if (useDefault.get(SimConfigUtility.UseDefault.ENUM_INSULATION)) {
			thermalPropCode = generator.nextDouble() > 0.25 ? "NORWAY_MEDINSULATED" : "NORWAY_HEAVYINSULATED";

		} else if (houseInsulation == null) {
			String readInsulation = "";
			BufferedReader br = null;
			try {
				String sCurrentLine;
				br = new BufferedReader(new FileReader(
						"./data/houses/insulation.txt"));
				while ((sCurrentLine = br.readLine()) != null) {
					readInsulation += sCurrentLine + " ";
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
			houseInsulation = readInsulation.trim().split(" ");
			thermalPropCode = houseInsulation[houseID];
		} else {
			thermalPropCode = houseInsulation[houseID];
		}
		double[] thermalProp = BuildingThermalIntegrity.getThermalPropSetFromCode(thermalPropCode);
		env.setThermalProperty(thermalProp);
	
		// Copy relevant pattern-specific files when default values are used
		new File(policyPath + "/policies" + (houseID + 1)).mkdirs();
		if (useDefault==null || useDefault.get(SimConfigUtility.UseDefault.ENUM_ENTITIES)) {
			copyPatternFiles("./data/houses/housePatterns/" + folder + "/P"
					+ patternID + ".xml", policyPath + "/policies"
					+ (houseID + 1) + "/entity-spec.xet.xml");
		}
		if (useDefault==null || useDefault.get(SimConfigUtility.UseDefault.ENUM_ROOMTEMP)) {
			copyPatternFiles("./data/houses/housePatterns/" + folder + "/ROOM"
					+ patternID + ".txt", "./data/plan/room" + houseID + ".txt");
		}
		if (useDefault==null || useDefault.get(SimConfigUtility.UseDefault.ENUM_LAUNDRY)) {
			copyPatternFiles("./data/houses/housePatterns/" + folder + "/WM"
					+ patternID + ".txt", "./data/plan/wm" + houseID + ".txt");
		}
		if (useDefault==null || useDefault.get(SimConfigUtility.UseDefault.ENUM_EV)) {
			copyPatternFiles("./data/houses/housePatterns/" + folder + "/EV"
					+ patternID + ".txt", "./data/plan/ev-charge-" + houseID
					+ ".txt");
		}
		if (useDefault==null || useDefault.get(SimConfigUtility.UseDefault.ENUM_WATERDEMAND)) {
			copyPatternFiles("./data/houses/housePatterns/" + folder + "/WD"
					+ patternID + ".txt", "./data/plan/wd" + houseID + ".txt");
		}

		// Copy load data and historic energy consumption data files of the
		// associated house pattern
		copyPatternFiles("./data/load/temp/" + patternID + "/L"
				+ (startMonth < 10 ? "0" + startMonth : startMonth) + startYear
				+ ".txt", "./data/load/H" + (houseID + 1) + "/L"
				+ (startMonth < 10 ? "0" + startMonth : startMonth) + startYear
				+ ".txt");
		copyPatternFiles("./data/houses/histEnergy/P" + patternID + "/HE-"
				+ thermalPropCode + ".txt", "./data/histEnergy/H" + (houseID + 1)
				+ ".txt");

		// Write the resulting house thermal prop code so we later know if the
		// house has heavy or medium insulation in case it's random
		File file = new File("./data/histEnergy/H" + (houseID + 1) + ".cfg");
		FileWriter fw = null;
		try {
			fw = new FileWriter(file, false);
		} catch (IOException e) {
			e.printStackTrace();
		}
		PrintWriter pw = new PrintWriter(fw);
		pw.print("ThermalLevel=" + thermalPropCode);
		pw.close();

		// read XML file for the corresponding house spec and instantiate rooms
		// and devices
		Document spec = EntitySpecUtility.readSpecFromFile(policyPath
				+ "/policies" + (houseID + 1) + "/entity-spec.xet.xml");
		int householdSize = getHouseholdSize(patternID);
		if(spec.getElementsByTagName("smash:HouseholdSize").getLength()>0)
			householdSize = Integer.parseInt(spec.getElementsByTagName("smash:HouseholdSize").item(0).getTextContent());
		env.setHouseholdSize(householdSize);
		
		ArrayList<Object> roomDevices = generateRoomsDevices(spec, houseID);
		ArrayList<Room> rooms = (ArrayList<Room>) roomDevices.get(0);
		HashMap<Device, ArrayList<ComfortPreference>> devicePrefs = (HashMap<Device, ArrayList<ComfortPreference>>) roomDevices
				.get(1);
		for (Room r : rooms) {
			env.addRoom(r);
		}

		HashMap<Device, DeviceModel> map = generateDeviceModels(houseID,
				simTime, startDay, startMonth, startYear, devicePrefs,
				thermalProp);
		Set<Device> keys = map.keySet();
		for (Device k : keys) {
			env.addDeviceModel(k, map.get(k));
		}

		for (Device dev : devicePrefs.keySet()) {
			ArrayList<ComfortPreference> prefs = devicePrefs.get(dev);
			for (ComfortPreference pref : prefs) {
				env.addComfortMeasure(pref.getComfortMeasure());
			}
			if (dev.getType() == Device.DEVTYPE_EWATERHEATER)
				env.setDLCDevice(dev);
		}

		return env;
	}

	public static ArrayList<Object> generateRoomsDevices(Document spec,
			int houseID) {
		ArrayList<Room> rooms = new ArrayList<Room>();
		HashMap<Device, ArrayList<ComfortPreference>> devicePrefs = new HashMap<Device, ArrayList<ComfortPreference>>();
		HashMap<String, double[]> plans = ConsumerPlanUtility
				.readRoomTempPlans("./data/plan/room" + houseID + ".txt");

		spec.getDocumentElement().normalize();
		NodeList entities = spec.getElementsByTagName("smash:Entity");

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

				ArrayList<ComfortPreference> prefs = parseComfortPreference(entity);

				// devices
				ArrayList<Device> roomDevices = new ArrayList<Device>();

				// always have a heater
				Device roomHeater = createSpecializedDevice(
						Device.DEVTYPE_HEATER, Device.DEVENERGYTYPE_CONSTANT,
						devId++, entityName, heaterWatt, null);
				roomDevices.add(roomHeater);
				devicePrefs.put(roomHeater, prefs);

				// other devices
				if (entity.getElementsByTagName("smash:Devices").getLength() > 0) {
					NodeList deviceList = ((Element) entity
							.getElementsByTagName("smash:Devices").item(0))
							.getElementsByTagName("smash:Device");
					for (int j = 0; j < deviceList.getLength(); j++) {
						Element deviceNode = (Element) deviceList.item(j);
						String deviceName = deviceNode
								.getAttribute("rdf:about");
						double deviceWatt = Double
								.parseDouble(((Element) (deviceNode
										.getElementsByTagName("smash:power")
										.item(0))).getTextContent());
						int deviceId = devId++;
						int deviceType = assignDeviceType(((Element) (deviceNode
								.getElementsByTagName("smash:entityType")
								.item(0))).getTextContent());
						int energyType = Integer
								.parseInt(((Element) (deviceNode
										.getElementsByTagName("smash:energyType")
										.item(0))).getTextContent());

						ArrayList<ComfortPreference> prefs2 = parseComfortPreference(entity);

						Device roomDevice = createSpecializedDevice(deviceType,
								energyType, deviceId, deviceName, deviceWatt,
								deviceNode);
						roomDevices.add(roomDevice);

						devicePrefs.put(roomDevice, prefs2);
					}
				}

				// instantiate
				double[] p = new double[24];
				System.arraycopy(plans.get(entityName), 0, p, 0, p.length);
				Room room = new Room(entityName, null, null, height, width,
						length, numWin, winWidth, winHeight, doorArea, 0,
						roomDevices, new Plan(p));

				// get adjacent rooms
				NodeList adjRoomNodes = ((Element) (entity
						.getElementsByTagName("smash:AdjacentRooms").item(0)))
						.getElementsByTagName("smash:room");
				for (int j = 0; j < adjRoomNodes.getLength(); j++) {
					Element adjRoomNode = (Element) adjRoomNodes.item(j);
					String roomName = adjRoomNode.getTextContent();
					room.addAdjacentRoom(roomName);
				}

				for (Device device : roomDevices) {
					device.setRoom(room);
				}

				rooms.add(room);
			} else {
				// it's a device without a room then
				Device independentDevice = createSpecializedDevice(
						assignDeviceType(entityType),
						Integer.parseInt(((Element) entity
								.getElementsByTagName("smash:energyType").item(
										0)).getTextContent()), devId++,
						entity.getAttribute("rdf:about"),
						Double.parseDouble(((Element) entity
								.getElementsByTagName("smash:power").item(0))
								.getTextContent()), entity);

				ArrayList<ComfortPreference> prefs = parseComfortPreference(entity);

				devicePrefs.put(independentDevice, prefs);
			}
		}

		ArrayList<Object> toReturn = new ArrayList<Object>();
		toReturn.add(rooms);
		toReturn.add(devicePrefs);

		return toReturn;
	}

	public static HashMap<Device, DeviceModel> generateDeviceModels(
			int houseID, int simTime, int startDay, int startMonth,
			int startYear,
			HashMap<Device, ArrayList<ComfortPreference>> devices,
			double[] thermalProp) {
		HashMap<Device, DeviceModel> map = new HashMap<Device, DeviceModel>();

		for (Device device : devices.keySet()) {
			int deviceType = device.getType();
			DeviceModel model = null;

			switch (deviceType) {
			case Device.DEVTYPE_HEATER:
				model = new HeaterModel(device, device.getRoom(), thermalProp);
				break;
			case Device.DEVTYPE_EWATERHEATER:
				double[] whSchedule = { 70, 70, 70, 70, 70, 70, 70, 70, 70, 70,
						70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70 };
				model = new ElectricWaterHeaterModel(device, device.getRoom(),
						new Plan(whSchedule), 70, "wd" + houseID + ".txt",
						(ElectricWaterHeater) device);
				break;
			case Device.DEVTYPE_WASHINGMACHINEDRYER:
				model = new WasherDryerModel(device, device.getRoom(), "wm"
						+ houseID + ".txt");
				break;
			case Device.DEVTYPE_EVCHARGER:
				// EVUsagePlanUtility.writeEVDrivePattern(startMonth,startYear,"ev-charge-"
				// + houseID + ".txt", (EVLiionBattery) device);
				model = new EVChargerModel(device, null, "ev-charge-" + houseID
						+ ".txt");
				break;
			case Device.DEVTYPE_BULK:
				model = new BulkLoadModel(device, null);
				break;
			}

			ArrayList<ComfortPreference> comfortPrefs = devices.get(device);
			for (ComfortPreference pref : comfortPrefs) {
				model.addComfortPreference(pref.getComfortMeasure(), pref);
			}
			map.put(device, model);
		}

		return map;
	}

	private static int assignDeviceType(String type) {
		int devType = -1;

		if (type.equals("SpaceHeater")) {
			return Device.DEVTYPE_HEATER;
		} else if (type.equals("ElectricWaterHeater")) {
			return Device.DEVTYPE_EWATERHEATER;
		} else if (type.equals("WashingMachineDryer")) {
			return Device.DEVTYPE_WASHINGMACHINEDRYER;
		} else if (type.equals("BatteryEV")) {
			return Device.DEVTYPE_EVCHARGER;
		} else if (type.equals("BulkLoad")) {
			return Device.DEVTYPE_BULK;
		}

		return devType;
	}

	// TODO
	private static Device createSpecializedDevice(int devType, int energyType,
			int devId, String devName, double devPower, Element devNode) {
		Device dev = null;

		switch (devType) {
		case Device.DEVTYPE_HEATER:
			dev = new Heater();
			break;
		case Device.DEVTYPE_EWATERHEATER:
			dev = new ElectricWaterHeater();
			// requires height, diameter, volume, r-val
			((ElectricWaterHeater) dev).setTankHeight(Double
					.parseDouble(((Element) devNode.getElementsByTagName(
							"smash:tankHeight").item(0)).getTextContent()));
			((ElectricWaterHeater) dev).setTankDiameter(Double
					.parseDouble(((Element) devNode.getElementsByTagName(
							"smash:tankDiameter").item(0)).getTextContent()));
			((ElectricWaterHeater) dev).setTankVolume(Double
					.parseDouble(((Element) devNode.getElementsByTagName(
							"smash:tankVolume").item(0)).getTextContent()));
			((ElectricWaterHeater) dev).setrValue(Double
					.parseDouble(((Element) devNode.getElementsByTagName(
							"smash:rVal").item(0)).getTextContent()));
			break;
		case Device.DEVTYPE_WASHINGMACHINEDRYER:
			dev = new WashingMachineDryer();
			// requires many things!!!!!
			break;
		case Device.DEVTYPE_EVCHARGER:
			dev = new EVLiionBattery();
			// requires kwh, max range, lv2 charging or not
			((EVLiionBattery) dev).setMaxRange(Double
					.parseDouble(((Element) devNode.getElementsByTagName(
							"smash:maxRange").item(0)).getTextContent()));
			((EVLiionBattery) dev).setkWh(Double.parseDouble(((Element) devNode
					.getElementsByTagName("smash:batterySize").item(0))
					.getTextContent()));
			((EVLiionBattery) dev).setCanChargeLV2(false);
			break;
		case Device.DEVTYPE_BULK:
			dev = new BulkLoad();
			break;
		}

		dev.setId(devId);
		dev.setName(devName);
		dev.setWatt(devPower);
		dev.setType(devType);
		dev.setEnergyConsumptionType(energyType);

		return dev;
	}

	public static void copyPatternFiles(String originalFile, String copyTo) {
		InputStream inStream;
		OutputStream outStream;
		File origin = new File(originalFile);
		File copy = new File(copyTo);
		int length = 0;

		if (origin.exists()) {
			try {
				inStream = new FileInputStream(origin);
				outStream = new FileOutputStream(copy);

				byte[] buffer = new byte[1024];
				while ((length = inStream.read(buffer)) > 0) {
					outStream.write(buffer, 0, length);
				}

				inStream.close();
				outStream.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static int[] generateHouseTypeAssignments(double[] prob, int numHouse) {
		Integer[] assignment = new Integer[numHouse];
		ArrayList<Integer> roundedDown = new ArrayList<Integer>();

		int aIndex = 0;
		int assigned = 0;
		for (int i = 0; i < prob.length; i++) {
			int numHouseOfPattern = (int) Math.rint(prob[i] * numHouse);
			if (numHouseOfPattern < prob[i] * numHouse)
				roundedDown.add(i);
			for (int j = 0; j < numHouseOfPattern; j++) {
				if (aIndex < assignment.length) {
					assignment[aIndex++] = i + 1;
					assigned++;
				}
			}
		}

		if (assigned < numHouse) {
			int remain = numHouse - assigned;
			for (int i = 0; i <= remain; i++) {
				for (int j = 0; i <= remain && j < roundedDown.size()
						&& aIndex < assignment.length; j++) {
					assignment[aIndex++] = roundedDown.get(j) + 1;
					remain--;
				}
			}
		}

		Collections.shuffle(Arrays.asList(assignment), new Random(482)); // need
																			// to
																			// have
																			// the
																			// same
																			// shuffling
																			// every
																			// time
																			// to
																			// be
																			// able
																			// to
																			// compare
																			// results
																			// properly

		int[] toReturn = new int[numHouse];
		for (int i = 0; i < assignment.length; i++) {
			toReturn[i] = assignment[i];
		}

		return toReturn;
	}

	public static boolean[] generateChargeLevelAssignments(double[] prob,
			int numEV, int numLv2Fixed) { // the size of prob is always two
											// (lv1,lv2) which is
		// equivalent to (false,true)
		List<Boolean> assignment = new ArrayList<Boolean>();

		int numEVAtLv1 = (int) Math.rint(prob[0] * numEV);
		int numEVAtLv2 = (int) Math.rint(prob[1] * numEV);
		if (numEVAtLv1 < prob[0] * numEV)
			numEVAtLv1++;
		if (numEVAtLv2 < prob[1] * numEV)
			numEVAtLv2++;

		numEVAtLv2 -= numLv2Fixed;

		for (int i = 0; i < numEVAtLv1; i++) {
			assignment.add(false);
		}
		for (int i = 0; i < numEVAtLv2; i++) {
			assignment.add(true);
		}

		Collections.shuffle(assignment, new Random(2648));

		boolean[] toReturn = new boolean[numEV - numLv2Fixed];
		for (int i = 0; i < toReturn.length; i++) {
			toReturn[i] = assignment.get(i);
		}

		return toReturn;
	}

	public static int[] generateChargeLevelAssignments(
			int[] numHousesInFeeders, int numHouses, int numLv2, int numTesla,
			int numRenault) {
		Random rand = new Random(876);
		int[] assignments = new int[numHouses];

		int numLv2PerFeeder = numLv2 / numHousesInFeeders.length;

		int[] numExtraTeslasInFeeders = new int[numHousesInFeeders.length];
		int[] numExtraRenaultInFeeders = new int[numHousesInFeeders.length];

		int numTeslaPerFeeder = numTesla / numHousesInFeeders.length;
		int remainingTesla = numTesla % numHousesInFeeders.length;
		ArrayList<Integer> feedersWithExtraTesla = new ArrayList<Integer>();
		while (feedersWithExtraTesla.size() < remainingTesla) {
			int f = rand.nextInt(numHousesInFeeders.length);
			if (!feedersWithExtraTesla.contains(f)) {
				feedersWithExtraTesla.add(f);
				numExtraTeslasInFeeders[f] += 1;
			}
		}

		boolean sameFeeder = numLv2PerFeeder - 1 > 0 ? true : false;

		int numRenaultPerFeeder = numRenault / numHousesInFeeders.length;
		int remainingRenault = numRenault % numHousesInFeeders.length;
		ArrayList<Integer> feedersWithExtraRenault = new ArrayList<Integer>();
		while (feedersWithExtraRenault.size() < remainingRenault) {
			int f = rand.nextInt(numHousesInFeeders.length);
			if (!feedersWithExtraRenault.contains(f)
					&& (sameFeeder ? true : !feedersWithExtraTesla.contains(f))) {
				feedersWithExtraRenault.add(f);
				numExtraRenaultInFeeders[f] += 1;
			}
		}

		int index = 0;
		for (int feeder = 0; feeder < numHousesInFeeders.length; feeder++) {
			int[] feederAssignments = generateChargeLevelAssignmentsInFeeder(
					feeder, numHousesInFeeders[feeder], numLv2PerFeeder,
					numTeslaPerFeeder + numExtraTeslasInFeeders[feeder],
					numRenaultPerFeeder + numExtraRenaultInFeeders[feeder]);
			for (int j = 0; j < feederAssignments.length; j++) {
				assignments[index] = feederAssignments[j];
				System.out.println("House " + index + " is "
						+ feederAssignments[j]);
				index++;
			}
		}

		return assignments;
	}

	public static int[] generateChargeLevelAssignmentsInFeeder(int feeder,
			int numHouses, int numLv2, int numTesla, int numRenault) {
		Random rand = new Random(45 * (feeder + 1));
		int[] assignments = new int[numHouses];

		ArrayList<Integer> teslaPositions = new ArrayList<Integer>();
		ArrayList<Integer> renaultPositions = new ArrayList<Integer>();
		ArrayList<Integer> lv2Positions = new ArrayList<Integer>();
		// random where tesla is
		while (teslaPositions.size() < numTesla) {
			int position = rand.nextInt(numHouses);
			if (!teslaPositions.contains(position))
				teslaPositions.add(position);
		}

		// random where renault is
		while (renaultPositions.size() < numRenault) {
			int position = rand.nextInt(numHouses);
			if (!renaultPositions.contains(position)
					&& !teslaPositions.contains(position))
				renaultPositions.add(position);
		}

		// random where the other lv2 cars are
		while (lv2Positions.size() < numLv2 - numTesla - numRenault) {
			int position = rand.nextInt(numHouses);
			if (!renaultPositions.contains(position)
					&& !teslaPositions.contains(position)
					&& !lv2Positions.contains(position))
				lv2Positions.add(position);
		}

		// assmeble the array to return
		// 1 = lv1, 2 = lv2, 3 = tesla, 4 = renault
		for (int i = 0; i < assignments.length; i++) {
			if (teslaPositions.contains(i))
				assignments[i] = 3;
			else if (renaultPositions.contains(i))
				assignments[i] = 4;
			else if (lv2Positions.contains(i))
				assignments[i] = 2;
			else
				assignments[i] = 1;
		}

		return assignments;
	}

	private static ArrayList<ComfortPreference> parseComfortPreference(
			Element entity) {
		// read comfort measures and preferences
		ArrayList<ComfortPreference> prefs = new ArrayList<ComfortPreference>();
		HashMap<String, ComfortMeasure> measureList = new HashMap<String, ComfortMeasure>();
		NodeList comfortNodes = entity
				.getElementsByTagName("smash:ComfortSpec");
		for (int j = 0; j < comfortNodes.getLength(); j++) {
			Element comfortNode = (Element) comfortNodes.item(j);
			String measureID = comfortNode.getAttribute("smash:ComfortMeasure");
			String measureName = measureID;
			String measureUnit = comfortNode.getAttribute("smash:unit");

			ComfortMeasure m = new ComfortMeasure(measureID, measureName,
					measureUnit);
			ComfortPreference pref = new ComfortPreference();
			Element n = (Element) ((Element) comfortNode.getElementsByTagName(
					"smash:normalComfort").item(0)).getElementsByTagName(
					"smash:logicExpression").item(0);
			Element r = (Element) ((Element) comfortNode.getElementsByTagName(
					"smash:reducedComfort").item(0)).getElementsByTagName(
					"smash:logicExpression").item(0);
			Element u = (Element) ((Element) comfortNode.getElementsByTagName(
					"smash:unacceptableComfort").item(0)).getElementsByTagName(
					"smash:logicExpression").item(0);
			pref.setComfortMeasure(m);
			pref.setNormal(parseLogicExpression(n));
			pref.setReduced(parseLogicExpression(r));
			pref.setUnacceptable(parseLogicExpression(u));
			pref.setDoDCalculation(parseDoDCalculation((Element) comfortNode
					.getElementsByTagName("smash:dodCalculation").item(0)));
			if (comfortNode.getAttribute("smash:IncludeInStat").equals("true"))
				prefs.add(pref);
			measureList.put(measureID, m);
		}

		return prefs;
	}

	private static LogicExpression parseLogicExpression(Element element) {
		Object value1 = null;
		Object value2 = null;

		// composed of value1, operator, value2
		Element val1 = getDirectChild(element, "smash:val1");
		Element operator = getDirectChild(element, "smash:operator");
		Element val2 = getDirectChild(element, "smash:val2");
		if (getDirectChild(val1, "smash:logicExpression") != null)
			value1 = parseLogicExpression(getDirectChild(val1,
					"smash:logicExpression"));
		else
			value1 = val1.getTextContent();
		String op = operator.getTextContent();
		if (getDirectChild(val2, "smash:logicExpression") != null)
			value2 = parseLogicExpression(getDirectChild(val2,
					"smash:logicExpression"));
		else
			value2 = val2.getTextContent();

		LogicExpression exp = new LogicExpression(value1, op, value2);
		return exp;
	}

	private static Element getDirectChild(Element parent, String name) {
		for (Node child = parent.getFirstChild(); child != null; child = child
				.getNextSibling()) {
			if (child instanceof Element && name.equals(child.getNodeName()))
				return (Element) child;
		}
		return null;
	}

	private static Object[] parseDoDCalculation(Element element) {
		Object[] cal = new Object[3];

		String[] levels = { "smash:normalComfort", "smash:reducedComfort",
				"smash:unacceptableComfort" };
		int index = 0;
		for (String level : levels) {
			double[] bounds = new double[2];
			Element el = getDirectChild(element, level);
			bounds[0] = Double.parseDouble(getDirectChild(el,
					"smash:lowerBound").getTextContent());
			bounds[1] = Double.parseDouble(getDirectChild(el,
					"smash:upperBound").getTextContent());
			cal[index++] = bounds;
		}

		return cal;
	}

	public static int[] parseSubscribedContracts(int numSubscriber) {
		return parseSubscribedContracts(numSubscriber,
				"./data/dso/subscribers.txt");
	}

	public static int[] parseSubscribedContracts(int numSubscriber,
			String filename) {
		int[] contracts = new int[numSubscriber];

		BufferedReader br = null;
		int line = 1;
		try {
			String sCurrentLine;
			br = new BufferedReader(new FileReader(filename));

			while ((sCurrentLine = br.readLine()) != null
					&& !sCurrentLine.isEmpty()) {
				contracts[line - 1] = Integer.parseInt(sCurrentLine);
				if (++line > numSubscriber)
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

		return contracts;
	}

	public static int getHouseholdSize(int patternID) {
		switch (patternID) {
		case 1:
			return 2;
		case 2:
			return 4;
		case 3:
			return 3;
		case 4:
			return 2;
		case 5:
			return 4;
		}
		return 1;
	}
}
