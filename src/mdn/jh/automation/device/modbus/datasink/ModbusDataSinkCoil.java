package mdn.jh.automation.device.modbus.datasink;

import mdn.jh.automation.io.DataComponentStub;
import mdn.jh.automation.io.logic.InputDefinition;

public class ModbusDataSinkCoil extends ModbusDataSink {

	private static final long serialVersionUID = 3571829315715427478L;
	private static InputDefinition inputDefinitions[] = {
			new InputDefinition("in", DataComponentStub.TYPE_BOOLEAN_IO, "Bool / true=on") };

	public ModbusDataSinkCoil() {
		super(inputDefinitions);
	}

	@Override
	public void updateCycle() {
		if (myModbusDevice == null) {
			setDataValid(false);
			return;
		}
		try {
			boolean value = getInputData(0).getOutputAsBoolean();
			myModbusDevice.setCoil(getAddress(), value);
			if(value) {
				myOutput.setValue("on");
			//	setOutputValue("on");
		//		setStatusMessage("true");
			}else {
				myOutput.setValue("off");
			//	setOutputValue("Off");
		//		setStatusMessage("false");
			}
			setDataValid(true);
		} catch (Exception e) {
			myOutput.setValue("###");
			//setStatusMessage("###");
			setDataValid(false);
			return;
		}
	}



}
