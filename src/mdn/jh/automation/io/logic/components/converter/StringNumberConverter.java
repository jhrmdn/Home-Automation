package mdn.jh.automation.io.logic.components.converter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import mdn.jh.automation.io.DataComponentStub;
import mdn.jh.automation.io.logic.InputDefinition;
import mdn.jh.automation.io.logic.LogicBase;
import mdn.jh.automation.io.logic.LogicComponentDescription;

/** Converts a String input to an integer or floating-point value with a configurable fallback. */
public class StringNumberConverter extends LogicBase {
	private static final long serialVersionUID = 1L;
	public static final String TYPE_INTEGER = "integer", TYPE_FLOAT = "float";
	private static final InputDefinition[] INPUTS = {
			new InputDefinition("String input", DataComponentStub.TYPE_STRING_IO) };
	private String outputType = TYPE_INTEGER;
	private double failureValue;

	public StringNumberConverter() {
		super(DataComponentStub.TYPE_DOUBLE_IO, INPUTS, new LogicComponentDescription("String to number"));
		setName("String to number");
		calculateMyActualState();
	}

	public void configure(String outputType, String failureValue) {
		if (!TYPE_INTEGER.equals(outputType) && !TYPE_FLOAT.equals(outputType))
			throw new IllegalArgumentException("Output type must be integer or float");
		double parsedFailure = parse(outputType, failureValue);
		this.outputType = outputType;
		this.failureValue = parsedFailure;
		calculateMyActualState();
		updateMyOutputs();
	}

	public String getOutputType() { return outputType; }
	public double getFailureValue() { return failureValue; }

	private static double parse(String type, String value) {
		if (TYPE_INTEGER.equals(type)) return Long.parseLong(value.trim());
		double parsed = Double.parseDouble(value.trim());
		if (!Double.isFinite(parsed)) throw new IllegalArgumentException("Float value must be finite");
		return parsed;
	}

	@Override public int getDataTypeOutput() { return TYPE_DOUBLE_IO; }

	@Override public String getOutputAsString() {
		return TYPE_INTEGER.equals(outputType) ? Long.toString((long) getOutputAsNumber())
				: Double.toString(getOutputAsNumber());
	}

	@Override protected void calculateMyActualState() {
		double result = failureValue;
		if (myInputValues[0] != null) {
			try { result = parse(outputType, myInputValues[0].getOutputAsString()); }
			catch (RuntimeException ignored) { result = failureValue; }
		}
		myActualOutputState.setValue(result);
		myActualOutputState.setDataValid(myInputValues[0] == null || myInputValues[0].isDataValid());
	}

	@Override public void resetSpecific() { calculateMyActualState(); updateMyOutputs(); }

	@Override public Node getStorageXML(Document doc) {
		Element root = (Element) super.getStorageXML(doc), config = doc.createElement("StringNumberConverter");
		config.setAttribute("outputType", outputType);
		config.setAttribute("failureValue", getOutputText(failureValue));
		root.appendChild(config);
		return root;
	}

	private String getOutputText(double value) {
		return TYPE_INTEGER.equals(outputType) ? Long.toString((long) value) : Double.toString(value);
	}

	@Override public boolean initDataComponent(Node node) throws Exception {
		if (!super.initDataComponent(node)) return false;
		NodeList children = node.getChildNodes();
		for (int i = 0; i < children.getLength(); i++)
			if ("StringNumberConverter".equals(children.item(i).getNodeName())) {
				Node config = children.item(i);
				configure(config.getAttributes().getNamedItem("outputType").getNodeValue(),
						config.getAttributes().getNamedItem("failureValue").getNodeValue());
			}
		return true;
	}
}
