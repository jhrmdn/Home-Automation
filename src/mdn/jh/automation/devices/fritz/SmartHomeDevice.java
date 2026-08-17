package mdn.jh.automation.devices.fritz;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.math.BigInteger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class SmartHomeDevice implements Serializable {

	/**
		 * 
		 */
	private static final long serialVersionUID = -1353182253970667398L;
	// String xmlDeviceString = null;
	String identifier = null;
	int id = -1;
	String functionbitmask = null;
	String fwversion = null;
	String manufacturer = null;
	String productname = null;
	Node myXMLNode = null;

	public SmartHomeDevice(Node deviceNode) {
		myXMLNode = deviceNode;
		init();

	}

	/**
	 * 
	 * @param xmlDeviceString
	 */
	public SmartHomeDevice(String xmlDeviceString) {
		update(xmlDeviceString);
		init();
	}

	private void init() {
		identifier = getValue(myXMLNode, "//device/@identifier");
		id = Integer.valueOf(getValue(myXMLNode, "//device/@id"));
		fwversion = getValue(myXMLNode, "//device/@fwversion");
		manufacturer = getValue(myXMLNode, "//device/@manufacturer");
		productname = getValue(myXMLNode, "//device/@productname");
		functionbitmask = getValue(myXMLNode, "//device/@functionbitmask");
	}

	public void update(Node deviceNode) {
		this.myXMLNode = deviceNode;
	}

	public void update(String xmlDeviceString) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
			myXMLNode = builder.parse(new InputSource(new StringReader(xmlDeviceString)));
		} catch (ParserConfigurationException | SAXException | IOException e) {
			e.printStackTrace();
		}
	}

	public String getIdentifier() {
		return identifier;
	}

	public int getId() {
		return id;
	}

	/**
	 * Bit 0: HAN-FUN Gerät Bit 2: Licht/Lampe Bit 4: Alarm-Sensor Bit 5: AVM-Button
	 * Bit 6: Heizkörperregler Bit 7: Energie Messgerät Bit 8: Temperatursensor Bit
	 * 9: Schaltsteckdose Bit 10: AVM DECT Repeater Bit 11: Mikrofon Bit 13:
	 * HAN-FUN-Unit Bit 15: an-/ausschaltbares Gerät/Steckdose/Lampe/Aktor Bit 16:
	 * Gerät mit einstellbarem Dimm-, Höhen- bzw. Niveau-Level Bit 17: Lampe mit
	 * einstellbarer Farbe/Farbtemperatur
	 * 
	 * @return
	 */
	public String getFunctionbitmask() {
		return functionbitmask;
	}

	public String getFunctionbitmaskBitString() {
		BigInteger bigInteger = new BigInteger(getFunctionbitmask());
		return bigInteger.toString(2);
	}

	public String getFwversion() {
		return fwversion;
	}

	public String getManufacturer() {
		return manufacturer;
	}

	public String getProductname() {
		return productname;
	}

	public String getValue(String xpathString) {
		return getValue(myXMLNode, xpathString);
	}

	private String getValue(Node node, String xpathString) {
		if (node == null || xpathString == null) {
			return null;
		}
		XPathFactory xpathFactory = XPathFactory.newInstance();
		XPath xpath = xpathFactory.newXPath();

		XPathExpression expr;
		try {
			expr = xpath.compile(xpathString);
			return (String) expr.evaluate(node, XPathConstants.STRING);
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

}
