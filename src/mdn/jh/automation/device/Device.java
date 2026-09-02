package mdn.jh.automation.device;

import java.util.logging.Level;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import mdn.jh.automation.Main;
import mdn.jh.automation.gui.DataSinkCreator;
import mdn.jh.automation.gui.DataSourceCreator;
import mdn.jh.automation.storage.Storeable;

public abstract class Device implements Storeable {
	public static final int TYPE_UNKNOWN = -1;
	public static final int TYPE_CORE = 0;
	public static final int TYPE_FRITZ_BOX = 1;
	public static final int TYPE_MODBUS = 2;
	public static final int TYPE_HOMEMATIC = 3;
	public static final int TYPE_XML = 4;
	public static final int TYPE_SHELLY = 5;
	public static final int TYPE_DATABASE = 6;
	public static final int TYPE_MODBUS_SERVER = 7;
	public static final int TYPE_MQTT_BROKER = 8;
	public static final int TYPE_JSON = 9;
	protected DataSourceHandler dataSourceHandler = null;
	protected DataSinkHandler dataSinkHandler = null;

	private int type = TYPE_UNKNOWN;
	private static int idCounter = 1;
	private int id = -1;

	// Update times in ms
	private boolean running = true;
	Thread dataSourceHandlerThread = null;
	Thread dataSinkHandlerThread = null;

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

	public long getUpdate_time_dataSource() {
		return getDataSourceHandler().getUpdate_time();
	}

	public void setUpdate_time_dataSource(long update_time_dataSource) {
		getDataSourceHandler().setUpdate_time(update_time_dataSource);
	}

	public long getUpdate_time_dataSink() {
		return getDataSinkHandler().getUpdate_time();
	}

	public void setUpdate_time_dataSink(long update_time_dataSink) {
		getDataSinkHandler().setUpdate_time(update_time_dataSink);
	}

	/**
	 * 
	 * @param type the type defined in class Device: FritzBox, Modbus,....
	 */
	public Device(int type) {
		this(type, true);
	}

	/**
	 * 
	 * @param type
	 * @param createNewDeviceID if true a new device id will be created otherwise it
	 *                          must be set internally by loading with node
	 */
	protected Device(int type, boolean createNewDeviceID) {
		this.type = type;
		if (createNewDeviceID) {
			this.id = ++idCounter;
		}

	}

	public void startUpdateThreads() {
		if (getDataSourceHandler() == null) {
			Main.getLogger().log(Level.WARNING, "DataSourceHandler is NULL: " + getName());
			return;
		}

		Main.getLogger().log(Level.ALL, "Starting dataSourceHandler for " + getName());
		getDataSourceHandler().setUpdate_time(getUpdate_time_dataSource());
		dataSourceHandlerThread = new Thread(getDataSourceHandler());
		dataSourceHandlerThread.start();

		if (getDataSourceHandler() == null) {
			Main.getLogger().log(Level.WARNING, "DataSourceHandler is NULL: " + getName());
			return;
		}
		Main.getLogger().log(Level.ALL, "Starting dataSinkHandler for " + getName());
		getDataSinkHandler().setUpdate_time(getUpdate_time_dataSink());
		dataSinkHandlerThread = new Thread(getDataSinkHandler());
		dataSinkHandlerThread.start();

	}

	public void stopUpdateThreads() {
		setRunning(false);
		if (getDataSourceHandler() != null) getDataSourceHandler().stop();
		if (getDataSinkHandler() != null) getDataSinkHandler().stop();
		if (dataSourceHandlerThread != null) dataSourceHandlerThread.interrupt();
		if (dataSinkHandlerThread != null) dataSinkHandlerThread.interrupt();
	}

	/**
	 * shall not return null!
	 * 
	 * @return
	 */
	public abstract DataSourceHandler getDataSourceHandler();

	/**
	 * shall not return null!
	 * 
	 * @return
	 */

	public abstract DataSinkHandler getDataSinkHandler();

	public abstract DataSourceCreator getDataSourceCreator() throws Exception;

	public abstract DataSinkCreator getDataSinkCreator();

	// A readable and distinguishable Name of the device (e.g. Fritz Box - IP....)
	public abstract String getName();

	@Override
	public String toString() {
		return getName();
	}

	public int getType() {
		return type;
	}

	protected void overrideDeviceID(int deviceID) {
		this.id = ++deviceID;
		if (deviceID > idCounter) {
			idCounter = deviceID;
		}
	}

	public int getDeviceID() {
		return id;
	}

	/**
	 * Storage of specific attributes etc. including the DataSource- and
	 * DataSinkhandler
	 * rootElement.appendChild(dataSourceHandler.getStorageXML(doc));
	 * rootElement.appendChild(dataSinkHandler.getStorageXML(doc));
	 * 
	 * @param doc
	 * @return
	 */
	protected abstract Node getSpecificStorage(Document doc);

	/**
	 * A little bit complicated. The easiest way is to see an example e.g. from
	 * Modbus
	 * 
	 * @param node
	 * @return
	 * @throws Exception
	 */
	public abstract boolean initSpecific(Node node) throws Exception;

	@Override
	public boolean initDataComponent(Node node) throws Exception {
		if (node == null)
			return false;

		if (!"Device".equals(node.getNodeName())) {
			throw new Exception("Creating - expected Node: Device - Found:" + node.getNodeName());
		}

		NamedNodeMap attributes = node.getAttributes();
		String devID = attributes.getNamedItem("deviceID").getNodeValue();
		overrideDeviceID(Integer.valueOf(devID).intValue());

		// NodeList nl = node.getChildNodes();
		// Node temp = null;
		initSpecific(node);

		/*
		 * for (int i = 0; i < nl.getLength(); i++) { temp = nl.item(i);
		 * initSpecific(node); }
		 */
		return true;
	}

	@Override
	public Node getStorageXML(Document doc) {
		Element rootElement = null;
		rootElement = doc.createElement("Device");
		rootElement.setAttribute("class", getClass().getCanonicalName());
		rootElement.setAttribute("deviceID", "" + getDeviceID());
		Node n = getSpecificStorage(doc);

		Node temp = getDataSourceHandler().getStorageXML(doc);
		if (temp != null)
			n.appendChild(temp);
		temp = getDataSinkHandler().getStorageXML(doc);
		if (temp != null)
			n.appendChild(temp);

		rootElement.appendChild(n);

		return rootElement;
	}

}
