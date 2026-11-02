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
import mdn.jh.automation.io.sink.DataSink;
import mdn.jh.automation.storage.Storeable;

/**
 * 
 * @author jhrib
 *
 */
public abstract class DataSinkHandler implements Serializable, Storeable, Runnable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1250590437525729692L;
	private boolean updateInProgress = false;
	private volatile boolean running = true;
	protected Vector<DataSink> myDataSinks = new Vector<DataSink>();
	// private int sourceDeviceType = 0;
	private volatile long update_time = 1000;

	public long getUpdate_time() {
		return update_time;
	}

	public void setUpdate_time(long update_time) {
		this.update_time = update_time;
	}

	@Override
	public void run() {
		running = true;
		while (running) {
			executeActions();
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

	protected abstract String getDataSinkDetail();

	public Vector<DataSink> getMyDataSinks() {
		return myDataSinks;
	}

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
	/*
	 * public DataSinkHandler(int deviceType) { this.sourceDeviceType = deviceType;
	 * }
	 */
	public void executeActions() {
		Iterator<DataSink> it = myDataSinks.iterator();
		while (it.hasNext()) {
			// TODO Create Scheduler
			it.next().executeActions();

		}
	}

	public void addDataSink(DataSink dataSink) {
		// System.out.println("FritzDevices: Add Data Source: " + dataSource);
		Main.getLogger().log(Level.FINER, "DataSinkHandler: Add DataSink: " + dataSink);
		if (dataSink == null)
			return;
		dataSink.setSinkName(getDataSinkDetail());
		myDataSinks.add(dataSink);
		// Main.getMySmartHomeHandler().registerDataSource(dataSource);
	}

	public boolean removeDataSink(DataSink dataSource) {
		Main.getLogger().log(Level.FINER, "DataSinkHandler: Remove DataSink: " + dataSource);
		return myDataSinks.remove(dataSource);
	}

	public boolean removeDataSink(int id) {
		Main.getLogger().log(Level.FINER, "DataSinkHandler: Remove DataSink by ID: " + id);
		Iterator<DataSink> it = myDataSinks.iterator();
		while (it.hasNext()) {
			DataSink d = it.next();
			if (d.getId() == id) {

				return removeDataSink(d);
			}
		}
		return false;

	}

	/*
	 * public String getDataSinkDeviceName() { switch (sourceDeviceType) { case
	 * Device.TYPE_CORE: return "Core"; case Device.TYPE_FRITZ_BOX: return
	 * "FritzBox"; case Device.TYPE_MODBUS: return "Modbus"; case
	 * Device.TYPE_HOMEMATIC: return "Homematic"; case Device.TYPE_XML: return
	 * "XML"; } return "Unknown"; }
	 */
	/*
	 * public int getDataSourceDeviceType() { return sourceDeviceType; }
	 */
	/**
	 * If overwritten this function must be called from child-class!
	 */
	public boolean initDataComponent(Node node) throws Exception {
		if (node == null)
			return false;
		String name = node.getNodeName();
		if (!"DataSinks".equals(name)) {
			throw new Exception("Creating - expected Node: DataSinks - Found:" + name);
		}
		Node updateTimeNode = node.getAttributes().getNamedItem("updateTime");
		if (updateTimeNode != null) update_time = Long.parseLong(updateTimeNode.getNodeValue());
		NodeList nl = node.getChildNodes();
		Node temp = null;
		for (int j = 0; j < nl.getLength(); j++) {
			temp = nl.item(j);
			// System.out.println(temp1.getNodeName());
			if ("DataSink".equals(temp.getNodeName())) {
				Main.getLogger().log(Level.INFO, "Add  DataSink from XML");
				String className = temp.getAttributes().getNamedItem("class").getNodeValue();
				// String id = temp.getAttributes().getNamedItem("id").getNodeValue();
				Class<?> clazz = Class.forName(className);
				Constructor<?> constructor = clazz.getConstructor();
				Object instance = constructor.newInstance();
				DataSink dataSink = (DataSink) instance;

				NodeList ndp = temp.getChildNodes();
				for (int k = 0; k < ndp.getLength(); k++) {
					if ("DataComponent".equals(ndp.item(k).getNodeName())) {
						dataSink.initDataComponent(ndp.item(k));
					}
					// if ("Attributes".equals(ndp.item(k).getNodeName())) {
					// dataSink.initSpecific(ndp.item(k));
					// }
				}
				dataSink.initSpecific(temp);
				// fritzDataSink.setFritzBox(this.fritzBox);
				addDataSink(dataSink);
			}
		}
		return true;
	}

	@Override
	public Node getStorageXML(Document doc) {
		Element rootElement = null;
		rootElement = doc.createElement("DataSinks");
		rootElement.setAttribute("updateTime", Long.toString(update_time));

		Iterator<DataSink> it = myDataSinks.iterator();
		while (it.hasNext()) {
			rootElement.appendChild(it.next().getStorageXML(doc));
		}
		return rootElement;
	}

}
