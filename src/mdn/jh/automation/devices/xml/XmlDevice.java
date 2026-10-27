package mdn.jh.automation.devices.xml;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import mdn.jh.automation.Main;
import mdn.jh.automation.MySSLSocketfactory;
import mdn.jh.automation.device.DataSinkHandler;
import mdn.jh.automation.device.DataSourceHandler;
import mdn.jh.automation.device.Device;
import mdn.jh.automation.devices.xml.datasink.XmlDataSinkHandler;
import mdn.jh.automation.devices.xml.datasource.XmlDataSourceHandler;
import mdn.jh.automation.gui.DataSinkCreator;
import mdn.jh.automation.gui.DataSourceCreator;

public class XmlDevice extends Device {

	private XmlDataSourceHandler dataSourceHandler = null;

	public static int XML_type_file = 1;
	public static int XML_type_webserver = 2;
	protected int xml_type = XML_type_webserver;
	protected String url = "http://192.168.17.54/heizung/UBAMonitorFast.xml";
	protected boolean httpsConnection = false;

	public XmlDevice() {
		super(TYPE_XML);
	}

	/**
	 * 
	 * @param xml_type
	 * @param url
	 * @param https    if true https is used (ignored for file type)
	 */
	public XmlDevice(int xml_type, String url, boolean httpsConnection) throws Exception {
		this();
		this.xml_type = xml_type;
		this.url = url;
		this.httpsConnection = httpsConnection;
		checkConnection();
	}

	@Override
	public String getName() {
		if (xml_type == XML_type_file) {
			return "File: " + url;
		} else {
			return "Web: " + url;
		}
	}

	public boolean checkConnection() throws Exception {
		if (xml_type == XML_type_file) {
			File f = new File(url);
			return f.exists();
		}

		if (xml_type == XML_type_webserver) {
			Document reply = null;
			if (httpsConnection) {
				reply = getWebReplyHTTPS(url);
			} else {
				reply = getWebReplyHTTP(url);
			}
			return reply != null;
		}

		return false;

	}

	/**
	 * 
	 * @return The XML Document for the XMLDataSourceHandler
	 * @throws Exception
	 */
	public Document getXMLDocument() throws Exception {
		if (xml_type == XML_type_file) {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			return factory.newDocumentBuilder().parse(new File(url));
		}
		if (httpsConnection) {
			return getWebReplyHTTPS(url);
		}
		return getWebReplyHTTP(url);

	}

	protected Document getWebReplyHTTP(String urlString) throws Exception {
		Document doc = null;

		Main.getLogger().log(Level.FINER, "XML - Execute:" + urlString);
		URL url = new URL(urlString);
		HttpURLConnection conn = null;
		conn = (HttpURLConnection) url.openConnection();
		conn.setDoOutput(true);
		conn.setInstanceFollowRedirects(false);
		conn.setRequestMethod("GET");
		conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		conn.setRequestProperty("charset", "utf-8");
		conn.setUseCaches(false);
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);

		doc = factory.newDocumentBuilder().parse(conn.getInputStream());

		return doc;

	}

	protected Document getWebReplyHTTPS(String urlString) throws Exception {
		Document doc = null;

		Main.getLogger().log(Level.FINER, "XML - Execute:" + urlString);
		URL url = new URL(urlString);

		HttpsURLConnection conn = null;
		conn = (HttpsURLConnection) url.openConnection();

		SSLSocketFactory sslFactory = MySSLSocketfactory.getMySSLSocketfactory();
		if (sslFactory == null) {
			Main.getLogger().log(Level.SEVERE,
					"SSL Factory not initialized. Please check the certificates in the cert folder for XML Device.");
			return null;
		}
		conn.setSSLSocketFactory(sslFactory);
		conn.setHostnameVerifier(new HostnameVerifier() {
			@Override
			public boolean verify(String arg0, SSLSession arg1) {
				return true;
			}
		});

		conn.setDoOutput(true);
		conn.setInstanceFollowRedirects(false);
		conn.setRequestMethod("GET");
		conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		conn.setRequestProperty("charset", "utf-8");
		conn.setUseCaches(false);
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);

		doc = factory.newDocumentBuilder().parse(conn.getInputStream());
		return doc;

	}

	@Override
	public DataSourceHandler getDataSourceHandler() {
		if (dataSourceHandler == null) {
			dataSourceHandler = new XmlDataSourceHandler(this);
		}
		return dataSourceHandler;
	}

	@Override
	public DataSinkHandler getDataSinkHandler() {
		if (dataSinkHandler == null) {
			dataSinkHandler = new XmlDataSinkHandler();
		}
		return dataSinkHandler;
	}

	@Override
	public DataSourceCreator getDataSourceCreator() throws Exception {
		XmlDataSourceCreator c = new XmlDataSourceCreator();
		Document d = getXMLDocument();
		c.setDocument(d);
		return c;

	}

	@Override
	public DataSinkCreator getDataSinkCreator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Node getSpecificStorage(Document doc) {
		Element rootElement = null;
		rootElement = doc.createElement("XML");
		rootElement.setAttribute("type", "" + xml_type);
		rootElement.setAttribute("url", url);
		rootElement.setAttribute("https", Boolean.toString(httpsConnection));
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
			if ("XML".equals(temp.getNodeName()))
				break;
			else {
				temp = null;
			}
		}
		if (temp == null) {
			return false;
		}

		NamedNodeMap attributes = temp.getAttributes();
		String t = attributes.getNamedItem("type").getNodeValue();
		if (t != null) {
			try {
				xml_type = Integer.valueOf(t);
			} catch (Exception e) {

			}

		}

		url = attributes.getNamedItem("url").getNodeValue();
		Node https = attributes.getNamedItem("https");
		httpsConnection = https != null && Boolean.parseBoolean(https.getNodeValue());

		NodeList nl = temp.getChildNodes();
		temp = null;
		for (int i = 0; i < nl.getLength(); i++) {
			temp = nl.item(i);

			if ("DataSources".equals(temp.getNodeName())) {
				Main.getLogger().log(Level.INFO, "Add Modbus DataSourceHandler");
				dataSourceHandler = new XmlDataSourceHandler(this, temp);
			}

			if ("DataSinks".equals(temp.getNodeName())) {
				Main.getLogger().log(Level.INFO, "Add Modbus DataSinkHandler");
				// dataSinkHandler = new XmlDa(this, temp);
			}
		}
		return true;
	}

}
