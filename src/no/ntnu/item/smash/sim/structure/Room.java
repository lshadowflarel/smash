package no.ntnu.item.smash.sim.structure;

import java.util.ArrayList;
import java.util.Arrays;

import no.ntnu.item.smash.css.em.EnergyManagement;
import no.ntnu.item.smash.sim.data.Constants;
import no.ntnu.item.smash.sim.data.stats.RoomIlluminanceStat;
import no.ntnu.item.smash.sim.externalsp.WeatherService;

public class Room extends Entity {

	private ArrayList<RoomWall> outerWalls;
	private ArrayList<RoomWall> innerWalls;
	private double height;
	private double width;
	private double length;
	private int windowNo;
	private double windowW;
	private double windowL;
	private double roofPit;
	private ArrayList<Device> devices;
	private Plan plan;
	private Schedule schedule;
	private double currentTemp;
	private double utilizationFactor, lightLossFactor;
	private RoomIlluminanceStat illuminanceStat;
	private Plan lightPlan;
	private ArrayList<String> adjacentRooms = new ArrayList<String>();

	public double areaDoor;
	public double areaWindow;
	public double areaEWall;
	public double areaIWall;
	public double areaCeiling;
	public double areaFloor;
	
	public Room() {

	}

	public Room(String name, ArrayList<RoomWall> outerWalls,
			ArrayList<RoomWall> innerWalls, double height, double width,
			double length, int windowNo, double windowW, double windowL, double doorArea,
			double roofPit, ArrayList<Device> devices, Plan plan) {
		this.name = name;
		this.outerWalls = outerWalls;
		this.innerWalls = innerWalls;
		this.height = height;
		this.width = width;
		this.length = length;
		this.windowNo = windowNo;
		this.windowW = windowW;
		this.windowL = windowL;
		this.roofPit = roofPit;
		this.devices = devices;
		this.areaDoor = doorArea;
		this.areaFloor = length * width;
		this.areaCeiling = this.areaFloor;
		this.areaEWall = (height * width) + (height * length) - (this.windowNo * this.windowW * this.windowL);
		this.areaIWall = (height * width) + (height * length) - doorArea;

		double[] schedule = new double[24];
		System.arraycopy(plan.getHourlyPlan(), 0, schedule, 0, schedule.length);
		this.plan = plan;
		this.schedule = new Schedule(
				Schedule.createHighResolutionSchedule(schedule));
		this.currentTemp = schedule[0];
	}

	public Room(String name, ArrayList<RoomWall> outerWalls,
			ArrayList<RoomWall> innerWalls, double height, double width,
			double length, int windowNo, double windowW, double windowL,
			double roofPit, ArrayList<Device> devices, Plan plan, double uf,
			double llf) {
		this.name = name;
		this.outerWalls = outerWalls;
		this.innerWalls = innerWalls;
		this.height = height;
		this.width = width;
		this.length = length;
		this.windowNo = windowNo;
		this.windowW = windowW;
		this.windowL = windowL;
		this.roofPit = roofPit;
		this.devices = devices;
		this.utilizationFactor = uf;
		this.lightLossFactor = llf;
		illuminanceStat = new RoomIlluminanceStat(this);

		double[] schedule = new double[24];
		System.arraycopy(plan.getHourlyPlan(), 0, schedule, 0, schedule.length);
		this.plan = plan;
		this.schedule = new Schedule(
				Schedule.createHighResolutionSchedule(schedule));
		this.currentTemp = schedule[0];
	}

	public ArrayList<RoomWall> getOuterWalls() {
		return outerWalls == null ? new ArrayList<RoomWall>() : outerWalls;
	}

	public ArrayList<RoomWall> getInnerWalls() {
		return innerWalls == null ? new ArrayList<RoomWall>() : innerWalls;
	}

	public void setOuterWalls(ArrayList<RoomWall> outerWalls) {
		this.outerWalls = outerWalls;
	}

	public void setInnerWalls(ArrayList<RoomWall> innerWalls) {
		this.innerWalls = innerWalls;
	}

	public void addAdjacentRoom(String r) {
		adjacentRooms.add(r);
	}
	
	public void setAdjacentRoom(ArrayList<String> rooms) {
		this.adjacentRooms = rooms;
	}
	
	public ArrayList<String> getAdjacentRooms() {
		return adjacentRooms;
	}
	
	public double getHeight() {
		return height;
	}

	public void setHeight(double height) {
		this.height = height;
	}

	public double getWidth() {
		return width;
	}

	public void setWidth(double width) {
		this.width = width;
	}

	public double getLength() {
		return length;
	}

	public void setLength(double length) {
		this.length = length;
	}

	public double getDoorArea() {
		return areaDoor;
	}
	
	public void setDoorArea(double doorArea) {
		this.areaDoor = doorArea;
	}
	
	public int getWindowNo() {
		return windowNo;
	}

	public void setWindowNo(int windowNo) {
		this.windowNo = windowNo;
	}

	public double getWindowW() {
		return windowW;
	}

	public void setWindowW(double windowW) {
		this.windowW = windowW;
	}

	public double getWindowL() {
		return windowL;
	}

