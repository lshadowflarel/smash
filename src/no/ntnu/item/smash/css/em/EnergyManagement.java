package no.ntnu.item.smash.css.em;

import java.util.HashMap;

public class EnergyManagement {

	public static double[] thermalProp = BuildingThermalIntegrity.NORWAY_MEDINSULATED;
	private static final double waterDensity = 1; // kg/litre
	private static final double cp = 4186; // J/Ckg
	private static double w_solar = 20000 / (24 * 30); // W/m2
	private static final double fract_solar_wall = 0.4;
	private static double internal = 50;

	public static double functionAdaptor(String name,
			HashMap<String, Object> params) {
		if (name.equals("estimatedHeatTime")) {
			double watt = parseParam(params.get("watt"));
			
			double heatInput = watt - ((((4-(0.94+0.8+1)) * (1*0.97*1.05))-1) * watt) - (((1-0.8)/0.8)*watt); // radiant heater
			//double heatInput = watt - ((((4-(0.91+0.955+1)) * (1*0.98*1.05))-1) * watt) - (((1-0.91)/0.91)*watt);// floor heating
			
			return estimatedTime((String)params.get("entity"),
					parseParam(params.get("otemp")),
					parseParam(params.get("rtemp")),
					parseParam(params.get("rtemp")) + parseParam(params.get("deltaT")),
					parseParam(params.get("roomW")),
					parseParam(params.get("roomL")),
					parseParam(params.get("roomH")),
					parseParam(params.get("doorArea")),
					(int) parseParam(params.get("numWin")),
					parseParam(params.get("winH")),
					parseParam(params.get("winW")),
					heatInput, true);
		} else if (name.equals("estimatedEnergy")) {
			return estimatedEnergy((String)params.get("entity"),
					parseParam(params.get("otemp")),
					parseParam(params.get("rtemp")),
					parseParam(params.get("deltaT")),
					parseParam(params.get("roomW")),
					parseParam(params.get("roomL")),
					parseParam(params.get("roomH")),
					parseParam(params.get("doorArea")),
					(int) parseParam(params.get("numWin")),
					parseParam(params.get("winH")),
					parseParam(params.get("winW")),
					parseParam(params.get("watt")));
		} else if (name.equals("estimatedTempDrop")) {
			return estimatedTempDrop((String)params.get("entity"),
					parseParam(params.get("otemp")),
					parseParam(params.get("rtemp")),
					parseParam(params.get("roomW")),
					parseParam(params.get("roomL")),
					parseParam(params.get("roomH")),
					parseParam(params.get("doorArea")),
					(int) parseParam(params.get("numWin")),
					parseParam(params.get("winH")),
					parseParam(params.get("winW")),
					parseParam(params.get("duration")));
		} else if (name.equals("estimatedTempDropTime")) {
			return estimatedTime((String)params.get("entity"),
					parseParam(params.get("otemp")),
					parseParam(params.get("rtemp")),
					parseParam(params.get("rtemp")) - parseParam(params.get("deltaT")),
					parseParam(params.get("roomW")),
					parseParam(params.get("roomL")),
					parseParam(params.get("roomH")),
					parseParam(params.get("doorArea")),
					(int) parseParam(params.get("numWin")),
					parseParam(params.get("winH")),
					parseParam(params.get("winW")),
					0, false);
		} else if (name.equals("estimatedWaterHeatTime")) {
			return estimatedWaterHeatTime(parseParam(params.get("otemp")),
					parseParam(params.get("rtemp")),
					parseParam(params.get("tankHeight")),
					parseParam(params.get("tankDiameter")),
					parseParam(params.get("tankVolume")),
					parseParam(params.get("wattage")),
					parseParam(params.get("rValue")),
					parseParam(params.get("beginT")),
					parseParam(params.get("targetT")));
		} else if (name.equals("estimatedWHEnergy")) {
			return estimatedWHEnergy(parseParam(params.get("otemp")),
					parseParam(params.get("rtemp")),
					parseParam(params.get("tankHeight")),
					parseParam(params.get("tankDiameter")),
					parseParam(params.get("tankVolume")),
					parseParam(params.get("wattage")),
					parseParam(params.get("rValue")),
					parseParam(params.get("beginT")),
					parseParam(params.get("targetT")));
		} else if (name.equals("estimatedWaterTempDrop")) {
			return estimatedWaterTempDrop(parseParam(params.get("otemp")),
					parseParam(params.get("rtemp")),
					parseParam(params.get("tankHeight")),
					parseParam(params.get("tankDiameter")),
					parseParam(params.get("tankVolume")),
					parseParam(params.get("rValue")),
					parseParam(params.get("beginT")),
					parseParam(params.get("duration")));
		} else if (name.equals("estimatedWaterTempDropTime")) {
			return estimatedWaterTempDropTime(parseParam(params.get("otemp")),
					parseParam(params.get("rtemp")),
					parseParam(params.get("tankHeight")),
					parseParam(params.get("tankDiameter")),
					parseParam(params.get("tankVolume")),
					parseParam(params.get("rValue")),
					parseParam(params.get("beginT")),
					parseParam(params.get("targetT")));
		} else if (name.equals("estimatedWaterTemp")) {
			return estimatedWaterTemp(parseParam(params.get("otemp")),
					parseParam(params.get("rtemp")),
					parseParam(params.get("tankHeight")),
					parseParam(params.get("tankDiameter")),
					parseParam(params.get("tankVolume")),
					parseParam(params.get("rValue")),
					parseParam(params.get("wattage")),
					parseParam(params.get("beginT")),
					parseParam(params.get("duration")));
		} else {
			return 0;
		}
	}

