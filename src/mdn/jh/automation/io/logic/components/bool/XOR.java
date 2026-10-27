package mdn.jh.automation.io.logic.components.bool;

import mdn.jh.automation.io.DataComponentStub;
import mdn.jh.automation.io.logic.InputDefinition;
import mdn.jh.automation.io.logic.LogicBaseBoolean;
import mdn.jh.automation.io.logic.LogicComponentDescription;

/**
 * Boolean exclusive OR with exactly two inputs.
 */
public class XOR extends LogicBaseBoolean {

	private static final long serialVersionUID = 5283566091303899267L;

	private static final InputDefinition[] inputDefinitions = {
			new InputDefinition("in 1", DataComponentStub.TYPE_BOOLEAN_IO, "Standard bool input"),
			new InputDefinition("in 2", DataComponentStub.TYPE_BOOLEAN_IO, "Standard bool input")
	};

	public XOR() {
		super(DataComponentStub.TYPE_BOOLEAN_IO, inputDefinitions, new LogicComponentDescription("XOR"));
	}

	@Override
	public void resetSpecific() {
	}

	@Override
	protected void calculateMyActualState() {
		boolean first = myInputValues[0] != null && myInputValues[0].getOutputAsBoolean();
		boolean second = myInputValues[1] != null && myInputValues[1].getOutputAsBoolean();
		myActualOutputState.setValue(first ^ second);
	}
}
