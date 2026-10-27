package mdn.jh.automation.io.logic.components.bool;

import mdn.jh.automation.io.DataComponentStub;
import mdn.jh.automation.io.logic.InputDefinition;
import mdn.jh.automation.io.logic.LogicBaseBoolean;
import mdn.jh.automation.io.logic.LogicComponentDescription;

public class INVERT extends LogicBaseBoolean {
	private static final long serialVersionUID = 2852710705865612460L;
	private static InputDefinition inputNames[] = {
			new InputDefinition("in", DataComponentStub.TYPE_BOOLEAN_IO, "Standard bool input") };

	public INVERT() {
		super(DataComponentStub.TYPE_BOOLEAN_IO, inputNames, new LogicComponentDescription("INVERT"));
	}

	@Override
	public void resetSpecific() {
	}

	@Override
	protected void calculateMyActualState() {

		myActualOutputState.setValue(!myInputValues[0].getOutputAsBoolean());

	}
}
