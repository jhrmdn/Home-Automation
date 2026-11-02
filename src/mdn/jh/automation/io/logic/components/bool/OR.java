package mdn.jh.automation.io.logic.components.bool;

import mdn.jh.automation.io.DataComponentStub;
import mdn.jh.automation.io.logic.InputDefinition;
import mdn.jh.automation.io.logic.LogicBaseBoolean;
import mdn.jh.automation.io.logic.LogicComponentDescription;

public class OR extends LogicBaseBoolean {

	private static final long serialVersionUID = 1352191857733511322L;
	private static InputDefinition inputNames[] = {
			new InputDefinition("in", DataComponentStub.TYPE_BOOLEAN_IO, "Standard bool input"),
			new InputDefinition("in", DataComponentStub.TYPE_BOOLEAN_IO, "Standard bool input"),
			new InputDefinition("in", DataComponentStub.TYPE_BOOLEAN_IO, "Standard bool input"),
			new InputDefinition("in", DataComponentStub.TYPE_BOOLEAN_IO, "Standard bool input"),
			new InputDefinition("in", DataComponentStub.TYPE_BOOLEAN_IO, "Standard bool input") };

	public OR() {
		super(DataComponentStub.TYPE_BOOLEAN_IO, inputNames, new LogicComponentDescription("OR"));
	}

	@Override
	public void resetSpecific() {
	}

	@Override
	protected void calculateMyActualState() {

		for (int i = 0; i < myInputValues.length; i++) {
			if (myInputValues[i] != null) {
				if (myInputValues[i].getOutputAsBoolean()) {
					myActualOutputState.setValue(true);
					return;
				}
			}
		}
		myActualOutputState.setValue(false);
	}

}
