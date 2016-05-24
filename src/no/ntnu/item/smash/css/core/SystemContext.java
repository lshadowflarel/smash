package no.ntnu.item.smash.css.core;

import no.ntnu.item.smash.css.comm.CSSEndPoint;
import no.ntnu.item.smash.css.comm.TimeSynchronizer;
import no.ntnu.item.smash.css.em.EnergyManagement;
import no.ntnu.item.smash.sim.core.SimulationModel;

public class SystemContext {

	private BSCMachine bsc;
	private MONMachine mon;
	private PGCMachine pgc;
	private SDCMachine sdc;
	private CSSEndPoint css;
	private TimeSynchronizer time;
	private SimulationModel house;
	
	public SystemContext() {
		initMachines();
	}
	
	public SystemContext(SimulationModel house) {
		css = new CSSEndPoint(this);
		this.house = house;
		initMachines(); 
	}
	
	private void initMachines() {
		bsc = new BSCMachine(this);
		mon = new MONMachine(this);
		pgc = new PGCMachine(this);
		sdc = new SDCMachine(this);
		time = new TimeSynchronizer();
	}
	
	public BSCMachine getBsc() {
		return bsc;
	}

	public MONMachine getMon() {
		return mon;
	}

	public PGCMachine getPgc() {
		return pgc;
	}

	public SDCMachine getSdc() {
		return sdc;
	}

	public TimeSynchronizer getTime() {
		return time;
	}
	
	public CSSEndPoint getCss() {
		return css;
	}

	public void setCss(CSSEndPoint css) {
		this.css = css;
	}
	
	public SimulationModel getHouse() {
		return house;
	}
	
	public void setThermalProperties(double[] thermalProp) {
		EnergyManagement.thermalProp = thermalProp;
	}
	
}
