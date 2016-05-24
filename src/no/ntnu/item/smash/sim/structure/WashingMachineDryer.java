package no.ntnu.item.smash.sim.structure;

import java.util.HashMap;

public class WashingMachineDryer extends Device{

	private double maxWashKg = 5;
	private double maxDryKg = 2.5;
	private double[] spinSpeeds = {0,500,600,700,800,900,1000,1100,1200,1300,1400};
	private double[] washTemps = {30,40,60,90};
	
	protected class WashingProgram {
		protected String name;
		protected double spinSpeed;
		protected double washTemp;
		protected double duration;
		protected double[] stageWatts;
		protected double[] stageTime;
		
		protected WashingProgram(String name, double spinSpeed, double washTemp, double[] stageWatts, double[] stageTime, double duration) {
			this.name = name;
			this.spinSpeed = spinSpeed;
			this.washTemp = washTemp;
			this.stageWatts = stageWatts;
			this.stageTime = stageTime;
			this.duration = duration;
		}
		
		protected double getSpinSpeed() { return spinSpeed; }
		protected double getWashTemp() { return washTemp; }
		protected double[] getStageWatts() { return stageWatts; }
		protected double getAvgPower() {
			double totalWatt = 0, totalDuration=0;
			for(int i=0; i<stageTime.length; i++) {
				totalWatt += (stageWatts[i]*stageTime[i]);
				totalDuration += stageTime[i];
			}
			
			return totalWatt/totalDuration;
		}
	}
	
	protected class DryingProgram {
		protected String name;
		protected double tumbleSpeed;
		protected double[] stageWatts;
		protected double[] stageTime;
		protected double duration;
		
		protected DryingProgram(String name, double tumbleSpeed, double[] stageWatts, double[] stageTime, double duration) {
			this.name = name;
			this.tumbleSpeed = tumbleSpeed;
			this.stageWatts = stageWatts;
			this.stageTime = stageTime;
			this.duration = duration;
		}
		
		protected double getTumbleSpeed() { return tumbleSpeed; }
		protected double[] getStageWatts() { return stageWatts; }
		protected double getAvgPower() {
			double totalWatt = 0, totalDuration=0;
			for(int i=0; i<stageTime.length; i++) {
				totalWatt += (stageWatts[i]*stageTime[i]);
				totalDuration += stageTime[i];
			}
			
			return totalWatt/totalDuration;
		}
	}
	
	private HashMap<Integer,WashingProgram> washPrograms = new HashMap<Integer,WashingProgram>();
	private HashMap<Integer,DryingProgram> dryPrograms = new HashMap<Integer,DryingProgram>();
	
	/** Constructor **/
	public WashingMachineDryer() {
		washPrograms.put(new Integer(1), new WashingProgram("mix",1400,40,new double[]{40,2000,250,48,250,48,350,20,350,20,500,10},new double[]{2,10,15,3,15,3,12,1,12,1,5,1}, 80));
		washPrograms.put(new Integer(2), new WashingProgram("wool",1000,30,new double[]{40,1600,200,48,200,48,250,20,300,10},new double[]{2,10,8,5,8,5,3,2,3,2}, 50));
		dryPrograms.put(new Integer(2), new DryingProgram("damp",1,new double[]{1700,700,1700,700,1700,400},new double[]{19,1,19,1,19,1}, 60));
		dryPrograms.put(new Integer(3), new DryingProgram("dry",2,new double[]{1700,500,1700,500,1700,500,1700,500,1700,200},new double[]{19,1,19,1,19,1,19,1,19,1}, 100));
	}
	
	/** Methods **/
	public double getMaxWashKg() {
		return maxWashKg;
	}
	
	public double getMaxDryKg() {
		return maxDryKg;
	}
	
	public double[] getPossibleSpinSpeeds() {
		return spinSpeeds;
	}
	
	public double[] getPossibleWashTemps() {
		return washTemps;
	}
	
	public HashMap<Integer,WashingProgram> getWashPrograms() {
		return washPrograms;
	}
	
	public HashMap<Integer,DryingProgram> getDryingPrograms() {
		return dryPrograms;
	}
	
	public double getWWattFromProgramAndTime(Integer prog, double second) {
		WashingProgram program = washPrograms.get(prog);
		int time = 0;
		for(int i=0; i<program.stageTime.length; i++) {
			time += program.stageTime[i]*60;
			if(second<=time) return program.stageWatts[i];
		}
		
		return 0;
	}
	
	public double getDWattFromProgramAndTime(Integer prog, double second) {
		DryingProgram program = dryPrograms.get(prog);
		int time = 0;
		for(int i=0; i<program.stageTime.length; i++) {
			time += program.stageTime[i]*60;
			if(second<=time) return program.stageWatts[i];
		}
		
		return 0;
	}
	
	public double getWashDurationFromProgram(Integer prog) {
		return washPrograms.get(prog).duration;
	}
	
	public double getDryDurationFromProgram(Integer prog) {
		return dryPrograms.get(prog).duration;
	}
	
	public double getAvgWattFromWProgram(Integer prog) {
		return washPrograms.get(prog).getAvgPower();
	}
	
	public double getAvgWattFromDProgram(Integer prog) {
		return dryPrograms.get(prog).getAvgPower();
	}
	
}
