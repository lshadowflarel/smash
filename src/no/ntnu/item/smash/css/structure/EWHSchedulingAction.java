package no.ntnu.item.smash.css.structure;

import java.util.Calendar;
import java.util.Date;

import no.ntnu.item.smash.css.core.SystemContext;

public class EWHSchedulingAction extends DecidedAction {

	private Entity room;
	private Object targetValue;
	private Date schedulingTime;
	private int duration;
	
	public EWHSchedulingAction() {
		
	}
	
	public EWHSchedulingAction(Entity room, Object value, Date time) {
		this.room = room;
		targetValue = value;
		schedulingTime = time;
	}
	
	public Entity getRoom() {
		return room;
	}

	public void setRoom(Entity room) {
		this.room = room;
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

	@Override
	public void execute(SystemContext context) {
		// TODO: A SchedulingAction's job is to modify the ActivitySchedule
		
		// TODO: All these will be replaced later
		Calendar cal = Calendar.getInstance();
		cal.setTime(schedulingTime);
		context.getHouse().startRescheduling(1, room.getResourceURI(), (Integer)targetValue, duration, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), schedulingTime, 0);
	}
	
}
