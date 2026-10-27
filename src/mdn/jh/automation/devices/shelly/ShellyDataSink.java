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
	private String componentKey = "";
	private boolean analog;
	private Double last;
	private boolean valid;

	public ShellyDataSink() {
		super(INPUTS);
	}

	public ShellyDataSink(ShellyDevice d, int c) {
        this(d,c,"",false);
    }
    public ShellyDataSink(ShellyDevice d,int c,String key,boolean analog) {
        this();device=d;channel=c;componentKey=key;this.analog=analog;
        configureInputs();setStatusMessage("Output "+(key.isEmpty()?c:key));
    }
    private void configureInputs(){
        defineInputs(analog?new InputDefinition[]{new InputDefinition("Level (%)",DataComponentStub.TYPE_DOUBLE_IO,"Output level 0–100%")}:INPUTS);
    }
    public String getComponentKey(){return componentKey.isEmpty()?Integer.toString(channel):componentKey;}

	public void setDevice(ShellyDevice d) {
		device = d;
		setStatusMessage((d != null && d.isCoverActor(channel) ? "Cover" : "Relay") + " channel " + channel);
	}

	@Override
	public void updateCycle() {
		if (myInputValues[0] == null)
			return;
		double v = analog ? myInputValues[0].getOutputAsNumber() : (myInputValues[0].getOutputAsBoolean()?1:0);
		if (last != null && last == v)
			return;
		try {
			if(analog)device.setActorLevel(getComponentKey(),v);
			else device.setActor(getComponentKey(),v!=0);
			last = v;
			valid = true;
			myOutput.setValue(analog ? v + "%" : (v!=0 ? "on" : "off"));
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
		e.setAttribute("key",componentKey);
		e.setAttribute("analog",Boolean.toString(analog));
		r.appendChild(e);
		return r;
	}

	@Override
	public void initSpecific(Node n) {
		NodeList l = n.getChildNodes();
		for (int i = 0; i < l.getLength(); i++)
			if ("ShellyActor".equals(l.item(i).getNodeName())) {
                Element actor=(Element)l.item(i);channel=Integer.parseInt(actor.getAttribute("channel"));
                componentKey=actor.getAttribute("key");analog=Boolean.parseBoolean(actor.getAttribute("analog"));
            }
        configureInputs();
		setStatusMessage((device != null && device.isCoverActor(channel) ? "Cover" : "Relay") + " channel " + channel);
	}

    public int getChannel(){return channel;}
}
