package no.ntnu.item.smash.sim.data.utility;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;

import no.ntnu.item.smash.sim.data.Constants;
import no.ntnu.item.smash.sim.structure.EVLiionBattery;

public class EVUsagePlanUtility {

	// these are no longer used - used in fuzzy trip model
	private static final int HOME = 0;
	private static final int WORK = 1;
	private static final int SHOPPING = 2;
	private static final int RESTAURANT = 3;
	private static final int SPORT = 4;
	private static final int CHARGE = 5;
	private static final int OTHER = 6;

	private static final double[][] weekDaysProb = new double[24][7];
	private static final double[][] weekendsProb = new double[24][7];
	private static final double[][] durationProb = new double[12][7];
	private static final double[][] distances = new double[8][7];

	private static final int latestMin = 1320; // before heading home

	// these are no longer used - used in fuzzy trip model
	static {
		weekDaysProb[0][HOME] = 98;
		weekDaysProb[1][HOME] = 99;
		weekDaysProb[2][HOME] = 99;
		weekDaysProb[3][HOME] = 100;
		weekDaysProb[4][HOME] = 100;
		weekDaysProb[5][HOME] = 100;
		weekDaysProb[6][HOME] = 95;
		weekDaysProb[7][HOME] = 90;
		weekDaysProb[8][HOME] = 3;
		weekDaysProb[9][HOME] = 3;
		weekDaysProb[10][HOME] = 3;
		weekDaysProb[11][HOME] = 3;
		weekDaysProb[12][HOME] = 3;
		weekDaysProb[13][HOME] = 3;
		weekDaysProb[14][HOME] = 3;
		weekDaysProb[15][HOME] = 3;
		weekDaysProb[16][HOME] = 3;
		weekDaysProb[17][HOME] = 3;
		weekDaysProb[18][HOME] = 80;
		weekDaysProb[19][HOME] = 85;
		weekDaysProb[20][HOME] = 85;
		weekDaysProb[21][HOME] = 95;
		weekDaysProb[22][HOME] = 97;
		weekDaysProb[23][HOME] = 98;

		weekDaysProb[0][WORK] = 98;
		weekDaysProb[1][WORK] = 99;
		weekDaysProb[2][WORK] = 99;
		weekDaysProb[3][WORK] = 100;
		weekDaysProb[4][WORK] = 100;
		weekDaysProb[5][WORK] = 100;
		weekDaysProb[6][WORK] = 97;
		weekDaysProb[7][WORK] = 93;
		weekDaysProb[8][WORK] = 99;
		weekDaysProb[9][WORK] = 99;
		weekDaysProb[10][WORK] = 99;
		weekDaysProb[11][WORK] = 99;
		weekDaysProb[12][WORK] = 99;
		weekDaysProb[13][WORK] = 99;
		weekDaysProb[14][WORK] = 99;
		weekDaysProb[15][WORK] = 99;
		weekDaysProb[16][WORK] = 99;
		weekDaysProb[17][WORK] = 99;
		weekDaysProb[18][WORK] = 85;
		weekDaysProb[19][WORK] = 87;
		weekDaysProb[20][WORK] = 86;
		weekDaysProb[21][WORK] = 95;
		weekDaysProb[22][WORK] = 97;
		weekDaysProb[23][WORK] = 98;

		weekDaysProb[0][SHOPPING] = 98;
		weekDaysProb[1][SHOPPING] = 99;
		weekDaysProb[2][SHOPPING] = 99;
		weekDaysProb[3][SHOPPING] = 100;
		weekDaysProb[4][SHOPPING] = 100;
		weekDaysProb[5][SHOPPING] = 100;
		weekDaysProb[6][SHOPPING] = 97;
		weekDaysProb[7][SHOPPING] = 93;
		weekDaysProb[8][SHOPPING] = 99;
		weekDaysProb[9][SHOPPING] = 99;
		weekDaysProb[10][SHOPPING] = 99.5;
		weekDaysProb[11][SHOPPING] = 99.5;
		weekDaysProb[12][SHOPPING] = 99.5;
		weekDaysProb[13][SHOPPING] = 99.5;
		weekDaysProb[14][SHOPPING] = 99.5;
		weekDaysProb[15][SHOPPING] = 99.5;
		weekDaysProb[16][SHOPPING] = 99.5;
		weekDaysProb[17][SHOPPING] = 99.5;
		weekDaysProb[18][SHOPPING] = 90;
		weekDaysProb[19][SHOPPING] = 92;
		weekDaysProb[20][SHOPPING] = 86;
		weekDaysProb[21][SHOPPING] = 95;
		weekDaysProb[22][SHOPPING] = 97;
		weekDaysProb[23][SHOPPING] = 98;

		weekDaysProb[0][RESTAURANT] = 98;
		weekDaysProb[1][RESTAURANT] = 99;
		weekDaysProb[2][RESTAURANT] = 99;
		weekDaysProb[3][RESTAURANT] = 100;
		weekDaysProb[4][RESTAURANT] = 100;
		weekDaysProb[5][RESTAURANT] = 100;
		weekDaysProb[6][RESTAURANT] = 97;
		weekDaysProb[7][RESTAURANT] = 93;
		weekDaysProb[8][RESTAURANT] = 99;
		weekDaysProb[9][RESTAURANT] = 99;
		weekDaysProb[10][RESTAURANT] = 99.5;
		weekDaysProb[11][RESTAURANT] = 99.5;
		weekDaysProb[12][RESTAURANT] = 99.7;
		weekDaysProb[13][RESTAURANT] = 99.7;
		weekDaysProb[14][RESTAURANT] = 99.6;
		weekDaysProb[15][RESTAURANT] = 99.5;
		weekDaysProb[16][RESTAURANT] = 99.5;
		weekDaysProb[17][RESTAURANT] = 99.5;
		weekDaysProb[18][RESTAURANT] = 92;
		weekDaysProb[19][RESTAURANT] = 95;
		weekDaysProb[20][RESTAURANT] = 90;
		weekDaysProb[21][RESTAURANT] = 96;
		weekDaysProb[22][RESTAURANT] = 97;
		weekDaysProb[23][RESTAURANT] = 98;

		weekDaysProb[0][SPORT] = 98;
		weekDaysProb[1][SPORT] = 99;
		weekDaysProb[2][SPORT] = 99;
		weekDaysProb[3][SPORT] = 100;
		weekDaysProb[4][SPORT] = 100;
		weekDaysProb[5][SPORT] = 100;
		weekDaysProb[6][SPORT] = 97;
		weekDaysProb[7][SPORT] = 93.2;
		weekDaysProb[8][SPORT] = 99.2;
		weekDaysProb[9][SPORT] = 99.2;
		weekDaysProb[10][SPORT] = 99.8;
		weekDaysProb[11][SPORT] = 99.7;
		weekDaysProb[12][SPORT] = 99.8;
		weekDaysProb[13][SPORT] = 99.8;
		weekDaysProb[14][SPORT] = 99.7;
		weekDaysProb[15][SPORT] = 99.6;
		weekDaysProb[16][SPORT] = 99.6;
		weekDaysProb[17][SPORT] = 99.6;
		weekDaysProb[18][SPORT] = 94;
		weekDaysProb[19][SPORT] = 96;
		weekDaysProb[20][SPORT] = 90;
		weekDaysProb[21][SPORT] = 96;
		weekDaysProb[22][SPORT] = 97;
		weekDaysProb[23][SPORT] = 98;

		weekDaysProb[0][CHARGE] = 98;
		weekDaysProb[1][CHARGE] = 99;
		weekDaysProb[2][CHARGE] = 99;
		weekDaysProb[3][CHARGE] = 100;
		weekDaysProb[4][CHARGE] = 100;
		weekDaysProb[5][CHARGE] = 100;
		weekDaysProb[6][CHARGE] = 97;
		weekDaysProb[7][CHARGE] = 93.2;
		weekDaysProb[8][CHARGE] = 99.2;
		weekDaysProb[9][CHARGE] = 99.2;
		weekDaysProb[10][CHARGE] = 99.8;
		weekDaysProb[11][CHARGE] = 99.7;
		weekDaysProb[12][CHARGE] = 99.9;
		weekDaysProb[13][CHARGE] = 99.9;
		weekDaysProb[14][CHARGE] = 99.8;
		weekDaysProb[15][CHARGE] = 99.7;
		weekDaysProb[16][CHARGE] = 99.7;
		weekDaysProb[17][CHARGE] = 99.7;
		weekDaysProb[18][CHARGE] = 94.2;
		weekDaysProb[19][CHARGE] = 96.2;
		weekDaysProb[20][CHARGE] = 90;
		weekDaysProb[21][CHARGE] = 96;
		weekDaysProb[22][CHARGE] = 97;
		weekDaysProb[23][CHARGE] = 98;

		weekDaysProb[0][OTHER] = 100;
		weekDaysProb[1][OTHER] = 100;
		weekDaysProb[2][OTHER] = 100;
		weekDaysProb[3][OTHER] = 100;
		weekDaysProb[4][OTHER] = 100;
		weekDaysProb[5][OTHER] = 100;
		weekDaysProb[6][OTHER] = 100;
		weekDaysProb[7][OTHER] = 100;
		weekDaysProb[8][OTHER] = 100;
		weekDaysProb[9][OTHER] = 100;
		weekDaysProb[10][OTHER] = 100;
		weekDaysProb[11][OTHER] = 100;
		weekDaysProb[12][OTHER] = 100;
		weekDaysProb[13][OTHER] = 100;
		weekDaysProb[14][OTHER] = 100;
		weekDaysProb[15][OTHER] = 100;
		weekDaysProb[16][OTHER] = 100;
		weekDaysProb[17][OTHER] = 100;
		weekDaysProb[18][OTHER] = 100;
		weekDaysProb[19][OTHER] = 100;
		weekDaysProb[20][OTHER] = 100;
		weekDaysProb[21][OTHER] = 100;
		weekDaysProb[22][OTHER] = 100;
		weekDaysProb[23][OTHER] = 100;
	}

