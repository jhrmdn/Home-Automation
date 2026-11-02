package mdn.jh.automation.devices.shelly;

import javax.swing.JPanel;
import org.w3c.dom.*;
import mdn.jh.automation.io.DataComponentStub;
import mdn.jh.automation.io.logic.InputDefinition;
import mdn.jh.automation.io.sink.DataSink;

public class ShellyDataSink extends DataSink implements mdn.jh.automation.io.ElectricalPower {
	private static final long serialVersionUID = 1L;
	private static final InputDefinition[] INPUTS = {
			new InputDefinition("in", DataComponentStub.TYPE_BOOLEAN_IO, "Actor on/off") };
	private int channel;
	private transient ShellyDevice device;
	private String componentKey = "";
	private boolean analog;
	private Double last;
	private boolean valid;
    private volatile boolean pollingEnabled = true;
    private volatile boolean restoreInputOnce;
    private Boolean observedInput;
    private boolean restoreAttempted;
    public boolean isRelay(){return !analog;}
    public boolean isRestoreInputOnce(){return restoreInputOnce;}
    public void setRestoreInputOnce(boolean enabled){restoreInputOnce=enabled&&!analog;}

    public boolean isPollingEnabled(){return pollingEnabled;}
    public void setPollingEnabled(boolean enabled){pollingEnabled=enabled;}

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
        try {
            boolean reconcile=restoreInputOnce&&!analog;
            Boolean desired=null;
            if(reconcile&&myInputValues[0]!=null&&myInputValues[0].isDataValid()){
                desired=myInputValues[0].getOutputAsBoolean();
                if(!desired.equals(observedInput)){observedInput=desired;restoreAttempted=false;}
            }
            if(myInputValues[0]!=null&&(!reconcile||desired!=null)) {
                double value=analog?myInputValues[0].getOutputAsNumber():(myInputValues[0].getOutputAsBoolean()?1:0);
                if(last==null || last!=value){
                    // Remember the initial command even on failure when one-shot reconciliation is enabled.
                    if(reconcile)last=value;
                    if(analog)device.setActorLevel(getComponentKey(),value);
                    else device.setActor(getComponentKey(),value!=0);
                    last=value;
                    myOutput.setValue(analog?value+"%":(value!=0?"on":"off"));
                    valid=true;
                }
            }
            if(pollingEnabled){
                Object actual=device.readActorValue(getComponentKey());
                myOutput.setValue(analog?actual+"%":((Boolean)actual?"on":"off"));
                valid=true;
                if(reconcile&&desired!=null&&!desired.equals(actual)&&!restoreAttempted){
                    restoreAttempted=true;
                    device.setActor(getComponentKey(),desired);
                    // Keep displaying the measured state until the next successful status query.
                }
            }
        } catch(Exception exception){valid=false;myOutput.setValue("###");}
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
        e.setAttribute("pollingEnabled",Boolean.toString(pollingEnabled));
        e.setAttribute("restoreInputOnce",Boolean.toString(restoreInputOnce));
		r.appendChild(e);
		return r;
	}

	@Override
	public void initSpecific(Node n) {
		NodeList l = n.getChildNodes();
		for (int i = 0; i < l.getLength(); i++)
			if ("ShellyActor".equals(l.item(i).getNodeName())) {
                Element actor=(Element)l.item(i);channel=Integer.parseInt(actor.getAttribute("channel"));
                pollingEnabled=!actor.hasAttribute("pollingEnabled")||Boolean.parseBoolean(actor.getAttribute("pollingEnabled"));
                componentKey=actor.getAttribute("key");analog=Boolean.parseBoolean(actor.getAttribute("analog"));
                setRestoreInputOnce(Boolean.parseBoolean(actor.getAttribute("restoreInputOnce")));
            }
        configureInputs();
		setStatusMessage((device != null && device.isCoverActor(channel) ? "Cover" : "Relay") + " channel " + channel);
	}

    @Override public Reading getElectricalPower(){
        Reading reading=device==null||!isRelay()?null:device.getActorPower(getComponentKey());
        return reading==null?null:new Reading(reading.watts(),reading.valid()&&valid&&pollingEnabled&&!isActionsSuppressed());
    }
    public int getChannel(){return channel;}
}
