package mdn.jh.automation.io.logic.components.generator;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import mdn.jh.automation.io.DataComponentStub;
import mdn.jh.automation.io.DataValue;
import mdn.jh.automation.io.logic.InputDefinition;
import mdn.jh.automation.io.logic.LogicBase;
import mdn.jh.automation.io.logic.LogicComponentDescription;

/** Periodic rectangular signal with boolean or integer output. */
public class RectangleGenerator extends LogicBase {
	private static final long serialVersionUID = 1L;
	public static final String OUTPUT_BOOLEAN = "boolean";
	public static final String OUTPUT_INTEGER = "integer";
	private static final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor(runnable -> {
		Thread thread = new Thread(runnable, "rectangle-generator"); thread.setDaemon(true); return thread;
	});

	private String outputType = OUTPUT_BOOLEAN;
	private long onMillis = 1000;
	private long offMillis = 1000;
	private long onValue = 1;
	private long offValue = 0;
	private boolean manuallyEnabled = true;
	private transient long startedAt = System.currentTimeMillis();
	private transient ScheduledFuture<?> task;

	public RectangleGenerator() {
		super(DataComponentStub.TYPE_BOOLEAN_IO, new InputDefinition[]{new InputDefinition("Enable",DataComponentStub.TYPE_BOOLEAN_IO,"Enables the generator while true")}, new LogicComponentDescription("Rectangle Generator"));
		setName("Rectangle Generator"); setHelptext("Generates a periodic rectangular boolean or integer signal. Connect Enable to control it externally, or use the switch on the component while it is unconnected.");
		myActualOutputState.setDataValid(true); start();
	}

	public synchronized void configure(String type, long onMillis, long offMillis, long onValue, long offValue) {
		if (!OUTPUT_BOOLEAN.equals(type) && !OUTPUT_INTEGER.equals(type)) throw new IllegalArgumentException("Output type must be boolean or integer");
		if (onMillis <= 0 || offMillis <= 0) throw new IllegalArgumentException("On and off duration must be greater than zero");
		if (!type.equals(outputType) && myOutputs != null && !myOutputs.isEmpty()) throw new IllegalStateException("Disconnect the output before changing its type");
		this.outputType=type; this.onMillis=onMillis; this.offMillis=offMillis; this.onValue=onValue; this.offValue=offValue;
		myActualOutputState = OUTPUT_BOOLEAN.equals(type) ? new DataValue(false) : new DataValue((double) offValue);
		myActualOutputState.setDataValid(true); startedAt=System.currentTimeMillis(); calculateMyActualState(); updateMyOutputs();
	}

	private synchronized void start() { if (task==null) task=SCHEDULER.scheduleAtFixedRate(this::tick, 0, 25, TimeUnit.MILLISECONDS); }
	private void tick() { synchronized(this) { calculateMyActualState(); updateMyOutputs(); } }
	@Override protected synchronized void calculateMyActualState() { boolean enabled=isEnabled();long phase=Math.floorMod(System.currentTimeMillis()-startedAt,onMillis+offMillis); boolean on=enabled&&phase<onMillis; if(OUTPUT_BOOLEAN.equals(outputType))myActualOutputState.setValue(on);else myActualOutputState.setValue((double)(on?onValue:offValue)); myActualOutputState.setDataValid(true); }
	@Override public synchronized int getDataTypeOutput(){return OUTPUT_BOOLEAN.equals(outputType)?TYPE_BOOLEAN_IO:TYPE_DOUBLE_IO;}
	@Override public synchronized boolean isDataValid(){return myActualOutputState.isDataValid();}
	@Override public synchronized String getOutputAsString(){return OUTPUT_INTEGER.equals(outputType)?Long.toString(Math.round(myActualOutputState.getOutputAsNumber())):Boolean.toString(myActualOutputState.getOutputAsBoolean());}
	public synchronized String getOutputType(){return outputType;} public synchronized long getOnMillis(){return onMillis;} public synchronized long getOffMillis(){return offMillis;} public synchronized long getOnValue(){return onValue;} public synchronized long getOffValue(){return offValue;}
	public synchronized boolean isEnabled(){return isInputConnected(0)?myInputValues[0]!=null&&myInputValues[0].getOutputAsBoolean():manuallyEnabled;} public synchronized boolean isManuallyEnabled(){return manuallyEnabled;} public synchronized void setManuallyEnabled(boolean enabled){manuallyEnabled=enabled;calculateMyActualState();updateMyOutputs();}
	@Override public synchronized void resetSpecific(){startedAt=System.currentTimeMillis();calculateMyActualState();updateMyOutputs();}
	@Override public synchronized void deleteThis(){shutdownRuntime();super.deleteThis();}
	@Override public synchronized void shutdownRuntime(){if(task!=null)task.cancel(false);task=null;}
	@Override public synchronized Node getStorageXML(Document doc){Element root=(Element)super.getStorageXML(doc),config=doc.createElement("RectangleGenerator");config.setAttribute("outputType",outputType);config.setAttribute("onMillis",Long.toString(onMillis));config.setAttribute("offMillis",Long.toString(offMillis));config.setAttribute("onValue",Long.toString(onValue));config.setAttribute("offValue",Long.toString(offValue));config.setAttribute("enabled",Boolean.toString(manuallyEnabled));root.appendChild(config);return root;}
	@Override public synchronized boolean initDataComponent(Node node)throws Exception{if(!super.initDataComponent(node))return false;NodeList children=node.getChildNodes();for(int i=0;i<children.getLength();i++){Node child=children.item(i);if("RectangleGenerator".equals(child.getNodeName())){var a=child.getAttributes();configure(a.getNamedItem("outputType").getNodeValue(),Long.parseLong(a.getNamedItem("onMillis").getNodeValue()),Long.parseLong(a.getNamedItem("offMillis").getNodeValue()),Long.parseLong(a.getNamedItem("onValue").getNodeValue()),Long.parseLong(a.getNamedItem("offValue").getNodeValue()));if(a.getNamedItem("enabled")!=null)manuallyEnabled=Boolean.parseBoolean(a.getNamedItem("enabled").getNodeValue());}}start();return true;}
}
