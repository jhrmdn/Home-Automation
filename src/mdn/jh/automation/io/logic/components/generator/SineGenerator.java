package mdn.jh.automation.io.logic.components.generator;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.w3c.dom.Document; import org.w3c.dom.Element; import org.w3c.dom.Node; import org.w3c.dom.NodeList;
import mdn.jh.automation.io.DataComponentStub; import mdn.jh.automation.io.logic.InputDefinition; import mdn.jh.automation.io.logic.LogicBase; import mdn.jh.automation.io.logic.LogicComponentDescription;

/** Periodic floating-point sine signal. */
public class SineGenerator extends LogicBase {
	private static final long serialVersionUID=1L;
	private static final ScheduledExecutorService SCHEDULER=Executors.newSingleThreadScheduledExecutor(r->{Thread t=new Thread(r,"sine-generator");t.setDaemon(true);return t;});
	private double frequency=1.0,peak=1.0; private boolean manuallyEnabled=true; private transient long startedAt=System.nanoTime(); private transient ScheduledFuture<?> task;
	public SineGenerator(){super(DataComponentStub.TYPE_DOUBLE_IO,new InputDefinition[]{new InputDefinition("Enable",DataComponentStub.TYPE_BOOLEAN_IO,"Enables the generator while true")},new LogicComponentDescription("Sine Generator"));setName("Sine Generator");setHelptext("Generates a sine wave with configurable frequency and peak. Connect Enable to control it externally, or use the switch on the component while it is unconnected.");myActualOutputState.setDataValid(true);start();}
	public synchronized void configure(double frequency,double peak){if(!Double.isFinite(frequency)||frequency<=0)throw new IllegalArgumentException("Frequency must be greater than zero");if(!Double.isFinite(peak)||peak<0)throw new IllegalArgumentException("Peak must be non-negative");this.frequency=frequency;this.peak=peak;startedAt=System.nanoTime();calculateMyActualState();updateMyOutputs();}
	private synchronized void start(){if(task==null)task=SCHEDULER.scheduleAtFixedRate(this::tick,0,25,TimeUnit.MILLISECONDS);} private void tick(){synchronized(this){calculateMyActualState();updateMyOutputs();}}
	@Override protected synchronized void calculateMyActualState(){if(!isEnabled()){myActualOutputState.setValue(0.0);myActualOutputState.setDataValid(true);return;}double seconds=(System.nanoTime()-startedAt)/1_000_000_000.0;myActualOutputState.setValue(peak*Math.sin(2*Math.PI*frequency*seconds));myActualOutputState.setDataValid(true);}
	@Override public int getDataTypeOutput(){return TYPE_DOUBLE_IO;} @Override public synchronized boolean isDataValid(){return true;} public synchronized double getFrequency(){return frequency;} public synchronized double getPeak(){return peak;}
	public synchronized boolean isEnabled(){return isInputConnected(0)?myInputValues[0]!=null&&myInputValues[0].getOutputAsBoolean():manuallyEnabled;} public synchronized boolean isManuallyEnabled(){return manuallyEnabled;} public synchronized void setManuallyEnabled(boolean enabled){manuallyEnabled=enabled;calculateMyActualState();updateMyOutputs();}
	@Override public synchronized void resetSpecific(){startedAt=System.nanoTime();calculateMyActualState();updateMyOutputs();} @Override public synchronized void deleteThis(){shutdownRuntime();super.deleteThis();} @Override public synchronized void shutdownRuntime(){if(task!=null)task.cancel(false);task=null;}
	@Override public synchronized Node getStorageXML(Document doc){Element root=(Element)super.getStorageXML(doc),config=doc.createElement("SineGenerator");config.setAttribute("frequency",Double.toString(frequency));config.setAttribute("peak",Double.toString(peak));config.setAttribute("enabled",Boolean.toString(manuallyEnabled));root.appendChild(config);return root;}
	@Override public synchronized boolean initDataComponent(Node node)throws Exception{if(!super.initDataComponent(node))return false;NodeList children=node.getChildNodes();for(int i=0;i<children.getLength();i++){Node child=children.item(i);if("SineGenerator".equals(child.getNodeName())){var a=child.getAttributes();configure(Double.parseDouble(a.getNamedItem("frequency").getNodeValue()),Double.parseDouble(a.getNamedItem("peak").getNodeValue()));if(a.getNamedItem("enabled")!=null)manuallyEnabled=Boolean.parseBoolean(a.getNamedItem("enabled").getNodeValue());}}start();return true;}
}
