package mdn.jh.automation.corefunctions;

import javax.swing.JPanel;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import mdn.jh.automation.io.DataComponentStub;
import mdn.jh.automation.io.source.DataSource;

/** A Boolean source operated directly from the web automation canvas. */
public class WebUiBooleanDataSource extends DataSource {
    private static final long serialVersionUID = 1L;
    public static final String MODE_PUSHBUTTON = "pushbutton";
    public static final String MODE_SWITCH = "switch";

    private String mode = MODE_SWITCH;
    private boolean value;

    public WebUiBooleanDataSource() { setStatusMessage("Web UI switch"); }

    public WebUiBooleanDataSource(String mode) throws Exception {
        setMode(mode);
    }

    public synchronized String getMode() { return mode; }

    public synchronized void setMode(String mode) throws Exception {
        if (!MODE_PUSHBUTTON.equals(mode) && !MODE_SWITCH.equals(mode)) throw new Exception("Web UI control must be a pushbutton or switch");
        this.mode = mode;
        if (MODE_PUSHBUTTON.equals(mode)) value = false;
        setStatusMessage("Web UI " + mode);
    }

    public synchronized void setValue(boolean value) {
        if (this.value == value) return;
        this.value = value;
        updateMyOutputs();
    }

    public synchronized void toggle() throws Exception {
        if (!MODE_SWITCH.equals(mode)) throw new Exception("Only a switch can be toggled");
        setValue(!value);
    }

    @Override public synchronized boolean getOutputAsBoolean() { return value; }
    @Override public synchronized double getOutputAsNumber() { return value ? 1 : 0; }
    @Override public synchronized String getOutputAsString() { return Boolean.toString(value); }
    @Override public int getDataTypeOutput() { return DataComponentStub.TYPE_BOOLEAN_IO; }
    @Override public boolean isDataValid() { return true; }
    @Override public JPanel getSpecificDetailsPanel() { return null; }

    @Override public synchronized Node getStorageXML(Document doc) {
        Element root = getStorage(doc), control = doc.createElement("WebUiBoolean");
        control.setAttribute("mode", mode);
        control.setAttribute("value", Boolean.toString(value));
        if (getName() != null) control.setAttribute("name", getName());
        root.appendChild(control);
        return root;
    }

    @Override public boolean initDataComponent(Node node) throws Exception {
        initDataSource(node);
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) if ("WebUiBoolean".equals(children.item(i).getNodeName())) {
            Node control = children.item(i);
            setMode(control.getAttributes().getNamedItem("mode").getNodeValue());
            Node storedValue = control.getAttributes().getNamedItem("value");
            value = MODE_SWITCH.equals(mode) && storedValue != null && Boolean.parseBoolean(storedValue.getNodeValue());
            Node storedName = control.getAttributes().getNamedItem("name");
            if (storedName != null) setName(storedName.getNodeValue());
        }
        return true;
    }
}
