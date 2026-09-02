package mdn.jh.automation.device.modbus.datasource;

import java.util.Iterator;

import org.w3c.dom.Node;

import mdn.jh.automation.device.DataSourceHandler;
import mdn.jh.automation.device.modbus.ModbusDevice;
import mdn.jh.automation.io.source.DataSource;

public class ModbusDataSourceHandler extends DataSourceHandler {

	private static final long serialVersionUID = -544430296862377259L;
	private ModbusDevice myModbusDevice = null;

	public ModbusDataSourceHandler(ModbusDevice modebusDevice) {
		super();
		this.myModbusDevice = modebusDevice;
	}

	public ModbusDataSourceHandler(ModbusDevice modebusDevice, Node node) throws Exception {
		super();
		this.myModbusDevice = modebusDevice;
		initDataComponent(node);
	}

	@Override
	public void update() {
		Iterator<DataSource> it = getMyDataSources().iterator();
		ModbusDataSource modbusDataSource = null;

		if (myModbusDevice == null) {
			while (it.hasNext()) {
				modbusDataSource = (ModbusDataSource) it.next();
				modbusDataSource.setDataValid(false);

			}
			return;
		}

		while (it.hasNext()) {
			modbusDataSource = (ModbusDataSource) it.next();
			int type = modbusDataSource.getModbusDataType();
			try {
				if (type == ModbusDevice.TYPE_COIL_RW) {
					modbusDataSource.updateDiscrete(myModbusDevice.getCoil(modbusDataSource.getAddress()));
				} else if (type == ModbusDevice.TYPE_REGISTER_DISCRETE_R) {
					modbusDataSource.updateDiscrete(myModbusDevice.getDiscreteInput(modbusDataSource.getAddress()));
				} else if (type == ModbusDevice.TYPE_HOLDING_RW) {
					modbusDataSource
							.updateRegisterValue(myModbusDevice.getHoldingRegister(modbusDataSource.getAddress()));
				} else if (type == ModbusDevice.TYPE_INPUT_R) {
					modbusDataSource
							.updateRegisterValue(myModbusDevice.getInputRegister(modbusDataSource.getAddress()));
				}

				modbusDataSource.setDataValid(true);

			} catch (Exception e) {
				modbusDataSource.setDataValid(false);
			}

		}

	}

	@Override
	protected String getDataSourceDetail() {
		if (myModbusDevice == null)
			return "Error";
		return myModbusDevice.getName();
	}

}
