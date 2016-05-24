package no.ntnu.item.smash.sim.structure;


public class ComfortMeasure {

	private String id;
	private String name;
	private String unit;
	
	public ComfortMeasure() {}
	
	public ComfortMeasure(String id, String name, String unit) {
		this.id = id;
		this.name = name;
		this.unit = unit;
	}

	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}
	
	@Override
	public boolean equals(Object object) {
		
		if (object != null && object instanceof ComfortMeasure)
        {
			if(((ComfortMeasure)object).getId().equals(id)) return true;
        }
		
		return false;
	}
	
}
