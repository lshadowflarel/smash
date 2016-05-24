package no.ntnu.item.smash.sim.data.utility;

import org.apache.commons.math3.distribution.GammaDistribution;
import org.apache.commons.math3.distribution.LogNormalDistribution;

public class MathDistributionUtility {

	public static void main(String[] args) {
		System.out.println("Gamma: " + sampleFromGamma(50, 17, 1));
		System.out.println("Log-Normal: " + sampleFromLognormal(50, 3.0121, 0.6294));
		System.out.println("Uniform: " + sampleFromUniform(50, 50, 100));
	}
	
	// mcRound = # Monte-Carlo rounds
	public static double sampleFromGamma(int mcRound, double shape, double scale) {
		GammaDistribution d = new GammaDistribution(shape, scale);
		
		double accum = 0;
		for(int i=0; i<mcRound; i++) {
			accum += d.sample();
		}
		
		return accum/mcRound;
	}
	
	public static double sampleFromLognormal(int mcRound, double shape, double scale) {
		LogNormalDistribution d = new LogNormalDistribution(shape, scale);
		
		double accum = 0;
		for(int i=0; i<mcRound; i++) {
			accum += d.sample();
		}
		
		return accum/mcRound;
	}
	
	public static double sampleFromUniform(int mcRound, double min, double max) {
		double accum = 0;
		for(int i=0; i<mcRound; i++) {
			accum+= min+(Math.random()*(max-min));
		}
		
		return accum/mcRound;
	}
	
}
