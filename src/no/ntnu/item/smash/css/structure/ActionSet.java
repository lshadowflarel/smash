package no.ntnu.item.smash.css.structure;

import java.util.ArrayList;

public class ActionSet {

	ArrayList<DecidedAction> actions;
	
	public ActionSet() {
		actions = new ArrayList<DecidedAction>();
	}
	
	public void add(DecidedAction action) {
		actions.add(action);
	}
	
	public ArrayList<DecidedAction> getActions() {
		return actions;
	}
}
