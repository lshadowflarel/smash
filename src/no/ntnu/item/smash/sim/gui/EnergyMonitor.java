package no.ntnu.item.smash.sim.gui;


public class EnergyMonitor {
	
	// statistics for the last simulation day
	private double[] hourlyEnergy = new double[24];
	private double[] hourlyOTemp = new double[24];
	private double[] hourlyPrice = new double[24];
	private double[] hourlyCost = new double[24];
	
	public EnergyMonitor() {
		initializeAllData();
	}
	
	public void addEnergyAtHour(int hour, double value) {
		hourlyEnergy[hour] = hourlyEnergy[hour]+value;
	}
	
	public void addOTempAtHour(int hour, double value) {
		if(hourlyOTemp[hour]==9999)
			hourlyOTemp[hour] = value;
	}
	
	public void addPriceAtHour(int hour, double value) {
		if(hourlyPrice[hour]==9999)
			hourlyPrice[hour] =value;
	}
	
	public void addCostAtHour(int hour, double value) {
		hourlyCost[hour] = hourlyCost[hour]+value;
	}
	
	public double[] getEnergyCDF() {
		return convertToCDF(hourlyEnergy);
	}
	
	public double[] getOTempPDF() {
		return hourlyOTemp;
	}
	
	public double[] getPricePDF() {
		return hourlyPrice;
	}
	
	public double[] getCostCDF() {
		return convertToCDF(hourlyCost);
	}
	
	public double getTotalEnergy() {
		return sum(hourlyEnergy);
	}
	
	public double getTotalCost() {
		return sum(hourlyCost);
	}
	
	private double sum(double[] values) {
		double sum = 0;
		for(double val:values) {
			sum+=val;
		}
		
		return sum;
	}
	
	private double[] convertToCDF(double[] pdf) {
		double[] cdf = new double[pdf.length];

		cdf[0] = pdf[0];
		for (int i = 1; i < pdf.length; i++) {
			cdf[i] = cdf[i - 1] + pdf[i];
		}

		return cdf;
	}
	
	private void initializeAllData() {
		initializeData(hourlyPrice);
		initializeData(hourlyOTemp);
	}
	
	private void initializeData(double[] dataArray) {
		for(int i=0; i<dataArray.length; i++) {
			dataArray[i] = 9999;
		}
	}
}
