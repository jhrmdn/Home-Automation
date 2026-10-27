package mdn.jh.automation.io;

import java.io.Serializable;

/** Delegates to the connected source while applying a target-port input override. */
public final class ComponentInputView extends DataValue implements Serializable {
	private static final long serialVersionUID = 1L;
	private final DataComponentStub owner; private final int input; private final DataOutputIF delegate;
	public ComponentInputView(DataComponentStub owner,int input,DataOutputIF delegate){this.owner=owner;this.input=input;this.delegate=delegate;}
	@Override public boolean getOutputAsBoolean(){return owner.hasInputOverride(input)?Boolean.parseBoolean(owner.getInputOverrideValue(input)):delegate.getOutputAsBoolean();}
	@Override public double getOutputAsNumber(){return owner.hasInputOverride(input)?Double.parseDouble(owner.getInputOverrideValue(input)):delegate.getOutputAsNumber();}
	@Override public String getOutputAsString(){return owner.hasInputOverride(input)?owner.getInputOverrideValue(input):delegate.getOutputAsString();}
	@Override public boolean isDataValid(){return owner.hasInputOverride(input)||delegate.isDataValid();}
	@Override public int getDataTypeOutput(){return delegate.getDataTypeOutput();}
	@Override public void setValue(boolean value){if(delegate instanceof DataValue)((DataValue)delegate).setValue(value);else super.setValue(value);}
	@Override public void setValue(double value){if(delegate instanceof DataValue)((DataValue)delegate).setValue(value);else super.setValue(value);}
	@Override public void setValue(String value){if(delegate instanceof DataValue)((DataValue)delegate).setValue(value);else super.setValue(value);}
	@Override public void setDataValid(boolean valid){if(delegate instanceof DataValue)((DataValue)delegate).setDataValid(valid);else super.setDataValid(valid);}
	@Override public void changeDataType(int type){if(delegate instanceof DataValue)((DataValue)delegate).changeDataType(type);else super.changeDataType(type);}
}
