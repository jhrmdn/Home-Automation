package mdn.jh.automation.device;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Level;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import mdn.jh.automation.Main;
import mdn.jh.automation.io.source.DataSource;
import mdn.jh.automation.storage.Storeable;

/**
 * 
 * @author jhrib
 *
 */
public abstract class DataSourceHandler implements Serializable, Storeable, Runnable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1250590437525729692L;
	// public static final int SOURCE_DEVICE_FRITZ_BOX = 1;
	// public static final int SOURCE_DEVICE_MODBUS = 2;
	private boolean updateInProgress = false;
	private volatile boolean running = true;
	protected Vector<DataSource> myDataSources = new Vector<DataSource>();
	//private int sourceDeviceType = 0;
	private long update_time = 1000;

	public long getUpdate_time() {
		return update_time;
	}

	public void setUpdate_time(long update_time_dataSource) {
		this.update_time = update_time_dataSource;
	}

	public Vector<DataSource> getMyDataSources() {
		return myDataSources;
	}

	@Override
	public void run() {
		running = true;
		while (running) {
			update();
			try {
				Thread.sleep(update_time);
			} catch (InterruptedException e) {
				// Re-check the running flag immediately.
			}
		}

	}

	public void stop() {
		running = false;
	}

	/**
	 * Updates all registered DataSource
	 */
	public abstract void update();

	public boolean isUpdateInProgress() {
		return updateInProgress;
	}

	protected void setUpdateInProgress(boolean updateInProgress) {
		this.updateInProgress = updateInProgress;
	}

	/**
	 * 
	 * @param deviceType taken from class {@link mdn.jh.automation.device.Device}
	 */

	/*public DataSourceHandler(int deviceType) {
		this.sourceDeviceType = deviceType;
	}*/

	protected abstract String getDataSourceDetail();

	public void addDataSource(DataSource dataSource) {
		if (dataSource == null)
			return;
		// System.out.println("FritzDevices: Add Data Source: " + dataSource);
		Main.getLogger().log(Level.FINER, "DataSourceHandler: Add Data Source. Name:" + dataSource.getName());

		dataSource.setSourceName(getDataSourceDetail());
		dataSource.setMyDataSourceHandler(this);
		myDataSources.add(dataSource);
		// Main.getMySmartHomeHandler().registerDataSource(dataSource);
	}

	public boolean removeDataSource(DataSource dataSource) {
		if (dataSource == null)
			return false;

		boolean success = myDataSources.remove(dataSource);
		if (success)
			Main.getLogger().log(Level.FINER, "DataSourceHandler: Remove Data Source. Name: " + dataSource.getName());
		return success;
	}

	public boolean removeDataSource(int id) {

		Iterator<DataSource> it = myDataSources.iterator();
		while (it.hasNext()) {
			DataSource d = it.next();
			if (d.getId() == id) {
				Main.getLogger().log(Level.FINER, "DataSourceHandler: Remove Data Source by ID: " + id);
				return removeDataSource(d);
			}
		}
		return false;

	}

	/*
	public String getDataSourceDeviceName() {
		switch (sourceDeviceType) {
		case Device.TYPE_CORE:
			return "Core";
		case Device.TYPE_FRITZ_BOX:
			return "FritzBox";
		case Device.TYPE_MODBUS:
			return "Modbus";
		case Device.TYPE_HOMEMATIC:
			return "Homematic";
		}
		return "Unknown";
	}
*/
	
	/*
	public int getDataSourceDeviceType() {
		return sourceDeviceType;
	}
	*/

	@Override
	public Node getStorageXML(Document doc) {
		Element rootElement = null;
		rootElement = doc.createElement("DataSources");
		rootElement.setAttribute("updateTime", Long.toString(update_time));
		// rootElement.setAttribute("type", "" + getDataSourceDeviceType());

		Iterator<DataSource> it = myDataSources.iterator();
		while (it.hasNext()) {
			rootElement.appendChild(it.next().getStorageXML(doc));
		}
		return rootElement;
	}

	@Override
	public boolean initDataComponent(Node node) throws Exception {
		if (node == null)
			return false;
		String name = node.getNodeName();
		if (!"DataSources".equals(name)) {
			throw new Exception("Creating - expected Node: DataSources - Found:" + name);
		}
		NodeList nl = node.getChildNodes();
		Node temp = null;
		for (int j = 0; j < nl.getLength(); j++) {
			temp = nl.item(j);
			if ("DataSource".equals(temp.getNodeName())) {
				String className = temp.getAttributes().getNamedItem("class").getNodeValue();
				Class<?> clazz = Class.forName(className);
				Constructor<?> constructor = clazz.getConstructor();
				Object instance = constructor.newInstance();
				DataSource dataSourceStringInput = (DataSource) instance;
				dataSourceStringInput.initDataComponent(temp);
				dataSourceStringInput.setMyDataSourceHandler(this);
				Main.getLogger().log(Level.INFO,
						"Add DataSource from XML: " + className + " - Handler: " + this.getClass().getCanonicalName());
				addDataSource(dataSourceStringInput);
			}
		}
		return true;
	}
}
