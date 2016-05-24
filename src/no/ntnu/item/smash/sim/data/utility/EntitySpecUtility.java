package no.ntnu.item.smash.sim.data.utility;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class EntitySpecUtility {

	public static Document readSpec(String path) {
		Document doc = null;
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    factory.setIgnoringElementContentWhitespace(true);
	    try {
	        DocumentBuilder builder = factory.newDocumentBuilder();
	        File file = new File(path);
	        doc = builder.parse(file);
	    } catch (ParserConfigurationException e) {
	    	e.printStackTrace();
	    } catch (SAXException e) {
	    	e.printStackTrace();
	    } catch (IOException e) {
	    	e.printStackTrace();
	    }
	    
	    return doc;
	}
	
	public static Document readSpecFromPattern(int id, String folder) {
		return EntitySpecUtility.readSpec("./data/houses/hosuePatterns/" + folder + "/P" + id + ".xml");
	}
	
	public static Document readSpecFromFile(String path) {
		return EntitySpecUtility.readSpec(path);
	}
	
}
