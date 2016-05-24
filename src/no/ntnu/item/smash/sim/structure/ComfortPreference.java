package no.ntnu.item.smash.sim.structure;

import java.util.ArrayList;

import no.ntnu.item.smash.sim.structure.logic.LogicExpression;

public class ComfortPreference {

	private ComfortMeasure measure;
	private ArrayList<Object> placeholders;
	private LogicExpression normal;
	private LogicExpression reduced;
	private LogicExpression unacceptable;
	private Object[] dodCalculation = new Object[3];
	
	public ComfortPreference() {}
	
	public ComfortPreference(ComfortMeasure measure, LogicExpression normal, LogicExpression reduced, LogicExpression unacceptable) {
		this.measure = measure;
		this.normal = normal;
		this.reduced = reduced;
		this.unacceptable = unacceptable;
	}

	public ComfortMeasure getComfortMeasure() {
		return measure;
	}
	
	public void setComfortMeasure(ComfortMeasure measure) {
		this.measure = measure;
	}
	
	public void setDoDCalculation(Object[] cal) {
		dodCalculation = cal;
	}
	
	public Object[] getDoDCalculation() {
		return dodCalculation;
	}
	
	public ArrayList<Object> getPlaceholder() {
		return placeholders;
	}
	
	public void setPlaceholder(ArrayList<Object> realValues) {
		placeholders = realValues;
	}
	
	public LogicExpression getNormal() {
		return normal;
	}

	public void setNormal(LogicExpression normal) {
		this.normal = normal;
	}

	public LogicExpression getReduced() {
		return reduced;
	}

	public void setReduced(LogicExpression reduced) {
		this.reduced = reduced;
	}

	public LogicExpression getUnacceptable() {
		return unacceptable;
	}

	public void setUnacceptable(LogicExpression unacceptable) {
		this.unacceptable = unacceptable;
	}
	
	public boolean checkNormal(ArrayList<Object> values) {
		setPlaceholder(values);
		return normal.resolve(placeholders);
	}
	
	public boolean checkReduced(ArrayList<Object> values) {
		setPlaceholder(values);
		return reduced.resolve(placeholders);
	}
	
	public boolean checkUnacceptable(ArrayList<Object> values) {
		setPlaceholder(values);
		return unacceptable.resolve(placeholders);
	}
	
}
