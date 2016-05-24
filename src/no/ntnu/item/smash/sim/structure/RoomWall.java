package no.ntnu.item.smash.sim.structure;

public class RoomWall {
	private int id;
	private double length;
	private double area; // effective area (excluding window area) in m2
	private boolean enclosed;
	private Room adjacentRoom;
	private double temperature;
	private double innerSurfaceTemp;
	private double outerSurfaceTemp;
	private double solarArea = 0;

	public RoomWall(int id, double length, double area, boolean enclosed, Room adjacentRoom, double outerTemp, double innerTemp) {
		this.id = id;
		this.length = length;
		this.area = area;
		this.enclosed = enclosed;
		this.adjacentRoom = adjacentRoom;
		this.innerSurfaceTemp = innerTemp;
		this.outerSurfaceTemp = outerTemp;
	}
	
	public RoomWall(int id, double length, double area, boolean enclosed, Room adjacentRoom, double temperature) {
		this.id = id;
		this.length = length;
		this.area = area;
		this.enclosed = enclosed;
		this.adjacentRoom = adjacentRoom;
		this.temperature = temperature;
	}

	public int getId() {
		return id;
	}
	
	public double getLength() {
		return length;
	}
	
	public double getArea() {
		return area;
	}

	public boolean enclosed() {
		return enclosed;
	}

	public Room getAdjacentRoom() {
		return adjacentRoom;
	}

	public double getTemperature() {
		return temperature;
	}
	
	public double getInnerSurfaceTemp() {
		return innerSurfaceTemp;
	}
	
	public double getOuterSurfaceTemp() {
		return outerSurfaceTemp;
	}
	
	public double getSolarArea() {
		return solarArea;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public void setLength(double length) {
		this.length = length;
	}
	
	public void setArea(double area) {
		this.area = area;
	}

	public void setEnclosed(boolean enclosed) {
		this.enclosed = enclosed;
	}

	public void setAdjacentRoom(Room adjacentRoom) {
		this.adjacentRoom = adjacentRoom;
	}
	
	public void setTemperature(double temperature) {
		this.temperature = temperature;
	}
	
	public void setInnerSurfaceTemp(double temp) {
		this.innerSurfaceTemp = temp;
	}
	
	public void setOuterSurfaceTemp(double temp) {
		this.outerSurfaceTemp = temp;
	}
	
	public void setSolarArea(double solarArea) {
		this.solarArea = solarArea;
	}
}
