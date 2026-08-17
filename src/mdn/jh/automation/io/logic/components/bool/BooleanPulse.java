package mdn.jh.automation.io.logic.components.bool;

import java.awt.FlowLayout;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import mdn.jh.automation.io.DataComponentStub;
import mdn.jh.automation.io.logic.InputDefinition;
import mdn.jh.automation.io.logic.LogicBaseBoolean;
import mdn.jh.automation.io.logic.LogicComponentDescription;

/** Produces a configurable boolean pulse for every rising input edge. */
public class BooleanPulse extends LogicBaseBoolean {
	private static final long serialVersionUID = 1L;
	private static final long DEFAULT_PULSE_MILLIS = 1000;
	private static final InputDefinition[] INPUTS = {
			new InputDefinition("Trigger", DataComponentStub.TYPE_BOOLEAN_IO, "Rising-edge pulse trigger") };
	private static final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor(
			new ThreadFactory() {
				@Override
				public Thread newThread(Runnable runnable) {
					Thread thread = new Thread(runnable, "boolean-pulse");
					thread.setDaemon(true);
					return thread;
				}
			});

	private long pulseMillis = DEFAULT_PULSE_MILLIS;
	private transient boolean previousInput;
	private transient ScheduledFuture<?> pulseEnd;
	private transient long pulseSequence;

	public BooleanPulse() {
		super(DataComponentStub.TYPE_BOOLEAN_IO, INPUTS, new LogicComponentDescription("Boolean Pulse"));
		setName("Boolean Pulse");
		setHelptext("Creates a configurable pulse on every rising input edge");
	}

	public synchronized long getPulseMillis() {
		return pulseMillis;
	}

	public synchronized void setPulseMillis(long pulseMillis) {
		if (pulseMillis <= 0) throw new IllegalArgumentException("Pulse duration must be greater than zero");
		this.pulseMillis = pulseMillis;
	}

	@Override
	protected synchronized void calculateMyActualState() {
		boolean input = myInputValues[0] != null && myInputValues[0].getOutputAsBoolean();
		if (input && !previousInput) startPulse();
		previousInput = input;
	}

	private void startPulse() {
		cancelPulseEnd();
		myActualOutputState.setValue(true);
		updateMyOutputs();
		long sequence = pulseSequence;
		pulseEnd = SCHEDULER.schedule(() -> finishPulse(sequence), pulseMillis, TimeUnit.MILLISECONDS);
	}

	private synchronized void finishPulse(long sequence) {
		if (sequence != pulseSequence) return;
		pulseEnd = null;
		myActualOutputState.setValue(false);
		updateMyOutputs();
	}

	private void cancelPulseEnd() {
		pulseSequence++;
		if (pulseEnd != null) pulseEnd.cancel(false);
		pulseEnd = null;
	}

	@Override
	public synchronized void resetSpecific() {
		cancelPulseEnd();
		previousInput = myInputValues[0] != null && myInputValues[0].getOutputAsBoolean();
		myActualOutputState.setValue(false);
		updateMyOutputs();
	}

	@Override
	public synchronized void deleteThis() {
		cancelPulseEnd();
		super.deleteThis();
	}

	@Override
	public synchronized JPanel getSpecificDetailsPanel() {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(new JLabel("Pulse duration (ms):"));
		JSpinner duration = new JSpinner(new SpinnerNumberModel(pulseMillis, 1L, Long.MAX_VALUE, 100L));
		duration.addChangeListener(event -> setPulseMillis(((Number) duration.getValue()).longValue()));
		panel.add(duration);
		return panel;
	}

	@Override
	public synchronized Node getStorageXML(Document doc) {
		Element root = (Element) super.getStorageXML(doc);
		Element pulse = doc.createElement("Pulse");
		pulse.setAttribute("durationMillis", Long.toString(pulseMillis));
		root.appendChild(pulse);
		return root;
	}

	@Override
	public synchronized boolean initDataComponent(Node node) throws Exception {
		if (!super.initDataComponent(node)) return false;
		NodeList children = node.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			if ("Pulse".equals(child.getNodeName())) {
				Node duration = child.getAttributes().getNamedItem("durationMillis");
				if (duration != null) setPulseMillis(Long.parseLong(duration.getNodeValue()));
			}
		}
		return true;
	}
}
