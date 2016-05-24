package no.ntnu.item.smash.css.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;

import net.sf.xet.nxet.data.InMemoryObjects;
import net.sf.xet.nxet.executor.Executor;
import no.ntnu.item.smash.css.parser.XMLParser;
import no.ntnu.item.smash.css.role.Reasoner;
import no.ntnu.item.smash.css.structure.ActionSet;
import no.ntnu.item.smash.css.structure.ComfortMeasure;
import no.ntnu.item.smash.css.structure.EWHSchedulingAction;
import no.ntnu.item.smash.css.structure.Entity;
import no.ntnu.item.smash.css.structure.GenericSchedulingAction;
import no.ntnu.item.smash.css.structure.RoomSchedulingAction;
import no.ntnu.item.smash.css.structure.FeedbackAction;
import no.ntnu.item.smash.css.structure.TriggerPolicyAction;
import no.ntnu.item.smash.sim.core.ComplexSimulation;
import no.ntnu.item.smash.sim.data.utility.SimEnvironmentGenerator;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class NxETReasoner implements Reasoner {

	public static double totalCost = 0;
	private Executor executor;
	private SystemContext context;

	public NxETReasoner(SystemContext context) {
		this.context = context;
	}

	@Override
	public synchronized ActionSet processPolicy(String policyID,
			String policyFilePath, HashMap<String, Object> data) {
		File policyFile = new File(policyFilePath);

		// get NxET query file
		String queryFile = policyFile.getParent() + "\\q_"
				+ policyFile.getName();

		// get policy (NxET rule) file
		String ruleFile = policyFilePath;

		InMemoryObjects memoryObject = new InMemoryObjects();

		// get NxET datasource file
		String dataSourceFile = getDataFilePath(policyFilePath);
		ConsoleHandler handler = new ConsoleHandler();
		handler.setLevel(Level.OFF);
		//if(context.getHouse().getModelID()==18 && context.getTime().day()==8 && policyFilePath.equals("Y:/FinalPaper/CLC100/60/multi/policies19/p_clcResponse.xet.xml")) handler.setLevel(Level.INFO);
		executor = new Executor(queryFile, ruleFile, dataSourceFile, null,
				handler, "Y:/Workspaces/SMASH/SMASH");
		executor.setParent(this);
		executor.setInputs(data);
		executor.execute();
		logAnswers(executor.answers());
		memoryObject.getInMemoryObject().clear();
		return constructActionSet(executor.cleanAnswers()[0]);
	}

	private void logAnswers(String answer) {
		FileOutputStream fop = null;
		File f;

		try {
			f = new File(SimEnvironmentGenerator.EXECUTION_PATH + "/log/log.log");
			// if file doesnt exists, then create it
			if (!f.exists()) {
				f.createNewFile();
			}

			fop = new FileOutputStream(f, true);

			// get the content in bytes
			fop.write(answer.getBytes());

			fop.flush();
			fop.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private ActionSet constructActionSet(String answer) {
		ActionSet actions = new ActionSet();

		double costCost = 0, comfortCost = 0;

		Document doc = XMLParser.getDOMFromXMLString(answer);
		NodeList actionList = doc.getElementsByTagName("smash:Action");
		for (int i = 0; i < actionList.getLength(); i++) {
			Element actionNode = (Element) actionList.item(i);
			String actionType = actionNode.getAttribute("smash:type");
			if (actionType.equals("RoomSchedulingAction")) {
				DateFormat df = new SimpleDateFormat(
						"EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
				Date startTime = null;
				try {
					startTime = df.parse(actionNode
							.getElementsByTagName("smash:start").item(0)
							.getTextContent());
				} catch (DOMException e) {
					e.printStackTrace();
					continue;
				} catch (ParseException e) {
					e.printStackTrace();
					continue;
				}

				RoomSchedulingAction ra = new RoomSchedulingAction();
				ra.setComfortMeasure(new ComfortMeasure());
				ra.setRoom(new Entity(actionNode
						.getElementsByTagName("smash:Entity").item(0)
						.getTextContent(), actionNode
						.getElementsByTagName("smash:Entity").item(0)
						.getTextContent()));
				ra.setSchedulingTime(startTime);
				ra.setDuration((int) Math.round(Double.parseDouble(actionNode
						.getElementsByTagName("smash:duration").item(0)
						.getTextContent())));
				ra.setTargetValue((int) Math.round(Double
						.parseDouble(actionNode
								.getElementsByTagName("smash:newTemp").item(0)
								.getTextContent())));
				actions.add(ra);
			} else if (actionType.equals("EWHSchedulingAction")) {
				DateFormat df = new SimpleDateFormat(
						"EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
				Date startTime = null;
				try {
					startTime = df.parse(actionNode
							.getElementsByTagName("smash:start").item(0)
							.getTextContent());
				} catch (DOMException e) {
					e.printStackTrace();
					continue;
				} catch (ParseException e) {
					e.printStackTrace();
					continue;
				}

				EWHSchedulingAction ewha = new EWHSchedulingAction();
				ewha.setRoom(new Entity(actionNode
						.getElementsByTagName("smash:Entity").item(0)
						.getTextContent(), actionNode
						.getElementsByTagName("smash:Entity").item(0)
						.getTextContent()));
				ewha.setSchedulingTime(startTime);
				ewha.setDuration(Integer.parseInt(actionNode
						.getElementsByTagName("smash:duration").item(0)
						.getTextContent()));
				ewha.setTargetValue((int) Math.round(Double
						.parseDouble(actionNode
								.getElementsByTagName("smash:newTemp").item(0)
								.getTextContent())));
				actions.add(ewha);
			} else if (actionType.equals("SchedulingAction")) {
				DateFormat df = new SimpleDateFormat(
						"EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
				Date startTime = null;
				try {
					startTime = df.parse(actionNode
							.getElementsByTagName("smash:start").item(0)
							.getTextContent());
				} catch (DOMException e) {
					e.printStackTrace();
					continue;
				} catch (ParseException e) {
					e.printStackTrace();
					continue;
				}

				GenericSchedulingAction a = new GenericSchedulingAction();
				a.setEntity(new Entity(actionNode
						.getElementsByTagName("smash:Entity").item(0)
						.getTextContent(), actionNode
						.getElementsByTagName("smash:Entity").item(0)
						.getTextContent()));
				a.setDeviceType(Integer.parseInt(actionNode
						.getElementsByTagName("smash:deviceType").item(0)
						.getTextContent()));
				if (actionNode.getElementsByTagName("smash:taskID").getLength() > 0) {
					a.setTaskID(Integer.parseInt(actionNode
							.getElementsByTagName("smash:taskID").item(0)
							.getTextContent()));
				} else {
					a.setTaskID(1);
				}
				a.setSchedulingTime(startTime);
				a.setDuration((int) Double.parseDouble(actionNode
						.getElementsByTagName("smash:duration").item(0)
						.getTextContent()));
				if (actionNode.getElementsByTagName("smash:prog").getLength() > 0) {
					a.setTargetValue((int) Math.round(Double
							.parseDouble(actionNode
									.getElementsByTagName("smash:prog").item(0)
									.getTextContent())));
				} else {
					a.setTargetValue(0);
				}

				actions.add(a);
			} else if (actionType.equals("TriggerPolicy")) {
				TriggerPolicyAction a = new TriggerPolicyAction();
				a.setTriggerType(Integer.parseInt(actionNode
						.getElementsByTagName("smash:triggerType").item(0)
						.getTextContent()));
				HashMap<String, Object> data = new HashMap<String, Object>();
				NodeList nl = actionNode.getChildNodes();
				for (int n = 0; n < nl.getLength(); n++) {
					if (nl.item(n) instanceof Element
							&& ((Element) nl.item(n)).getNodeName().startsWith(
									"smash:data_")) {
						String nodeName = ((Element) nl.item(n)).getNodeName()
								.replace("smash:data_", "");
						data.put(nodeName,
								((Element) nl.item(n)).getTextContent());
					}
				}
				a.setData(data);
				actions.add(a);
			} else if (actionType.equals("FeedbackAction")) {
				FeedbackAction a = new FeedbackAction();
				a.setId(actionNode.getElementsByTagName("smash:id").item(0)
						.getTextContent());
				NodeList nl = actionNode.getChildNodes();
				for (int n = 0; n < nl.getLength(); n++) {
					if (nl.item(n) instanceof Element
							&& ((Element) nl.item(n)).getNodeName().startsWith(
									"smash:fb_")) {
						String nodeName = ((Element) nl.item(n)).getNodeName()
								.replace("smash:fb_", "");
						a.addFeedback(nodeName,
								((Element) nl.item(n)).getTextContent());
					}
				}
				actions.add(a);
			}

			costCost += Double.parseDouble(((Element) actionNode
					.getElementsByTagName("smash:cost").item(0))
					.getAttribute("smash:cost_cost"));
			comfortCost += Double.parseDouble(((Element) actionNode
					.getElementsByTagName("smash:cost").item(0))
					.getAttribute("smash:cost_comfort"));
		}

		totalCost += Math.round(costCost * 100.00) / 100.00;

		// System.out.println("Estimated \"cost\" cost = " +
		// Math.round(costCost*100.00)/100.00 + " / \"comfort\" cost = " +
		// Math.round(comfortCost*100.00)/100.00);

		return actions;
	}

	/*
	 * Parse the rule file (with added data source file path) to obtain the
	 * datasource file path
	 */
	private String getDataFilePath(String policyFilePath) {
		File policyFile = new File(policyFilePath);
		Document doc = XMLParser.getDOMFromFile(policyFilePath);

		if (doc != null)
			return policyFile.getParentFile()
					+ "\\"
					+ doc.getElementsByTagName("xet:Data").item(0)
							.getTextContent();

		return null;
	}

	public SystemContext getCSS() {
		return context;
	}
}
