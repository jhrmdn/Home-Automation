package mdn.jh.automation.devices.fritz.datasink;

import mdn.jh.automation.devices.fritz.datasource.FritzDataSourceHandler;
import mdn.jh.automation.io.DataComponentStub;
import mdn.jh.automation.io.logic.InputDefinition;

public class FritzSimpleOnOff extends FritzDataSink {
	private static final long serialVersionUID = 5362966370945371120L;
	private static InputDefinition inputDefinitions[] = {
			new InputDefinition("in", DataComponentStub.TYPE_BOOLEAN_IO, "Bool / true=on") };
	boolean lastValue = false;
	boolean newValue = false;
	static String ON = "setswitchon";
	static String OFF = "setswitchoff";

	public FritzSimpleOnOff() {
		super(inputDefinitions);
		setStatusMessage("-");
	}

	@Override
	public void updateCycle() {

		FritzDataSourceHandler f = (FritzDataSourceHandler) getFritzBox().getDataSourceHandler();
		String status = f
				.getNodeValue("//devicelist/device[@identifier='" + getIdentifierOriginal() + "']/simpleonoff/state");
		if ("1".equals(status)) {
			myOutput.setValue("on");
			// setStatusMessage("Switch: On");
		} else {
			myOutput.setValue("off");
			// setStatusMessage("Switch: Off");
		}

		if (myInputValues[0] == null)
			return;

		newValue = myInputValues[0].getOutputAsBoolean();

		if (newValue != lastValue) {
			if (newValue) {
				execute(ON, null);
			} else {
				execute(OFF, null);
			}
		}
		lastValue = newValue;

	}

	/*
	 * @Override public Node getStorageXML(Document doc) { Element rootElement =
	 * null; rootElement = doc.createElement("DataSink");
	 * rootElement.setAttribute("id", "" + getId());
	 * rootElement.setAttribute("class", this.getClass().getCanonicalName());
	 * rootElement.appendChild(getDataComponentBaseXML(doc));
	 * 
	 * 
	 * rootElement.appendChild(super.getStorageXML(doc)); return rootElement; }
	 * 
	 */
	@Override
	public int[] getActionBits() {
		int bitsWithAction[] = { 2, 6, 9, 15, 16, 17, 18 };
		return bitsWithAction;
	}

}
