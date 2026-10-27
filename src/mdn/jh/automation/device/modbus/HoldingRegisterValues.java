package mdn.jh.automation.device.modbus;

public class HoldingRegisterValues extends ModbusValue {

	int[] registerValues=null;
	
	public HoldingRegisterValues() {
		super();
	}
	
	public int[] getRegisterValues() {
		return registerValues;
	}
	public void setRegisterValues(int[] registerValues) {
		this.registerValues = registerValues;
	}

}
