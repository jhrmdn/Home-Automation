package mdn.jh.automation.io.logic.components.converter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import mdn.jh.automation.io.DataComponentStub;
import mdn.jh.automation.io.logic.InputDefinition;
import mdn.jh.automation.io.logic.LogicBaseBoolean;
import mdn.jh.automation.io.logic.LogicComponentDescription;

/** Converts an integer/number using >=, <=, or exact comparison. */
public class IntegerBooleanConverter extends LogicBaseBoolean {
    private static final long serialVersionUID = 1L;
    public static final String GREATER_OR_EQUAL = "greaterOrEqual", LESS_OR_EQUAL = "lessOrEqual", EQUAL = "equal";
    private static final InputDefinition[] INPUTS = { new InputDefinition("Integer input", DataComponentStub.TYPE_DOUBLE_IO) };
    private String comparison = GREATER_OR_EQUAL;
    private long comparisonValue;

    public IntegerBooleanConverter() {
        super(DataComponentStub.TYPE_BOOLEAN_IO, INPUTS, new LogicComponentDescription("Integer to Boolean"));
        setName("Integer to Boolean"); myActualOutputState.setDataValid(true); calculateMyActualState();
    }
    public void configure(String comparison, long value) {
        if (!GREATER_OR_EQUAL.equals(comparison) && !LESS_OR_EQUAL.equals(comparison) && !EQUAL.equals(comparison)) throw new IllegalArgumentException("Unknown integer comparison");
        this.comparison = comparison; this.comparisonValue = value; calculateMyActualState(); updateMyOutputs();
    }
    public String getComparison() { return comparison; }
    public long getComparisonValue() { return comparisonValue; }
    @Override protected void calculateMyActualState() {
        double input = myInputValues[0] == null ? 0 : myInputValues[0].getOutputAsNumber();
        boolean result = GREATER_OR_EQUAL.equals(comparison) ? input >= comparisonValue : LESS_OR_EQUAL.equals(comparison) ? input <= comparisonValue : input == comparisonValue;
        myActualOutputState.setValue(result); myActualOutputState.setDataValid(myInputValues[0] == null || myInputValues[0].isDataValid());
    }
    @Override public void resetSpecific() { calculateMyActualState(); updateMyOutputs(); }
    @Override public Node getStorageXML(Document doc) {
        Element root = (Element) super.getStorageXML(doc), config = doc.createElement("IntegerBooleanConverter");
        config.setAttribute("comparison", comparison); config.setAttribute("value", Long.toString(comparisonValue)); root.appendChild(config); return root;
    }
    @Override public boolean initDataComponent(Node node) throws Exception {
        if (!super.initDataComponent(node)) return false; NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) if ("IntegerBooleanConverter".equals(children.item(i).getNodeName())) {
            Node config = children.item(i); configure(config.getAttributes().getNamedItem("comparison").getNodeValue(), Long.parseLong(config.getAttributes().getNamedItem("value").getNodeValue()));
        }
        return true;
    }
}
