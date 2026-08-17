package mdn.jh.automation.devices.shelly;

import javax.swing.JPanel;
import org.w3c.dom.*;
import mdn.jh.automation.io.DataComponentStub;
import mdn.jh.automation.io.logic.InputDefinition;
import mdn.jh.automation.io.sink.DataSink;

public class ShellyDataSink extends DataSink {
	private static final long serialVersionUID = 1L;
	private static final InputDefinition[] INPUTS = {
			new InputDefinition("in", DataComponentStub.TYPE_BOOLEAN_IO, "Actor on/off") };
	private int channel;
	private transient ShellyDevice device;
	private Boolean last;
	private boolean valid;

	public ShellyDataSink() {
		super(INPUTS);
	}

	public ShellyDataSink(ShellyDevice d, int c) {
		this();
		device = d;
		channel = c;
		setStatusMessage((d.isCoverActor(c) ? "Cover" : "Relay") + " channel " + c);
	}

	public void setDevice(ShellyDevice d) {
		device = d;
		setStatusMessage((d != null && d.isCoverActor(channel) ? "Cover" : "Relay") + " channel " + channel);
	}

	@Override
	public void updateCycle() {
		if (myInputValues[0] == null)
			return;
		boolean v = myInputValues[0].getOutputAsBoolean();
		if (last != null && last == v)
			return;
		try {
			device.setActor(channel, v);
			last = v;
			valid = true;
			myOutput.setValue(device.isCoverActor(channel) ? (v ? "open" : "closed") : (v ? "on" : "off"));
		} catch (Exception e) {
			valid = false;
			myOutput.setValue("###");
		}
	}

	@Override
	public boolean isDataValid() {
		return valid;
	}

	@Override
	public JPanel getSpecificDetailsPanel() {
		return null;
	}

	@Override
	public Node getStorageXML(Document d) {
		Element r = getStorageDataSink(d), e = d.createElement("ShellyActor");
		e.setAttribute("channel", Integer.toString(channel));
		r.appendChild(e);
		return r;
	}

	@Override
	public void initSpecific(Node n) {
		NodeList l = n.getChildNodes();
		for (int i = 0; i < l.getLength(); i++)
			if ("ShellyActor".equals(l.item(i).getNodeName()))
				channel = Integer.parseInt(l.item(i).getAttributes().getNamedItem("channel").getNodeValue());
		setStatusMessage((device != null && device.isCoverActor(channel) ? "Cover" : "Relay") + " channel " + channel);
	}
}
