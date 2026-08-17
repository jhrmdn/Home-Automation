package mdn.jh.automation.io.logic.components.number;

import java.awt.FlowLayout;

import javax.swing.JComboBox;
import javax.swing.JCheckBox;
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

/** A rising-edge counter with configurable floating-point step size. */
public class Counter extends LogicBase {
	private static final long serialVersionUID = 1L;
	private static final InputDefinition[] INPUTS = {
			new InputDefinition("Increment", DataComponentStub.TYPE_BOOLEAN_IO, "Increment on rising edge"),
			new InputDefinition("Decrement", DataComponentStub.TYPE_BOOLEAN_IO, "Decrement on rising edge"),
			new InputDefinition("Reset", DataComponentStub.TYPE_BOOLEAN_IO, "Reset to zero on rising edge") };

	private double counterValue;
	private double stepSize = 1.0;
	private String outputType = NumericOperation.OUTPUT_FLOAT;
	private boolean persistenceEnabled;
	private transient boolean previousIncrement;
	private transient boolean previousDecrement;
	private transient boolean previousReset;

	public Counter() {
		super(DataComponentStub.TYPE_DOUBLE_IO, INPUTS, new LogicComponentDescription("Counter"));
		setName("Counter");
		setHelptext("Counts rising Increment and Decrement edges; Reset sets the value to zero");
		publishValue();
	}

	public synchronized double getCounterValue() {
		return counterValue;
	}

	public synchronized double getStepSize() {
		return stepSize;
	}

	public synchronized void setStepSize(double stepSize) {
		if (!Double.isFinite(stepSize) || stepSize <= 0)
			throw new IllegalArgumentException("Step size must be a finite number greater than zero");
		this.stepSize = stepSize;
	}

	public synchronized String getOutputType() {
		return outputType;
	}

	public synchronized boolean isPersistenceEnabled() {
		return persistenceEnabled;
	}

	public synchronized void setPersistenceEnabled(boolean persistenceEnabled) {
		this.persistenceEnabled = persistenceEnabled;
		if (persistenceEnabled) CounterPersistence.save(getId(), counterValue);
		else CounterPersistence.remove(getId());
	}

	public synchronized void setOutputType(String outputType) {
		if (!NumericOperation.OUTPUT_FLOAT.equals(outputType)
				&& !NumericOperation.OUTPUT_INTEGER.equals(outputType))
			throw new IllegalArgumentException("Output type must be integer or float");
		this.outputType = outputType;
		publishValue();
		updateMyOutputs();
	}

	@Override
	public int getDataTypeOutput() {
		return DataComponentStub.TYPE_DOUBLE_IO;
	}

	@Override
	public synchronized boolean isDataValid() {
		return myActualOutputState.isDataValid();
	}

	@Override
	public synchronized String getOutputAsString() {
		return NumericOperation.OUTPUT_INTEGER.equals(outputType)
				? Long.toString(Math.round(counterValue)) : Double.toString(counterValue);
	}

	@Override
	protected synchronized void calculateMyActualState() {
		boolean increment = inputState(0);
		boolean decrement = inputState(1);
		boolean reset = inputState(2);

		boolean changed = false;
		if (reset && !previousReset) {
			counterValue = 0;
			changed = true;
		} else {
			if (increment && !previousIncrement) {
				counterValue += stepSize;
				changed = true;
			}
			if (decrement && !previousDecrement) {
				counterValue -= stepSize;
				changed = true;
			}
		}
		previousIncrement = increment;
		previousDecrement = decrement;
		previousReset = reset;
		publishValue();
		if (changed && persistenceEnabled) CounterPersistence.save(getId(), counterValue);
	}

	private boolean inputState(int index) {
		return myInputValues[index] != null && myInputValues[index].getOutputAsBoolean();
	}

	private void publishValue() {
		double output = NumericOperation.OUTPUT_INTEGER.equals(outputType)
				? Math.round(counterValue) : counterValue;
		myActualOutputState.setValue(output);
		myActualOutputState.setDataValid(inputsAreValid());
	}

	private boolean inputsAreValid() {
		for (int i = 0; i < myInputValues.length; i++)
			if (myInputValues[i] != null && !myInputValues[i].isDataValid()) return false;
		return true;
	}

	@Override
	public synchronized void resetSpecific() {
		counterValue = 0;
		previousIncrement = inputState(0);
		previousDecrement = inputState(1);
		previousReset = inputState(2);
		publishValue();
		if (persistenceEnabled) CounterPersistence.save(getId(), counterValue);
		updateMyOutputs();
	}

	@Override
	public synchronized JPanel getSpecificDetailsPanel() {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(new JLabel("Step size:"));
		JSpinner step = new JSpinner(new SpinnerNumberModel(stepSize, 0.000001d,
				Double.MAX_VALUE, 0.1d));
		step.addChangeListener(event -> setStepSize(((Number) step.getValue()).doubleValue()));
		panel.add(step);
		panel.add(new JLabel("Output:"));
		JComboBox<String> output = new JComboBox<String>(new String[] {
				NumericOperation.OUTPUT_FLOAT, NumericOperation.OUTPUT_INTEGER });
		output.setSelectedItem(outputType);
		output.addActionListener(event -> setOutputType((String) output.getSelectedItem()));
		panel.add(output);
		JCheckBox persistence = new JCheckBox("Persist counter value", persistenceEnabled);
		persistence.addActionListener(event -> setPersistenceEnabled(persistence.isSelected()));
		panel.add(persistence);
		return panel;
	}

	@Override
	public synchronized Node getStorageXML(Document doc) {
		Element root = (Element) super.getStorageXML(doc);
		Element config = doc.createElement("Counter");
		config.setAttribute("stepSize", Double.toString(stepSize));
		config.setAttribute("outputType", outputType);
		config.setAttribute("persistenceEnabled", Boolean.toString(persistenceEnabled));
		root.appendChild(config);
		return root;
	}

	@Override
	public synchronized boolean initDataComponent(Node node) throws Exception {
		if (!super.initDataComponent(node)) return false;
		NodeList children = node.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			if (!"Counter".equals(child.getNodeName())) continue;
			Node step = child.getAttributes().getNamedItem("stepSize");
			Node output = child.getAttributes().getNamedItem("outputType");
			Node persistence = child.getAttributes().getNamedItem("persistenceEnabled");
			if (step != null) setStepSize(Double.parseDouble(step.getNodeValue()));
			if (output != null) outputType = output.getNodeValue();
			if (persistence != null) persistenceEnabled = Boolean.parseBoolean(persistence.getNodeValue());
		}
		if (persistenceEnabled) {
			Double storedValue = CounterPersistence.load(getId());
			if (storedValue != null) counterValue = storedValue;
		}
		setOutputType(outputType);
		return true;
	}

	@Override
	public synchronized void deleteThis() {
		if (persistenceEnabled) CounterPersistence.remove(getId());
		super.deleteThis();
	}
}
