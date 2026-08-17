package mdn.jh.automation.devices.fritz.datasource;

import java.io.Serializable;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import mdn.jh.automation.storage.Storeable;

public class FritzDataSourceInputParameter implements Serializable, Storeable {

	public FritzDataSourceInputParameter() {

	}

	public FritzDataSourceInputParameter(Node node) throws Exception {
		initDataComponent(node);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -6835268352075594656L;
	String parameter1 = null;
	String parameter2 = null;
	String parameter3 = null;

	/**
	 * Parameter 2+3 optional The condition is: -fritzbox parameter1: xpath string
	 * -modbus parameter1: type (coil / word), parameter 2:address -homematic
	 * (planned) xpath string
	 * 
	 * @param condition
	 * @return
	 */

	public String getParameter1() {
		return parameter1;
	}

	public void setParameter1(String parameter1) {
		this.parameter1 = parameter1;
	}

	public String getParameter2() {
		return parameter2;
	}

	public void setParameter2(String parameter2) {
		this.parameter2 = parameter2;
	}

	public String getParameter3() {
		return parameter3;
	}

	public void setParameter3(String parameter3) {
		this.parameter3 = parameter3;
	}

	@Override
	public boolean initDataComponent(Node node) throws Exception {

		if (node == null)
			return false;

		if (!"Parameters".equals(node.getNodeName())) {
			throw new Exception("Creating - expected Node: Parameters - Found:" + node.getNodeName());
		}

		NodeList outputs = node.getChildNodes();
		Node out = null;
		for (int i = 0; i < outputs.getLength(); i++) {
			out = outputs.item(i);
			if ("Parameter1".equals(out.getNodeName())) {
				this.parameter1 = out.getTextContent();
			}
			if ("Parameter2".equals(out.getNodeName())) {
				this.parameter2 = out.getTextContent();
			}
			if ("Parameter3".equals(out.getNodeName())) {
				this.parameter3 = out.getTextContent();
			}

		}

		return true;
	}

	@Override
	public Node getStorageXML(Document doc) {
		Element rootElement = null;
		rootElement = doc.createElement("Parameters");

		Element parameter1 = doc.createElement("Parameter1");
		Element parameter2 = doc.createElement("Parameter2");
		Element parameter3 = doc.createElement("Parameter3");
		parameter1.appendChild(doc.createTextNode(getParameter1()));
		parameter2.appendChild(doc.createTextNode(getParameter2()));
		parameter3.appendChild(doc.createTextNode(getParameter3()));

		rootElement.appendChild(parameter1);
		rootElement.appendChild(parameter2);
		rootElement.appendChild(parameter3);

		return rootElement;
	}

}
