package mdn.jh.automation.corefunctions;

import java.util.HashSet;
import org.json.*;
import org.w3c.dom.*;
import mdn.jh.automation.io.source.DataSource;

/** Single selection of a label, exposing its assigned integer. Starts at zero. */
public final class StringSelectorDataSource extends DataSource {
    private static final long serialVersionUID = 1L;
    private JSONArray options = new JSONArray();
    private int selectedIndex = -1;
    public StringSelectorDataSource() { setName("String Selector"); setStatusMessage("String Selector"); }
    public synchronized JSONArray getOptions() throws JSONException { return new JSONArray(options.toString()); }
    public synchronized int getSelectedIndex() { return selectedIndex; }
    public synchronized void configure(JSONArray incoming) throws JSONException {
        if (incoming.length() > 200) throw new IllegalArgumentException("At most 200 choices are allowed");
        JSONArray validated = new JSONArray(); var labels = new HashSet<String>();
        for (int i=0;i<incoming.length();i++) {
            JSONObject entry=incoming.getJSONObject(i); String label=entry.getString("label").trim();
            Object raw=entry.get("value");
            if (!(raw instanceof Number)) throw new IllegalArgumentException("Each value must be an integer");
            double value=((Number)raw).doubleValue();
            if (!Double.isFinite(value) || value!=Math.rint(value) || value<Integer.MIN_VALUE || value>Integer.MAX_VALUE)
                throw new IllegalArgumentException("Each value must be a 32-bit integer");
            if (label.isEmpty() || label.length()>200 || !labels.add(label)) throw new IllegalArgumentException("Choice labels must be unique and 1–200 characters long");
            validated.put(new JSONObject().put("label",label).put("value",(int)value));
        }
        if (validated.toString().equals(options.toString())) return;
        options=validated; selectedIndex=-1; updateMyOutputs();
    }
    public synchronized void select(int index) {
        if (index < -1 || index >= options.length()) throw new IllegalArgumentException("Unknown choice");
        selectedIndex=index; updateMyOutputs();
    }
    @Override public synchronized double getOutputAsNumber() { return selectedIndex<0 ? 0 : options.optJSONObject(selectedIndex).optInt("value",0); }
    @Override public boolean getOutputAsBoolean() { return getOutputAsNumber()!=0; }
    @Override public String getOutputAsString() { return Integer.toString((int)getOutputAsNumber()); }
    @Override public int getDataTypeOutput() { return TYPE_DOUBLE_IO; }
    @Override public boolean isDataValid() { return true; }
    @Override public javax.swing.JPanel getSpecificDetailsPanel() { return null; }
    @Override public synchronized Node getStorageXML(Document document) {
        Element root=getStorage(document), settings=document.createElement("StringSelector");
        settings.setAttribute("name",getName()==null?"String Selector":getName());
        settings.setTextContent(options.toString()); root.appendChild(settings); return root;
    }
    @Override public boolean initDataComponent(Node node) throws Exception {
        initDataSource(node);
        Element settings=(Element)((Element)node).getElementsByTagName("StringSelector").item(0);
        configure(new JSONArray(settings.getTextContent()));setName(settings.getAttribute("name"));selectedIndex=-1;return true;
    }
}
