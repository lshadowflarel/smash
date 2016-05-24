package no.ntnu.item.smash.sim.data.utility;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Random;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import no.ntnu.item.smash.css.structure.DeviceStartTrigger;
import no.ntnu.item.smash.css.structure.ExternalEventTrigger;
import no.ntnu.item.smash.css.structure.GenericEventTrigger;
import no.ntnu.item.smash.css.structure.PriceTrigger;
import no.ntnu.item.smash.css.structure.TimeTrigger;
import no.ntnu.item.smash.css.structure.Trigger;
import no.ntnu.item.smash.sim.core.ComplexSimulation;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class CSSConfigUtility {
	
	private static String[] policies = {"p_ev", "p_clcResponse", "p_highPriceTrigger", "p_powerLimit", "p_wm"};
	private static String[] triggerTypes = {"device_start", "externalEvent", "priceNextInt", "powerLimit", "device_start"};
	private static String[] triggerConditions = {"equal", "equal", "greaterthan", "greaterthan", "equal"};
	private static String[] triggerValues = {"4", "DSO", "1", "15000", "3"};
	
	public static void main(String[] args) {
//		CSSConfigUtility.printRandomHousesWithPolicies(30, 15);
//		CSSConfigUtility.generateCSSConfigs(30);
		CSSConfigUtility.copyPolicyToHouses("Y:/FinalPaper/CLC/60/multi","p_clcResponse.xet.xml", 0, new int[]{1,3,5,7,9,11,13,15,17
				,19,21,23,25,27,29,31,33,35,37,39,41,43,45,47,49});
		CSSConfigUtility.copyPolicyToHouses("Y:/FinalPaper/CLC/60/multi","p_clcResponse.xet.xml", 0, new int[]{2,4,6,8,10,12,14,16,18
				,20,22,24,26,28,30,32,34,36,38,40,42,44,46,48});
//		CSSConfigUtility.copyPolicyToHouses("Y:/FinalPaper/CLC/20/multi","p_highPriceTrigger.xet.xml", 0, new int[]{23,12,1,3,8,18,14,19,15,20,11,24,4,16});
	}
	
	public static void printRandomHousesWithPolicies(int numHouse, int numPolicies) {
		Random generator = new Random(165);
		ArrayList<Integer> pol = new ArrayList<Integer>();
		
		while(pol.size()<numPolicies) {
			int h = generator.nextInt(numHouse);
			if(!pol.contains(h)) {
				pol.add(h);
				System.out.println(h);
			}
		}
	}
	
	public static void generateCSSConfigs(String folderPath, int numHouse) {
		Random generator = new Random(23045); 
		
		for(int i=0; i<numHouse; i++) {
			
			try {
				DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		 
				// root elements
				Document doc = docBuilder.newDocument();
				Element rootElement = doc.createElement("CSSConfig");
				doc.appendChild(rootElement);
				
				// random whether CSS will be enabled
				boolean cssEnabled = true;
				if(generator.nextInt(10)>4)  cssEnabled = true;
				System.out.println(cssEnabled);
				Element enabledElem = doc.createElement("Enabled");
				enabledElem.setTextContent(cssEnabled?"1":"0");
				rootElement.appendChild(enabledElem);
				
				Element policiesElem = doc.createElement("Policies");
				rootElement.appendChild(policiesElem);
				
				// random the policies to be activated
				for(int j=0; j<policies.length; j++) {
					Element policyElem = doc.createElement("Policy");
					policyElem.setAttribute("name", policies[j]);
					Element activatedElem = doc.createElement("Activated");
					
					// will this policy be activated?
					activatedElem.setTextContent(generator.nextInt(10)>2?"1":"1");
					
					// random the goal of this policy
					Element goalElem = doc.createElement("Goal");
					goalElem.setTextContent(generator.nextInt(10)>4?"cost":"comfort");
					
					Element triggerElem = doc.createElement("Trigger");
					Element triggerTypeElem = doc.createElement("TriggerType");
					triggerTypeElem.setTextContent(triggerTypes[j]);
					Element triggerCondElem = doc.createElement("TriggerCondition");
					triggerCondElem.setTextContent(triggerConditions[j]);
					Element triggerValueElem = doc.createElement("TriggerValue");
					triggerValueElem.setTextContent(triggerValues[j]);
					
					triggerElem.appendChild(triggerTypeElem);
					triggerElem.appendChild(triggerCondElem);
					triggerElem.appendChild(triggerValueElem);
					
					policyElem.appendChild(activatedElem);
					policyElem.appendChild(goalElem);
					policyElem.appendChild(triggerElem);
					policiesElem.appendChild(policyElem);
				}
				
				TransformerFactory transformerFactory = TransformerFactory.newInstance();
				Transformer transformer = transformerFactory.newTransformer();
				transformer.setOutputProperty(OutputKeys.INDENT, "yes");
				transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
				DOMSource source = new DOMSource(doc);
				StreamResult result = new StreamResult(new File(folderPath + "/policies" + (i+1) + "/css.cfg"));
				transformer.transform(source, result);
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			} catch (TransformerConfigurationException e) {
				e.printStackTrace();
			} catch (TransformerException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static ArrayList<Object[]> readCSSConfigForHouse(String folderPath, int houseID) {
		ArrayList<Object[]> config = new ArrayList<Object[]>();
		
		String path = folderPath + "/policies" + (houseID+1) + "/css.cfg";
		Document doc = null;
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    factory.setIgnoringElementContentWhitespace(true);
	    try {
	        DocumentBuilder builder = factory.newDocumentBuilder();
	        File file = new File(path);
	        doc = builder.parse(file);
	        doc.getDocumentElement().normalize();
	        
	        if(doc.getElementsByTagName("Enabled").item(0).getTextContent().equals("0")) {
	        	return config;
	        }
	        
	        NodeList policyElems = doc.getElementsByTagName("Policy");
	        for (int i = 0; i < policyElems.getLength(); i++) {
	        	Element policyElem = (Element)policyElems.item(i);
	        	
	        	Object[] cfg = new Object[4];
	        	cfg[0] = policyElem.getAttribute("name");
	        	cfg[1] = ((Element)policyElem.getElementsByTagName("Goal").item(0)).getTextContent();
	        	Element triggerElem = (Element)policyElem.getElementsByTagName("Trigger").item(0);
	        	String triggerType = ((Element)triggerElem.getElementsByTagName("TriggerType").item(0)).getTextContent();
	        	String triggerCondition = ((Element)triggerElem.getElementsByTagName("TriggerCondition").item(0)).getTextContent();
	        	String triggerValue = ((Element)triggerElem.getElementsByTagName("TriggerValue").item(0)).getTextContent();
	        	Trigger trigger = null;	        	
	        	
	        	int cond = -1;
	        	if(triggerCondition.equals("equal")) {
	        		cond = Trigger.COND_EQUALTO;
	        	} else if(triggerCondition.equals("greaterthan")) {
	        		cond = Trigger.COND_GREATERTHAN;
	        	} else if(triggerCondition.equals("lessthan")) {
	        		cond = Trigger.COND_LESSTHAN;
	        	} else if(triggerCondition.equals("notequal")) {
	        		cond = Trigger.COND_NOTEQUALTO;
	        	} else if(triggerCondition.equals("greaterthanorequal")) {
	        		cond = Trigger.COND_GREATERTHANEQ;
	        	} else if(triggerCondition.equals("lessthanorequal")) {
	        		cond = Trigger.COND_LESSTHANEQ;
	        	}
	        	
	        	if(triggerType.equals("time")) {
	        		trigger = new TimeTrigger(Trigger.TYPE_TIME, cond, Integer.parseInt(triggerValue));
	        	} else if(triggerType.equals("device_start")) {
	        		trigger = new DeviceStartTrigger(Trigger.TYPE_DEVICE_START, cond,
							Integer.parseInt(triggerValue));
	        	} else if(triggerType.equals("price")) {
	        		trigger = new PriceTrigger(Trigger.TYPE_PRICE, cond, Double.parseDouble(triggerValue));
	        	} else if(triggerType.equals("esp-price")) {
	        		trigger = new PriceTrigger(Trigger.TYPE_PRICE_ESP, cond, Double.parseDouble(triggerValue));
	        	} else if(triggerType.equals("dso-price")) {
	        		trigger = new PriceTrigger(Trigger.TYPE_PRICE_DSO, cond, Double.parseDouble(triggerValue));
	        	} else if(triggerType.equals("priceNextInt")) {
	        		trigger = new PriceTrigger(Trigger.TYPE_PRICE_NEXTINT, cond, Double.parseDouble(triggerValue));
	        	} else if(triggerType.equals("esp-priceNextInt")) {
	        		trigger = new PriceTrigger(Trigger.TYPE_PRICE_ESP_NEXTINT, cond, Double.parseDouble(triggerValue));
	        	} else if(triggerType.equals("dso-priceNextInt")) {
	        		trigger = new PriceTrigger(Trigger.TYPE_PRICE_DSO_NEXTINT, cond, Double.parseDouble(triggerValue));
	        	} else if(triggerType.equals("powerLimit")) {
	        		trigger = new GenericEventTrigger(Trigger.TYPE_POWERLIMIT, cond, Double.parseDouble(triggerValue));
	        	} else if(triggerType.equals("externalEvent")) {
	        		trigger = new ExternalEventTrigger(Trigger.TYPE_EXTERNAL_EVENT, cond, triggerValue);
	        	}
	        	cfg[2] = trigger;
	        	cfg[3] = Integer.parseInt(((Element)policyElem.getElementsByTagName("Activated").item(0)).getTextContent());
	        	config.add(cfg);
	        }
	    } catch (ParserConfigurationException e) {
	    	e.printStackTrace();
	    } catch (SAXException e) {
	    	e.printStackTrace();
	    } catch (IOException e) {
	    	e.printStackTrace();
	    }		
		
		return config;
	}
	
	public static void copyPolicyToHouses(String folderPath, String policyFileName, int fromID, int[] toIDs) {
		InputStream inStream = null;
		OutputStream outStream = null;
		File res = new File(folderPath + "/policies" + (fromID+1) + "/" + policyFileName);
		
		for(int id=0; id<toIDs.length; id++) { 
			try {
				byte[] buffer = new byte[1024];
				File copied = new File(folderPath + "/policies" + (toIDs[id]+1)
						+ "/" + policyFileName);
				inStream = new FileInputStream(res);
				outStream = new FileOutputStream(copied);

				int length;
				while ((length = inStream.read(buffer)) > 0) {
					outStream.write(buffer, 0, length);
				}

				inStream.close();
				outStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
}
