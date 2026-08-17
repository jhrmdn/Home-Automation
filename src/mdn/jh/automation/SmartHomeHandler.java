package mdn.jh.automation;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Level;

import mdn.jh.automation.corefunctions.CoreDevice;
import mdn.jh.automation.device.modbus.ModbusServerDevice;
import mdn.jh.automation.devices.mqtt.MqttBrokerDevice;
import mdn.jh.automation.device.DataSinkHandler;
import mdn.jh.automation.device.DataSourceHandler;
import mdn.jh.automation.device.Device;
import mdn.jh.automation.io.DataInputIF;
import mdn.jh.automation.io.DataOutputIF;
import mdn.jh.automation.io.logic.LogicBase;
import mdn.jh.automation.io.sink.DataSink;
import mdn.jh.automation.io.source.DataSource;
import mdn.jh.automation.webserver.DataPoint;
import mdn.jh.automation.webserver.DataPointSink;
import mdn.jh.automation.webserver.DataPointSource;

public class SmartHomeHandler implements Serializable {
	private static final long serialVersionUID = 4376357826536396049L;
	Vector<LogicBase> dataProcessors = new Vector<LogicBase>();

	// The actions will be carried out after the inputs have been updated
	// Vector<DataSink> actionProcessors = new Vector<DataSink>();
	Vector<Device> devices = new Vector<Device>();
	Vector<DataSink> dataSinks = new Vector<DataSink>();

	public Vector<Device> getDevices() {
		return devices;
	}
	
	

	public void registerDevice(Device device) {
		if (device == null)
			return;
		// System.out.println("#########Add "+device.getName());
		devices.add(device);
	}

	public boolean removeDevice(int deviceID) {
		Iterator<Device> iterator = devices.iterator();
		while (iterator.hasNext()) {
			Device device = iterator.next();
			if (device.getDeviceID() == deviceID) {
				if (device.getType() == Device.TYPE_CORE) return false;
				device.stopUpdateThreads();
				iterator.remove();
				Main.getLogger().log(Level.INFO, "Removed device: " + device.getName() + " (ID " + deviceID + ")");
				return true;
			}
		}
		return false;
	}

	public Vector<LogicBase> getDataProcessors() {
		return dataProcessors;
	}

	public SmartHomeHandler() {
		// CoreDevice device = new CoreDevice();
		// registerDevice(device);
	}

	/**
	 * It will be checked if the core device is already existing (started from
	 * settings file). If not it will be created
	 */
	public void initCoreDevice() {
		Iterator<Device> devices = getDevices().iterator();

		while (devices.hasNext()) {
			Device d = devices.next();
			if (d.getType() == Device.TYPE_CORE) {
				return;
			}

		}
		Main.getLogger().log(Level.ALL, "Init Core Device");
		CoreDevice device = new CoreDevice();
		registerDevice(device);
	}

	/** Ensures that exactly one non-deletable built-in Modbus server device exists. */
	public void initModbusServerDevice() {
		for (Device device : getDevices()) if (device.getType() == Device.TYPE_MODBUS_SERVER) return;
		registerDevice(new ModbusServerDevice());
	}
	public void initMqttBrokerDevice() { for(Device device:getDevices())if(device.getType()==Device.TYPE_MQTT_BROKER)return;registerDevice(new MqttBrokerDevice()); }

	/**
	 * Removes a unit by id (searches Datasources, processors, etc...)
	 * 
	 * @param id
	 */
	public void removeUnit(int id) {
		Main.getLogger().log(Level.FINE, "Delete unit with id:" + id);

		Iterator<Device> itDS = devices.iterator();
		Device tDS = null;
		while (itDS.hasNext()) {
			tDS = itDS.next();
			if (tDS.getDataSourceHandler().removeDataSource(id)) {
				return;
			}
			if (tDS.getDataSinkHandler().removeDataSink(id)) {
				return;
			}
		}

		Iterator<LogicBase> itLB = dataProcessors.iterator();
		LogicBase tLB = null;
		while (itLB.hasNext()) {
			tLB = itLB.next();
			if (tLB.getId() == id) {
				unRegisterDataProcessor(tLB);
				tLB.deleteThis();
				return;
			}
		}
		Main.getLogger().log(Level.WARNING, "Removal of unit failed. ID:" + id);

	}

