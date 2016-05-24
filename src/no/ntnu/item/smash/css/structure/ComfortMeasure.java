package no.ntnu.item.smash.css.structure;

public class ComfortMeasure {

	public static final int COMFORTTYPE_TEMP = 0;
	public static final int COMFORTTYPE_HUMID = 1;
	public static final int COMFORTTYPE_LIGHT = 2;
	public static final int COMFORTTYPE_DEVATTR = 3;
	
	private int type;
	private int dataType;
	
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public int getDataType() {
		return dataType;
	}
	public void setDataType(int dataType) {
		this.dataType = dataType;
	}
	
	
}
