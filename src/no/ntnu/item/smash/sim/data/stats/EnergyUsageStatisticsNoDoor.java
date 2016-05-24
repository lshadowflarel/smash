package no.ntnu.item.smash.sim.data.stats;

import no.ntnu.item.smash.sim.structure.BuildingThermalIntegrity;

public class EnergyUsageStatisticsNoDoor {

	public static double[] thermalProp = BuildingThermalIntegrity.NORWAY_HEAVYINSULATED;
	
	// Energy consumption HEURISTICS - estimated energy consumption required for one room
	// to maintain a setpoint temperature for 1 hour with regards to outdoor temperature
	public static final double[][] energyToMaintain_LivingRoom = new double[6][12];
	public static final double[][] energyToMaintain_Kitchen = new double[6][12];
	public static final double[][] energyToMaintain_Bathroom1 = new double[6][12];
	public static final double[][] energyToMaintain_Bathroom2 = new double[6][12];
	
	static {
		// 1st index - 0=27,1=25,2=23,3=21,4=19,5=17
		// 2nd index - 0=-35,1=-30,2=-25,3=-20,4=-15,5=-10,6=-5,7=0,8=5,9=10,10=15,11=20	
		energyToMaintain_LivingRoom[0][0] = 0.75;
		energyToMaintain_LivingRoom[0][1] = 0.6;
		energyToMaintain_LivingRoom[0][2] = 0.55;
		energyToMaintain_LivingRoom[0][3] = 0.51;
		energyToMaintain_LivingRoom[0][4] = 0.44;
		energyToMaintain_LivingRoom[0][5] = 0.36;
		energyToMaintain_LivingRoom[0][6] = 0.27;
		energyToMaintain_LivingRoom[0][7] = 0.23;
		energyToMaintain_LivingRoom[0][8] = 0.16;
		energyToMaintain_LivingRoom[0][9] = 0.1;
		energyToMaintain_LivingRoom[0][10] = 0.04;
		energyToMaintain_LivingRoom[0][11] = 0;
		energyToMaintain_LivingRoom[1][0] = 0.73;
		energyToMaintain_LivingRoom[1][1] = 0.57;
		energyToMaintain_LivingRoom[1][2] = 0.55;
		energyToMaintain_LivingRoom[1][3] = 0.48;
		energyToMaintain_LivingRoom[1][4] = 0.43;
		energyToMaintain_LivingRoom[1][5] = 0.33;
		energyToMaintain_LivingRoom[1][6] = 0.26;
		energyToMaintain_LivingRoom[1][7] = 0.20;
		energyToMaintain_LivingRoom[1][8] = 0.14;
		energyToMaintain_LivingRoom[1][9] = 0.08;
		energyToMaintain_LivingRoom[1][10] = 0.02;
		energyToMaintain_LivingRoom[1][11] = 0;
		energyToMaintain_LivingRoom[2][0] = 0.67;
		energyToMaintain_LivingRoom[2][1] = 0.57;
		energyToMaintain_LivingRoom[2][2] = 0.54;
		energyToMaintain_LivingRoom[2][3] = 0.45;
		energyToMaintain_LivingRoom[2][4] = 0.38;
		energyToMaintain_LivingRoom[2][5] = 0.3;
		energyToMaintain_LivingRoom[2][6] = 0.24;
		energyToMaintain_LivingRoom[2][7] = 0.16;
		energyToMaintain_LivingRoom[2][8] = 0.12;
		energyToMaintain_LivingRoom[2][9] = 0.05;
		energyToMaintain_LivingRoom[2][10] = 0;
		energyToMaintain_LivingRoom[2][11] = 0;		
		energyToMaintain_LivingRoom[3][0] = 0.64;
		energyToMaintain_LivingRoom[3][1] = 0.55;
		energyToMaintain_LivingRoom[3][2] = 0.51;
		energyToMaintain_LivingRoom[3][3] = 0.44;
		energyToMaintain_LivingRoom[3][4] = 0.35;
		energyToMaintain_LivingRoom[3][5] = 0.27;
		energyToMaintain_LivingRoom[3][6] = 0.22;
		energyToMaintain_LivingRoom[3][7] = 0.14;
		energyToMaintain_LivingRoom[3][8] = 0.1;
		energyToMaintain_LivingRoom[3][9] = 0.02;
		energyToMaintain_LivingRoom[3][10] = 0;
		energyToMaintain_LivingRoom[3][11] = 0;
		energyToMaintain_LivingRoom[4][0] = 0.6;
		energyToMaintain_LivingRoom[4][1] = 0.52;
		energyToMaintain_LivingRoom[4][2] = 0.49;
		energyToMaintain_LivingRoom[4][3] = 0.4;
		energyToMaintain_LivingRoom[4][4] = 0.32;
		energyToMaintain_LivingRoom[4][5] = 0.24;
		energyToMaintain_LivingRoom[4][6] = 0.2;
		energyToMaintain_LivingRoom[4][7] = 0.12;
		energyToMaintain_LivingRoom[4][8] = 0.06;
		energyToMaintain_LivingRoom[4][9] = 0;
		energyToMaintain_LivingRoom[4][10] = 0;
		energyToMaintain_LivingRoom[4][11] = 0;
		energyToMaintain_LivingRoom[5][0] = 0.59;
		energyToMaintain_LivingRoom[5][1] = 0.52;
		energyToMaintain_LivingRoom[5][2] = 0.45;
		energyToMaintain_LivingRoom[5][3] = 0.37;
		energyToMaintain_LivingRoom[5][4] = 0.27;
		energyToMaintain_LivingRoom[5][5] = 0.22;
		energyToMaintain_LivingRoom[5][6] = 0.16;
		energyToMaintain_LivingRoom[5][7] = 0.1;
		energyToMaintain_LivingRoom[5][8] = 0.04;
		energyToMaintain_LivingRoom[5][9] = 0;
		energyToMaintain_LivingRoom[5][10] = 0;
		energyToMaintain_LivingRoom[5][11] = 0;
	}
	
