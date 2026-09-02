package mdn.jh.automation.device.modbus.datasink;

import java.util.Iterator;

import org.w3c.dom.Node;

import mdn.jh.automation.device.DataSinkHandler;
import mdn.jh.automation.device.modbus.ModbusDevice;
import mdn.jh.automation.io.sink.DataSink;

public class ModbusDataSinkHandler extends DataSinkHandler {

	private static final long serialVersionUID = 7559398588101329210L;
	private ModbusDevice myModbusDevice = null;

	public ModbusDataSinkHandler(ModbusDevice modbusDevice) {
		super();
		this.myModbusDevice = modbusDevice;
	}

	public boolean initDataComponent(Node node) throws Exception {

		boolean ok = super.initDataComponent(node);

		Iterator<DataSink> it = getMyDataSinks().iterator();
		ModbusDataSink modbusDataSink = null;
		while (it.hasNext()) {
			modbusDataSink = (ModbusDataSink) it.next();
			modbusDataSink.setModbusDevice(myModbusDevice);
		}

		return ok;

	}

	public ModbusDataSinkHandler(ModbusDevice modebusDevice, Node node) throws Exception {
		super();
		this.myModbusDevice = modebusDevice;
		initDataComponent(node);
	}

	@Override
	protected String getDataSinkDetail() {
		return myModbusDevice.getName();
	}

}
