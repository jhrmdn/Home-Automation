package mdn.jh.automation.io.logic.components.number;

import java.awt.GridLayout;

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
import mdn.jh.automation.io.logic.LogicBaseBoolean;
import mdn.jh.automation.io.logic.LogicComponentDescription;

/** Stateful numeric switch with independent on and off comparisons. */
public class NumericThresholdSwitch extends LogicBaseBoolean {
	private static final long serialVersionUID = 1L;
	public static final String GREATER = ">";
	public static final String GREATER_OR_EQUAL = ">=";
	public static final String LESS = "<";
	public static final String LESS_OR_EQUAL = "<=";
	public static final String EQUAL = "=";
	private static final InputDefinition[] INPUTS = {
			new InputDefinition("Value", DataComponentStub.TYPE_DOUBLE_IO, "Integer or floating-point value") };

	private String onOperator = GREATER;
	private double onThreshold = 1;
	private String offOperator = LESS;
	private double offThreshold;
	private boolean state;

	public NumericThresholdSwitch() {
		super(DataComponentStub.TYPE_BOOLEAN_IO, INPUTS, new LogicComponentDescription("Numeric threshold switch"));
		setName("Numeric threshold switch");
		setHelptext("Switches on and off using separate >, >=, <, <= or = numeric conditions");
		calculateMyActualState();
	}

	public synchronized void configure(String onOperator, double onThreshold, String offOperator, double offThreshold) {
		validateOperator(onOperator);
		validateOperator(offOperator);
		if (!Double.isFinite(onThreshold) || !Double.isFinite(offThreshold))
			throw new IllegalArgumentException("Thresholds must be finite numbers");
		this.onOperator = onOperator;
		this.onThreshold = onThreshold;
		this.offOperator = offOperator;
		this.offThreshold = offThreshold;
		calculateMyActualState();
		updateOutputValidity();
		updateMyOutputs();
	}

	private static void validateOperator(String operator) {
		if (!GREATER.equals(operator) && !GREATER_OR_EQUAL.equals(operator) && !LESS.equals(operator)
				&& !LESS_OR_EQUAL.equals(operator) && !EQUAL.equals(operator))
			throw new IllegalArgumentException("Comparison must be >, >=, <, <= or =");
	}

	private static boolean matches(double value, String operator, double threshold) {
		return GREATER.equals(operator) ? value > threshold
				: GREATER_OR_EQUAL.equals(operator) ? value >= threshold
				: LESS.equals(operator) ? value < threshold
				: LESS_OR_EQUAL.equals(operator) ? value <= threshold
				: Double.compare(value, threshold) == 0;
	}

	@Override
	protected synchronized void calculateMyActualState() {
		if (myInputValues[0] != null) {
			double value = myInputValues[0].getOutputAsNumber();
			if (state) {
				if (matches(value, offOperator, offThreshold)) state = false;
			} else if (matches(value, onOperator, onThreshold)) state = true;
		}
		myActualOutputState.setValue(state);
	}

	public synchronized String getOnOperator() { return onOperator; }
	public synchronized double getOnThreshold() { return onThreshold; }
	public synchronized String getOffOperator() { return offOperator; }
	public synchronized double getOffThreshold() { return offThreshold; }

	@Override public void resetSpecific() { state = false; calculateMyActualState(); updateOutputValidity(); updateMyOutputs(); }

	@Override
	public synchronized Node getStorageXML(Document document) {
		Element root = (Element) super.getStorageXML(document);
		Element config = document.createElement("NumericThresholdSwitch");
		config.setAttribute("onOperator", onOperator);
		config.setAttribute("onThreshold", Double.toString(onThreshold));
		config.setAttribute("offOperator", offOperator);
		config.setAttribute("offThreshold", Double.toString(offThreshold));
		root.appendChild(config);
		return root;
	}

	@Override
	public synchronized boolean initDataComponent(Node node) throws Exception {
		if (!super.initDataComponent(node)) return false;
		NodeList children = node.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) if ("NumericThresholdSwitch".equals(children.item(i).getNodeName())) {
			Node config = children.item(i);
			configure(config.getAttributes().getNamedItem("onOperator").getNodeValue(),
					Double.parseDouble(config.getAttributes().getNamedItem("onThreshold").getNodeValue()),
					config.getAttributes().getNamedItem("offOperator").getNodeValue(),
					Double.parseDouble(config.getAttributes().getNamedItem("offThreshold").getNodeValue()));
		}
		return true;
	}

	@Override
	public synchronized JPanel getSpecificDetailsPanel() {
		JPanel panel = new JPanel(new GridLayout(2, 4, 5, 5));
		JComboBox<String> on = new JComboBox<String>(new String[] { GREATER, GREATER_OR_EQUAL, LESS, LESS_OR_EQUAL, EQUAL }); on.setSelectedItem(onOperator);
		JComboBox<String> off = new JComboBox<String>(new String[] { GREATER, GREATER_OR_EQUAL, LESS, LESS_OR_EQUAL, EQUAL }); off.setSelectedItem(offOperator);
		JSpinner onValue = new JSpinner(new SpinnerNumberModel(onThreshold, -Double.MAX_VALUE, Double.MAX_VALUE, 0.1));
		JSpinner offValue = new JSpinner(new SpinnerNumberModel(offThreshold, -Double.MAX_VALUE, Double.MAX_VALUE, 0.1));
		Runnable save = () -> configure((String) on.getSelectedItem(), ((Number) onValue.getValue()).doubleValue(),
				(String) off.getSelectedItem(), ((Number) offValue.getValue()).doubleValue());
		on.addActionListener(event -> save.run()); off.addActionListener(event -> save.run());
		onValue.addChangeListener(event -> save.run()); offValue.addChangeListener(event -> save.run());
		panel.add(new JLabel("Switch on")); panel.add(on); panel.add(new JLabel("Threshold")); panel.add(onValue);
		panel.add(new JLabel("Switch off")); panel.add(off); panel.add(new JLabel("Threshold")); panel.add(offValue);
		return panel;
	}
}
