package mdn.jh.automation.devices.xml.datasource;

import java.lang.reflect.Constructor;

import javax.swing.JPanel;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import mdn.jh.automation.io.converter.Converter;
import mdn.jh.automation.io.source.DataSource;
import mdn.jh.automation.io.source.WrongDataTypeException;

public class XMLDataSource extends DataSource {

	private static final long serialVersionUID = 7833275851279106570L;
	Converter myDataConverter = null;
	String xpath = null;
	boolean valid = false;

	public void setValid(boolean valid) {
		this.valid = valid;
	}

	public XMLDataSource() {
		// TODO Auto-generated constructor stub
	}

	public String getXpath() {
		return xpath;
	}

	public void setXpath(String xpath) {
		this.xpath = xpath;
	}

	public void updateValue(String value) {
		if (myDataConverter == null)
			return;
		myDataConverter.setValue(value);
		updateMyOutputs();
	}

	public Converter getDataConverter() {
		return myDataConverter;
	}

	public void setDataConverter(Converter dataConverter) throws WrongDataTypeException {

		if (isDataTypeLocked()) {
			throw new WrongDataTypeException();
		}
		// setMyDataValue(dataConverter);
		this.myDataConverter = dataConverter;
	}

	/**
	 * Returns the actual value depending on the datatype
	 */
	@Override
	public String toString() {
		if (myDataConverter == null) {
			return null;
		}
		return myDataConverter.getOutputAsString();
	}

	@Override
	public boolean getOutputAsBoolean() {
		if (myDataConverter == null)
			return false;
		return myDataConverter.getOutputAsBoolean();
	}

	@Override
	public double getOutputAsNumber() {
		if (myDataConverter == null)
			return 0;
		return myDataConverter.getOutputAsNumber();
	}

	@Override
	public String getOutputAsString() {
		if (myDataConverter == null)
			return null;
		return myDataConverter.getOutputAsString();
	}

	public int getDataTypeOutput() {
		return myDataConverter.getDataTypeOutput();
	}

	@Override
	public JPanel getSpecificDetailsPanel() {
		XMLDataSourceDetailsPanel p = new XMLDataSourceDetailsPanel();
		p.setXMLDataSource(this);
		return p;
	}

	@Override
	public boolean isDataValid() {
		return valid;
	}

	@Override
	public Node getStorageXML(Document doc) {
		Element rootElement = null;

		rootElement = getStorage(doc);
		Element xp = doc.createElement("Xpath");
		xp.appendChild(doc.createCDATASection(xpath));

		rootElement.appendChild(xp);
		if (myDataConverter != null)
			rootElement.appendChild(myDataConverter.getStorageXML(doc));

		return rootElement;

	}

	@Override
	public boolean initDataComponent(Node node) throws Exception {
		if (node == null)
			return false;
		String name = node.getNodeName();
		if (!"DataSource".equals(name)) {
			throw new Exception("Creating - Expected Node: DataSource - Found:" + name);
		}

		initDataSource(node);

		// String nodeID = node.getAttributes().getNamedItem("id").getNodeValue();
		// overrideID(Integer.valueOf(nodeID));

		NodeList nl = node.getChildNodes();
		Node temp = null;
		for (int i = 0; i < nl.getLength(); i++) {
			temp = nl.item(i);

			if ("Xpath".equals(temp.getNodeName())) {
				xpath = temp.getTextContent();
			//	System.out.println("XPATH: " + xpath);

			}

			/*
			 * if ("Parameters".equals(temp.getNodeName())) { FritzDataSourceInputParameter
			 * di = new FritzDataSourceInputParameter(temp);
			 * setDataSourceInputParameter(di);
			 * 
			 * }
			 */
			if ("DataConverter".equals(temp.getNodeName())) {
				Node converterClass = temp.getAttributes().getNamedItem("class");
				if (converterClass == null) {
					throw new Exception("DataConverter attribute not set for DataSource:");
				}
				String cl = converterClass.getNodeValue();
				Class<?> clazz = Class.forName(cl);
				Constructor<?> constructor = clazz.getConstructor();
				Object instance = constructor.newInstance();

				myDataConverter = (Converter) instance;
				myDataConverter.initDataComponent(temp);
				// setMyDataValue(myDataConverter);

			}

		}

		return true;
	}

}
