package mdn.jh.automation.device.modbus.datasource;

import javax.swing.JPanel;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import mdn.jh.automation.device.modbus.ModbusDevice;
import mdn.jh.automation.io.DataComponentStub;
import mdn.jh.automation.io.source.DataSource;

public class ModbusDataSource extends DataSource {

	private static final long serialVersionUID = 785548350074344154L;
	// 1=coil, 2=registervalue
	private int modbusDataType = 1;
	private int address = 0;
	int value = 0;
	boolean valid = true;

	public ModbusDataSource() {
		super();
		// setMyDataValue(this);
	}

	// public ModbusDataSource(boolean withNewID) {
	// super(withNewID);
	// }

	public ModbusDataSource(int type, int address) {
		super();
		this.modbusDataType = type;
		this.address = address;
		// setMyDataValue(this);
		createStatusMessage();
	}

	/**
	 * The type of modbus register / coil
	 * 
	 * @return
	 */
	public int getModbusDataType() {
		return modbusDataType;
	}

	protected void createStatusMessage() {
		String status = "Type: " + ModbusDevice.getTypeAsString(modbusDataType) + "\n";
		status = status + "Address: " + address;
		setStatusMessage(status);
	}

	/**
	 * The Modbus Type as String Taken from ModbusDevice
	 * 
	 * @return
	 */
	public String getTypeAsString() {
		return ModbusDevice.getTypeAsString(modbusDataType);
	}

	public int getAddress() {
		return address;
	}

	public void updateDiscrete(boolean onOff) {
		if (onOff)
			value = 1;
		else
			value = 0;
		updateMyOutputs();

	}

	public void updateRegisterValue(int value) {
		this.value = value;
		updateMyOutputs();
	}

	@Override
	public boolean getOutputAsBoolean() {
		return (value == 1);
	}

	@Override
	public double getOutputAsNumber() {
		return value;
	}

	@Override
	public String getOutputAsString() {
		if (modbusDataType == ModbusDevice.TYPE_COIL_RW) {
			if (value == 1)
				return "On";
			else
				return "Off";
		}
		return Integer.toString(value);
	}

	@Override
	public Node getStorageXML(Document doc) {
		Element rootElement = null;

		rootElement = getStorage(doc);
		// rootElement.appendChild(getDataComponentBaseXML(doc));

		Element e = doc.createElement("ModbusParameter");
		e.setAttribute("type", "" + modbusDataType);
		e.setAttribute("address", "" + address);
		rootElement.appendChild(e);

		return rootElement;
	}

	public boolean initDataComponent(Node node) throws Exception {
		if (node == null)
			return false;
		String name = node.getNodeName();
		if (!"DataSource".equals(name)) {
			throw new Exception("Creating - Expected Node: DataSource - Found:" + name);
		}

		initDataSource(node);

		// String nodeID = node.getAttributes().getNamedItem("id").getNodeValue();
		// overrideID(Integer.valueOf(nodeID));

		NodeList nl = node.getChildNodes();
		Node temp = null;
		for (int i = 0; i < nl.getLength(); i++) {
			temp = nl.item(i);

			if ("ModbusParameter".equals(temp.getNodeName())) {

				try {
					modbusDataType = Integer.valueOf(temp.getAttributes().getNamedItem("type").getNodeValue());
					address = Integer.valueOf(temp.getAttributes().getNamedItem("address").getNodeValue());

				} catch (Exception e) {
					setStatusMessage("INVALID");
					throw new Exception("Creating Modbus Node - Missing parameter type or address");

				}
			}

		}
		createStatusMessage();
		return true;
	}

	@Override
	public JPanel getSpecificDetailsPanel() {
		ModbusSourceDetailsPanel m = new ModbusSourceDetailsPanel();
		m.setModbusDataSource(this);
		return m;
	}

	@Override
	public int getDataTypeOutput() {
		switch (modbusDataType) {
		case ModbusDevice.TYPE_COIL_RW:
			return DataComponentStub.TYPE_BOOLEAN_IO;
		case ModbusDevice.TYPE_REGISTER_DISCRETE_R:
			return DataComponentStub.TYPE_BOOLEAN_IO;
		case ModbusDevice.TYPE_INPUT_R:
			return DataComponentStub.TYPE_DOUBLE_IO;
		case ModbusDevice.TYPE_HOLDING_RW:
			return DataComponentStub.TYPE_DOUBLE_IO;

		default:
			return DataComponentStub.TYPE_NOT_SET;
		}
	}

	public void setDataValid(boolean valid) {
		this.valid = valid;
	}

	@Override
	public boolean isDataValid() {
		return valid;
	}

}
