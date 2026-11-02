package mdn.jh.automation.io.logic.components.number;

import mdn.jh.automation.io.DataComponentStub;
import mdn.jh.automation.io.logic.InputDefinition;

/** Subtracts the subtrahend from the minuend. */
public class Difference extends NumericOperation {
	private static final long serialVersionUID = 1L;
	private static final InputDefinition[] INPUTS = {
			new InputDefinition("Minuend", DataComponentStub.TYPE_DOUBLE_IO, "Value to subtract from"),
			new InputDefinition("Subtrahend", DataComponentStub.TYPE_DOUBLE_IO, "Value to subtract") };

	public Difference() {
		super("Difference", INPUTS);
	}

	@Override
	protected double calculateResult() {
		return (hasInput(0) ? inputValue(0) : 0) - (hasInput(1) ? inputValue(1) : 0);
	}
}
