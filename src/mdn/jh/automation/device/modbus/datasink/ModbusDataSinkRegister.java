package mdn.jh.automation.device.modbus.datasink;

import mdn.jh.automation.io.DataComponentStub;
import mdn.jh.automation.io.DataValue;
import mdn.jh.automation.io.logic.InputDefinition;

public class ModbusDataSinkRegister extends ModbusDataSink {

	private static final long serialVersionUID = 3571829315715427478L;
	private static InputDefinition inputDefinitions[] = {
			new InputDefinition("Integer value", DataComponentStub.TYPE_DOUBLE_IO, "Whole-number holding-register value") };

	public ModbusDataSinkRegister() {
		super(inputDefinitions);
		myOutput = new HoldingRegisterValue();
	}

	@Override
	public void updateCycle() {
		if (myModbusDevice == null) {
			setDataValid(false);
			return;
		}
		try {
			double numeric = getInputData(0).getOutputAsNumber();
			if (!Double.isFinite(numeric) || numeric != Math.rint(numeric) || numeric < 0 || numeric > 65535)
				throw new IllegalArgumentException("Holding-register input must be an integer between 0 and 65535");
			int value = (int) numeric;
			myModbusDevice.setHoldingRegister(getAddress(), value);
			myOutput.setValue(value);
			myOutput.setDataValid(true);
		//	setOutputValue(""+value);
			//setStatusMessage(""+value);
			setDataValid(true);
		} catch (Exception e) {
			myOutput.setDataValid(false);
			//setStatusMessage("###");
			setDataValid(false);
			return;
		}
	}

	private static final class HoldingRegisterValue extends DataValue {
		private static final long serialVersionUID = 1L;

		private HoldingRegisterValue() {
			changeDataType(TYPE_DOUBLE_IO);
		}

		@Override
		public String getOutputAsString() {
			return isDataValid() ? Integer.toString((int) getOutputAsNumber()) : "###";
		}
	}


}
