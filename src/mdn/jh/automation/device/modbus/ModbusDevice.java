package mdn.jh.automation.device.modbus;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.intelligt.modbus.jlibmodbus.Modbus;
import com.intelligt.modbus.jlibmodbus.exception.ModbusIOException;
import com.intelligt.modbus.jlibmodbus.master.ModbusMaster;
import com.intelligt.modbus.jlibmodbus.master.ModbusMasterFactory;
import com.intelligt.modbus.jlibmodbus.tcp.TcpParameters;

import mdn.jh.automation.Main;
import mdn.jh.automation.device.DataSinkHandler;
import mdn.jh.automation.device.DataSourceHandler;
import mdn.jh.automation.device.Device;
import mdn.jh.automation.device.modbus.datasink.ModbusDataSinkCreator;
import mdn.jh.automation.device.modbus.datasink.ModbusDataSinkHandler;
import mdn.jh.automation.device.modbus.datasource.ModbusDataSourceCreator;
import mdn.jh.automation.device.modbus.datasource.ModbusDataSourceHandler;
import mdn.jh.automation.gui.DataSinkCreator;
import mdn.jh.automation.gui.DataSourceCreator;

public class ModbusDevice extends Device {

	ModbusMaster modbusMaster = null;
	String ip_hostname = "";
	boolean dataValid = true;
	int maxErrorCountBeforeInvalid = 6;

	public String getIp_hostname() {
		return ip_hostname;
	}

	public int getPort() {
		return port;
	}

	public int getSlaveID() {
		return slaveID;
	}

	int port = 502;
	int slaveID = 1;
//	private ModbusDataSinkHandler dataSinkHandler = null;
//	private ModbusDataSourceHandler dataSourceHandler = null;
	public static final int TYPE_COIL_RW = 1;
	public static final int TYPE_REGISTER_DISCRETE_R = 2;
	public static final int TYPE_HOLDING_RW = 3;
	public static final int TYPE_INPUT_R = 4;

	public static String getTypeAsString(int type) {
		switch (type) {
		case ModbusDevice.TYPE_COIL_RW:
			return "Coil";
		case ModbusDevice.TYPE_REGISTER_DISCRETE_R:
			return "Discrete Register";
		case ModbusDevice.TYPE_HOLDING_RW:
			return "Holding Register";
		case ModbusDevice.TYPE_INPUT_R:
			return "Input Register";
		default:
			return "UNKNOWN";
		}
	}

	public ModbusDevice() {
		super(TYPE_MODBUS);
		dataSourceHandler = new ModbusDataSourceHandler(this);
		dataSinkHandler = new ModbusDataSinkHandler(this);
	}

	public ModbusDevice(Node node) throws Exception {
		super(TYPE_MODBUS, false);
		initDataComponent(node);
	}

	/**
	 * Modbus connection data
	 * 
	 * @param ip_hostname
	 * @param port
	 * @param slaveID
	 */
	public void setConnectionData(String ip_hostname, int port, int slaveID) {
		resetModbusMaster();
		this.ip_hostname = ip_hostname;
		this.port = port;
		this.slaveID = slaveID;
	}

	public void setConnectionData(String ip_hostname, String port, String slaveID) throws Exception {
		setConnectionData(ip_hostname, Integer.valueOf(port), Integer.valueOf(slaveID));
	}

	private synchronized void resetModbusMaster() {
		if (modbusMaster != null && modbusMaster.isConnected()) {
			try {
				modbusMaster.disconnect();
			} catch (ModbusIOException e) {
				Main.getLogger().log(Level.WARNING, "Failed to close previous Modbus connection", e);
			}
		}
		modbusMaster = null;
		dataValid = true;
	}

	@Override
	public DataSourceHandler getDataSourceHandler() {
		return dataSourceHandler;
	}

	@Override
	public DataSinkHandler getDataSinkHandler() {
		return dataSinkHandler;
	}

	@Override
	public DataSourceCreator getDataSourceCreator() {
		return new ModbusDataSourceCreator(this);
	}

	@Override
	public DataSinkCreator getDataSinkCreator() {
		return new ModbusDataSinkCreator(this);
	}

	// #############################################################################################

	@Override
	public String getName() {
		return "Modbus: " + ip_hostname + " (Port: " + port + " Slave-ID: " + slaveID + ")";
	}

	/**
	 * Get the value from Coil 'number'
	 * 
	 * @param address
	 * @return
	 * @throws Exception
	 */
	public boolean getCoil(int address) throws Exception {
		if (!dataValid) {
			throw new Exception("Data from Modbus Device invalid");
		}
		int quantity = 1;
		boolean[] registerValues = getModbusMaster().readCoils(slaveID, address, quantity);
		return registerValues[0];
	}

	/**
	 * Get the value from discrete input 'number'
	 * 
	 * @param address
	 * @return
	 * @throws Exception
	 */
	public boolean getDiscreteInput(int address) throws Exception {
		if (!dataValid) {
			throw new Exception("Data from Modbus Device invalid");
		}

		int quantity = 1;
		boolean[] registerValues = getModbusMaster().readDiscreteInputs(slaveID, address, quantity);
		return registerValues[0];
	}

	public void setCoil(int address, boolean value) throws Exception {
		if (!dataValid) {
			throw new Exception("Data from Modbus Device invalid");
		}
		boolean[] val = { value };
		getModbusMaster().writeMultipleCoils(getSlaveID(), address, val);
	}