	static {
		weekendsProb[0][HOME] = 100;
		weekendsProb[1][HOME] = 100;
		weekendsProb[2][HOME] = 100;
		weekendsProb[3][HOME] = 100;
		weekendsProb[4][HOME] = 100;
		weekendsProb[5][HOME] = 100;
		weekendsProb[6][HOME] = 100;
		weekendsProb[7][HOME] = 95;
		weekendsProb[8][HOME] = 95;
		weekendsProb[9][HOME] = 95;
		weekendsProb[10][HOME] = 90;
		weekendsProb[11][HOME] = 90;
		weekendsProb[12][HOME] = 90;
		weekendsProb[13][HOME] = 88;
		weekendsProb[14][HOME] = 85;
		weekendsProb[15][HOME] = 85;
		weekendsProb[16][HOME] = 85;
		weekendsProb[17][HOME] = 85;
		weekendsProb[18][HOME] = 85;
		weekendsProb[19][HOME] = 88;
		weekendsProb[20][HOME] = 90;
		weekendsProb[21][HOME] = 95;
		weekendsProb[22][HOME] = 95;
		weekendsProb[23][HOME] = 100;

		weekendsProb[0][WORK] = 100;
		weekendsProb[1][WORK] = 100;
		weekendsProb[2][WORK] = 100;
		weekendsProb[3][WORK] = 100;
		weekendsProb[4][WORK] = 100;
		weekendsProb[5][WORK] = 100;
		weekendsProb[6][WORK] = 100;
		weekendsProb[7][WORK] = 95;
		weekendsProb[8][WORK] = 95;
		weekendsProb[9][WORK] = 95;
		weekendsProb[10][WORK] = 90;
		weekendsProb[11][WORK] = 90;
		weekendsProb[12][WORK] = 90;
		weekendsProb[13][WORK] = 88;
		weekendsProb[14][WORK] = 85;
		weekendsProb[15][WORK] = 85;
		weekendsProb[16][WORK] = 85;
		weekendsProb[17][WORK] = 85;
		weekendsProb[18][WORK] = 85;
		weekendsProb[19][WORK] = 88;
		weekendsProb[20][WORK] = 90;
		weekendsProb[21][WORK] = 95;
		weekendsProb[22][WORK] = 95;
		weekendsProb[23][WORK] = 100;

		weekendsProb[0][SHOPPING] = 100;
		weekendsProb[1][SHOPPING] = 100;
		weekendsProb[2][SHOPPING] = 100;
		weekendsProb[3][SHOPPING] = 100;
		weekendsProb[4][SHOPPING] = 100;
		weekendsProb[5][SHOPPING] = 100;
		weekendsProb[6][SHOPPING] = 100;
		weekendsProb[7][SHOPPING] = 95;
		weekendsProb[8][SHOPPING] = 95;
		weekendsProb[9][SHOPPING] = 95;
		weekendsProb[10][SHOPPING] = 93;
		weekendsProb[11][SHOPPING] = 92;
		weekendsProb[12][SHOPPING] = 92;
		weekendsProb[13][SHOPPING] = 91;
		weekendsProb[14][SHOPPING] = 90;
		weekendsProb[15][SHOPPING] = 90;
		weekendsProb[16][SHOPPING] = 90;
		weekendsProb[17][SHOPPING] = 88;
		weekendsProb[18][SHOPPING] = 85;
		weekendsProb[19][SHOPPING] = 88;
		weekendsProb[20][SHOPPING] = 90;
		weekendsProb[21][SHOPPING] = 95;
		weekendsProb[22][SHOPPING] = 95;
		weekendsProb[23][SHOPPING] = 100;

		weekendsProb[0][RESTAURANT] = 100;
		weekendsProb[1][RESTAURANT] = 100;
		weekendsProb[2][RESTAURANT] = 100;
		weekendsProb[3][RESTAURANT] = 100;
		weekendsProb[4][RESTAURANT] = 100;
		weekendsProb[5][RESTAURANT] = 100;
		weekendsProb[6][RESTAURANT] = 100;
		weekendsProb[7][RESTAURANT] = 95;
		weekendsProb[8][RESTAURANT] = 95;
		weekendsProb[9][RESTAURANT] = 95;
		weekendsProb[10][RESTAURANT] = 93;
		weekendsProb[11][RESTAURANT] = 92;
		weekendsProb[12][RESTAURANT] = 93;
		weekendsProb[13][RESTAURANT] = 92;
		weekendsProb[14][RESTAURANT] = 91;
		weekendsProb[15][RESTAURANT] = 91;
		weekendsProb[16][RESTAURANT] = 91;
		weekendsProb[17][RESTAURANT] = 93;
		weekendsProb[18][RESTAURANT] = 95;
		weekendsProb[19][RESTAURANT] = 91;
		weekendsProb[20][RESTAURANT] = 94;
		weekendsProb[21][RESTAURANT] = 96;
		weekendsProb[22][RESTAURANT] = 95;
		weekendsProb[23][RESTAURANT] = 100;

		weekendsProb[0][SPORT] = 98;
		weekendsProb[1][SPORT] = 100;
		weekendsProb[2][SPORT] = 100;
		weekendsProb[3][SPORT] = 100;
		weekendsProb[4][SPORT] = 100;
		weekendsProb[5][SPORT] = 100;
		weekendsProb[6][SPORT] = 100;
		weekendsProb[7][SPORT] = 96;
		weekendsProb[8][SPORT] = 97;
		weekendsProb[9][SPORT] = 97;
		weekendsProb[10][SPORT] = 94;
		weekendsProb[11][SPORT] = 93;
		weekendsProb[12][SPORT] = 93;
		weekendsProb[13][SPORT] = 93;
		weekendsProb[14][SPORT] = 92;
		weekendsProb[15][SPORT] = 92;
		weekendsProb[16][SPORT] = 92;
		weekendsProb[17][SPORT] = 93;
		weekendsProb[18][SPORT] = 95;
		weekendsProb[19][SPORT] = 91;
		weekendsProb[20][SPORT] = 94;
		weekendsProb[21][SPORT] = 96;
		weekendsProb[22][SPORT] = 95;
		weekendsProb[23][SPORT] = 100;

		weekendsProb[0][CHARGE] = 100;
		weekendsProb[1][CHARGE] = 100;
		weekendsProb[2][CHARGE] = 100;
		weekendsProb[3][CHARGE] = 100;
		weekendsProb[4][CHARGE] = 100;
		weekendsProb[5][CHARGE] = 100;
		weekendsProb[6][CHARGE] = 100;
		weekendsProb[7][CHARGE] = 96;
		weekendsProb[8][CHARGE] = 97;
		weekendsProb[9][CHARGE] = 97;
		weekendsProb[10][CHARGE] = 94;
		weekendsProb[11][CHARGE] = 93;
		weekendsProb[12][CHARGE] = 93;
		weekendsProb[13][CHARGE] = 94;
		weekendsProb[14][CHARGE] = 93;
		weekendsProb[15][CHARGE] = 93;
		weekendsProb[16][CHARGE] = 93;
		weekendsProb[17][CHARGE] = 94;
		weekendsProb[18][CHARGE] = 95;
		weekendsProb[19][CHARGE] = 91;
		weekendsProb[20][CHARGE] = 94;
		weekendsProb[21][CHARGE] = 96;
		weekendsProb[22][CHARGE] = 95;
		weekendsProb[23][CHARGE] = 100;

		weekendsProb[0][OTHER] = 100;
		weekendsProb[1][OTHER] = 100;
		weekendsProb[2][OTHER] = 100;
		weekendsProb[3][OTHER] = 100;
		weekendsProb[4][OTHER] = 100;
		weekendsProb[5][OTHER] = 100;
		weekendsProb[6][OTHER] = 100;
		weekendsProb[7][OTHER] = 100;
		weekendsProb[8][OTHER] = 100;
		weekendsProb[9][OTHER] = 100;
		weekendsProb[10][OTHER] = 100;
		weekendsProb[11][OTHER] = 100;
		weekendsProb[12][OTHER] = 100;
		weekendsProb[13][OTHER] = 100;
		weekendsProb[14][OTHER] = 100;
		weekendsProb[15][OTHER] = 100;
		weekendsProb[16][OTHER] = 100;
		weekendsProb[17][OTHER] = 100;
		weekendsProb[18][OTHER] = 100;
		weekendsProb[19][OTHER] = 100;
		weekendsProb[20][OTHER] = 100;
		weekendsProb[21][OTHER] = 100;
		weekendsProb[22][OTHER] = 100;
		weekendsProb[23][OTHER] = 100;
	}

