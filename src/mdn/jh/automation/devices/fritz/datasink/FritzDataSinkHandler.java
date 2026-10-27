package mdn.jh.automation.devices.fritz.datasink;

import java.util.Iterator;

import org.w3c.dom.Node;

import mdn.jh.automation.device.DataSinkHandler;
import mdn.jh.automation.devices.fritz.FritzBoxDevice;
import mdn.jh.automation.io.sink.DataSink;

public class FritzDataSinkHandler extends DataSinkHandler {

	private static final long serialVersionUID = -4592082315523897403L;
	private FritzBoxDevice fritzBox = null;

	public FritzDataSinkHandler(FritzBoxDevice fritzBox) {
		super();
		this.fritzBox = fritzBox;
	}

	public FritzDataSinkHandler(FritzBoxDevice fritzBox, Node node) throws Exception {
		super();
		this.fritzBox = fritzBox;
		initDataComponent(node);

	}

	public boolean initDataComponent(Node node) throws Exception {

		boolean ok = super.initDataComponent(node);

		Iterator<DataSink> it = getMyDataSinks().iterator();
		FritzDataSink fritzDataSink = null;
		while (it.hasNext()) {
			fritzDataSink = (FritzDataSink) it.next();
			fritzDataSink.setFritzBox(this.fritzBox);
		}

		return ok;

		/*
		 * if (node == null) return false; String name = node.getNodeName(); if
		 * (!"DataSinks".equals(name)) { throw new
		 * Exception("Creating - expected Node: DataSinks - Found:" + name); } NodeList
		 * nl = node.getChildNodes(); Node temp = null; for (int j = 0; j <
		 * nl.getLength(); j++) { temp = nl.item(j); //
		 * System.out.println(temp1.getNodeName()); if
		 * ("DataSink".equals(temp.getNodeName())) { Main.getLogger().log(Level.INFO,
		 * "Add  DataSink from XML"); String className =
		 * temp.getAttributes().getNamedItem("class").getNodeValue(); String
		 * id=temp.getAttributes().getNamedItem("id").getNodeValue(); Class<?> clazz =
		 * Class.forName(className); Constructor<?> constructor =
		 * clazz.getConstructor(); Object instance = constructor.newInstance();
		 * FritzDataSink fritzDataSink = (FritzDataSink) instance;
		 * 
		 * NodeList ndp = temp.getChildNodes(); for (int k = 0; k < ndp.getLength();
		 * k++) { if ("DataComponent".equals(ndp.item(k).getNodeName())) {
		 * fritzDataSink.initDataComponent(ndp.item(k)); } if
		 * ("Attributes".equals(ndp.item(k).getNodeName())) {
		 * fritzDataSink.initFritzDataSink(ndp.item(k)); } }
		 * 
		 * addDataSink(fritzDataSink); } }
		 */
		// return true;
	}

	/*
	 * @Override public Node getStorageXML(Document doc) { Element rootElement =
	 * null; rootElement = doc.createElement("DataSinks"); Iterator<DataSink> it =
	 * myDataSinks.iterator(); while (it.hasNext()) {
	 * rootElement.appendChild(it.next().getStorageXML(doc)); } return rootElement;
	 * }
	 */
	@Override
	protected String getDataSinkDetail() {
		return "Fritzbox" + fritzBox.getMyIPAddress();
	}

}
