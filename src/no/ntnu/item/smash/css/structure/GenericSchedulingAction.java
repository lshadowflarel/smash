package no.ntnu.item.smash.css.structure;

import java.util.Calendar;
import java.util.Date;

import no.ntnu.item.smash.css.core.SystemContext;

public class GenericSchedulingAction extends DecidedAction {

	private Entity device;
	private int deviceType;
	private Object targetValue;
	private Date schedulingTime;
	private int duration;
	private int taskID;
	
	public GenericSchedulingAction() {
		
	}
	
	public GenericSchedulingAction(Entity device, Object value, Date time) {
		this.device = device;
		targetValue = value;
		schedulingTime = time;
	}
	
	public Entity getEntity() {
		return device;
	}

	public void setEntity(Entity device) {
		this.device = device;
	}

	public int getDeviceType() {
		return deviceType;
	}
	
	public void setDeviceType(int deviceType) {
		this.deviceType = deviceType;
	}
	
	public Object getTargetValue() {
		return targetValue;
	}

	public void setTargetValue(Object targetValue) {
		this.targetValue = targetValue;
	}

	public Date getSchedulingTime() {
		return schedulingTime;
	}

	public void setSchedulingTime(Date schedulingTime) {
		this.schedulingTime = schedulingTime;
	}
	
	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}
	
	public int getTaskID() {
		return taskID;
	}
	
	public void setTaskID(int taskID) {
		this.taskID = taskID;
	}

	@Override
	public void execute(SystemContext context) {
		// TODO: A SchedulingAction's job is to modify the ActivitySchedule
		
		// TODO: All these will be replaced later
		Calendar cal = Calendar.getInstance();
		cal.setTime(schedulingTime);
		context.getHouse().startRescheduling(deviceType, device.getResourceURI(), (Integer)targetValue, duration, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), schedulingTime, taskID);
	}
	
}