	static {
		durationProb[0][HOME] = 20;
		durationProb[1][HOME] = 70;
		durationProb[2][HOME] = 90;
		durationProb[3][HOME] = 95;
		durationProb[4][HOME] = 100;
		durationProb[5][HOME] = 100;
		durationProb[6][HOME] = 100;
		durationProb[7][HOME] = 100;
		durationProb[8][HOME] = 100;
		durationProb[9][HOME] = 100;
		durationProb[10][HOME] = 100;
		durationProb[11][HOME] = 100;

		durationProb[0][WORK] = 0;
		durationProb[1][WORK] = 0;
		durationProb[2][WORK] = 0;
		durationProb[3][WORK] = 2;
		durationProb[4][WORK] = 6;
		durationProb[5][WORK] = 8;
		durationProb[6][WORK] = 10;
		durationProb[7][WORK] = 97;
		durationProb[8][WORK] = 98;
		durationProb[9][WORK] = 99;
		durationProb[10][WORK] = 100;
		durationProb[11][WORK] = 100;

		durationProb[0][SHOPPING] = 40;
		durationProb[1][SHOPPING] = 90;
		durationProb[2][SHOPPING] = 97;
		durationProb[3][SHOPPING] = 99;
		durationProb[4][SHOPPING] = 100;
		durationProb[5][SHOPPING] = 100;
		durationProb[6][SHOPPING] = 100;
		durationProb[7][SHOPPING] = 100;
		durationProb[8][SHOPPING] = 100;
		durationProb[9][SHOPPING] = 100;
		durationProb[10][SHOPPING] = 100;
		durationProb[11][SHOPPING] = 100;

		durationProb[0][RESTAURANT] = 10;
		durationProb[1][RESTAURANT] = 80;
		durationProb[2][RESTAURANT] = 95;
		durationProb[3][RESTAURANT] = 100;
		durationProb[4][RESTAURANT] = 100;
		durationProb[5][RESTAURANT] = 100;
		durationProb[6][RESTAURANT] = 100;
		durationProb[7][RESTAURANT] = 100;
		durationProb[8][RESTAURANT] = 100;
		durationProb[9][RESTAURANT] = 100;
		durationProb[10][RESTAURANT] = 100;
		durationProb[11][RESTAURANT] = 100;

		durationProb[0][SPORT] = 88;
		durationProb[1][SPORT] = 98;
		durationProb[2][SPORT] = 100;
		durationProb[3][SPORT] = 100;
		durationProb[4][SPORT] = 100;
		durationProb[5][SPORT] = 100;
		durationProb[6][SPORT] = 100;
		durationProb[7][SPORT] = 100;
		durationProb[8][SPORT] = 100;
		durationProb[9][SPORT] = 100;
		durationProb[10][SPORT] = 100;
		durationProb[11][SPORT] = 100;

		durationProb[0][CHARGE] = 82;
		durationProb[1][CHARGE] = 92;
		durationProb[2][CHARGE] = 98;
		durationProb[3][CHARGE] = 100;
		durationProb[4][CHARGE] = 100;
		durationProb[5][CHARGE] = 100;
		durationProb[6][CHARGE] = 100;
		durationProb[7][CHARGE] = 100;
		durationProb[8][CHARGE] = 100;
		durationProb[9][CHARGE] = 100;
		durationProb[10][CHARGE] = 100;
		durationProb[11][CHARGE] = 100;

		durationProb[0][OTHER] = 80;
		durationProb[1][OTHER] = 90;
		durationProb[2][OTHER] = 97;
		durationProb[3][OTHER] = 99;
		durationProb[4][OTHER] = 100;
		durationProb[5][OTHER] = 100;
		durationProb[6][OTHER] = 100;
		durationProb[7][OTHER] = 100;
		durationProb[8][OTHER] = 100;
		durationProb[9][OTHER] = 100;
		durationProb[10][OTHER] = 100;
		durationProb[11][OTHER] = 100;
	}

