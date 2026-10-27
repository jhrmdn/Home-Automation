package mdn.jh.automation.io.logic.components.number;

import java.awt.GridLayout;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.swing.JComboBox;
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
import mdn.jh.automation.io.logic.LogicBase;
import mdn.jh.automation.io.logic.LogicComponentDescription;

/** Time-aware PID controller with integral anti-windup through output clamping. */
public class PidController extends LogicBase {
	private static final long serialVersionUID = 1L;
	private static final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor(runnable -> {
		Thread thread = new Thread(runnable, "pid-controller");
		thread.setDaemon(true);
		return thread;
	});
	private static final InputDefinition[] INPUTS = {
			new InputDefinition("Process value", DataComponentStub.TYPE_DOUBLE_IO, "Integer or floating-point process value"),
			new InputDefinition("External setpoint", DataComponentStub.TYPE_DOUBLE_IO,
					"Optional external setpoint; when connected it overrides the configured setpoint") };
	private double setpoint;
	private double kp = 1;
	private double ki;
	private double kd;
	private double minimum = -100;
	private double maximum = 100;
	private String outputType = NumericOperation.OUTPUT_FLOAT;
	private double updateInterval = 1000;
	private String updateIntervalUnit = "ms";
	private transient double integral;
	private transient double previousError;
	private transient long previousNanos;
	private transient ScheduledFuture<?> updateTask;

	public PidController() {
		super(DataComponentStub.TYPE_DOUBLE_IO, INPUTS, new LogicComponentDescription("PID controller"));
		setName("PID controller");
		setHelptext("PID controller with an optional external setpoint, Kp, Ki, Kd and bounded integer or float output");
		myActualOutputState.setDataValid(false);
		startUpdateTask();
	}

	public synchronized void configure(double setpoint, double kp, double ki, double kd,
			double minimum, double maximum, String outputType) {
		if (!Double.isFinite(setpoint) || !Double.isFinite(kp) || !Double.isFinite(ki) || !Double.isFinite(kd)
				|| !Double.isFinite(minimum) || !Double.isFinite(maximum) || maximum <= minimum)
			throw new IllegalArgumentException("PID values must be finite and maximum must be greater than minimum");
		if (!NumericOperation.OUTPUT_FLOAT.equals(outputType) && !NumericOperation.OUTPUT_INTEGER.equals(outputType))
			throw new IllegalArgumentException("PID output type must be integer or float");
		this.setpoint = setpoint; this.kp = kp; this.ki = ki; this.kd = kd;
		this.minimum = minimum; this.maximum = maximum; this.outputType = outputType;
		resetController();
		calculateMyActualState();
		updateMyOutputs();
	}

	@Override
	protected synchronized void calculateMyActualState() {
		if (myInputValues[0] == null || !myInputValues[0].isDataValid()
				|| (isInputInUse(1) && (myInputValues[1] == null || !myInputValues[1].isDataValid()))) {
			myActualOutputState.setDataValid(false);
			return;
		}
		double error = getEffectiveSetpoint() - myInputValues[0].getOutputAsNumber();
		long now = System.nanoTime();
		double dt = previousNanos == 0 ? 0 : (now - previousNanos) / 1_000_000_000.0;
		double candidateIntegral = integral + (dt > 0 ? error * dt : 0);
		double derivative = dt > 0 ? (error - previousError) / dt : 0;
		double unclamped = kp * error + ki * candidateIntegral + kd * derivative;
		double output = Math.max(minimum, Math.min(maximum, unclamped));
		if (output == unclamped || Math.signum(error) != Math.signum(unclamped - output)) integral = candidateIntegral;
		if (NumericOperation.OUTPUT_INTEGER.equals(outputType)) output = Math.rint(output);
		myActualOutputState.setValue(output);
		myActualOutputState.setDataValid(Double.isFinite(output));
		previousError = error;
		previousNanos = now;
	}

	private void resetController() { integral = 0; previousError = 0; previousNanos = 0; }
	private synchronized void startUpdateTask() {
		if (updateTask != null) updateTask.cancel(false);
		long millis = getUpdateIntervalMillis();
		updateTask = SCHEDULER.scheduleAtFixedRate(this::scheduledUpdate, millis, millis, TimeUnit.MILLISECONDS);
	}
	private void scheduledUpdate() { synchronized (this) { calculateMyActualState(); updateMyOutputs(); } }
	public synchronized void setUpdateInterval(double value, String unit) {
		if (!Double.isFinite(value) || value <= 0 || !("ms".equals(unit) || "s".equals(unit)))
			throw new IllegalArgumentException("PID update interval must be greater than zero and use ms or s");
		double millis = "s".equals(unit) ? value * 1000 : value;
		if (!Double.isFinite(millis) || millis < 1 || millis > Long.MAX_VALUE)
			throw new IllegalArgumentException("PID update interval must be at least 1 ms");
		updateInterval = value;
		updateIntervalUnit = unit;
		resetController();
		startUpdateTask();
	}
	private long getUpdateIntervalMillis() { return Math.round("s".equals(updateIntervalUnit) ? updateInterval * 1000 : updateInterval); }
	@Override public synchronized void resetSpecific() { resetController(); calculateMyActualState(); updateMyOutputs(); }
	@Override public synchronized void unlinkInput(int inputNumber) { super.unlinkInput(inputNumber); resetController(); calculateMyActualState(); updateMyOutputs(); }
	@Override public synchronized void deleteThis() { if (updateTask != null) updateTask.cancel(false); updateTask = null; super.deleteThis(); }
	@Override public synchronized void shutdownRuntime() { if (updateTask != null) updateTask.cancel(false); updateTask = null; }
	@Override public int getDataTypeOutput() { return DataComponentStub.TYPE_DOUBLE_IO; }
	@Override public synchronized boolean isDataValid() { return myActualOutputState.isDataValid(); }
	@Override public synchronized String getOutputAsString() { return NumericOperation.OUTPUT_INTEGER.equals(outputType) ? Long.toString(Math.round(getOutputAsNumber())) : Double.toString(getOutputAsNumber()); }
	public synchronized double getSetpoint() { return setpoint; }
	public synchronized double getEffectiveSetpoint() { return isInputInUse(1) ? myInputValues[1].getOutputAsNumber() : setpoint; }
	public synchronized String getProcessValueAsString() { return myInputValues[0] == null ? "" : myInputValues[0].getOutputAsString(); }
	public synchronized double getUpdateInterval() { return updateInterval; }
	public synchronized String getUpdateIntervalUnit() { return updateIntervalUnit; }
	public synchronized double getKp() { return kp; }
	public synchronized double getKi() { return ki; }
	public synchronized double getKd() { return kd; }
	public synchronized double getMinimum() { return minimum; }
	public synchronized double getMaximum() { return maximum; }
	public synchronized String getOutputType() { return outputType; }

