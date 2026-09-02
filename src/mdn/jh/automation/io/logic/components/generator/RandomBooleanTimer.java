package mdn.jh.automation.io.logic.components.generator;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import mdn.jh.automation.io.DataComponentStub;
import mdn.jh.automation.io.DataOutputIF;
import mdn.jh.automation.io.RecursionException;
import mdn.jh.automation.io.logic.InputDefinition;
import mdn.jh.automation.io.logic.LogicBase;
import mdn.jh.automation.io.logic.LogicComponentDescription;

/** Boolean timer alternating between independently randomized on and off periods. */
public class RandomBooleanTimer extends LogicBase {
	private static final long serialVersionUID = 1L;
	private static final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor(runnable -> {
		Thread thread = new Thread(runnable, "random-boolean-timer");
		thread.setDaemon(true);
		return thread;
	});

	private long minimumOnMillis = 1000;
	private long maximumOnMillis = 5000;
	private long minimumOffMillis = 1000;
	private long maximumOffMillis = 5000;
	private boolean manuallyEnabled = true;
	private transient ScheduledFuture<?> transition;

	public RandomBooleanTimer() {
		super(DataComponentStub.TYPE_BOOLEAN_IO,
				new InputDefinition[]{new InputDefinition("Enable", DataComponentStub.TYPE_BOOLEAN_IO, "Enables the random timer while true")},
				new LogicComponentDescription("Random Timer"));
		setName("Random Timer");
		setHelptext("Alternates a Boolean output using random on and off durations. Each duration is selected between its configured minimum and maximum. Connect Enable to control the timer externally, or use the switch on the component while Enable is unconnected.");
		myActualOutputState.setValue(false);
		myActualOutputState.setDataValid(true);
		restartCycle();
	}

	public synchronized void configure(long minimumOnMillis, long maximumOnMillis, long minimumOffMillis, long maximumOffMillis) {
		validateRange("On", minimumOnMillis, maximumOnMillis);
		validateRange("Off", minimumOffMillis, maximumOffMillis);
		this.minimumOnMillis = minimumOnMillis;
		this.maximumOnMillis = maximumOnMillis;
		this.minimumOffMillis = minimumOffMillis;
		this.maximumOffMillis = maximumOffMillis;
		restartCycle();
	}

	private static void validateRange(String name, long minimum, long maximum) {
		if (minimum <= 0 || maximum <= 0) throw new IllegalArgumentException(name + " times must be greater than zero");
		if (maximum < minimum) throw new IllegalArgumentException(name + " maximum must be greater than or equal to its minimum");
	}

	private synchronized void restartCycle() {
		cancelTransition();
		myActualOutputState.setValue(false);
		myActualOutputState.setDataValid(true);
		updateMyOutputs();
		if (isEnabled()) scheduleTransition(randomDuration(minimumOffMillis, maximumOffMillis));
	}

	private long randomDuration(long minimum, long maximum) {
		return minimum == maximum ? minimum : ThreadLocalRandom.current().nextLong(minimum, maximum + 1);
	}

	private synchronized void scheduleTransition(long delay) {
		transition = SCHEDULER.schedule(this::transition, delay, TimeUnit.MILLISECONDS);
	}

	private void transition() {
		synchronized (this) {
			transition = null;
			if (!isEnabled()) { setOutput(false); return; }
			boolean next = !myActualOutputState.getOutputAsBoolean();
			setOutput(next);
			scheduleTransition(randomDuration(next ? minimumOnMillis : minimumOffMillis,
					next ? maximumOnMillis : maximumOffMillis));
		}
	}

	private void setOutput(boolean value) {
		myActualOutputState.setValue(value);
		myActualOutputState.setDataValid(true);
		updateMyOutputs();
	}

	private synchronized void synchronizeEnabledState() {
		if (!isEnabled()) { cancelTransition(); setOutput(false); }
		else if (transition == null) restartCycle();
	}

	private void cancelTransition() { if (transition != null) transition.cancel(false); transition = null; }

	@Override public synchronized void update(int connectionInputNumber) { calculateMyActualState(); synchronizeEnabledState(); }
	@Override public synchronized void setConnection(DataOutputIF output, int sourceId, int input) throws RecursionException { super.setConnection(output, sourceId, input); synchronizeEnabledState(); }
	@Override public synchronized void unlinkInput(int input) { super.unlinkInput(input); synchronizeEnabledState(); }
	@Override protected synchronized void calculateMyActualState() { if (!isEnabled()) myActualOutputState.setValue(false); myActualOutputState.setDataValid(true); }
	@Override public int getDataTypeOutput() { return TYPE_BOOLEAN_IO; }
	@Override public synchronized boolean isDataValid() { return true; }
	@Override public synchronized void resetSpecific() { restartCycle(); }
	@Override public synchronized void deleteThis() { shutdownRuntime(); super.deleteThis(); }
	@Override public synchronized void shutdownRuntime() { cancelTransition(); }

	public synchronized boolean isEnabled() { return isInputConnected(0) ? myInputValues[0] != null && myInputValues[0].getOutputAsBoolean() : manuallyEnabled; }
	public synchronized boolean isManuallyEnabled() { return manuallyEnabled; }
	public synchronized void setManuallyEnabled(boolean enabled) { manuallyEnabled = enabled; synchronizeEnabledState(); }
	public synchronized long getMinimumOnMillis() { return minimumOnMillis; }
	public synchronized long getMaximumOnMillis() { return maximumOnMillis; }
	public synchronized long getMinimumOffMillis() { return minimumOffMillis; }
	public synchronized long getMaximumOffMillis() { return maximumOffMillis; }

	@Override public synchronized Node getStorageXML(Document document) {
		Element root = (Element) super.getStorageXML(document);
		Element settings = document.createElement("RandomBooleanTimer");
		settings.setAttribute("minimumOnMillis", Long.toString(minimumOnMillis));
		settings.setAttribute("maximumOnMillis", Long.toString(maximumOnMillis));
		settings.setAttribute("minimumOffMillis", Long.toString(minimumOffMillis));
		settings.setAttribute("maximumOffMillis", Long.toString(maximumOffMillis));
		settings.setAttribute("enabled", Boolean.toString(manuallyEnabled));
		root.appendChild(settings);
		return root;
	}

	@Override public synchronized boolean initDataComponent(Node node) throws Exception {
		if (!super.initDataComponent(node)) return false;
		NodeList children = node.getChildNodes();
		for (int index = 0; index < children.getLength(); index++) {
			Node child = children.item(index);
			if (!"RandomBooleanTimer".equals(child.getNodeName())) continue;
			var attributes = child.getAttributes();
			minimumOnMillis = Long.parseLong(attributes.getNamedItem("minimumOnMillis").getNodeValue());
			maximumOnMillis = Long.parseLong(attributes.getNamedItem("maximumOnMillis").getNodeValue());
			minimumOffMillis = Long.parseLong(attributes.getNamedItem("minimumOffMillis").getNodeValue());
			maximumOffMillis = Long.parseLong(attributes.getNamedItem("maximumOffMillis").getNodeValue());
			validateRange("On", minimumOnMillis, maximumOnMillis);
			validateRange("Off", minimumOffMillis, maximumOffMillis);
			if (attributes.getNamedItem("enabled") != null) manuallyEnabled = Boolean.parseBoolean(attributes.getNamedItem("enabled").getNodeValue());
		}
		restartCycle();
		return true;
	}
}
