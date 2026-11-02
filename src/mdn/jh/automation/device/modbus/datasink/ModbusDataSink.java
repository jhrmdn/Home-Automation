package mdn.jh.automation.device.modbus.datasink;

import javax.swing.JPanel;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import mdn.jh.automation.device.modbus.ModbusDevice;
import mdn.jh.automation.io.logic.InputDefinition;
import mdn.jh.automation.io.sink.DataSink;

public abstract class ModbusDataSink extends DataSink {
	protected ModbusDevice myModbusDevice = null;
	protected boolean valid = true;
	private boolean address_valid = true;
	private static final long serialVersionUID = 1145165271099862336L;
	private int modbusDataType = 1;
	private int address = 0;

	public ModbusDataSink(InputDefinition[] inputDefinitions) {
		super(inputDefinitions);
	}

	public void setType(int type) {
		this.modbusDataType = type;
		createStatusMessage();
	}

	public void setAddress(int address) {
		if (address < 0 || address > 65535) throw new IllegalArgumentException("Modbus address must be between 0 and 65535");
		this.address = address;
		createStatusMessage();
	}

	public int getAddress() {
		return address;
	}
	
	protected void createStatusMessage() {
		String status = "Type: " + ModbusDevice.getTypeAsString(modbusDataType) + "\n";
		status = status + "Address: " + address;
		setStatusMessage(status);
	}
	public void setModbusDevice(ModbusDevice modbusDevice) {
		this.myModbusDevice = modbusDevice;
	}

	protected void setDataValid(boolean valid) {
		this.valid = valid;
	}

	@Override
	public JPanel getSpecificDetailsPanel() {
		return new ModbusDataSinkDetail(this);
	}

	@Override
	public boolean isDataValid() {
		return valid && address_valid;
	}

	@Override
	public Node getStorageXML(Document doc) {
		Element rootElement = null;

		rootElement = getStorageDataSink(doc);
		//rootElement.appendChild(getDataComponentBaseXML(doc));

		Element e = doc.createElement("ModbusParameter");
		e.setAttribute("type", "" + modbusDataType);
		e.setAttribute("address", "" + address);
		rootElement.appendChild(e);

		return rootElement;
	}

	@Override
	public void initSpecific(Node node) {

		if (node == null)
			return;

		NodeList ndp = node.getChildNodes();
		Node temp = null;
		for (int k = 0; k < ndp.getLength(); k++) {
			temp = ndp.item(k);

			if ("ModbusParameter".equals(temp.getNodeName())) {

				try {
					this.modbusDataType = Integer.parseInt(temp.getAttributes().getNamedItem("type").getNodeValue());
					setAddress(Integer.parseInt(temp.getAttributes().getNamedItem("address").getNodeValue()));

				} catch (Exception e) {
					address_valid = false;
					return;
				}

			}
		}
		createStatusMessage();
	}

	/**
	 * The Modbus Type as String Taken from ModbusDevice
	 * 
	 * @return
	 */
	public String getTypeAsString() {
		return ModbusDevice.getTypeAsString(modbusDataType);
	}

}