	static {
		distances[HOME][HOME] = 0;
		distances[HOME][WORK] = 5.5+5;
		distances[HOME][SHOPPING] = 7+5;
		distances[HOME][RESTAURANT] = 4.5+5;
		distances[HOME][SPORT] = 5+5;
		distances[HOME][CHARGE] = 4+5;
		distances[WORK][WORK] = 0;
		distances[WORK][HOME] = 5.5+5;
		distances[WORK][SHOPPING] = 3+5;
		distances[WORK][RESTAURANT] = 2+5;
		distances[WORK][SPORT] = 1+5;
		distances[WORK][CHARGE] = 2.5+5;
		distances[SHOPPING][SHOPPING] = 0;
		distances[SHOPPING][HOME] = 7+5;
		distances[SHOPPING][WORK] = 3+5;
		distances[SHOPPING][RESTAURANT] = 4+5;
		distances[SHOPPING][SPORT] = 2+5;
		distances[SHOPPING][CHARGE] = 3+5;
		distances[RESTAURANT][RESTAURANT] = 0;
		distances[RESTAURANT][HOME] = 4.5+5;
		distances[RESTAURANT][WORK] = 2+5;
		distances[RESTAURANT][SHOPPING] = 4+5;
		distances[RESTAURANT][SPORT] = 1+5;
		distances[RESTAURANT][CHARGE] = 1+5;
		distances[SPORT][SPORT] = 0;
		distances[SPORT][HOME] = 5+5;
		distances[SPORT][WORK] = 1+5;
		distances[SPORT][SHOPPING] = 2+5;
		distances[SPORT][RESTAURANT] = 1+5;
		distances[SPORT][CHARGE] = 2+5;
		distances[CHARGE][CHARGE] = 0;
		distances[CHARGE][HOME] = 4+5;
		distances[CHARGE][WORK] = 2.5+5;
		distances[CHARGE][SHOPPING] = 3+5;
		distances[CHARGE][RESTAURANT] = 1+5;
		distances[CHARGE][SPORT] = 2+5;
	}