	@Override
	public synchronized Node getStorageXML(Document document) {
		Element root = (Element) super.getStorageXML(document), config = document.createElement("PidController");
		config.setAttribute("setpoint", Double.toString(setpoint)); config.setAttribute("kp", Double.toString(kp));
		config.setAttribute("ki", Double.toString(ki)); config.setAttribute("kd", Double.toString(kd));
		config.setAttribute("minimum", Double.toString(minimum)); config.setAttribute("maximum", Double.toString(maximum));
		config.setAttribute("outputType", outputType); config.setAttribute("updateInterval", Double.toString(updateInterval));
		config.setAttribute("updateIntervalUnit", updateIntervalUnit); root.appendChild(config); return root;
	}

	@Override
	public synchronized boolean initDataComponent(Node node) throws Exception {
		if (!super.initDataComponent(node)) return false;
		NodeList children = node.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) if ("PidController".equals(children.item(i).getNodeName())) {
			var attributes = children.item(i).getAttributes();
			configure(Double.parseDouble(attributes.getNamedItem("setpoint").getNodeValue()),
					Double.parseDouble(attributes.getNamedItem("kp").getNodeValue()), Double.parseDouble(attributes.getNamedItem("ki").getNodeValue()),
					Double.parseDouble(attributes.getNamedItem("kd").getNodeValue()), Double.parseDouble(attributes.getNamedItem("minimum").getNodeValue()),
					Double.parseDouble(attributes.getNamedItem("maximum").getNodeValue()), attributes.getNamedItem("outputType").getNodeValue());
			Node interval = attributes.getNamedItem("updateInterval"), unit = attributes.getNamedItem("updateIntervalUnit");
			if (interval != null) setUpdateInterval(Double.parseDouble(interval.getNodeValue()), unit == null ? "ms" : unit.getNodeValue());
		}
		return true;
	}

	@Override
	public synchronized JPanel getSpecificDetailsPanel() {
		JPanel panel = new JPanel(new GridLayout(8, 2, 5, 5));
		JSpinner target = spinner(setpoint), p = spinner(kp), i = spinner(ki), d = spinner(kd), min = spinner(minimum), max = spinner(maximum);
		JComboBox<String> type = new JComboBox<String>(new String[] { NumericOperation.OUTPUT_FLOAT, NumericOperation.OUTPUT_INTEGER }); type.setSelectedItem(outputType);
		JPanel intervalPanel = new JPanel(); JSpinner interval = spinner(updateInterval); JComboBox<String> intervalUnit = new JComboBox<String>(new String[] { "ms", "s" }); intervalUnit.setSelectedItem(updateIntervalUnit); intervalPanel.add(interval); intervalPanel.add(intervalUnit);
		Runnable save = () -> configure(number(target), number(p), number(i), number(d), number(min), number(max), (String) type.getSelectedItem());
		for (JSpinner field : new JSpinner[] { target, p, i, d, min, max }) field.addChangeListener(event -> save.run()); type.addActionListener(event -> save.run());
		Runnable saveInterval = () -> setUpdateInterval(number(interval), (String) intervalUnit.getSelectedItem()); interval.addChangeListener(event -> saveInterval.run()); intervalUnit.addActionListener(event -> saveInterval.run());
		addRow(panel, "Setpoint", target); addRow(panel, "Kp", p); addRow(panel, "Ki", i); addRow(panel, "Kd", d);
		addRow(panel, "Minimum", min); addRow(panel, "Maximum", max); addRow(panel, "Output", type); addRow(panel, "Update interval", intervalPanel); return panel;
	}

	private static JSpinner spinner(double value) { return new JSpinner(new SpinnerNumberModel(value, -Double.MAX_VALUE, Double.MAX_VALUE, 0.1)); }
	private static double number(JSpinner spinner) { return ((Number) spinner.getValue()).doubleValue(); }
	private static void addRow(JPanel panel, String label, java.awt.Component field) { panel.add(new JLabel(label)); panel.add(field); }
}
