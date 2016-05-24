package no.ntnu.item.smash.sim.structure;

public class BuildingThermalIntegrity {

	public static final double[] OLD_UNINSULATED = {1.94, 0.7, 0.7, 7.2, 0.53};
	public static final double[] OLD_INSULATED = {3.35, 1.94, 0.7, 4.6, 0.53};
	public static final double[] OLD_WEATHERIZED = {3.35, 1.94, 1.94, 4.6, 0.53};
	public static final double[] OLD_RETROFIT = {5.29, 1.94, 3.35, 3.4, 0.53};
	public static final double[] MODERATE_INSULATED = {5.29, 3.35, 1.94, 3.4, 0.53};
	public static final double[] WELL_INSULATED = {5.29, 3.35, 3.87, 2.67, 0.88};
	public static final double[] EXTREMELY_INSULATED = {8.45, 3.87, 5.29, 1.76, 1.94};
	public static final double[] NORWAY_MININSULATED = {5.55, 4.54, 5.55, 1.6, 0.625};
	public static final double[] NORWAY_MEDINSULATED = {6.67, 5, 6.25, 1.5, 0.67};
	public static final double[] NORWAY_HEAVYINSULATED = {7.69, 5.55, 6.67, 1.2, 0.833};
	
	public static final int R_CEILING_INDEX = 0;
	public static final int R_WALLS_INDEX = 1;
	public static final int R_FLOORS_INDEX = 2;
	public static final int U_WINDOW_INDEX = 3;
	public static final int R_DOOR_INDEX = 4;
	
	public static double[] getThermalPropSetFromCode(String code) {
		if(code.equals("OLD_UNINSULATED")) return OLD_UNINSULATED;
		else if(code.equals("OLD_INSULATED")) return OLD_INSULATED;
		else if(code.equals("OLD_WEATHERIZED")) return OLD_WEATHERIZED;
		else if(code.equals("OLD_RETROFIT")) return OLD_RETROFIT;
		else if(code.equals("MODERATE_INSULATED")) return MODERATE_INSULATED;
		else if(code.equals("WELL_INSULATED")) return WELL_INSULATED;
		else if(code.equals("EXTREMELY_INSULATED")) return EXTREMELY_INSULATED;
		else if(code.equals("NORWAY_MININSULATED")) return NORWAY_MININSULATED;
		else if(code.equals("NORWAY_MEDINSULATED")) return NORWAY_MEDINSULATED;
		else if(code.equals("NORWAY_HEAVYINSULATED")) return NORWAY_HEAVYINSULATED;
		return NORWAY_MEDINSULATED;
	}
}
