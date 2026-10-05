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
	private final boolean[] previous = new boolean[3];
	private boolean valid;

	public ShellyCoverDataSink() { super(INPUTS); }
	public ShellyCoverDataSink(ShellyDevice device, int channel) {
		this(); this.device = device; this.channel = channel;
		setStatusMessage("Cover channel " + channel);
	}

	public void setDevice(ShellyDevice device) { this.device=device; setStatusMessage("Cover channel " + channel); }
	public int getChannel() { return channel; }
	public String getCoverState() { String value=myOutput.getOutputAsString(); return value==null||value.isBlank()?"unknown":value; }
	public synchronized void command(String command) throws Exception { device.controlCover(channel, command); refreshState(); }

	@Override public synchronized void updateCycle() {
		try {
			String[] commands={"open","close","stop"};
			for(int i=0;i<commands.length;i++) {
				boolean current=myInputValues[i]!=null&&myInputValues[i].isDataValid()&&myInputValues[i].getOutputAsBoolean();
				if(current&&!previous[i]) device.controlCover(channel,commands[i]);
				previous[i]=current;
			}
			refreshState();
		} catch(Exception exception) { valid=false; myOutput.setValue("unknown"); }
	}
	private void refreshState() throws Exception { myOutput.setValue(device.readCoverState(channel)); valid=true; }
	@Override public boolean isDataValid() { return valid; }
	@Override public JPanel getSpecificDetailsPanel() { return null; }
	@Override public Node getStorageXML(Document document) { Element root=getStorageDataSink(document),element=document.createElement("ShellyCover");element.setAttribute("channel",Integer.toString(channel));root.appendChild(element);return root; }
	@Override public void initSpecific(Node node) { NodeList children=node.getChildNodes();for(int i=0;i<children.getLength();i++)if("ShellyCover".equals(children.item(i).getNodeName()))channel=Integer.parseInt(children.item(i).getAttributes().getNamedItem("channel").getNodeValue());setStatusMessage("Cover channel "+channel); }
}