	static {
		// 1st index - 0=27,1=25,2=23,3=21,4=19,5=17
		// 2nd index - 0=-35,1=-30,2=-25,3=-20,4=-15,5=-10,6=-5,7=0,8=5,9=10,10=15,11=20	
		energyToMaintain_Kitchen[0][0] = 0.47;
		energyToMaintain_Kitchen[0][1] = 0.42;
		energyToMaintain_Kitchen[0][2] = 0.34;
		energyToMaintain_Kitchen[0][3] = 0.33;
		energyToMaintain_Kitchen[0][4] = 0.3;
		energyToMaintain_Kitchen[0][5] = 0.24;
		energyToMaintain_Kitchen[0][6] = 0.22;
		energyToMaintain_Kitchen[0][7] = 0.16;
		energyToMaintain_Kitchen[0][8] = 0.12;
		energyToMaintain_Kitchen[0][9] = 0.05;
		energyToMaintain_Kitchen[0][10] = 0;
		energyToMaintain_Kitchen[0][11] = 0.04;
		energyToMaintain_Kitchen[1][0] = 0.47;
		energyToMaintain_Kitchen[1][1] = 0.38;
		energyToMaintain_Kitchen[1][2] = 0.32;
		energyToMaintain_Kitchen[1][3] = 0.30;
		energyToMaintain_Kitchen[1][4] = 0.27;
		energyToMaintain_Kitchen[1][5] = 0.24;
		energyToMaintain_Kitchen[1][6] = 0.2;
		energyToMaintain_Kitchen[1][7] = 0.16;
		energyToMaintain_Kitchen[1][8] = 0.1;
		energyToMaintain_Kitchen[1][9] = 0.03;
		energyToMaintain_Kitchen[1][10] = 0;
		energyToMaintain_Kitchen[1][11] = 0;
		energyToMaintain_Kitchen[2][0] = 0.45;
		energyToMaintain_Kitchen[2][1] = 0.34;
		energyToMaintain_Kitchen[2][2] = 0.34;
		energyToMaintain_Kitchen[2][3] = 0.3;
		energyToMaintain_Kitchen[2][4] = 0.26;
		energyToMaintain_Kitchen[2][5] = 0.22;
		energyToMaintain_Kitchen[2][6] = 0.18;
		energyToMaintain_Kitchen[2][7] = 0.13;
		energyToMaintain_Kitchen[2][8] = 0.08;
		energyToMaintain_Kitchen[2][9] = 0.02;
		energyToMaintain_Kitchen[2][10] = 0;
		energyToMaintain_Kitchen[2][11] = 0;		
		energyToMaintain_Kitchen[3][0] = 0.42;
		energyToMaintain_Kitchen[3][1] = 0.37;
		energyToMaintain_Kitchen[3][2] = 0.3;
		energyToMaintain_Kitchen[3][3] = 0.28;
		energyToMaintain_Kitchen[3][4] = 0.24;
		energyToMaintain_Kitchen[3][5] = 0.22;
		energyToMaintain_Kitchen[3][6] = 0.17;
		energyToMaintain_Kitchen[3][7] = 0.11;
		energyToMaintain_Kitchen[3][8] = 0.06;
		energyToMaintain_Kitchen[3][9] = 0;
		energyToMaintain_Kitchen[3][10] = 0;
		energyToMaintain_Kitchen[3][11] = 0;
		energyToMaintain_Kitchen[4][0] = 0.4;
		energyToMaintain_Kitchen[4][1] = 0.33;
		energyToMaintain_Kitchen[4][2] = 0.3;
		energyToMaintain_Kitchen[4][3] = 0.26;
		energyToMaintain_Kitchen[4][4] = 0.21;
		energyToMaintain_Kitchen[4][5] = 0.18;
		energyToMaintain_Kitchen[4][6] = 0.14;
		energyToMaintain_Kitchen[4][7] = 0.08;
		energyToMaintain_Kitchen[4][8] = 0.06;
		energyToMaintain_Kitchen[4][9] = 0;
		energyToMaintain_Kitchen[4][10] = 0;
		energyToMaintain_Kitchen[4][11] = 0;
		energyToMaintain_Kitchen[5][0] = 0.37;
		energyToMaintain_Kitchen[5][1] = 0.31;
		energyToMaintain_Kitchen[5][2] = 0.3;
		energyToMaintain_Kitchen[5][3] = 0.24;
		energyToMaintain_Kitchen[5][4] = 0.22;
		energyToMaintain_Kitchen[5][5] = 0.17;
		energyToMaintain_Kitchen[5][6] = 0.12;
		energyToMaintain_Kitchen[5][7] = 0.05;
		energyToMaintain_Kitchen[5][8] = 0.02;
		energyToMaintain_Kitchen[5][9] = 0;
		energyToMaintain_Kitchen[5][10] = 0;
		energyToMaintain_Kitchen[5][11] = 0;
	}
	
