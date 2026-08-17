package mdn.jh.automation.io.logic;

public abstract class LogicBaseBoolean extends LogicBase {

	private static final long serialVersionUID = 253471254945747913L;

	public LogicBaseBoolean(int dataTypeOutput, InputDefinition[] inputs, LogicComponentDescription logicComponentDescription) {
		super(dataTypeOutput,inputs, logicComponentDescription);
	}

	
	
	@Override
	public int getDataTypeOutput() {
		return TYPE_BOOLEAN_IO;
	}
}