	public DataPoint getDataPoint(long id,String htmlElementID) {
		
		
		/*
		Iterator<LogicBase> dp = dataProcessors.iterator();
		LogicBase dpt = null;
		while (dp.hasNext()) {
			dpt = dp.next();
			if (dpt.getId() == id)
				return dpt;
		}
		 */
		Iterator<Device> devs = devices.iterator();
		DataSourceHandler dataSourceHandler = null;
		DataSinkHandler dataSinkHandler = null;
		Device tempDevice = null;
		while (devs.hasNext()) {
			tempDevice = devs.next();

			dataSourceHandler = tempDevice.getDataSourceHandler();
			Iterator<DataSource> dataSources = dataSourceHandler.getMyDataSources().iterator();
			DataSource dataSourceTemp = null;
			while (dataSources.hasNext()) {
				dataSourceTemp = dataSources.next();
				if (dataSourceTemp.getId() == id) {
					return new DataPointSource(htmlElementID, id, dataSourceTemp);
				}
			}
			dataSinkHandler = tempDevice.getDataSinkHandler();
			Iterator<DataSink> dataSinks = dataSinkHandler.getMyDataSinks().iterator();
			DataSink dataSinkTemp = null;
			while (dataSinks.hasNext()) {
				dataSinkTemp = dataSinks.next();
				if (dataSinkTemp.getId() == id) {
					return new DataPointSink(htmlElementID, id, dataSinkTemp);
				}
			}
		}

		return null;
		
		
	}
	
	
	public Vector<DataSource> getAllDataOutputInterfaces() {
		Vector<DataSource> v = new Vector<DataSource>();
		/*
		 * Iterator<LogicBase> dp = dataProcessors.iterator(); while (dp.hasNext()) {
		 * v.add(dp.next()); }
		 */
		Iterator<Device> devs = devices.iterator();
		DataSourceHandler dataSourceHandler = null;
		Device tempDevice = null;

		while (devs.hasNext()) {
			tempDevice = devs.next();
			dataSourceHandler = tempDevice.getDataSourceHandler();
			Iterator<DataSource> dataSources = dataSourceHandler.getMyDataSources().iterator();
			while (dataSources.hasNext()) {
				v.add(dataSources.next());
			}
		}
		return v;

	}

	public DataOutputIF getDataOutputInterface(long id) {

		Iterator<LogicBase> dp = dataProcessors.iterator();
		LogicBase dpt = null;
		while (dp.hasNext()) {
			dpt = dp.next();
			if (dpt.getId() == id)
				return dpt;
		}

		Iterator<Device> devs = devices.iterator();
		DataSourceHandler dataSourceHandler = null;
		DataSinkHandler dataSinkHandler = null;
		Device tempDevice = null;
		while (devs.hasNext()) {
			tempDevice = devs.next();

			dataSourceHandler = tempDevice.getDataSourceHandler();
			Iterator<DataSource> dataSources = dataSourceHandler.getMyDataSources().iterator();
			DataSource dataSourceTemp = null;
			while (dataSources.hasNext()) {
				dataSourceTemp = dataSources.next();
				if (dataSourceTemp.getId() == id) {
					return dataSourceTemp;
				}
			}
			dataSinkHandler = tempDevice.getDataSinkHandler();
			Iterator<DataSink> dataSinks = dataSinkHandler.getMyDataSinks().iterator();
			DataSink dataSinkTemp = null;
			while (dataSinks.hasNext()) {
				dataSinkTemp = dataSinks.next();
				if (dataSinkTemp.getId() == id) {
					return dataSinkTemp.getOutputDataValue();
				}
			}
		}

		return null;

	}

	public DataInputIF getDataInputInterface(int id) {

		Iterator<LogicBase> dp = dataProcessors.iterator();
		LogicBase dpt = null;
		while (dp.hasNext()) {
			dpt = dp.next();
			if (dpt.getId() == id)
				return dpt;
		}

		Iterator<Device> devs = devices.iterator();
		DataSinkHandler dataSinkHandler = null;
		while (devs.hasNext()) {
			dataSinkHandler = devs.next().getDataSinkHandler();
			Iterator<DataSink> dataSinks = dataSinkHandler.getMyDataSinks().iterator();
			DataSink dataSinkTemp = null;
			while (dataSinks.hasNext()) {
				dataSinkTemp = dataSinks.next();
				if (dataSinkTemp.getId() == id) {
					return dataSinkTemp;
				}
			}
		}

		return null;

	}

	public void registerNewDataProcessor(LogicBase dataProcessor) {
		Main.getLogger().log(Level.FINE, "New DataProcessor registered. ID:" + dataProcessor.getId() + " / class: "
				+ dataProcessor.getClass().getCanonicalName());
		dataProcessors.add(dataProcessor);
	}

	public void unRegisterDataProcessor(LogicBase dataProcessor) {
		dataProcessors.remove(dataProcessor);
	}

	/*
	 * public void registerNewDataSink(DataSink dataSink) {
	 * Main.getLogger().log(Level.FINE, "New DataSink registered. ID:" +
	 * dataSink.getId() + " / class: " + dataSinks.getClass().getCanonicalName());
	 * dataSinks.add(dataSink); }
	 * 
	 * public void unRegisterDataSink(DataSink dataSink) {
	 * dataSinks.remove(dataSink); }
	 */

}
