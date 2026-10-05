package mdn.jh.automation.device.modbus;

public class CoilValues extends ModbusValue {

	boolean[] registerValues=null;
	public CoilValues() {
		super();
	}
	
	
	
	public boolean[] getRegisterValues() {
		return registerValues;
	}
	public void setRegisterValues(boolean[] registerValues) {
		this.registerValues = registerValues;
	}
	
	
	

}