	public void setWindowL(double windowL) {
		this.windowL = windowL;
	}

	public double getRoofPit() {
		return roofPit;
	}

	public void setRoofPit(double roofPit) {
		this.roofPit = roofPit;
	}

	public ArrayList<Device> getDevices() {
		return devices;
	}

	public ArrayList<Device> getDevicesByType(int type) {
		ArrayList<Device> dev = new ArrayList<Device>();

		for (Device d : devices) {
			if (d.getType() == type)
				dev.add(d);
		}

		return dev;
	}

	public void setDevices(ArrayList<Device> devices) {
		this.devices = devices;
	}

	public void addDevice(Device device) {
		this.devices.add(device);
	}

	public Plan getPlannedSchedule() {
		return plan;
	}

	public Schedule getSchedule() {
		return schedule;
	}

	public void setSchedule(Schedule schedule) {
		this.schedule = schedule;
	}

	public double getCurrentTemp() {
		return currentTemp;
	}

	public void setCurrentTemp(double value) {
		currentTemp = value;
	}

	public double[] getExtendedSchedule(int extendedHours, int day, int month,
			int year) {
		double[] exschedule = new double[288 + (2 * 12 * extendedHours)];
		double[] schedule = Arrays
				.copyOf(this.getSchedule().getSchedule(), 288);

		// if there's -9999 in the schedule, it means that heating is off
		// in which case the system has to calculate the expected temperature

		int j = 0;
		for (int i = extendedHours; i > 0; i--) {
			double expectedTemp = schedule[288 - (i * 12)];
			if (expectedTemp == -9999) {
				expectedTemp = schedule[288 - (i * 12) - 12]
						- EnergyManagement.estimatedTempDrop(name, WeatherService
								.getActualTemp((24 - i) * 3600,
										day - 1 > 0 ? day - 1 : day, month,
										year), // TODO: day-1>0? is cheating
								schedule[288 - (i * 12) - 12], this
										.getWidth(), this.getLength(), this
										.getHeight(), areaDoor, this.getWindowNo(), windowL, windowW, 60);

				for (int y = 0; y < 12; y++)
					schedule[288 - (i * 12) + y] = expectedTemp;
			}

			for (int x = 0; x < 12; x++)
				exschedule[j * 12 + x] = expectedTemp;
			j++;
		}

		for (int i = 0; i < 24; i++) {
			double expectedTemp = schedule[i * 12];
			if (expectedTemp == -9999) {
				expectedTemp = schedule[i == 0 ? 23 * 12 : (i * 12) - 12]
						- EnergyManagement.estimatedTempDrop(name, WeatherService
								.getActualTemp(i * 3600, day, month, year),
								schedule[i == 0 ? 23 * 12 : (i * 12) - 12], this
										.getWidth(), this.getLength(), this
										.getHeight(), areaDoor, this.getWindowNo(), windowL, windowW, 60);

				for (int y = 0; y < 12; y++) {
					schedule[i * 12 + y] = expectedTemp;
				}
			}

			for (int x = 0; x < 12; x++) {
				exschedule[((i + extendedHours) * 12) + x] = expectedTemp;
			}
		}

		for (int i = 24 + extendedHours; i < 2 * extendedHours + 24; i++) {
			double expectedTemp = schedule[((i - extendedHours) * 12) % 288];
			if (expectedTemp == -9999) {
				expectedTemp = schedule[(i == 24 + extendedHours ? 23 * 12 : ((i
						- extendedHours) * 12) % 288) - 12]
						- EnergyManagement
								.estimatedTempDrop(name,
										WeatherService
												.getActualTemp(
														(i - extendedHours) * 12 % 288,
														day + 1 > Constants
																.getNumDaysInMonth(
																		month,
																		year) ? day
																: day + 1, // TODO:
																			// day+1>...?
																			// is
																			// cheating
														month, year),
										schedule[(i == 24 + extendedHours ? 23 * 12
												: (i - extendedHours) * 12) % 288 - 12],
										this.getWidth(), this.getLength(), this
												.getHeight(), areaDoor, this.getWindowNo(), windowL, windowW, 6);

				for (int y = 0; y < 12; y++)
					schedule[(i - extendedHours - 1) * 12 % 288 + y] = expectedTemp;
			}

			for (int x = 0; x < 12; x++)
				exschedule[i * 12 + x] = expectedTemp;
		}

		// System.out.println();
		// for(int m=0; m<exschedule.length; m++) {
		// System.out.print(exschedule[m] + " ");
		// }System.out.println();

		return exschedule;
	}

	public double getUtilizationFactor() {
		return utilizationFactor;
	}

	public void setUtilizationFactor(double uf) {
		utilizationFactor = uf;
	}

	public double getLightLossFactor() {
		return lightLossFactor;
	}

	public void setLightLossFactor(double llf) {
		lightLossFactor = llf;
	}

	public synchronized RoomIlluminanceStat getIlluminanceStat() {
		return illuminanceStat;
	}

	public Plan getLightPlan() {
		return lightPlan;
	}

	public void setLightPlan(Plan lightPlan) {
		this.lightPlan = lightPlan;
	}
}
