package mdn.jh.automation.devices.internalweb;

import org.w3c.dom.*;
import mdn.jh.automation.io.DataOutputIF;
import mdn.jh.automation.io.logic.InputDefinition;
import mdn.jh.automation.io.sink.DataSink;

/** Exposes its connected input as an HTTP response. */
public class ApiDataSink extends DataSink {
    private static final long serialVersionUID = 1L;
    private volatile String format = "plain";
    private volatile boolean loginRequired;
    private volatile String valueType = "string";

    public ApiDataSink() { super(new InputDefinition[]{new InputDefinition("Value", TYPE_STRING_IO)}); }
    public ApiDataSink(String format, boolean loginRequired) { this(); configure(format, loginRequired); }
    public ApiDataSink(String format, boolean loginRequired, String valueType) { this(); configure(format, loginRequired, valueType); }
    public void configure(String format, boolean loginRequired) {
        configure(format, loginRequired, valueType);
    }
    public synchronized void configure(String format, boolean loginRequired, String valueType) {
        if (!"plain".equals(format) && !"json".equals(format) && !"xml".equals(format))
            throw new IllegalArgumentException("Output format must be plain, json, or xml");
        if (!"string".equals(valueType) && !"number".equals(valueType) && !"boolean".equals(valueType))
            throw new IllegalArgumentException("Input datatype must be string, number, or boolean");
        if (!valueType.equals(this.valueType)) {
            if (isInputConnected(0) || hasInputOverride(0)) throw new IllegalArgumentException("Disconnect the input and clear its override before changing the datatype");
            defineInputs(new InputDefinition[]{new InputDefinition("Value", "string".equals(valueType) ? TYPE_STRING_IO : "boolean".equals(valueType) ? TYPE_BOOLEAN_IO : TYPE_DOUBLE_IO)});
        }
        this.valueType = valueType;
        this.format = format;
        this.loginRequired = loginRequired;
        setStatusMessage("API response: " + format);
    }
    public String getFormat() { return format; }
    public boolean isLoginRequired() { return loginRequired; }
    public String getValueType() { return valueType; }
    public synchronized DataOutputIF getApiValue() { return getInputData(0); }
    @Override public synchronized void updateCycle() {
        DataOutputIF input = getApiValue();
        myOutput.setValue(input == null ? "" : input.getOutputAsString());
        myOutput.setDataValid(input != null && input.isDataValid());
    }
    @Override public synchronized boolean isDataValid() { return getApiValue() != null && getApiValue().isDataValid(); }
    @Override public javax.swing.JPanel getSpecificDetailsPanel() { return null; }
    @Override public Node getStorageXML(Document document) {
        Element root = getStorageDataSink(document), config = document.createElement("ApiSink");
        config.setAttribute("format", format);
        config.setAttribute("loginRequired", Boolean.toString(loginRequired));
        config.setAttribute("valueType", valueType);
        root.appendChild(config);
        return root;
    }
    @Override public void initSpecific(Node node) {
        Element config = (Element) ((Element) node).getElementsByTagName("ApiSink").item(0);
        // Restore the input definition before base-class overrides are used.
        String type = config.getAttribute("valueType");
        if (type.isEmpty()) type = "string";
        valueType = type;
        defineInputs(new InputDefinition[]{new InputDefinition("Value", "string".equals(type) ? TYPE_STRING_IO : "boolean".equals(type) ? TYPE_BOOLEAN_IO : TYPE_DOUBLE_IO)});
        configure(config.getAttribute("format"), Boolean.parseBoolean(config.getAttribute("loginRequired")), type);
    }
}
