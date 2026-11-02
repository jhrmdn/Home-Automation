package mdn.jh.automation.io;

import java.io.Serializable;

/**
 * Class to keep a value. Type will be selected by the sending and receiving
 * DataSource
 */

public class DataValue implements DataOutputIF, Serializable {

	private static final long serialVersionUID = -8139315778921123952L;
	private int definedType = DataComponentStub.TYPE_STRING_IO;
	private boolean valueBoolean = false;
	private double valueDouble = 0;
	private String valueString = null;
	private boolean dataValid = false;

	/**
	 * Default is String Type; Value=null;
	 */
	public DataValue() {
	}

	public DataValue(boolean valueBoolean) {
		setValue(valueBoolean);
	}

	public DataValue(double valueDouble) {
		setValue(valueDouble);
	}

	public DataValue(String valueString) {
		setValue(valueString);
	}

	public void setValue(boolean valueBoolean) {
		this.valueBoolean = valueBoolean;
		definedType = DataComponentStub.TYPE_BOOLEAN_IO;
	}

	public void setValue(double valueDouble) {
		this.valueDouble = valueDouble;
		definedType = DataComponentStub.TYPE_DOUBLE_IO;
	}

	public void setValue(String value) {
		this.valueString = value;
	}

	public void changeDataType(int definedType) {
		this.definedType = definedType;
	}

	@Override
	public boolean getOutputAsBoolean() {
		return valueBoolean;
	}

	@Override
	public double getOutputAsNumber() {
		return valueDouble;
	}

	@Override
	public String getOutputAsString() {
		switch (definedType) {
		case DataComponentStub.TYPE_BOOLEAN_IO:
			return "" + valueBoolean;
		case DataComponentStub.TYPE_DOUBLE_IO:
			return "" + valueDouble;
		}

		return valueString;
	}

	@Override
	public int getDataTypeOutput() {
		return definedType;
	}

	@Override
	public boolean isDataValid() {
		return dataValid;
	}

	public void setDataValid(boolean dataValid) {
		this.dataValid = dataValid;
	}

}
