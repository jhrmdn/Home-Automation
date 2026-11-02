package mdn.jh.automation.io.logic.components.bool;

import mdn.jh.automation.io.DataComponentStub;
import mdn.jh.automation.io.logic.InputDefinition;
import mdn.jh.automation.io.logic.LogicBaseBoolean;
import mdn.jh.automation.io.logic.LogicComponentDescription;

public class AND extends LogicBaseBoolean {

	private static final long serialVersionUID = -7242183695373123639L;

	private static InputDefinition inputDefinitions[] = {
			new InputDefinition("in", DataComponentStub.TYPE_BOOLEAN_IO, "Standard bool input"),
			new InputDefinition("in", DataComponentStub.TYPE_BOOLEAN_IO, "Standard bool input"),
			new InputDefinition("in", DataComponentStub.TYPE_BOOLEAN_IO, "Standard bool input"),
			new InputDefinition("in", DataComponentStub.TYPE_BOOLEAN_IO, "Standard bool input"),
			new InputDefinition("in", DataComponentStub.TYPE_BOOLEAN_IO, "Standard bool input")
	};

	public AND() {
		super(DataComponentStub.TYPE_BOOLEAN_IO, inputDefinitions, new LogicComponentDescription("AND"));
	}
	
	

	@Override
	public void resetSpecific() {
	}

	@Override
	protected void calculateMyActualState() {
		for (int i = 0; i < myInputValues.length; i++) {
			if (myInputValues[i] != null) {
				if (!myInputValues[i].getOutputAsBoolean()) {
					myActualOutputState.setValue(false);
					return;
				}
			}
		}
		myActualOutputState.setValue(true);
	}


}
