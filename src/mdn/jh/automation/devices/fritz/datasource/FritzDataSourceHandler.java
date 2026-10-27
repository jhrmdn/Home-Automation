package mdn.jh.automation.devices.fritz.datasource;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Level;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import mdn.jh.automation.Main;
import mdn.jh.automation.device.DataSourceHandler;
import mdn.jh.automation.devices.fritz.FritzBoxDevice;
import mdn.jh.automation.devices.fritz.SmartHomeDevice;
import mdn.jh.automation.io.source.DataSource;
import mdn.jh.automation.test.FritzDevicesTest;

public class FritzDataSourceHandler extends DataSourceHandler implements Serializable {

	private static final long serialVersionUID = 66942326818995939L;
	Vector<SmartHomeDevice> devices = new Vector<SmartHomeDevice>();
	FritzBoxDevice myFritzBox = null;
	NodeList devicesNodeList = null;
	Document fritzDeviceList = null;
	boolean connectionProblem = false;

	public FritzDataSourceHandler(FritzBoxDevice fritzBox) {
		super();
		myFritzBox = fritzBox;
	}

	public FritzDataSourceHandler(FritzBoxDevice fritzBox, Node node) throws Exception {
		super();
		myFritzBox = fritzBox;
		initDataComponent(node);
	}

	@Override
	protected String getDataSourceDetail() {
		return myFritzBox.getName();
	}

	public Document getFritzDeviceList() {
		return fritzDeviceList;
	}

	@Override
	public void update() {

		if (Main.isTest()) {
			try {
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				factory.setNamespaceAware(true);
				fritzDeviceList = factory.newDocumentBuilder().parse(new java.io.ByteArrayInputStream(
						FritzDevicesTest.xmlDeviceString.getBytes(StandardCharsets.UTF_8)));
				updateMyDataSources();
			} catch (Exception e) {
				System.out.println("Exception: " + e.getLocalizedMessage());
			}
			return;

		} else {
			fritzDeviceList = myFritzBox.getReplyFromFritzBoxNodeList("&switchcmd=getdevicelistinfos");
		}

		if (fritzDeviceList == null) {
			if (!connectionProblem) {
				connectionProblem = true;
				Main.getLogger().log(Level.WARNING, "FRITZ!Box updates are unavailable: " + myFritzBox.getConnectionError());
			}
			return;
		}

		XPath xpath = XPathFactory.newInstance().newXPath();
		XPathExpression expr;
		try {
			expr = xpath.compile("//devicelist/device");
			devicesNodeList = (NodeList) expr.evaluate(fritzDeviceList, XPathConstants.NODESET);
		} catch (XPathExpressionException e) {
			// avoid continuous logging
			if (connectionProblem)
				return;
			connectionProblem = true;
			Main.getLogger().log(Level.WARNING, "Update from FritzBox Failed: " + e.getLocalizedMessage());
			return;
		}
		if (connectionProblem) {
			connectionProblem = false;
			Main.getLogger().log(Level.INFO, "Update from FRITZ!Box succeeded");
		}
		// TODO - Check if still necessary
		// update(devicesNodeList);
		// -----
		updateMyDataSources();
	}

	private void updateMyDataSources() {
		FritzDataSource dataSource = null;
		Iterator<DataSource> it = myDataSources.iterator();
		while (it.hasNext()) {
			dataSource = (FritzDataSource) it.next();
			FritzDataSourceInputParameter inputParameter = dataSource.getMyDataSourceInputParameter();
			String param = inputParameter.getParameter1();
			String value = getNodeValue(fritzDeviceList, param);
			dataSource.updateValue(value);
			String valid = getNodeValue(fritzDeviceList, inputParameter.getParameter3());
			//System.out.println(inputParameter.getParameter2() +" Valid:"+valid);
			
			dataSource.setDataFromFritzBoxValid("1".equals(valid));

		}
	}

	public SmartHomeDevice getByID(String id) {
		int i = Integer.valueOf(id);

		SmartHomeDevice temp = null;
		Iterator<SmartHomeDevice> iterator = devices.iterator();
		while (iterator.hasNext()) {
			temp = iterator.next();
			if (temp.getId() == i) {
				return temp;
			}
		}
		return null;
	}

	/**
	 * 
	 * @return The FritzDevice String for a single device
	 */
	public SmartHomeDevice getSmartHomeDeviceByIdentifier(String identifier) {
		if (identifier == null) {
			return null;
		}

		SmartHomeDevice temp = null;
		Iterator<SmartHomeDevice> iterator = devices.iterator();
		while (iterator.hasNext()) {
			temp = iterator.next();
			if (identifier.equals(temp.getIdentifier())) {
				return temp;
			}
		}
		return null;
	}

	public String getNodeValue(String xpathString) {
		return getNodeValue(fritzDeviceList, xpathString);
	}

	private String getNodeValue(Node node, String xpathString) {
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
			return null;
		}
	}

}