	static {
		// 1st index - 0=27,1=25,2=23,3=21,4=19,5=17
		// 2nd index - 0=-35,1=-30,2=-25,3=-20,4=-15,5=-10,6=-5,7=0,8=5,9=10,10=15,11=20	
		energyToMaintain_Bathroom1[0][0] = 0.43;
		energyToMaintain_Bathroom1[0][1] = 0.39;
		energyToMaintain_Bathroom1[0][2] = 0.34;
		energyToMaintain_Bathroom1[0][3] = 0.31;
		energyToMaintain_Bathroom1[0][4] = 0.27;
		energyToMaintain_Bathroom1[0][5] = 0.22;
		energyToMaintain_Bathroom1[0][6] = 0.18;
		energyToMaintain_Bathroom1[0][7] = 0.14;
		energyToMaintain_Bathroom1[0][8] = 0.1;
		energyToMaintain_Bathroom1[0][9] = 0.04;
		energyToMaintain_Bathroom1[0][10] = 0.02;
		energyToMaintain_Bathroom1[0][11] = 0;
		energyToMaintain_Bathroom1[1][0] = 0.42;
		energyToMaintain_Bathroom1[1][1] = 0.37;
		energyToMaintain_Bathroom1[1][2] = 0.32;
		energyToMaintain_Bathroom1[1][3] = 0.29;
		energyToMaintain_Bathroom1[1][4] = 0.24;
		energyToMaintain_Bathroom1[1][5] = 0.22;
		energyToMaintain_Bathroom1[1][6] = 0.16;
		energyToMaintain_Bathroom1[1][7] = 0.12;
		energyToMaintain_Bathroom1[1][8] = 0.08;
		energyToMaintain_Bathroom1[1][9] = 0.03;
		energyToMaintain_Bathroom1[1][10] = 0;
		energyToMaintain_Bathroom1[1][11] = 0;
		energyToMaintain_Bathroom1[2][0] = 0.41;
		energyToMaintain_Bathroom1[2][1] = 0.35;
		energyToMaintain_Bathroom1[2][2] = 0.32;
		energyToMaintain_Bathroom1[2][3] = 0.26;
		energyToMaintain_Bathroom1[2][4] = 0.24;
		energyToMaintain_Bathroom1[2][5] = 0.2;
		energyToMaintain_Bathroom1[2][6] = 0.16;
		energyToMaintain_Bathroom1[2][7] = 0.1;
		energyToMaintain_Bathroom1[2][8] = 0.07;
		energyToMaintain_Bathroom1[2][9] = 0;
		energyToMaintain_Bathroom1[2][10] = 0;
		energyToMaintain_Bathroom1[2][11] = 0;		
		energyToMaintain_Bathroom1[3][0] = 0.39;
		energyToMaintain_Bathroom1[3][1] = 0.31;
		energyToMaintain_Bathroom1[3][2] = 0.3;
		energyToMaintain_Bathroom1[3][3] = 0.24;
		energyToMaintain_Bathroom1[3][4] = 0.22;
		energyToMaintain_Bathroom1[3][5] = 0.18;
		energyToMaintain_Bathroom1[3][6] = 0.14;
		energyToMaintain_Bathroom1[3][7] = 0.1;
		energyToMaintain_Bathroom1[3][8] = 0.06;
		energyToMaintain_Bathroom1[3][9] = 0.01;
		energyToMaintain_Bathroom1[3][10] = 0;
		energyToMaintain_Bathroom1[3][11] = 0;
		energyToMaintain_Bathroom1[4][0] = 0.38;
		energyToMaintain_Bathroom1[4][1] = 0.29;
		energyToMaintain_Bathroom1[4][2] = 0.28;
		energyToMaintain_Bathroom1[4][3] = 0.24;
		energyToMaintain_Bathroom1[4][4] = 0.2;
		energyToMaintain_Bathroom1[4][5] = 0.16;
		energyToMaintain_Bathroom1[4][6] = 0.11;
		energyToMaintain_Bathroom1[4][7] = 0.08;
		energyToMaintain_Bathroom1[4][8] = 0.04;
		energyToMaintain_Bathroom1[4][9] = 0;
		energyToMaintain_Bathroom1[4][10] = 0;
		energyToMaintain_Bathroom1[4][11] = 0;
		energyToMaintain_Bathroom1[5][0] = 0.37;
		energyToMaintain_Bathroom1[5][1] = 0.31;
		energyToMaintain_Bathroom1[5][2] = 0.3;
		energyToMaintain_Bathroom1[5][3] = 0.24;
		energyToMaintain_Bathroom1[5][4] = 0.22;
		energyToMaintain_Bathroom1[5][5] = 0.17;
		energyToMaintain_Bathroom1[5][6] = 0.12;
		energyToMaintain_Bathroom1[5][7] = 0.05;
		energyToMaintain_Bathroom1[5][8] = 0.02;
		energyToMaintain_Bathroom1[5][9] = 0;
		energyToMaintain_Bathroom1[5][10] = 0;
		energyToMaintain_Bathroom1[5][11] = 0;
	}
	