	/**
	 * Get the value from Holding Register 'number'
	 * 
	 * @param address
	 * @return
	 * @throws Exception
	 */
	public int getHoldingRegister(int address) throws Exception {
		if (!dataValid) {
			throw new Exception("Data from Modbus Device invalid");
		}
		int quantity = 1;
		int[] registerValues = getModbusMaster().readHoldingRegisters(slaveID, address, quantity);
		return registerValues[0];
	}

	public void setHoldingRegister(int address, int value) throws Exception {
		if (!dataValid) {
			throw new Exception("Data from Modbus Device invalid");
		}

		int[] val = { value };
		getModbusMaster().writeMultipleRegisters(getSlaveID(), address, val);
	}

	/**
	 * Get the value from input 'number'
	 * 
	 * @param address
	 * @return
	 * @throws Exception
	 */
	public int getInputRegister(int address) throws Exception {
		if (!dataValid) {
			throw new Exception("Data from Modbus Device invalid");
		}
		int quantity = 1;
		int[] registerValues = getModbusMaster().readInputRegisters(slaveID, address, quantity);
		return registerValues[0];
	}

	public boolean isConnected() {
		ModbusMaster mo = getModbusMaster();
		return mo != null && mo.isConnected();
	}

	int errorCounter = 0;

	public synchronized ModbusMaster getModbusMaster() {
		if (modbusMaster == null) {
			initModbus();
			if (modbusMaster == null) {
				return null;
			}
			try {
				modbusMaster.connect();
			} catch (ModbusIOException e) {
				Main.getLogger().log(Level.WARNING, "Failed to connect to Modbus device " + ip_hostname + ":" + port, e);
			}
		}
		return modbusMaster;
	}

	private void initModbus() {
		TcpParameters tcpParameters = new TcpParameters();
		try {
			tcpParameters.setHost(InetAddress.getByName(ip_hostname));
			tcpParameters.setPort(port);
			// timeout in milliseconds
			tcpParameters.setConnectionTimeout(5000);
			
		} catch (UnknownHostException e) {
			Main.getLogger().log(Level.SEVERE,
					"ModbusDevice wrong parameter. Host:" + ip_hostname + " - Port: " + port);
			// not counting errors forever
			dataValid = false;
			return;
		}

		tcpParameters.setKeepAlive(true);

		modbusMaster = ModbusMasterFactory.createModbusMasterTCP(tcpParameters);

		if (modbusMaster == null) {
			// if 6 times wrong give out error and count +1 that the error is only given
			// once
			if (errorCounter <= maxErrorCountBeforeInvalid) {
				errorCounter++;
				return;
			}	
			if (errorCounter == maxErrorCountBeforeInvalid + 1) {
					dataValid = false;
					errorCounter++;
				Main.getLogger().log(Level.SEVERE, "ModbusDevice Data Invalid due to connection failure. Host:"
						+ ip_hostname + " - Port: " + port);
				return;
			}
		}

		Modbus.setAutoIncrementTransactionId(true);
		if (errorCounter == maxErrorCountBeforeInvalid + 2) {
			Main.getLogger().log(Level.INFO, "ModbusDevice Data valid again. Host:" + ip_hostname + " - Port: " + port);
		}

		errorCounter = 0;
		dataValid = true;

	}

	@Override
	public boolean initSpecific(Node node) throws Exception {
		if (node == null)
			return false;

		NodeList it = node.getChildNodes();
		Node temp = null;
		for (int i = 0; i < it.getLength(); i++) {
			temp = it.item(i);
			if ("ModbusClient".equals(temp.getNodeName()))
				break;
			else {
				temp = null;
			}
		}

		if (temp == null) {
			return false;
			// throw new Exception("Creating - expected Node: ModbusClient - Found:" +
			// node.getNodeName());
		}

		NamedNodeMap attributes = temp.getAttributes();
		String ip = attributes.getNamedItem("ip_host").getNodeValue();
		String pt = attributes.getNamedItem("port").getNodeValue();
		String sl = attributes.getNamedItem("slaveID").getNodeValue();
		try {
			setConnectionData(ip, pt, sl);

		} catch (Exception e) {
			Main.getLogger().log(Level.SEVERE,
					"Failed to read Data for ModbusClient from XML File. Device id:" + getDeviceID());
			return false;
		}

		NodeList nl = temp.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			temp = nl.item(i);

			if ("DataSources".equals(temp.getNodeName())) {
				Main.getLogger().log(Level.INFO, "Add Modbus DataSourceHandler");
				dataSourceHandler = new ModbusDataSourceHandler(this, temp);
			}

			if ("DataSinks".equals(temp.getNodeName())) {
				Main.getLogger().log(Level.INFO, "Add Modbus DataSinkHandler");
				dataSinkHandler = new ModbusDataSinkHandler(this, temp);
			}
		}
		return true;
	}

	@Override
	public Node getSpecificStorage(Document doc) {
		Element rootElement = null;
		rootElement = doc.createElement("ModbusClient");
		// rootElement.setAttribute("deviceID", "" + getDeviceID());
		rootElement.setAttribute("ip_host", "" + ip_hostname);
		rootElement.setAttribute("port", "" + Integer.toString(port));
		rootElement.setAttribute("slaveID", "" + Integer.toString(slaveID));
		// rootElement.appendChild(dataSourceHandler.getStorageXML(doc));
		// rootElement.appendChild(dataSinkHandler.getStorageXML(doc));

		return rootElement;
	}

}