	private static double parseParam(Object param) {
		String toParse = (String) param;
		return Double.parseDouble(toParse);
	}

	public static double estimatedEnergy(String room, double otemp, double rtemp,
			double deltaT, double roomW, double roomL, double roomH,
			double doorArea, int numWin, double winH, double winW, double watt) {
		double heatInput = watt - ((((0.97*1*1)/0.91)-1)*watt) - (((1-0.97)/0.97)*watt);  
		double heatMinutes = estimatedTime(room, otemp, rtemp, rtemp+deltaT, roomW,
				roomL, roomH, doorArea, numWin, winH, winW, heatInput, true);

		return watt * 0.001 * heatMinutes / 60; // kWh
	}

	public static void main(String[] args) {
		double rtemp = 26;
		double otemp = -13.5;
		double roomW = 2.5;
		double roomL = 4;
		double roomH = 2.6;
		double doorArea = 2;
		int numWin = 1;
		double winH = 1;
		double winW = 1;
		double duration = 30;
		double watt = 1500;
		
		double heatInput = watt - ((((4-(0.94+0.8+1)) * (1*0.97*1.05))-1) * watt) - (((1-0.8)/0.8)*watt); // radiant heater
		
		System.out.println("Estimated temp drop in 1 hour: "
				+ estimatedTempDrop("LivingRoom", otemp, rtemp, roomW, roomL, roomH,
						doorArea, numWin, winH, winW, duration));
		System.out.println("Estimated time to heat up 1C: "
				+ estimatedTime("LivingRoom", otemp, rtemp, rtemp+1, roomW, roomL, roomH,
						doorArea, numWin, winH, winW, heatInput, true));
		System.out.println("Estimated time to cool down 1C: "
				+ estimatedTime("LivingRoom", otemp, rtemp, rtemp-1, roomW, roomL, roomH,
						doorArea, numWin, winH, winW, 0, false));
	}

	public static double estimatedTempDrop(String room, double otemp, double rtemp,
			double roomW, double roomL, double roomH, double doorArea,
			int numWin, double winH, double winW, double duration) { // duration
																		// is in
																		// minutes
//		if (room.startsWith("Bath")) {
//			w_solar = 0;
//			internal = 0;
//		} else if (room.startsWith("Bed")) {
//			w_solar = 5000 / (24 * 30); // 10000
//			internal = 0;
//		} else if (room.equals("Kitchen")
//				|| room.equals("Guest")
//				|| room.equals("Work")) {
//			w_solar = 2500 / (24 * 30); // 5000
//			internal = 0;
//		}
		
		// estimate areas of walls, ceiling, and floor
		double areaEWall = (roomW * roomH) + (roomL * roomH);
		double areaIWall = areaEWall;
		double areaCeiling = roomW * roomL;
		double areaFloor = areaCeiling;
		double areaWindow = numWin * winH * winW;

		// init temp
		double initTemp_room = rtemp, initTemp_ewall = rtemp - 2, initTemp_iwall = rtemp;

		// mass
		double airMass = 3 * 1005.4 * 1.225 * roomW * roomL * roomH;
		double iwallMass = 40881.6 * areaFloor;
		double ewallMass = (400 * areaEWall * 0.02 * 1000 * 2.5)
				+ (1.205 * areaEWall * 0.02 * 1000 * 1.01)
				+ (32 * areaEWall * 0.15 * 1000 * 0.67)
				+ (13 * areaEWall * 0.02 * 1000 * 1.09);

		// u values
		double u_ewall = areaEWall
				/ thermalProp[BuildingThermalIntegrity.R_WALLS_INDEX];
		double u_window = areaWindow
				* thermalProp[BuildingThermalIntegrity.U_WINDOW_INDEX];
		double u_iwall = (doorArea / thermalProp[BuildingThermalIntegrity.R_DOOR_INDEX])
				+ (areaIWall / thermalProp[BuildingThermalIntegrity.R_WALLS_INDEX])
				+ (areaCeiling / thermalProp[BuildingThermalIntegrity.R_CEILING_INDEX])
				+ (areaFloor / thermalProp[BuildingThermalIntegrity.R_FLOORS_INDEX]);

		double secondsInIteration = 300;
		int numRounds = (int) ((duration * 60) / secondsInIteration);

		for (int i = 0; i < numRounds; i++) {
			double iwallDrop = (1 / iwallMass)
					* ((fract_solar_wall * w_solar * areaWindow * 0.5) + (u_iwall * (initTemp_room - initTemp_iwall)) + (0.5*internal));
			initTemp_iwall += iwallDrop * secondsInIteration;
			double ewallDrop = (1 / ewallMass)
					* ((u_ewall * (otemp - initTemp_ewall)) + (u_ewall * (initTemp_room - initTemp_ewall)));
			initTemp_ewall += ewallDrop * secondsInIteration;
			double roomDrop = (1 / airMass)
					* ((u_window * (otemp - initTemp_room))
							+ ((1-fract_solar_wall) * w_solar * areaWindow * 0.5)
							+ (u_ewall * (initTemp_ewall - initTemp_room)) + (u_iwall * (initTemp_iwall - initTemp_room)) + (0.5*internal));
			initTemp_room += roomDrop * secondsInIteration;

//			System.out.println("[No heating] After " + (secondsInIteration/60) +
//			 " minute(s): T_iwall = " + initTemp_iwall + " T_ewall = " + initTemp_ewall + " T_room = " +
//			 initTemp_room);
		}
		
		return rtemp - initTemp_room; // degree celsius
	}

