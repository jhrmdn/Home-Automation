package mdn.jh.automation.io;

import java.io.Serializable;

public interface DataOutputIF extends Serializable {

	public boolean getOutputAsBoolean();

	public double getOutputAsNumber();

	public String getOutputAsString();

	public boolean isDataValid();

	//public int getId();
	
	/**
	 * DataType defined in class {@link mdn.jh.automation.io.DataComponentStub} . The datatype which is set
	 * 
	 * @return
	 */
	public int getDataTypeOutput();
}