	public static void main(String[] args) {
		if(args.length<5) System.out.println("Needs 5 arguments: month (integer), year (integer), number of houses, battery capacity (kWh), max driving range (km)");
		
		try{
			int month = Integer.parseInt(args[0]);
			int year = Integer.parseInt(args[1]);
			int numHouses = Integer.parseInt(args[2]);
			int kwh = Integer.parseInt(args[3]);
			int range = Integer.parseInt(args[4]);
			EVLiionBattery battery = new EVLiionBattery();
			battery.setkWh(kwh);
			battery.setMaxRange(range);
			
			for(int i=0; i<numHouses; i++) {
				writeEVDrivePattern(month, year, "ev-charge-"+i+".txt", battery);
			}
		} catch(Exception e) {
			System.out.println("Failed to parse arguments. Check the formats.");
		}
	}

	
	/*
	 * Monte-Carlo + Log-Normal distribution
	 */
	public static void writeEVDrivePattern(int month, int year, String fileName, EVLiionBattery battery) {
		String path = "data/plan/" + fileName; // charge requirements
		File file = new File(path);
		
		if(file.exists()) file.delete();
		try {
			file.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		FileWriter fw = null;
		try {
			fw = new FileWriter(file, true);
		} catch (IOException e) {
			e.printStackTrace();
		}
		PrintWriter pw = new PrintWriter(fw);
		
		String toWrite = "";
		for(int i=0; i<Constants.getNumDaysInMonth(month, year); i++) {
			toWrite = "[";
			
			// random distance
			double distance = MathDistributionUtility.sampleFromLognormal(25, 3.6, 0.6294); //adapted from paper#17 (Methodology for optimization of power systems demand due to EV charging load
			if(battery.getWatt()==11000) distance = MathDistributionUtility.sampleFromLognormal(25, 4.2, 0.6294);			
			
			// calculate used SoC
			double usedSoC = (distance*1.1)/battery.getMaxRange()*100; // hack for 0.9 battery efficiency
			
			// random earliest start hour
			double startMin = MathDistributionUtility.sampleFromGamma(25, 18, 1)*60;
			
			// calculate min until departure
			double rand = Math.random();
			double minLeft = (1440 + (rand>0.33?(rand>0.67?480:420):360)) - startMin;
			
			toWrite += distance + "," + usedSoC + "," + startMin + "," + minLeft; 
			
			toWrite += "]";
			pw.println(toWrite);
		}
		pw.close();
	}
	
	/*
	 * With fuzzy trip model
	 */
	public static void writeEVDrivePattern(boolean wipe,
			ArrayList<ArrayList<double[]>> pattern, String fileName, String fileName2, EVLiionBattery battery) {
		String path = "data/plan/" + fileName; // driving pattern
		String path2 = "data/plan/" + fileName2; // charge requirements
		
		File file = new File(path);
		File file2 = new File(path2);
		
		// if file doesnt exists, then create it
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if (wipe) {
			try {
				file.delete();
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		if (!file2.exists()) {
			try {
				file2.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if (wipe) {
			try {
				file2.delete();
				file2.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		FileWriter fw = null;
		try {
			fw = new FileWriter(file, true);
		} catch (IOException e) {
			e.printStackTrace();
		}
		PrintWriter pw = new PrintWriter(fw);
		
		FileWriter fw2 = null;
		try {
			fw2 = new FileWriter(file2, true);
		} catch (IOException e) {
			e.printStackTrace();
		}
		PrintWriter pw2 = new PrintWriter(fw2);

		String toWrite = "";
		String toWrite2 = "";

		double totalDistance = 0;
		double arriveHome = 0;
		double maxChargeMin = 0;
		double wattRecovered = 0;
		
		for (int i = 0; i < pattern.size(); i++) {
			totalDistance = 0;
			
			ArrayList<double[]> daily = pattern.get(i);
			for (int j = 0; j < daily.size(); j++) {
				double[] trips = daily.get(j);

				double origin = trips[0];
				double destination = trips[1];
				double startMin = trips[2];
				double endMin = trips[3];
				double driveTime = trips[4];
				double distance = trips[5];
				double kWhGained = trips[6];

				toWrite += "[" + origin + "," + destination + "," + startMin
						+ "," + endMin + "," + driveTime + "," + distance + ","
						+ kWhGained + "];";
				
				totalDistance += distance;
				wattRecovered += kWhGained;
				if(j==daily.size()-1) { 
					arriveHome = startMin + driveTime; 
					maxChargeMin = (endMin<arriveHome?endMin + 1440 - arriveHome:endMin - arriveHome); 
				}
			}

			toWrite2 += "[" + totalDistance + "," + EVLiionBattery.getReducedSoCAfterDistance(battery.getkWh(), battery.getMaxRange(), totalDistance, wattRecovered) + "," + arriveHome + "," + maxChargeMin +"];";
			
			totalDistance = 0;
			arriveHome = 0;
			maxChargeMin = 0;
			wattRecovered = 0;
			
			pw.println(toWrite);
			pw2.println(toWrite2);
			toWrite = "";
			toWrite2 = "";
		}

		pw.close();
		pw2.close();
	}

	public static ArrayList<ArrayList<double[]>> generateEVDrivePattern(
			int numDays, int startDay, int startMonth, int startYear) {
		ArrayList<ArrayList<double[]>> pattern = new ArrayList<ArrayList<double[]>>();
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DAY_OF_MONTH, startDay);
		cal.set(Calendar.MONTH, startMonth);
		cal.set(Calendar.YEAR, startYear);

		for (int i = 0; i < numDays; i++) {
			pattern.add(generateDailyEVDrivePattern(startDay++, startMonth,
					startYear, 480));

			cal.add(Calendar.DAY_OF_MONTH, 1);
			startDay = cal.get(Calendar.DAY_OF_MONTH);
			startMonth = cal.get(Calendar.MONTH);
			startYear = cal.get(Calendar.YEAR);
		}

		return pattern;
	}

	public static ArrayList<double[]> generateDailyEVDrivePattern(int day,
			int month, int year, int startMin) {

		int dayType = Constants.getDayType(day, month, year);
		//System.out.println("DAY " + day + " (" + dayType + ")");
		int origin = HOME;
		
		double totalDistance = 0;

		ArrayList<double[]> dailyPlan = new ArrayList<double[]>();
		double[] trip = new double[7];

		double[][] destinationProb = null;

		if (dayType != 1 && dayType != 7) { // week days
			destinationProb = weekDaysProb;
		} else { // weekends
			destinationProb = weekendsProb;
		}

		boolean moreTrip = true; // before heading home

		while (moreTrip) {
			// random what will be done
			double r = Math.random() * 100;

			// determine the hour
			int hour = startMin / 60;

			for (int i = 0; i < 7; i++) {
				if (r <= destinationProb[hour][i]) { // doing that

					if (i == origin || i == HOME) {
						break;
					}

					// get/random distance + calculate drive duration
					double distance = 0;

					if (i < 6 && origin < 6) {
						distance = distances[origin][i];
					} else {
						distance = (int)(Math.random() * 20) + 5;
					}

					int driveDuration = (int) distance * 60 / 40;

					// random duration
					int duration = 0;
					double r2 = Math.random() * 100;

					for (int j = 0; j < 12; j++) {
						if (r2 < durationProb[j][i]) { // that long
							duration = (j + 1) * 60;
							break;
						}
					}

					int endMin = startMin + driveDuration + duration;

					if (i == SHOPPING) {
						endMin = Math.min(1200, endMin);
						duration = endMin - startMin - driveDuration;
					}

					if (endMin >= latestMin) {
						moreTrip = false;
						break;
					}

//					System.out.println("Hour: "
//							+ (hour < 10 ? "0" + hour : hour)
//							+ ":"
//							+ (startMin % 60 < 10 ? "0" + (startMin % 60)
//									: (startMin % 60))
//							+ " - "
//							+ (endMin / 60 < 10 ? "0" + (endMin / 60)
//									: (endMin / 60))
//							+ ":"
//							+ (endMin % 60 < 10 ? "0" + (endMin % 60)
//									: (endMin % 60)) + " "
//							+ resolvePlaceName(i) + " [distance=" + distance
//							+ ", drive time=" + driveDuration
//							+ ", total duration=" + duration + "].");

					trip[0] = origin;
					trip[1] = i;
					trip[2] = startMin;
					trip[3] = endMin;
					trip[4] = driveDuration;
					trip[5] = distance;
					trip[6] = (i == CHARGE ? 1000 : 100);

					totalDistance += distance;
					dailyPlan.add(trip);

					// will there be more trips?
					double r4 = Math.random();
					if (dailyPlan.size() < 4 && r4 > 0.85)
						moreTrip = false;
					else if (dailyPlan.size() >= 4 && r4 > 0.2)
						moreTrip = false;

					double r3 = Math.random();
					if (endMin <= 1020) {
						if (r3 > 0.85)
							moreTrip = false;
					} else if (endMin <= 1140) {
						if (r3 > 0.6)
							moreTrip = false;
					} else {
						if (r3 > 0.1)
							moreTrip = false;
					}

					// update origin and start minute
					origin = i;
					startMin = endMin;

					break;
				}
			}
		}

		// end the day with a home trip

		// get/random distance + calculate drive duration
		double distance = 0;

		if (origin < 6) {
			distance = distances[origin][HOME];
		} else {
			distance = (int)(Math.random() * 15) + 5;
		}

		int driveDuration = (int) distance * 60 / 40;

		int duration = ((23 + 8) * 60) - startMin - driveDuration;
		int endMin = startMin + driveDuration + duration;

		if (endMin > 23 * 60)
			endMin = endMin - (23 * 60);

//		System.out
//				.println("Hour: "
//						+ (startMin / 60 < 10 ? "0" + (startMin / 60)
//								: (startMin / 60))
//						+ ":"
//						+ (startMin % 60 < 10 ? "0" + (startMin % 60)
//								: (startMin % 60))
//						+ " - "
//						+ (endMin / 60 < 10 ? "0" + (endMin / 60)
//								: (endMin / 60))
//						+ ":"
//						+ (endMin % 60 < 10 ? "0" + (endMin % 60)
//								: (endMin % 60)) + " HOME [distance="
//						+ distance + ", drive time=" + driveDuration
//						+ ", total duration=" + duration + "].");

		totalDistance += distance;
		
		trip[0] = origin;
		trip[1] = HOME;
		trip[2] = startMin;
		trip[3] = endMin;
		trip[4] = driveDuration;
		trip[5] = distance;
		trip[6] = -1;

		dailyPlan.add(trip);

//		System.out.println("Total distance: " + totalDistance);
//		System.out.println();
		
		return dailyPlan;
	}

	private static String resolvePlaceName(int code) {
		switch (code) {
		case HOME:
			return "HOME";
		case WORK:
			return "WORK";
		case SHOPPING:
			return "SHOPPING";
		case RESTAURANT:
			return "RESTAURANT";
		case SPORT:
			return "SPORT";
		case CHARGE:
			return "CHARGING STATION";
		case OTHER:
			return "UNKNOWN";
		default:
			return "";
		}
	}

}
