package no.ntnu.item.smash.sim.data;

import java.util.ArrayList;

public class JSONResults {

	private ArrayList<Month> months = new ArrayList<Month>();

	public static class Month {
		private double[] energy;
		private double[] cost;
		private double[] otemp;
		private double[] price;

		public double[] getEnergy() {
			return energy;
		}

		public double[] getCost() {
			return cost;
		}

		public double[] getOTemp() {
			return otemp;
		}

		public double[] getPrice() {
			return price;
		}

		public void setEnergy(double[] energy) {
			this.energy = energy;
		}

		public void setCost(double[] cost) {
			this.cost = cost;
		}

		public void setOTemp(double[] otemp) {
			this.otemp = otemp;
		}

		public void setPrice(double[] price) {
			this.price = price;
		}
	}

	public ArrayList<Month> getMonths() {
		return months;
	}

	public void setMonths(ArrayList<Month> months) {
		this.months = months;
	}

}
