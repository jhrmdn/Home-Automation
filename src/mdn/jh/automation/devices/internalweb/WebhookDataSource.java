package mdn.jh.automation.devices.internalweb;

import org.w3c.dom.*;
import mdn.jh.automation.io.source.DataSource;

/** A typed source updated on receipt of an HTTP webhook. */
public class WebhookDataSource extends DataSource {
    private static final long serialVersionUID = 1L;
    private String valueType = "string", value = "";
    private boolean loginRequired, valid;

    public WebhookDataSource() { setStatusMessage("Waiting for webhook"); }
    public WebhookDataSource(String type, boolean loginRequired) { this(); configure(type, loginRequired); }

    public synchronized void configure(String type, boolean requireLogin) {
        if (!"int".equals(type) && !"number".equals(type) && !"string".equals(type))
            throw new IllegalArgumentException("Datatype must be int, string, or number");
        if (!type.equals(valueType)) {
            if (isDataTypeLocked()) throw new IllegalArgumentException("Disconnect outputs before changing the datatype");
            valid = false;
            value = "";
        }
        valueType = type;
        loginRequired = requireLogin;
    }

    public synchronized void receive(String incoming) {
        if (incoming == null) throw new IllegalArgumentException("Parameter 'value' is required");
        if (incoming.length() > 65536) throw new IllegalArgumentException("Value exceeds 65536 characters");
        String parsed = incoming;
        if ("int".equals(valueType)) {
            try { parsed = Integer.toString(Integer.parseInt(incoming.trim())); }
            catch (NumberFormatException e) { throw new IllegalArgumentException("Value must be a 32-bit integer"); }
        } else if ("number".equals(valueType)) {
            double number;
            try { number = Double.parseDouble(incoming.trim()); }
            catch (NumberFormatException e) { throw new IllegalArgumentException("Value must be a finite number"); }
            if (!Double.isFinite(number)) throw new IllegalArgumentException("Value must be a finite number");
            parsed = Double.toString(number);
        }
        value = parsed;
        valid = true;
        setStatusMessage("Webhook received");
        updateMyOutputs();
    }

    public synchronized String getValueType() { return valueType; }
    public synchronized boolean isLoginRequired() { return loginRequired; }
    @Override public synchronized String getOutputAsString() { return value; }
    @Override public synchronized double getOutputAsNumber() { try { return Double.parseDouble(value); } catch (NumberFormatException e) { return 0; } }
    @Override public synchronized boolean getOutputAsBoolean() { return "true".equalsIgnoreCase(value) || getOutputAsNumber() != 0; }
    @Override public synchronized int getDataTypeOutput() { return "string".equals(valueType) ? TYPE_STRING_IO : TYPE_DOUBLE_IO; }
    @Override public synchronized boolean isDataValid() { return valid; }
    @Override public javax.swing.JPanel getSpecificDetailsPanel() { return null; }
    @Override public synchronized Node getStorageXML(Document document) {
        Element root = getStorage(document), config = document.createElement("WebhookSource");
        config.setAttribute("valueType", valueType);
        config.setAttribute("loginRequired", Boolean.toString(loginRequired));
        if (getName() != null) config.setAttribute("name", getName());
        root.appendChild(config);
        return root;
    }
    @Override public boolean initDataComponent(Node node) throws Exception {
        initDataSource(node);
        Element config = (Element) ((Element) node).getElementsByTagName("WebhookSource").item(0);
        configure(config.getAttribute("valueType"), Boolean.parseBoolean(config.getAttribute("loginRequired")));
        if (config.hasAttribute("name")) setName(config.getAttribute("name"));
        return true;
    }
}
