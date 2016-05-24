package no.ntnu.item.smash.sim.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import no.ntnu.item.smash.css.em.EnergyManagement;
import no.ntnu.item.smash.sim.data.stats.EnergyUsageStatistics;
import no.ntnu.item.smash.sim.model.DeviceModel;
import no.ntnu.item.smash.sim.structure.BuildingThermalIntegrity;
import no.ntnu.item.smash.sim.structure.ComfortMeasure;
import no.ntnu.item.smash.sim.structure.Device;
import no.ntnu.item.smash.sim.structure.Plan;
import no.ntnu.item.smash.sim.structure.Room;
import no.ntnu.item.smash.sim.structure.RoomWall;

public class SimEnvironment {

	private double[] thermalProp = BuildingThermalIntegrity.NORWAY_MININSULATED;
	private int householdSize;
	private ArrayList<Room> rooms = new ArrayList<Room>();
	private HashMap<Device, DeviceModel> deviceModelMap = new HashMap<Device, DeviceModel>();
	private ArrayList<ComfortMeasure> comfortMeasures = new ArrayList<ComfortMeasure>();
	private Device dlcDevice;
	private String css;

	public void setThermalProperty(double[] prop) {
		EnergyManagement.thermalProp = prop;
		EnergyUsageStatistics.thermalProp = prop;
		thermalProp = prop;
	}
	
	public double[] getThermalProperty() {
		return thermalProp;
	}
	
	public ArrayList<Room> getRooms() {
		return rooms;
	}
	
	public void setHouseholdSize(int size) {
		householdSize = size;
	}
	
	public int getHouseholdSize() {
		return householdSize;
	}
	
	public void addComfortMeasure(ComfortMeasure measure) {
		if(!comfortMeasures.contains(measure)) comfortMeasures.add(measure);
	}
	
	public ComfortMeasure getComfortMeasureByName(String name) {
		for(ComfortMeasure m:comfortMeasures) {
			if(m.getId().equals(name)) return m;
		}
		
		return null;
	}
	
	public ArrayList<ComfortMeasure> getComfortMeasures() {
		return comfortMeasures;
	}
	
	public Room getRoom(String name) {
		for(Room r:rooms) {
			if(r.getName().equals(name))
				return r;
		}
		
		return null;
	}
	
	public void addRoom(Room room) {
		rooms.add(room);
	}

	public void addRoom(String name, ArrayList<RoomWall> outerWalls, ArrayList<RoomWall> innerWalls,
			double height, double width, double length, int windowNo, double roofPit, double windowW, double windowL,
			double areaDoor, ArrayList<Device> devices, Plan schedule) {
		Room room = new Room(name, outerWalls, innerWalls, height, width, length, windowNo, windowW, windowL, areaDoor, roofPit, devices, schedule);
		rooms.add(room);
	}
	
	public HashMap<Device, DeviceModel> getDeviceModelMap() {
		return deviceModelMap;
	}
	
	public void setDeviceModelMap(HashMap<Device, DeviceModel> map) {
		deviceModelMap = map;
	}
	
	public void addDeviceModel(Device device, DeviceModel model) {
		deviceModelMap.put(device, model);
	}
	
	public DeviceModel getDeviceModelByName(String name) {
		Set<Device> keySet = deviceModelMap.keySet();
		for(Device d:keySet) {
			if(d.getName().equals(name)) {
				return deviceModelMap.get(d);
			}
		}
		
		return null;
	}

	public Device getDLCDevice() {
		return dlcDevice;
	}
	
	public void setDLCDevice(Device device) {
		dlcDevice = device;
	}
	
	public String getCSS() {
		return css;
	}
	
	public void setCSS(String cssNetAddress) {
		this.css = cssNetAddress;		
	}
	
}
