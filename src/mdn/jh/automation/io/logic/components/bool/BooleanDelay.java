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
			new InputDefinition("in", DataComponentStub.TYPE_BOOLEAN_IO, "Boolean input") };
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
	private transient ScheduledFuture<?> pending;
	private transient long changeSequence;

	public BooleanDelay() {
		super(DataComponentStub.TYPE_BOOLEAN_IO, INPUTS, new LogicComponentDescription("Boolean Delay"));
		setName("Boolean Delay");
	}

	public synchronized long getOnDelayMillis() { return onDelayMillis; }
	public synchronized long getOffDelayMillis() { return offDelayMillis; }
	public synchronized void setOnDelayMillis(long value) { onDelayMillis = requireNonNegative(value, "On-delay"); calculateMyActualState(); }
	public synchronized void setOffDelayMillis(long value) { offDelayMillis = requireNonNegative(value, "Off-delay"); calculateMyActualState(); }

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
		boolean desired = myInputValues[0] != null && myInputValues[0].getOutputAsBoolean();
		cancelPending();
		if (desired == myActualOutputState.getOutputAsBoolean()) return;
		long delay = desired ? onDelayMillis : offDelayMillis;
		if (delay == 0) { apply(desired); return; }
		long sequence = changeSequence;
		pending = SCHEDULER.schedule(() -> {
			synchronized (BooleanDelay.this) {
				if (sequence != changeSequence) return;
				pending = null;
				apply(desired);
			}
		}, delay, TimeUnit.MILLISECONDS);
	}

	private void cancelPending() {
		changeSequence++;
		if (pending != null) pending.cancel(false);
		pending = null;
	}

	private void apply(boolean value) {
		myActualOutputState.setValue(value);
		updateMyOutputs();
	}

	@Override public synchronized Node getStorageXML(Document doc) {
		Element root = (Element) super.getStorageXML(doc);
		Element delays = doc.createElement("Delays");
		delays.setAttribute("onMillis", Long.toString(onDelayMillis));
		delays.setAttribute("offMillis", Long.toString(offDelayMillis));
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
				if (on != null) onDelayMillis = requireNonNegative(Long.parseLong(on.getNodeValue()), "On-delay");
				if (off != null) offDelayMillis = requireNonNegative(Long.parseLong(off.getNodeValue()), "Off-delay");
			}
		}
		return true;
	}
}
