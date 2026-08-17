package mdn.jh.automation.io.logic.components.number;

import mdn.jh.automation.io.DataComponentStub;
import mdn.jh.automation.io.logic.InputDefinition;

/** Divides the dividend by the divisor. */
public class Division extends NumericOperation {
	private static final long serialVersionUID = 1L;
	private static final InputDefinition[] INPUTS = {
			new InputDefinition("Dividend", DataComponentStub.TYPE_DOUBLE_IO, "Value to divide"),
			new InputDefinition("Divisor", DataComponentStub.TYPE_DOUBLE_IO, "Value to divide by") };

	public Division() {
		super("Division", INPUTS);
	}

	@Override
	protected double calculateResult() {
		if (!hasInput(0) || !hasInput(1) || inputValue(1) == 0) return Double.NaN;
		return inputValue(0) / inputValue(1);
	}
}
