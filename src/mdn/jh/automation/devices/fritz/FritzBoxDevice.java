package mdn.jh.automation.devices.fritz;

import java.io.Serializable;
import java.util.logging.Level;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import mdn.jh.automation.Main;
import mdn.jh.automation.device.DataSinkHandler;
import mdn.jh.automation.device.DataSourceHandler;
import mdn.jh.automation.device.Device;
import mdn.jh.automation.devices.fritz.datasink.FritzDataSinkCreator;
import mdn.jh.automation.devices.fritz.datasink.FritzDataSinkHandler;
import mdn.jh.automation.devices.fritz.datasource.FritzDataSourceCreator;
import mdn.jh.automation.devices.fritz.datasource.FritzDataSourceHandler;
import mdn.jh.automation.gui.DataSinkCreator;
import mdn.jh.automation.gui.DataSourceCreator;

public class FritzBoxDevice extends Device implements Serializable {

	private static final long serialVersionUID = -8360890981442011021L;
	FritzSessionHandler mySessionHandler = null;
	// DataSourceHandler dataSourceHandler = null;
	// FritzDataSinkHandler dataSinkHandler = null;

	/**
	 * For initialisation from config
	 * 
	 * @param node
	 */
	public FritzBoxDevice() throws Exception {
		super(TYPE_FRITZ_BOX);
	}

	public FritzBoxDevice(String fritzIP, String user, String pass) {
		this(fritzIP, user, pass, FritzSessionHandler.TLS_INSECURE, "");
	}

	public FritzBoxDevice(String fritzIP, String user, String pass, String tlsMode, String certificateBase64) {
		super(TYPE_FRITZ_BOX);
		setConnectionData(fritzIP, user, pass, tlsMode, certificateBase64);
		dataSourceHandler = new FritzDataSourceHandler(this);
		dataSinkHandler = new FritzDataSinkHandler(this);

	}

	public void setConnectionData(String fritzIP, String user, String pass) {
		setConnectionData(fritzIP, user, pass, FritzSessionHandler.TLS_INSECURE, "");
	}

	public void setConnectionData(String fritzIP, String user, String pass, String tlsMode, String certificateBase64) {
		mySessionHandler = new FritzSessionHandler(fritzIP, user, pass, tlsMode, certificateBase64);
	}

	@Override
	public String getName() {
		return "Fritz Box - " + getMyIPAddress();
	}

	public String getMyIPAddress() {
		return mySessionHandler.getFritz_ip();
	}

	/**
	 * 
	 * @return True if connection is ok and login data are correct
	 */
	public boolean checkConnectionOK() {
		if (mySessionHandler == null)
			return false;
		return mySessionHandler.checkConnectionOK();

	}

	public boolean isConnected() { return mySessionHandler != null && mySessionHandler.isConnected(); }
	public String getConnectionError() { return mySessionHandler == null ? "Connection is not configured" : mySessionHandler.getLastError(); }
	public String getTlsMode() { return mySessionHandler == null ? FritzSessionHandler.TLS_INSECURE : mySessionHandler.getTlsMode(); }
	public String getLastSuccessfulRequest() {
		return mySessionHandler == null || mySessionHandler.getLastSuccessfulRequest() == null
				? "" : mySessionHandler.getLastSuccessfulRequest().toString();
	}

	/**
	 * Returns the last sync of FritzDevices - if required - call update() before
	 * 
	 * @return
	 */

	public DataSourceHandler getDataSourceHandler() {
		if (dataSourceHandler == null) {
			dataSourceHandler = new FritzDataSourceHandler(this);
			// dataSourceHandler.update();
		}
		return dataSourceHandler;
	}

	public DataSinkHandler getDataSinkHandler() {
		if (dataSinkHandler == null) {
			dataSinkHandler = new FritzDataSinkHandler(this);
		}
		return dataSinkHandler;
	}

	/*
	 * @Override public void removeDataSource(DataSource dataSource) {
	 * getDataSourceHandler().removeDataSource(dataSource); }
	 */
	/**
	 * Example:
	 * https://192.168.17.1/webservices/homeautoswitch.lua?sid=$SID&switchcmd=getdevicelistinfos
	 * For devicelist: "&switchcmd=getdevicelistinfos"
	 * 
	 * @param urlString the URL String after SessionID incl &
	 * @return
	 */
	public Document getReplyFromFritzBoxNodeList(String urlGetParameters) {
		if (mySessionHandler == null)
			return null;

		String sessionID = mySessionHandler.getSessionID();
		if (sessionID == null) return null;
		String urlParameters = "sid=" + sessionID;
		if (urlGetParameters == null || "".equals(urlGetParameters)) {
			urlGetParameters = "";
		} else {
			urlGetParameters = "&" + urlGetParameters;
		}

		String urlString = mySessionHandler.getBaseURL() + "/webservices/homeautoswitch.lua?" + urlParameters
				+ urlGetParameters;

		// Main.getLogger().log(Level.FINER, "FritzBox - Execute:" + urlString);
		return mySessionHandler.getReplyFromFritzBoxNodeList(urlString);

	}

