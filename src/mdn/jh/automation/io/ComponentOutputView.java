package mdn.jh.automation.io;

import java.io.Serializable;

/** Delegates to a component output while applying its configured runtime override. */
public final class ComponentOutputView implements DataOutputIF, Serializable {
	private static final long serialVersionUID = 1L;
	private final DataComponentStub owner;
	private final DataOutputIF delegate;
	public ComponentOutputView(DataComponentStub owner, DataOutputIF delegate){this.owner=owner;this.delegate=delegate;}
	@Override public boolean getOutputAsBoolean(){return owner.hasOutputOverride()?Boolean.parseBoolean(owner.getOutputOverrideValue()):delegate.getOutputAsBoolean();}
	@Override public double getOutputAsNumber(){return owner.hasOutputOverride()?Double.parseDouble(owner.getOutputOverrideValue()):delegate.getOutputAsNumber();}
	@Override public String getOutputAsString(){return owner.hasOutputOverride()?owner.getOutputOverrideValue():delegate.getOutputAsString();}
	@Override public boolean isDataValid(){return owner.hasOutputOverride()||delegate.isDataValid();}
	@Override public int getDataTypeOutput(){return delegate.getDataTypeOutput();}
}