	public static double estimatedTime(String room, double otemp, double rtemp,
			double targetT, double roomW, double roomL, double roomH, double doorArea,
			int numWin, double winH, double winW, double heatInput, boolean heating) {
		rtemp = Math.round(rtemp * 100.0) / 100.0;

//		if (room.startsWith("Bath")) {
//			w_solar = 0;
//			internal = 0;
//		} else if (room.startsWith("Bed")) {
//			w_solar = 5000 / (24 * 30); // 10000
//			internal = 0;
//		} else if (room.equals("Kitchen")
//				|| room.equals("Guest")
//				|| room.equals("Work")) {
//			w_solar = 2500 / (24 * 30); // 5000
//			internal = 0;
//		}
		
		// estimate areas of walls, ceiling, and floor
		double areaEWall = (roomW * roomH) + (roomL * roomH);
		double areaIWall = areaEWall;
		double areaCeiling = roomW * roomL;
		double areaFloor = areaCeiling;
		double areaWindow = numWin * winH * winW;

		// init temp
		double initTemp_room = rtemp, initTemp_ewall = rtemp - 2, initTemp_iwall = rtemp;

		// mass
		double airMass = 3 * 1005.4 * 1.225 * roomW * roomL * roomH;
		double iwallMass = 40881.6 * areaFloor;
		double ewallMass = (400 * areaEWall * 0.02 * 1000 * 2.5)
				+ (1.205 * areaEWall * 0.02 * 1000 * 1.01)
				+ (32 * areaEWall * 0.15 * 1000 * 0.67)
				+ (13 * areaEWall * 0.02 * 1000 * 1.09);

		// u values
		double u_ewall = areaEWall
				/ thermalProp[BuildingThermalIntegrity.R_WALLS_INDEX];
		double u_window = areaWindow
				* thermalProp[BuildingThermalIntegrity.U_WINDOW_INDEX];
		double u_iwall = (doorArea / thermalProp[BuildingThermalIntegrity.R_DOOR_INDEX])
				+ (areaIWall / thermalProp[BuildingThermalIntegrity.R_WALLS_INDEX])
				+ (areaCeiling / thermalProp[BuildingThermalIntegrity.R_CEILING_INDEX])
				+ (areaFloor / thermalProp[BuildingThermalIntegrity.R_FLOORS_INDEX]);
		
		double secondsInIteration = 300;
		int rounds = 0;
		
		while((heating && initTemp_room < targetT) || (!heating && initTemp_room > targetT)) {
			double iwallDrop = (1 / iwallMass)
					* ((fract_solar_wall * w_solar * areaWindow * 0.5) + (u_iwall * (initTemp_room - initTemp_iwall)) + (0.5*internal) + (heatInput*0.45));
			initTemp_iwall += iwallDrop * secondsInIteration;
			double ewallDrop = (1 / ewallMass)
					* ((u_ewall * (otemp - initTemp_ewall)) + (u_ewall * (initTemp_room - initTemp_ewall)) + (heatInput*0.45));
			initTemp_ewall += ewallDrop * secondsInIteration;
			double roomDrop = (1 / airMass)
					* ((u_window * (otemp - initTemp_room))
							+ ((1-fract_solar_wall) * w_solar * areaWindow * 0.5)
							+ (u_ewall * (initTemp_ewall - initTemp_room)) + (u_iwall * (initTemp_iwall - initTemp_room)) + (0.5*internal) + (heatInput*0.1));
			initTemp_room += roomDrop * secondsInIteration;
			
//			System.out.println("[Heating] After " + (secondsInIteration/60) +
//					 " minute(s): T_iwall = " + initTemp_iwall + " T_ewall = " + initTemp_ewall + " T_room = " +
//					 initTemp_room);
			rounds++;
		}
		
		return rounds * (secondsInIteration / 60);
	}