	public String getReplyFromFritzBoxText(String urlGetParameters) {
		if (mySessionHandler == null)
			return null;

		String sessionID = mySessionHandler.getSessionID();
		if (sessionID == null) return null;
		String urlString = mySessionHandler.getBaseURL() + "/webservices/homeautoswitch.lua?sid=" + sessionID
				+ "&" + urlGetParameters;
		return mySessionHandler.getReplyFromFritzBoxText(urlString);
	}

	@Override
	protected Node getSpecificStorage(Document doc) {
		Element rootElement = null;
		rootElement = doc.createElement("FritzBox");
		rootElement.setAttribute("deviceID", "" + getDeviceID());
		rootElement.setAttribute("ip_host", "" + mySessionHandler.getFritz_ip());
		rootElement.setAttribute("username", "" + mySessionHandler.getFritz_user());
		rootElement.setAttribute("password", "" + mySessionHandler.getFritz_pass());
		rootElement.setAttribute("tlsMode", mySessionHandler.getTlsMode());
		if (!mySessionHandler.getCertificateBase64().isBlank()) {
			Element certificate = doc.createElement("Certificate");
			certificate.setTextContent(mySessionHandler.getCertificateBase64());
			rootElement.appendChild(certificate);
		}
		rootElement.appendChild(dataSourceHandler.getStorageXML(doc));
		rootElement.appendChild(dataSinkHandler.getStorageXML(doc));

		return rootElement;
	}

	@Override
	public boolean initSpecific(Node node) throws Exception {
		if (node == null)
			return false;

		NodeList it = node.getChildNodes();
		Node temp = null;
		for (int i = 0; i < it.getLength(); i++) {
			temp = it.item(i);
			if ("FritzBox".equals(temp.getNodeName()))
				break;
			else {
				temp = null;
			}
		}

		if (temp == null) {
			return false;
		}
		/*
		 * if (!"FritzBox".equals(node.getNodeName())) { throw new
		 * Exception("Creating - expected Node: FritzBox - Found:" +
		 * node.getNodeName()); }
		 */

		NamedNodeMap attributes = temp.getAttributes();
		String ip = attributes.getNamedItem("ip_host").getNodeValue();
		String user = attributes.getNamedItem("username").getNodeValue();
		String pass = attributes.getNamedItem("password").getNodeValue();
		Node tlsModeNode = attributes.getNamedItem("tlsMode");
		String tlsMode = tlsModeNode == null ? FritzSessionHandler.TLS_INSECURE : tlsModeNode.getNodeValue();
		String certificateBase64 = "";
		NodeList specificChildren = temp.getChildNodes();
		for (int i = 0; i < specificChildren.getLength(); i++) {
			if ("Certificate".equals(specificChildren.item(i).getNodeName())) {
				certificateBase64 = specificChildren.item(i).getTextContent();
				break;
			}
		}
		mySessionHandler = new FritzSessionHandler(ip, user, pass, tlsMode, certificateBase64);
		NodeList nl = temp.getChildNodes();
		// Node temp = null;
		for (int i = 0; i < nl.getLength(); i++) {
			temp = nl.item(i);
			if ("DataSources".equals(temp.getNodeName())) {
				Main.getLogger().log(Level.INFO, "Add Fritz DataSourceHandler");
				dataSourceHandler = new FritzDataSourceHandler(this, temp);
			}

			if ("DataSinks".equals(temp.getNodeName())) {
				Main.getLogger().log(Level.INFO, "Add Fritz DataSinkHandler");
				dataSinkHandler = new FritzDataSinkHandler(this, temp);
			}
		}
		return true;
	}

	@Override
	public DataSourceCreator getDataSourceCreator() {
		FritzDataSourceCreator sourceSelector = new FritzDataSourceCreator();

		// FritzSourceSelector sourceSelector = new FritzSourceSelector();
		FritzDataSourceHandler f = (FritzDataSourceHandler) getDataSourceHandler();

		sourceSelector.setFritzSource((f));
		return sourceSelector;
	}

	@Override
	public DataSinkCreator getDataSinkCreator() {
		FritzDataSinkCreator actionSelectorFritz = new FritzDataSinkCreator();
		FritzDataSourceHandler f = (FritzDataSourceHandler) getDataSourceHandler();
		actionSelectorFritz.setFritzSource(f.getFritzDeviceList(), this);
		return actionSelectorFritz;
	}

}
