package mdn.jh.automation.devices.fritz.datasink;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import mdn.jh.automation.io.DataComponentStub;
import mdn.jh.automation.io.logic.InputDefinition;

/** Writes an integer thermostat setpoint or percentage level to a FRITZ! device. */
public class FritzAnalogValue extends FritzDataSink {
	private static final long serialVersionUID = 1L;
	public static final String MODE_THERMOSTAT = "thermostat";
	public static final String MODE_LEVEL = "level";
	private static final InputDefinition[] INPUTS = {
			new InputDefinition("Setpoint", DataComponentStub.TYPE_DOUBLE_IO, "Integer value to write to the FRITZ! device"),
			new InputDefinition("Apply", DataComponentStub.TYPE_BOOLEAN_IO, "Optional rising edge that applies the current setpoint") };

	private String mode = MODE_LEVEL;
	private boolean writeOnChangeWithTrigger;
	private transient Integer lastObservedSetpoint;
	private transient boolean previousTrigger;

	public FritzAnalogValue() {
		super(INPUTS);
		setStatusMessage("Waiting for setpoint");
		setHelptext("Writes an integer FRITZ! setpoint. Without Apply, changes are written directly. With Apply connected, a rising edge writes the value; optionally changes can write as well.");
	}

	public synchronized void configure(String mode, boolean writeOnChangeWithTrigger) {
		if (!MODE_THERMOSTAT.equals(mode) && !MODE_LEVEL.equals(mode))
			throw new IllegalArgumentException("Unknown FRITZ! analog action mode");
		this.mode = mode;
		this.writeOnChangeWithTrigger = writeOnChangeWithTrigger;
	}

	public synchronized String getMode() { return mode; }
	public synchronized boolean isWriteOnChangeWithTrigger() { return writeOnChangeWithTrigger; }

	@Override
	public synchronized void updateCycle() {
		boolean trigger = myInputValues[1] != null && myInputValues[1].getOutputAsBoolean();
		boolean risingEdge = trigger && !previousTrigger;
		previousTrigger = trigger;
		if (myInputValues[0] == null || !myInputValues[0].isDataValid()) return;

		double input = myInputValues[0].getOutputAsNumber();
		if (!Double.isFinite(input) || input != Math.rint(input)) {
			setStatusMessage("Setpoint must be an integer");
			return;
		}
		int setpoint = (int) input;
		if (!isInRange(setpoint)) {
			setStatusMessage(MODE_LEVEL.equals(mode) ? "Level must be between 0 and 100"
					: "Thermostat setpoint must be 16–56, 253 or 254");
			return;
		}
		boolean changed = lastObservedSetpoint == null || lastObservedSetpoint.intValue() != setpoint;
		boolean triggerConnected = isInputConnected(1);
		boolean write = triggerConnected ? risingEdge || writeOnChangeWithTrigger && changed : changed;
		lastObservedSetpoint = setpoint;
		if (!write) return;

		String response = sendSetpoint(setpoint);
		boolean successful = response != null && !"inval".equalsIgnoreCase(response);
		myOutput.setValue(successful ? Integer.toString(setpoint) : "write failed");
		myOutput.setDataValid(successful);
		setStatusMessage(successful ? "Applied setpoint " + setpoint : "FRITZ! write failed");
	}

	private boolean isInRange(int value) {
		return MODE_LEVEL.equals(mode) ? value >= 0 && value <= 100
				: (value >= 16 && value <= 56) || value == 253 || value == 254;
	}

	protected String sendSetpoint(int value) {
		return MODE_LEVEL.equals(mode) ? execute("setlevelpercentage", "level=" + value)
				: execute("sethkrtsoll", "param=" + value);
	}

	@Override public int[] getActionBits() { return MODE_LEVEL.equals(mode) ? new int[] { 16 } : new int[] { 6 }; }

	@Override
	public synchronized Node getStorageXML(Document document) {
		Element root = (Element) super.getStorageXML(document);
		Element config = document.createElement("AnalogValue");
		config.setAttribute("mode", mode);
		config.setAttribute("writeOnChangeWithTrigger", Boolean.toString(writeOnChangeWithTrigger));
		root.appendChild(config);
		return root;
	}

	@Override
	public synchronized void initSpecific(Node node) {
		super.initSpecific(node);
		NodeList children = node.getChildNodes();
		for (int index = 0; index < children.getLength(); index++) {
			Node child = children.item(index);
			if (!"AnalogValue".equals(child.getNodeName())) continue;
			Node storedMode = child.getAttributes().getNamedItem("mode");
			Node storedBoth = child.getAttributes().getNamedItem("writeOnChangeWithTrigger");
			configure(storedMode == null ? MODE_LEVEL : storedMode.getNodeValue(),
					storedBoth != null && Boolean.parseBoolean(storedBoth.getNodeValue()));
		}
	}
}
