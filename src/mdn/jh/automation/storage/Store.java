package mdn.jh.automation.storage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.sql.ResultSet;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Level;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import mdn.jh.automation.Main;
import mdn.jh.automation.SmartHomeHandler;
import mdn.jh.automation.device.Device;
import mdn.jh.automation.io.DataConnectionIF;
import mdn.jh.automation.io.DataInputIF;
import mdn.jh.automation.io.logic.LogicBase;

public class Store {

	static String storeFile = "settings.xml";

	// Store for connections when loading from xml file
	private static Vector<Connection> connectionsStore = null;

	public static void addConnection(DataConnectionIF source, int targetID, int targetInputID) {

		if (connectionsStore == null) {
			connectionsStore = new Vector<Connection>();
		}

		connectionsStore.add(new Connection(source, targetID, targetInputID));
	}

	public static boolean loadFromXML(SmartHomeHandler smartHomeHandler) throws Exception {
		Document doc = null;

		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
			DocumentBuilder db = dbf.newDocumentBuilder();
			doc = db.parse(new File(storeFile));
			doc.getDocumentElement().normalize();
			Main.getLogger().log(Level.FINE, "Root Element :" + doc.getDocumentElement().getNodeName());
		} catch (Exception e) {

			// TODO: handle exception
		}
		if (doc == null) {
			return false;
		}

		NodeList nodeList = doc.getFirstChild().getChildNodes();
		Node node = null;
		for (int i = 0; i < nodeList.getLength(); i++) {
			node = nodeList.item(i);
			Main.getLogger().log(Level.FINE, "Load:" + node.getNodeName());

			if ("Device".equals(node.getNodeName())) {

				Node deviceClass = node.getAttributes().getNamedItem("class");
				if (deviceClass == null) {
					throw new Exception("Device - class attribute not set");
				}
				String cl = deviceClass.getNodeValue();
				Class<?> clazz = Class.forName(cl);
				Constructor<?> constructor = clazz.getConstructor();
				Object instance = constructor.newInstance();
				Device dp = (Device) instance;
				dp.initDataComponent(node);
				smartHomeHandler.registerDevice(dp);
			}

			

			if ("DataProcessors".equals(node.getNodeName())) {
				Node temp = null;
				NodeList dataProcessorsList = node.getChildNodes();

				for (int j = 0; j < dataProcessorsList.getLength(); j++) {
					temp = dataProcessorsList.item(j);
					if ("LogicBase".equals(temp.getNodeName())) {

						Node converterClass = temp.getAttributes().getNamedItem("class");
						if (converterClass == null) {
							throw new Exception("LogicBase attribute not set");
						}
						String cl = converterClass.getNodeValue();
						Class<?> clazz = Class.forName(cl);
						Constructor<?> constructor = clazz.getConstructor();
						Object instance = constructor.newInstance();
						LogicBase dp = (LogicBase) instance;
						dp.initDataComponent(temp);
						smartHomeHandler.registerNewDataProcessor(dp);
					}
				}

			}
		}

		if (connectionsStore != null) {
			Iterator<Connection> it = connectionsStore.iterator();
			Connection temp = null;
			while (it.hasNext()) {
				temp = it.next();
				DataInputIF targetDIF = smartHomeHandler.getDataInputInterface(temp.getTarget());
				if (targetDIF == null) {
					Main.getLogger().log(Level.SEVERE, "Failed to load connection for ID:" + temp.getTarget());
				} else {

					temp.getSource().addInputConnection(targetDIF, temp.getTargetInputID());

				}
			}
		}

		Main.getLogger().log(Level.INFO, "Load from XML File completed:" + storeFile);

		return true;
	}

	public static boolean save2XML(SmartHomeHandler smartHomeHandler) {
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = null;
		Document doc = null;
		try {
			docBuilder = docFactory.newDocumentBuilder();
			doc = docBuilder.newDocument();

		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

		Element rootElement = doc.createElement("HomeAutomation");
		doc.appendChild(rootElement);

		Iterator<Device> devices = smartHomeHandler.getDevices().iterator();
		while (devices.hasNext()) {
			Main.getLogger().log(Level.FINE, "Storing Device to XML");
			rootElement.appendChild(devices.next().getStorageXML(doc));
		}

		/*
		 * if (smartHomeHandler.getMyFritzBox() != null) {
		 * Main.getLogger().log(Level.FINE, "Storing FritzBox to XML");
		 * rootElement.appendChild(smartHomeHandler.getMyFritzBox().getStorageXML(doc));
		 * }
		 */
		Vector<LogicBase> dp = smartHomeHandler.getDataProcessors();
		Iterator<LogicBase> itD = dp.iterator();

		Element dataProcessors = doc.createElement("DataProcessors");
		Main.getLogger().log(Level.FINE, "Storing Data Processors to XML");

		while (itD.hasNext()) {
			dataProcessors.appendChild(itD.next().getStorageXML(doc));
		}
		rootElement.appendChild(dataProcessors);

		// Output....
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer transforme = null;
		try {
			transforme = tf.newTransformer();
			transforme.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			StringWriter writer = new StringWriter();
			transforme.transform(new DOMSource(doc), new StreamResult(writer));
			// String out = writer.getBuffer().toString().replaceAll("\n|\r", "");
			// String out = writer.getBuffer().toString();
			// System.out.println("######XML " + out);
			try {
				Main.getLogger().log(Level.FINE, "Save XML to file: " + storeFile);

				FileOutputStream f = new FileOutputStream(storeFile);
				f.write(writer.getBuffer().toString().getBytes());
				f.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return true;
	}

	ResultSet rsNode = null;
	ResultSet rsBuzzwords = null;

}
