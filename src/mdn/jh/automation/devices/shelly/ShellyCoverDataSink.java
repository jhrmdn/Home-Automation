package mdn.jh.automation.devices.shelly;

import javax.swing.JPanel;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import mdn.jh.automation.io.DataComponentStub;
import mdn.jh.automation.io.logic.InputDefinition;
import mdn.jh.automation.io.sink.DataSink;

/** Three-command Shelly cover action with device status feedback. */
public class ShellyCoverDataSink extends DataSink {
	private static final long serialVersionUID = 1L;
	private static final InputDefinition[] INPUTS = {
		new InputDefinition("Open", DataComponentStub.TYPE_BOOLEAN_IO, "A rising edge opens the cover"),
		new InputDefinition("Close", DataComponentStub.TYPE_BOOLEAN_IO, "A rising edge closes the cover"),
		new InputDefinition("Stop", DataComponentStub.TYPE_BOOLEAN_IO, "A rising edge stops the cover")
	};
	private int channel;
	private transient ShellyDevice device;
	private final java.util.concurrent.atomic.AtomicIntegerArray previous = new java.util.concurrent.atomic.AtomicIntegerArray(3);
    private final java.util.concurrent.ConcurrentLinkedQueue<Integer> pendingInputs = new java.util.concurrent.ConcurrentLinkedQueue<>();
	private boolean valid;
    private volatile boolean pollingEnabled = true;
    public boolean isPollingEnabled(){return pollingEnabled;}
    public void setPollingEnabled(boolean enabled){pollingEnabled=enabled;}

	public ShellyCoverDataSink() { super(INPUTS); }
	public ShellyCoverDataSink(ShellyDevice device, int channel) {
		this(); this.device = device; this.channel = channel;
		setStatusMessage("Cover channel " + channel);
	}

	public void setDevice(ShellyDevice device) { this.device=device; setStatusMessage("Cover channel " + channel); }
	public int getChannel() { return channel; }
	public String getCoverState() { String value=myOutput.getOutputAsString(); return value==null||value.isBlank()?"unknown":value; }
	public synchronized void command(String command) throws Exception { device.controlCover(channel, command); if(pollingEnabled)refreshState();else {myOutput.setValue(command);valid=true;} }

    @Override public void update(int input) {
        captureInput(input);
    }
    private void captureInput(int input) {
        boolean current=myInputValues[input]!=null&&myInputValues[input].isDataValid()&&myInputValues[input].getOutputAsBoolean();
        int old=previous.getAndSet(input,current?1:0);
        if(current&&old==0&&!isActionsSuppressed())pendingInputs.add(input);
    }
    @Override public synchronized void updateCycle() {
        try {
            for(int input=0;input<3;input++)captureInput(input);
            String[] commands={"open","close","stop"};
            Integer input;
            while((input=pendingInputs.peek())!=null){
                device.controlCover(channel,commands[input]);
                pendingInputs.poll();myOutput.setValue(commands[input]);valid=true;
            }
            if(pollingEnabled)refreshState();
        } catch(Exception exception) { valid=false; myOutput.setValue("unknown"); }
    }
	private void refreshState() throws Exception { myOutput.setValue(device.readCoverState(channel)); valid=true; }
	@Override public boolean isDataValid() { return valid; }
	@Override public JPanel getSpecificDetailsPanel() { return null; }
	@Override public Node getStorageXML(Document document) { Element root=getStorageDataSink(document),element=document.createElement("ShellyCover");element.setAttribute("channel",Integer.toString(channel));element.setAttribute("pollingEnabled",Boolean.toString(pollingEnabled));root.appendChild(element);return root; }
	@Override public void initSpecific(Node node) { NodeList children=node.getChildNodes();for(int i=0;i<children.getLength();i++)if("ShellyCover".equals(children.item(i).getNodeName())){Element element=(Element)children.item(i);channel=Integer.parseInt(element.getAttribute("channel"));pollingEnabled=!element.hasAttribute("pollingEnabled")||Boolean.parseBoolean(element.getAttribute("pollingEnabled"));}setStatusMessage("Cover channel "+channel); }
}