	static {
		// 1st index - 0=27,1=25,2=23,3=21,4=19,5=17
		// 2nd index - 0=-35,1=-30,2=-25,3=-20,4=-15,5=-10,6=-5,7=0,8=5,9=10,10=15,11=20	
		energyToMaintain_Bathroom2[0][0] = 0.29;
		energyToMaintain_Bathroom2[0][1] = 0.28;
		energyToMaintain_Bathroom2[0][2] = 0.19;
		energyToMaintain_Bathroom2[0][3] = 0.14;
		energyToMaintain_Bathroom2[0][4] = 0.12;
		energyToMaintain_Bathroom2[0][5] = 0.11;
		energyToMaintain_Bathroom2[0][6] = 0.08;
		energyToMaintain_Bathroom2[0][7] = 0.05;
		energyToMaintain_Bathroom2[0][8] = 0.03;
		energyToMaintain_Bathroom2[0][9] = 0.01;
		energyToMaintain_Bathroom2[0][10] = 0;
		energyToMaintain_Bathroom2[0][11] = 0;
		energyToMaintain_Bathroom2[1][0] = 0.3;
		energyToMaintain_Bathroom2[1][1] = 0.26;
		energyToMaintain_Bathroom2[1][2] = 0.17;
		energyToMaintain_Bathroom2[1][3] = 0.23;
		energyToMaintain_Bathroom2[1][4] = 0.11;
		energyToMaintain_Bathroom2[1][5] = 0.09;
		energyToMaintain_Bathroom2[1][6] = 0.07;
		energyToMaintain_Bathroom2[1][7] = 0.04;
		energyToMaintain_Bathroom2[1][8] = 0.03;
		energyToMaintain_Bathroom2[1][9] = 0;
		energyToMaintain_Bathroom2[1][10] = 0;
		energyToMaintain_Bathroom2[1][11] = 0;
		energyToMaintain_Bathroom2[2][0] = 0.28;
		energyToMaintain_Bathroom2[2][1] = 0.19;
		energyToMaintain_Bathroom2[2][2] = 0.17;
		energyToMaintain_Bathroom2[2][3] = 0.12;
		energyToMaintain_Bathroom2[2][4] = 0.11;
		energyToMaintain_Bathroom2[2][5] = 0.08;
		energyToMaintain_Bathroom2[2][6] = 0.07;
		energyToMaintain_Bathroom2[2][7] = 0.04;
		energyToMaintain_Bathroom2[2][8] = 0.03;
		energyToMaintain_Bathroom2[2][9] = 0;
		energyToMaintain_Bathroom2[2][10] = 0;
		energyToMaintain_Bathroom2[2][11] = 0;		
		energyToMaintain_Bathroom2[3][0] = 0.28;
		energyToMaintain_Bathroom2[3][1] = 0.16;
		energyToMaintain_Bathroom2[3][2] = 0.14;
		energyToMaintain_Bathroom2[3][3] = 0.12;
		energyToMaintain_Bathroom2[3][4] = 0.1;
		energyToMaintain_Bathroom2[3][5] = 0.09;
		energyToMaintain_Bathroom2[3][6] = 0.05;
		energyToMaintain_Bathroom2[3][7] = 0.03;
		energyToMaintain_Bathroom2[3][8] = 0.02;
		energyToMaintain_Bathroom2[3][9] = 0;
		energyToMaintain_Bathroom2[3][10] = 0;
		energyToMaintain_Bathroom2[3][11] = 0;
		energyToMaintain_Bathroom2[4][0] = 0.28;
		energyToMaintain_Bathroom2[4][1] = 0.14;
		energyToMaintain_Bathroom2[4][2] = 0.12;
		energyToMaintain_Bathroom2[4][3] = 0.1;
		energyToMaintain_Bathroom2[4][4] = 0.09;
		energyToMaintain_Bathroom2[4][5] = 0.07;
		energyToMaintain_Bathroom2[4][6] = 0.05;
		energyToMaintain_Bathroom2[4][7] = 0.02;
		energyToMaintain_Bathroom2[4][8] = 0;
		energyToMaintain_Bathroom2[4][9] = 0;
		energyToMaintain_Bathroom2[4][10] = 0;
		energyToMaintain_Bathroom2[4][11] = 0;
		energyToMaintain_Bathroom2[5][0] = 0.23;
		energyToMaintain_Bathroom2[5][1] = 0.14;
		energyToMaintain_Bathroom2[5][2] = 0.12;
		energyToMaintain_Bathroom2[5][3] = 0.1;
		energyToMaintain_Bathroom2[5][4] = 0.08;
		energyToMaintain_Bathroom2[5][5] = 0.06;
		energyToMaintain_Bathroom2[5][6] = 0.03;
		energyToMaintain_Bathroom2[5][7] = 0.02;
		energyToMaintain_Bathroom2[5][8] = 0;
		energyToMaintain_Bathroom2[5][9] = 0;
		energyToMaintain_Bathroom2[5][10] = 0;
		energyToMaintain_Bathroom2[5][11] = 0;
	}
		
