package mdn.jh.automation.io.logic.components.converter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import mdn.jh.automation.io.DataComponentStub;
import mdn.jh.automation.io.logic.InputDefinition;
import mdn.jh.automation.io.logic.LogicBase;
import mdn.jh.automation.io.logic.LogicComponentDescription;

/** Maps a Boolean input to configurable integer, floating-point, or String values. */
public class BooleanValueConverter extends LogicBase {
    private static final long serialVersionUID = 1L;
    public static final String TYPE_INTEGER = "integer", TYPE_FLOAT = "float", TYPE_STRING = "string";
    private static final InputDefinition[] INPUTS = { new InputDefinition("Boolean input", DataComponentStub.TYPE_BOOLEAN_IO) };
    private String outputType = TYPE_STRING, trueValue = "true", falseValue = "false";

    public BooleanValueConverter() {
        super(DataComponentStub.TYPE_STRING_IO, INPUTS, new LogicComponentDescription("Boolean to value"));
        setName("Boolean to value"); myActualOutputState.setDataValid(true); calculateMyActualState();
    }

    public void configure(String outputType, String trueValue, String falseValue) {
        if (!TYPE_INTEGER.equals(outputType) && !TYPE_FLOAT.equals(outputType) && !TYPE_STRING.equals(outputType)) throw new IllegalArgumentException("Output type must be integer, float, or string");
        if (TYPE_INTEGER.equals(outputType)) { Long.parseLong(trueValue); Long.parseLong(falseValue); }
        if (TYPE_FLOAT.equals(outputType)) { requireFinite(trueValue); requireFinite(falseValue); }
        this.outputType = outputType; this.trueValue = trueValue; this.falseValue = falseValue; calculateMyActualState(); updateMyOutputs();
    }
    public String getOutputType() { return outputType; }
    public String getTrueValue() { return trueValue; }
    public String getFalseValue() { return falseValue; }
    private double requireFinite(String value) { double parsed = Double.parseDouble(value); if (!Double.isFinite(parsed)) throw new IllegalArgumentException("Float values must be finite"); return parsed; }
    @Override public int getDataTypeOutput() { return TYPE_STRING.equals(outputType) ? TYPE_STRING_IO : TYPE_DOUBLE_IO; }
    @Override public String getOutputAsString() {
        if (TYPE_INTEGER.equals(outputType)) return Long.toString(Math.round(getOutputAsNumber()));
        if (TYPE_FLOAT.equals(outputType)) return String.format(java.util.Locale.ROOT, "%.2f", getOutputAsNumber());
        return super.getOutputAsString();
    }
    @Override protected void calculateMyActualState() {
        boolean input = myInputValues[0] != null && myInputValues[0].getOutputAsBoolean(); String selected = input ? trueValue : falseValue;
        if (TYPE_INTEGER.equals(outputType)) myActualOutputState.setValue((double) Long.parseLong(selected));
        else if (TYPE_FLOAT.equals(outputType)) myActualOutputState.setValue(requireFinite(selected));
        else { myActualOutputState.changeDataType(TYPE_STRING_IO); myActualOutputState.setValue(selected); }
        myActualOutputState.setDataValid(myInputValues[0] == null || myInputValues[0].isDataValid());
    }
    @Override public void resetSpecific() { calculateMyActualState(); updateMyOutputs(); }
    @Override public Node getStorageXML(Document doc) {
        Element root = (Element) super.getStorageXML(doc), config = doc.createElement("BooleanValueConverter");
        config.setAttribute("outputType", outputType); config.setAttribute("trueValue", trueValue); config.setAttribute("falseValue", falseValue); root.appendChild(config); return root;
    }
    @Override public boolean initDataComponent(Node node) throws Exception {
        if (!super.initDataComponent(node)) return false; NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) if ("BooleanValueConverter".equals(children.item(i).getNodeName())) {
            Node config = children.item(i); configure(config.getAttributes().getNamedItem("outputType").getNodeValue(), config.getAttributes().getNamedItem("trueValue").getNodeValue(), config.getAttributes().getNamedItem("falseValue").getNodeValue());
        }
        return true;
    }
}
