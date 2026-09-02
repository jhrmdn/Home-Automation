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
			new InputDefinition("Trigger", DataComponentStub.TYPE_BOOLEAN_IO, "Rising-edge pulse trigger"),
			new InputDefinition("Force off", DataComponentStub.TYPE_BOOLEAN_IO, "Immediately ends and suppresses the pulse") };
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
	private double pulseDuration = DEFAULT_PULSE_MILLIS;
	private String pulseUnit = "ms";
	private transient boolean previousInput;
	private transient ScheduledFuture<?> pulseEnd;
	private transient long pulseSequence;
	private transient long timingStartedNanos;

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
		this.pulseDuration = pulseMillis;
		this.pulseUnit = "ms";
	}

	public synchronized double getPulseDuration() { return pulseDuration; }
	public synchronized String getPulseUnit() { return pulseUnit; }
	public synchronized long getElapsedMillis() {
		if (pulseEnd == null) return 0;
		return Math.min(pulseMillis, TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - timingStartedNanos));
	}
	public synchronized long getRemainingMillis() { return pulseEnd == null ? 0 : Math.max(0, pulseMillis - getElapsedMillis()); }
	public synchronized double getElapsedTime(){return displayTime(getElapsedMillis()/(double)("min".equals(pulseUnit)?60000:"s".equals(pulseUnit)?1000:1));}
	public synchronized double getRemainingTime(){return displayTime(getRemainingMillis()/(double)("min".equals(pulseUnit)?60000:"s".equals(pulseUnit)?1000:1));}
	private double displayTime(double value){return "min".equals(pulseUnit)?Math.round(value*100.0)/100.0:value;}
	public synchronized void setPulseDuration(double duration, String unit) {
		long factor = "s".equals(unit) ? 1000 : "min".equals(unit) ? 60000 : "ms".equals(unit) ? 1 : -1;
		if (!Double.isFinite(duration) || duration <= 0 || factor < 0) throw new IllegalArgumentException("Invalid pulse duration or unit");
		double millis = duration * factor;
		if (millis > Long.MAX_VALUE || Math.round(millis) < 1) throw new IllegalArgumentException("Pulse duration is out of range");
		this.pulseDuration = duration;
		this.pulseUnit = unit;
		this.pulseMillis = Math.round(millis);
	}

	@Override
	protected synchronized void calculateMyActualState() {
		boolean input = myInputValues[0] != null && myInputValues[0].getOutputAsBoolean();
		boolean forceOff = myInputValues[1] != null && myInputValues[1].getOutputAsBoolean();
		if (forceOff) {
			if (pulseEnd != null || myActualOutputState.getOutputAsBoolean()) {
				cancelPulseEnd();
				myActualOutputState.setValue(false);
				updateMyOutputs();
			}
			previousInput = input;
			return;
		}
		if (input && !previousInput && pulseEnd == null) startPulse();
		previousInput = input;
	}

	private void startPulse() {
		cancelPulseEnd();
		myActualOutputState.setValue(true);
		updateMyOutputs();
		long sequence = pulseSequence;
		timingStartedNanos = System.nanoTime();
		pulseEnd = SCHEDULER.schedule(() -> finishPulse(sequence), pulseMillis, TimeUnit.MILLISECONDS);
	}

	private synchronized void finishPulse(long sequence) {
		if (sequence != pulseSequence) return;
		pulseEnd = null;
		timingStartedNanos = 0;
		myActualOutputState.setValue(false);
		updateMyOutputs();
	}

	private void cancelPulseEnd() {
		pulseSequence++;
		if (pulseEnd != null) pulseEnd.cancel(false);
		pulseEnd = null;
		timingStartedNanos = 0;
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

	@Override public synchronized void shutdownRuntime() { cancelPulseEnd(); }

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
		pulse.setAttribute("duration", Double.toString(pulseDuration));
		pulse.setAttribute("unit", pulseUnit);
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
				Node storedValue = child.getAttributes().getNamedItem("duration");
				Node storedUnit = child.getAttributes().getNamedItem("unit");
				if (storedValue != null && storedUnit != null) setPulseDuration(Double.parseDouble(storedValue.getNodeValue()), storedUnit.getNodeValue());
				else if (duration != null) setPulseMillis(Long.parseLong(duration.getNodeValue()));
			}
		}
		return true;
	}
}