	public static double getEstEnergyToMaintain(int otemp, int temp, String entityID) {
		double[][] energyToMaintain = null;
		if(entityID.equals("LivingRoom")) energyToMaintain = energyToMaintain_LivingRoom;
		else if(entityID.equals("Kitchen")) energyToMaintain = energyToMaintain_Kitchen;
		else if(entityID.equals("Bathroom1")) energyToMaintain = energyToMaintain_Bathroom1;
		else if(entityID.equals("Bathroom2")) energyToMaintain = energyToMaintain_Bathroom2;
		
		int startOtemp = (otemp/5)*5;
		int refTemp = temp%2==1?temp:temp+1;
		
		int index1 = 0, index2 = 0;
		switch(refTemp) {
		case 27:
			index1 = 0;
			break;
		case 25: 
			index1 = 1;
			break;
		case 23:
			index1 = 2;
			break;
		case 21:
			index1 = 3;
			break;
		case 19:
			index1 = 4;
			break;
		case 17:
			index1 = 5;
			break;
		}
		
		switch(startOtemp) {
		case -35:
			index2 = 0;
			break;
		case -30:
			index2 = 1;
			break;
		case -25:
			index2 = 2;
			break;
		case -20:
			index2 = 3;
			break;
		case -15:
			index2 = 4;
			break;
		case -10:
			index2 = 5;
			break;
		case -5:
			index2 = 6;
			break;
		case 0:
			index2 = 7;
			break;
		case 5: 
			index2 = 8;
			break;
		case 6:
			index2 = 9;
			break;
		case 7:
			index2 = 10;
			break;
		case 8:
			index2 = 11;
			break;
		}
		
		double kwhStart = energyToMaintain[index1][index2];
		double kwhNext = energyToMaintain[index1][index2<0?index2-1:index2+1];
		
		double step = (kwhNext-kwhStart)/5.0;
		
		double estimatedE = kwhStart + ((otemp%5)*step);
		
		return thermalProp==BuildingThermalIntegrity.NORWAY_HEAVYINSULATED?estimatedE:1.15*estimatedE; // 1.15
	}	
	
}
