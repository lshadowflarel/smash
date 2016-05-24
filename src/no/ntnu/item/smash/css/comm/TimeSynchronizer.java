package no.ntnu.item.smash.css.comm;

public class TimeSynchronizer {

	public int min = 0;
	public int hour = -1;
	public int day;
	public int month;
	public int year;
	
	public TimeSynchronizer() {
	}
	
	public int min() {
		return min;
	}
	
	public int hour() {
		return hour;
	}
	
	public int day() {
		return day;
	}
	
	public int month() {
		return month;
	}
	
	public int year() {
		return year;
	}
	
	public void setTime(int min, int hour, int day, int month, int year) {
		this.min = min;
		this.hour = hour;
		this.day = day;
		this.month = month;
		this.year = year;
	}
	
}
