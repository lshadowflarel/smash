package no.ntnu.item.smash.sim.structure.logic;

import java.util.ArrayList;

public class LogicExpression {

	private Object value1;
	private String operator;
	private Object value2;
	
	public LogicExpression(Object value1, String operator, Object value2) {
		this.value1 = value1;
		this.operator = operator;
		this.value2 = value2;
	}
	
	public boolean resolve(ArrayList<Object> values) { 				
		if(operator==null || value1==null || value2==null) return true;
		else if(!operator.equals("AND") && !operator.equals("OR")){
			if(value1 instanceof String && ((String)value1).startsWith("VAL")) {
				Object dup1 = (String)value1;
				dup1 = values.get(Integer.parseInt(((String)dup1).replace("VAL", ""))-1); 
				if(operator.equals("GT") && (Double)dup1>Double.parseDouble((String)value2)) return true;
				else if(operator.equals("GTE") && (Double)dup1>=Double.parseDouble((String)value2)) return true;
				else if(operator.equals("LT") && (Double)dup1<Double.parseDouble((String)value2)) return true;
				else if(operator.equals("LTE") && (Double)dup1<=Double.parseDouble((String)value2)) return true;
				else if(operator.equals("EQ") && (Double)dup1==Double.parseDouble((String)value2)) return true;
			} 
		} 
		else if(operator.equals("AND")) {
			if(((LogicExpression)value1).resolve(values) && ((LogicExpression)value2).resolve(values)) return true;
		} else if(operator.equals("OR")) {
			if(((LogicExpression)value1).resolve(values) || ((LogicExpression)value2).resolve(values)) return true;
		}

		return false;
	}
	
	public String toString() { 
		String ret = "";
		if(value1 instanceof String || value1 instanceof Double) ret += "" + value1;
		else if(value1 instanceof LogicExpression) ret += "(" + ((LogicExpression)value1).toString() + ")";
		ret += " " + operator + " ";
		if(value2 instanceof String || value2 instanceof Double) ret += "" + value2;
		else if(value2 instanceof LogicExpression) ret += "(" + ((LogicExpression)value2).toString() + ")";
			
		return ret;
	}
}
