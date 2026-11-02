package mdn.jh.automation.io.logic.components.bool;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import mdn.jh.automation.io.DataComponentStub;
import mdn.jh.automation.io.logic.InputDefinition;
import mdn.jh.automation.io.logic.LogicBaseBoolean;
import mdn.jh.automation.io.logic.LogicComponentDescription;

/** Boolean pass-through with independent delays for rising and falling edges. */
public class BooleanDelay extends LogicBaseBoolean {
	private static final long serialVersionUID = 1L;
	private static final InputDefinition[] INPUTS = {
			new InputDefinition("in", DataComponentStub.TYPE_BOOLEAN_IO, "Boolean input"),
			new InputDefinition("Forced off", DataComponentStub.TYPE_BOOLEAN_IO,
					"Immediately switches the output off and cancels a pending delay") };
	private static final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor(
			new ThreadFactory() {
				@Override public Thread newThread(Runnable runnable) {
					Thread thread = new Thread(runnable, "boolean-delay");
					thread.setDaemon(true);
					return thread;
				}
			});

	private long onDelayMillis;
	private long offDelayMillis;
	private double onDelay;
	private double offDelay;
	private String onDelayUnit = "ms";
	private String offDelayUnit = "ms";
	private transient ScheduledFuture<?> pending;
	private transient long changeSequence;
	private transient long timingStartedNanos;
	private transient long timingDurationMillis;
	private transient String timingUnit = "ms";

	public BooleanDelay() {
		super(DataComponentStub.TYPE_BOOLEAN_IO, INPUTS, new LogicComponentDescription("Boolean Delay"));
		setName("Boolean Delay");
	}

	public synchronized long getOnDelayMillis() { return onDelayMillis; }
	public synchronized long getOffDelayMillis() { return offDelayMillis; }
	public synchronized void setOnDelayMillis(long value) { onDelayMillis = requireNonNegative(value, "On-delay"); onDelay=value; onDelayUnit="ms"; calculateMyActualState(); }
	public synchronized void setOffDelayMillis(long value) { offDelayMillis = requireNonNegative(value, "Off-delay"); offDelay=value; offDelayUnit="ms"; calculateMyActualState(); }
	public synchronized void setOnDelay(double value,String unit){onDelayMillis=durationMillis(value,unit,"On-delay");onDelay=value;onDelayUnit=unit;calculateMyActualState();}
	public synchronized void setOffDelay(double value,String unit){offDelayMillis=durationMillis(value,unit,"Off-delay");offDelay=value;offDelayUnit=unit;calculateMyActualState();}
	public synchronized double getOnDelay(){return onDelay;} public synchronized double getOffDelay(){return offDelay;}
	public synchronized String getOnDelayUnit(){return onDelayUnit;} public synchronized String getOffDelayUnit(){return offDelayUnit;}
	public synchronized String getTimingUnit(){return pending==null?onDelayUnit:timingUnit;}
	public synchronized double getElapsedTime(){return displayTime(getElapsedMillis()/(double)unitFactor(getTimingUnit()));}
	public synchronized double getRemainingTime(){return displayTime(getRemainingMillis()/(double)unitFactor(getTimingUnit()));}
	private double displayTime(double value){return "min".equals(getTimingUnit())?Math.round(value*100.0)/100.0:value;}
	private long durationMillis(double value,String unit,String label){if(!Double.isFinite(value)||value<0)throw new IllegalArgumentException(label+" must be a non-negative number");double millis=value*unitFactor(unit);if(millis>Long.MAX_VALUE)throw new IllegalArgumentException(label+" is out of range");return Math.round(millis);}
	private long unitFactor(String unit){if("ms".equals(unit))return 1;if("s".equals(unit))return 1000;if("min".equals(unit))return 60000;throw new IllegalArgumentException("Invalid delay unit");}
	public synchronized long getElapsedMillis() {
		if (pending == null) return 0;
		return Math.min(timingDurationMillis, TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - timingStartedNanos));
	}
	public synchronized long getRemainingMillis() { return pending == null ? 0 : Math.max(0, timingDurationMillis - getElapsedMillis()); }

	private long requireNonNegative(long value, String label) {
		if (value < 0) throw new IllegalArgumentException(label + " must not be negative");
		return value;
	}

	@Override public synchronized void resetSpecific() {
		cancelPending();
		myActualOutputState.setValue(false);
		updateMyOutputs();
	}

	@Override protected synchronized void calculateMyActualState() {
		boolean forcedOff = myInputValues[1] != null && myInputValues[1].getOutputAsBoolean();
		if (forcedOff) {
			cancelPending();
			if (myActualOutputState.getOutputAsBoolean()) apply(false);
			return;
		}
		boolean desired = myInputValues[0] != null && myInputValues[0].getOutputAsBoolean();
		cancelPending();
		if (desired == myActualOutputState.getOutputAsBoolean()) return;
		long delay = desired ? onDelayMillis : offDelayMillis;
		if (delay == 0) { apply(desired); return; }
		long sequence = changeSequence;
		timingStartedNanos = System.nanoTime();
		timingDurationMillis = delay;
		timingUnit = desired ? onDelayUnit : offDelayUnit;
		pending = SCHEDULER.schedule(() -> {
			synchronized (BooleanDelay.this) {
				if (sequence != changeSequence) return;
				pending = null;
				timingStartedNanos = 0;
				timingDurationMillis = 0;
				apply(desired);
			}
		}, delay, TimeUnit.MILLISECONDS);
	}

	private void cancelPending() {
		changeSequence++;
		if (pending != null) pending.cancel(false);
		pending = null;
		timingStartedNanos = 0;
		timingDurationMillis = 0;
	}

	@Override public synchronized void shutdownRuntime() { cancelPending(); }

	private void apply(boolean value) {
		myActualOutputState.setValue(value);
		updateMyOutputs();
	}

	@Override public synchronized Node getStorageXML(Document doc) {
		Element root = (Element) super.getStorageXML(doc);
		Element delays = doc.createElement("Delays");
		delays.setAttribute("onMillis", Long.toString(onDelayMillis));
		delays.setAttribute("offMillis", Long.toString(offDelayMillis));
		delays.setAttribute("onValue", Double.toString(onDelay)); delays.setAttribute("onUnit", onDelayUnit);
		delays.setAttribute("offValue", Double.toString(offDelay)); delays.setAttribute("offUnit", offDelayUnit);
		root.appendChild(delays);
		return root;
	}

	@Override public synchronized boolean initDataComponent(Node node) throws Exception {
		if (!super.initDataComponent(node)) return false;
		NodeList children = node.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			if ("Delays".equals(child.getNodeName())) {
				Node on = child.getAttributes().getNamedItem("onMillis");
				Node off = child.getAttributes().getNamedItem("offMillis");
				Node onValue=child.getAttributes().getNamedItem("onValue"),onUnit=child.getAttributes().getNamedItem("onUnit"),offValue=child.getAttributes().getNamedItem("offValue"),offUnit=child.getAttributes().getNamedItem("offUnit");
				if(onValue!=null&&onUnit!=null)setOnDelay(Double.parseDouble(onValue.getNodeValue()),onUnit.getNodeValue());else if (on != null)setOnDelayMillis(Long.parseLong(on.getNodeValue()));
				if(offValue!=null&&offUnit!=null)setOffDelay(Double.parseDouble(offValue.getNodeValue()),offUnit.getNodeValue());else if (off != null)setOffDelayMillis(Long.parseLong(off.getNodeValue()));
			}
		}
		return true;
	}
}