	public static double estimatedWaterHeatTime(double otemp, double rtemp,
			double tankHeight, double tankDiameter, double tankVolume,
			double wattage, double rValue, double beginT, double targetT) {
		double inputTemp = otemp + Math.random();
		double tankRadius = tankDiameter / 2;
		double surfaceArea = (2 * 3.14 * (tankRadius * tankRadius))
				+ (2 * 3.14 * tankRadius * tankHeight);
		double g = surfaceArea * rValue;
		double c = tankVolume * waterDensity * cp;
		double b = waterDensity * 0 * cp;
		double rdash = 1.0 / (b + g);

		double estTime = -c
				* rdash
				* Math.log((rdash * wattage - targetT + inputTemp * b * rdash + rtemp
						* g * rdash)
						/ (rdash * wattage - beginT + inputTemp * b * rdash + rtemp
								* g * rdash));

		return estTime;
	}

	public static double estimatedWHEnergy(double otemp, double rtemp,
			double tankHeight, double tankDiameter, double tankVolume,
			double wattage, double rValue, double beginT, double targetT) {
		double heatTime = estimatedWaterHeatTime(otemp, rtemp, tankHeight,
				tankDiameter, tankVolume, wattage, rValue, beginT, targetT);

		return wattage * 0.001 * heatTime / 3600; // kWh
	}

	public static double estimatedWaterTempDrop(double otemp, double rtemp,
			double tankHeight, double tankDiameter, double tankVolume,
			double rValue, double beginT, double duration) {
		double inputTemp = otemp + Math.random();
		double tankRadius = tankDiameter / 2;
		double surfaceArea = (2 * 3.14 * (tankRadius * tankRadius))
				+ (2 * 3.14 * tankRadius * tankHeight);
		double g = surfaceArea * rValue;
		double c = tankVolume * waterDensity * cp;
		double b = waterDensity * 0 * cp;
		double rdash = 1.0 / (b + g);

		double temp = (beginT * Math.exp(-duration * (1.0 / (rdash * c))))
				+ ((g * rdash * rtemp + b * rdash * inputTemp + 0 * rdash) * (1.0 - Math
						.exp(-duration * (1.0 / (rdash * c)))));

		return beginT - temp;
	}

	public static double estimatedWaterTemp(double otemp, double rtemp,
			double tankHeight, double tankDiameter, double tankVolume,
			double rValue, double wattage, double beginT, double duration) {
		double inputTemp = otemp + Math.random();
		double tankRadius = tankDiameter / 2;
		double surfaceArea = (2 * 3.14 * (tankRadius * tankRadius))
				+ (2 * 3.14 * tankRadius * tankHeight);
		double g = surfaceArea * rValue;
		double c = tankVolume * waterDensity * cp;
		double b = waterDensity * 0 * cp;
		double rdash = 1.0 / (b + g);

		double temp = (beginT * Math.exp(-duration * (1.0 / (rdash * c))))
				+ ((g * rdash * rtemp + b * rdash * inputTemp + wattage * rdash) * (1.0 - Math
						.exp(-duration * (1.0 / (rdash * c)))));

		return temp;
	}

	public static double estimatedWaterTempDropTime(double otemp,
			double rtemp, double tankHeight, double tankDiameter,
			double tankVolume, double rValue, double beginT, double targetT) {
		double inputTemp = otemp + Math.random();
		double tankRadius = tankDiameter / 2;
		double surfaceArea = (2 * 3.14 * (tankRadius * tankRadius))
				+ (2 * 3.14 * tankRadius * tankHeight);
		double g = surfaceArea * rValue;
		double c = tankVolume * waterDensity * cp;
		double b = waterDensity * 0 * cp;
		double rdash = 1.0 / (b + g);

		double estTime = -c
				* rdash
				* Math.log((rdash * 0 - targetT + inputTemp * b * rdash + rtemp
						* g * rdash)
						/ (rdash * 0 - beginT + inputTemp * b * rdash + rtemp
								* g * rdash));

		return estTime;
	}

}
