package mdn.jh.automation.device.modbus.datasink;

import mdn.jh.automation.io.DataComponentStub;
import mdn.jh.automation.io.logic.InputDefinition;

public class ModbusDataSinkRegister extends ModbusDataSink {

	private static final long serialVersionUID = 3571829315715427478L;
	private static InputDefinition inputDefinitions[] = {
			new InputDefinition("in", DataComponentStub.TYPE_DOUBLE_IO, "Number") };

	public ModbusDataSinkRegister() {
		super(inputDefinitions);
		myOutput.changeDataType(TYPE_DOUBLE_IO);
	}

	@Override
	public void updateCycle() {
		if (myModbusDevice == null) {
			setDataValid(false);
			return;
		}
		try {
			int value = (int) getInputData(0).getOutputAsNumber();
			myModbusDevice.setHoldingRegister(getAddress(), value);
			myOutput.setValue(value);
		//	setOutputValue(""+value);
			//setStatusMessage(""+value);
			setDataValid(true);
		} catch (Exception e) {
			myOutput.setValue("###");
			//setStatusMessage("###");
			setDataValid(false);
			return;
		}
	}


}
