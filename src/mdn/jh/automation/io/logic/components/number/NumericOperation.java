package mdn.jh.automation.io.logic.components.number;

import java.awt.FlowLayout;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import mdn.jh.automation.io.DataComponentStub;
import mdn.jh.automation.io.logic.InputDefinition;
import mdn.jh.automation.io.logic.LogicBase;
import mdn.jh.automation.io.logic.LogicComponentDescription;

/** Common implementation for six-input numeric operations. */
public abstract class NumericOperation extends LogicBase {
	private static final long serialVersionUID = 1L;
	public static final String OUTPUT_FLOAT = "float";
	public static final String OUTPUT_INTEGER = "integer";
	private static final InputDefinition[] INPUTS = createInputs();

	private String outputType = OUTPUT_FLOAT;

	protected NumericOperation(String name) {
		this(name, INPUTS);
	}

	protected NumericOperation(String name, InputDefinition[] inputs) {
		super(DataComponentStub.TYPE_DOUBLE_IO, inputs, new LogicComponentDescription(name));
		setName(name);
		setHelptext("Combines up to six integer or floating-point Number inputs");
		myActualOutputState.setDataValid(true);
		calculateMyActualState();
	}

	private static InputDefinition[] createInputs() {
		InputDefinition[] inputs = new InputDefinition[6];
		for (int i = 0; i < inputs.length; i++)
			inputs[i] = new InputDefinition("Input " + (i + 1), DataComponentStub.TYPE_DOUBLE_IO,
					"Integer or floating-point number");
		return inputs;
	}

	public synchronized String getOutputType() {
		return outputType;
	}

	public synchronized void setOutputType(String outputType) {
		if (!OUTPUT_FLOAT.equals(outputType) && !OUTPUT_INTEGER.equals(outputType))
			throw new IllegalArgumentException("Output type must be integer or float");
		this.outputType = outputType;
		calculateMyActualState();
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
		return OUTPUT_INTEGER.equals(outputType)
				? Long.toString(Math.round(myActualOutputState.getOutputAsNumber()))
				: Double.toString(myActualOutputState.getOutputAsNumber());
	}

	@Override
	protected synchronized void calculateMyActualState() {
		double result = calculateResult();
		if (!Double.isFinite(result)) {
			myActualOutputState.setDataValid(false);
			return;
		}
		if (OUTPUT_INTEGER.equals(outputType)) result = Math.round(result);
		myActualOutputState.setValue(result);
		myActualOutputState.setDataValid(inputsAreValid());
	}

	protected abstract double calculateResult();

	protected final boolean hasInput(int index) {
		return myInputValues[index] != null;
	}

	protected final double inputValue(int index) {
		return myInputValues[index].getOutputAsNumber();
	}

	private boolean inputsAreValid() {
		for (int i = 0; i < myInputValues.length; i++)
			if (myInputValues[i] != null && !myInputValues[i].isDataValid()) return false;
		return true;
	}

	@Override
	public synchronized void resetSpecific() {
		calculateMyActualState();
		updateMyOutputs();
	}

	@Override
	public synchronized JPanel getSpecificDetailsPanel() {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(new JLabel("Output:"));
		JComboBox<String> output = new JComboBox<String>(new String[] { OUTPUT_FLOAT, OUTPUT_INTEGER });
		output.setSelectedItem(outputType);
		output.addActionListener(event -> setOutputType((String) output.getSelectedItem()));
		panel.add(output);
		return panel;
	}

	@Override
	public synchronized Node getStorageXML(Document doc) {
		Element root = (Element) super.getStorageXML(doc);
		Element config = doc.createElement("NumericOperation");
		config.setAttribute("outputType", outputType);
		root.appendChild(config);
		return root;
	}

	@Override
	public synchronized boolean initDataComponent(Node node) throws Exception {
		if (!super.initDataComponent(node)) return false;
		NodeList children = node.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			if ("NumericOperation".equals(child.getNodeName())) {
				Node output = child.getAttributes().getNamedItem("outputType");
				if (output != null) setOutputType(output.getNodeValue());
			}
		}
		return true;
	}
}
