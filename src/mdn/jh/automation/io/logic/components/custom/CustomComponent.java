package mdn.jh.automation.io.logic.components.custom;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.*;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.xml.sax.InputSource;
import org.json.*;
import org.w3c.dom.*;
import mdn.jh.automation.io.*;
import mdn.jh.automation.io.source.DataSource;
import mdn.jh.automation.device.Device;
import mdn.jh.automation.SmartHomeHandler;
import mdn.jh.automation.io.logic.*;

/** An independent, embedded graph with explicitly named, typed boundary ports. */
public final class CustomComponent extends LogicBase {
    private static final long serialVersionUID = 1L;
    private final Map<Integer, DataComponentStub> children = new LinkedHashMap<>();
    private final List<Bridge> bridges = new ArrayList<>();
    private final List<DataOutputIF> outputValues = new ArrayList<>();
    private final List<Observer> observers = new ArrayList<>();
    private JSONObject graph;
    private volatile boolean ready;
    private String templateName = "Custom component";
    private static final ThreadLocal<Integer> DEPTH = ThreadLocal.withInitial(() -> 0);

    public CustomComponent() {
        super(TYPE_STRING_IO, new InputDefinition[0], new LogicComponentDescription("Custom component"));
        setName(templateName);
    }
    public String getTemplateName() { return templateName; }
    public int getInternalComponentCount() { return children.size(); }
    public static Document document() throws Exception {
        return factory().newDocumentBuilder().newDocument();
    }
    private static DocumentBuilderFactory factory() throws Exception {
        var factory = DocumentBuilderFactory.newInstance();
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        return factory;
    }
    public static Element parse(String xml) throws Exception {
        return factory().newDocumentBuilder().parse(new InputSource(new StringReader(xml))).getDocumentElement();
    }
    public static String xml(Node node) throws Exception {
        var factory = TransformerFactory.newInstance();
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        var transformer = factory.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        var writer = new StringWriter(); transformer.transform(new DOMSource(node), new StreamResult(writer));
        return writer.toString();
    }
    public static Element settingsOnly(DataComponentStub component) throws Exception {
        var node = (Element)((mdn.jh.automation.storage.Storeable)component).getStorageXML(document());
        node.setAttribute("id", Integer.toString(component.getId()));
        for (Node child = node.getFirstChild(); child != null;) {
            Node next = child.getNextSibling();
            if ("Outputs".equals(child.getNodeName())) node.removeChild(child);
            child = next;
        }
        return node;
    }
    private static void reserveGraphIds(JSONObject definition, int depth) throws Exception {
        if (depth >= 12) throw new IllegalArgumentException("Custom component nesting is too deep");
        JSONArray nodes = definition.getJSONArray("nodes");
        if (nodes.length() > 200) throw new IllegalArgumentException("Too many internal components");
        for (int i = 0; i < nodes.length(); i++) {
            Element node = parse(nodes.getJSONObject(i).getString("xml"));
            reserveId(Integer.parseInt(node.getAttribute("id")));
            for (Node n = node.getFirstChild(); n != null; n = n.getNextSibling())
                if ("CustomGraph".equals(n.getNodeName())) reserveGraphIds(new JSONObject(n.getTextContent()), depth + 1);
        }
    }
    public void initialize(JSONObject definition, boolean fresh) throws Exception {
        int depth = DEPTH.get();
        if (depth >= 12) throw new IllegalArgumentException("Custom components may nest at most 12 levels");
        DEPTH.set(depth + 1);
        ready = false;
        try {
            if (!children.isEmpty()) throw new IllegalStateException("Component already initialized");
            graph = new JSONObject(definition.toString());
            templateName = graph.getString("name");
            JSONArray nodes = graph.getJSONArray("nodes"), inputs = graph.getJSONArray("inputs"), outputs = graph.getJSONArray("outputs");
            if (nodes.length() < 1 || nodes.length() > 200 || inputs.length() > 32 || outputs.length() < 1 || outputs.length() > 32)
                throw new IllegalArgumentException("Use 1–200 components, up to 32 inputs and 1–32 outputs");
            reserveGraphIds(graph, 0);
            Map<Integer, Element> stored = new LinkedHashMap<>();
            for (int i = 0; i < nodes.length(); i++) {
                JSONObject entry = nodes.getJSONObject(i); Element node = parse(entry.getString("xml"));
                if (stored.put(entry.getInt("key"), node) != null) throw new IllegalArgumentException("Duplicate internal component");
                reserveId(Integer.parseInt(node.getAttribute("id")));
            }
            for (var entry : stored.entrySet()) {
                Element node = entry.getValue(); String className = node.getAttribute("class");
                if (!className.startsWith("mdn.jh.automation.")) throw new IllegalArgumentException("Unsupported component class");
                Class<?> type = Class.forName(className);
                if (!LogicBase.class.isAssignableFrom(type) && !DataSource.class.isAssignableFrom(type)) throw new IllegalArgumentException("Only sources and logic can be embedded; sinks must stay outside");
                DataComponentStub child = (DataComponentStub)type.getConstructor().newInstance();
                children.put(entry.getKey(), child);
                if (fresh) {
                    String id = Integer.toString(child.getId()); node.setAttribute("id", id);
                    for (Node n = node.getFirstChild(); n != null; n = n.getNextSibling())
                        if (n instanceof Element base && "DataComponent".equals(n.getNodeName())) base.setAttribute("id", id);
                }
                // Internal edges are restored locally, never placed in Store's global pending list.
                for (Node n = node.getFirstChild(); n != null;) {
                    Node next = n.getNextSibling(); if ("Outputs".equals(n.getNodeName())) node.removeChild(n); n = next;
                }
                if (child instanceof CustomComponent custom) custom.load(node, fresh);
                else child.initDataComponent(node);
            }
            JSONArray edges = graph.getJSONArray("connections");
            for (int i = 0; i < edges.length(); i++) {
                JSONObject edge = edges.getJSONObject(i); DataConnectionIF source = (DataConnectionIF)child(edge.getInt("sourceId")); LogicBase target = inputChild(edge.getInt("targetId"));
                int input = edge.getInt("input"), output = edge.optInt("output", 0);
                validateInput(target, input, source.getOutputPort(output).getDataTypeOutput());
                source.addInputConnection(target, input, output);
            }
            InputDefinition[] definitions = new InputDefinition[inputs.length()];
            Set<String> names = new HashSet<>();
            for (int i = 0; i < inputs.length(); i++) {
                JSONObject port = inputs.getJSONObject(i); String name = portName(port, names); int type = port.getInt("type");
                LogicBase target = inputChild(port.getInt("targetId")); int input = port.getInt("input");
                validateInput(target, input, type);
                definitions[i] = new InputDefinition(name, type);
                Bridge bridge = new Bridge(type); bridges.add(bridge); bridge.addInputConnection(target, input);
            }
            defineInputs(definitions); names.clear();
            for (int i = 0; i < outputs.length(); i++) {
                JSONObject port = outputs.getJSONObject(i); portName(port, names);
                DataConnectionIF source = (DataConnectionIF)child(port.getInt("sourceId")); int output = port.optInt("output", 0);
                DataOutputIF value = source.getOutputPort(output);
                if (value.getDataTypeOutput() != port.getInt("type")) throw new IllegalArgumentException("Output datatype does not match its internal component");
                outputValues.add(value);
                Observer observer = new Observer(value.getDataTypeOutput()); observers.add(observer);
                source.addInputConnection(observer, 0, output);
            }
            setHelptext("Reusable component: " + templateName + ". Named inputs and outputs have independent datatypes.");
            ready = true; calculateMyActualState();
        } catch (Exception error) { shutdownRuntime(); throw error; }
        finally { DEPTH.set(depth); }
    }
    private DataComponentStub child(int key) {
        DataComponentStub child = children.get(key);
        if (child == null) throw new IllegalArgumentException("Port or connection references a missing internal component");
        return child;
    }
    private LogicBase inputChild(int key) {
        DataComponentStub component = child(key);
        if (!(component instanceof LogicBase logic)) throw new IllegalArgumentException("A source cannot receive input connections");
        return logic;
    }
    public List<DataSource> getEmbeddedSources() {
        List<DataSource> sources = new ArrayList<>();
        for (var child : children.values()) {
            if (child instanceof DataSource source) sources.add(source);
            if (child instanceof CustomComponent nested) sources.addAll(nested.getEmbeddedSources());
        }
        return sources;
    }
    /** Bind private source copies to their existing device's polling/subscription handler. */
    public void bindSources(SmartHomeHandler handler) throws Exception {
        try {
            JSONArray nodes = graph.getJSONArray("nodes");
            for (int i = 0; i < nodes.length(); i++) {
                JSONObject entry = nodes.getJSONObject(i); DataComponentStub component = child(entry.getInt("key"));
                if (component instanceof CustomComponent nested) nested.bindSources(handler);
                if (!(component instanceof DataSource source)) continue;
                if (source.getMyDataSourceDevice() != null) continue;
                int deviceId = entry.getInt("deviceId");
                Device device = handler.getDevices().stream().filter(candidate -> candidate.getDeviceID() == deviceId).findFirst().orElse(null);
                if (device == null) throw new IllegalArgumentException("Source device " + deviceId + " is missing for " + source.getName());
                source.setEmbeddedOwnerId(getId());
                device.getDataSourceHandler().addDataSource(source);
            }
        } catch (Exception error) { shutdownRuntime(); throw error; }
    }
    private static String portName(JSONObject port, Set<String> names) throws Exception {
        String name = port.getString("name").trim();
        if (name.isEmpty() || name.length() > 80 || !names.add(name)) throw new IllegalArgumentException("Port names must be unique and 1–80 characters long");
        return name;
    }
    private static void validateInput(LogicBase target, int input, int type) {
        if (input < 0 || input >= target.getInputDefinitions().length || target.isInputConnected(input)) throw new IllegalArgumentException("Input is missing or already connected");
        if (type < TYPE_DOUBLE_IO || type > TYPE_STRING_IO || target.getInputDefinitions()[input].getDataType() != type)
            throw new IllegalArgumentException("Input datatype does not match its internal component");
    }
    @Override public int getOutputCount() { return outputValues.size(); }
    @Override public String getOutputName(int index) {
        try { return graph.getJSONArray("outputs").getJSONObject(index).getString("name"); }
        catch (JSONException e) { throw new IllegalArgumentException(e); }
    }
    @Override public DataOutputIF getOutputPort(int index) {
        DataOutputIF output = outputValues.get(index);
        return index == 0 ? new ComponentOutputView(this, output) : output;
    }
    @Override public int getDataTypeOutput() { return outputValues.isEmpty() ? TYPE_STRING_IO : outputValues.get(0).getDataTypeOutput(); }
    @Override public boolean isDataValid() { return ready && !outputValues.isEmpty() && outputValues.get(0).isDataValid(); }
    @Override public boolean getOutputAsBoolean() { return !outputValues.isEmpty() && outputValues.get(0).getOutputAsBoolean(); }
    @Override public double getOutputAsNumber() { return outputValues.isEmpty() ? 0 : outputValues.get(0).getOutputAsNumber(); }
    @Override public String getOutputAsString() { return outputValues.isEmpty() ? "" : outputValues.get(0).getOutputAsString(); }
    @Override protected void calculateMyActualState() {
        if (!ready) return;
        for (int i = 0; i < bridges.size(); i++) bridges.get(i).forward(myInputValues[i]);
    }
    @Override public void unlinkInput(int input) {
        super.unlinkInput(input);
        if (hasInputOverride(input)) {
            DataValue empty = new DataValue(); empty.changeDataType(getInputDefinitions()[input].getDataType()); empty.setDataValid(false);
            myInputValues[input] = new ComponentInputView(this, input, empty);
        } else myInputValues[input] = null;
        calculateMyActualState(); updateMyOutputs();
    }
    @Override public void addInputConnection(DataInputIF input, int inputIndex) throws RecursionException { addInputConnection(input, inputIndex, 0); }
    @Override public void addInputConnection(DataInputIF input, int inputIndex, int outputIndex) throws RecursionException {
        DataOutputIF value = getOutputPort(outputIndex);
        if (inputIndex < 0 || inputIndex >= input.getInputDefinitions().length || input.getInputDefinitions()[inputIndex].getDataType() != value.getDataTypeOutput())
            throw new IllegalArgumentException("Incompatible input/output datatype");
        input.setConnection(value, getId(), inputIndex);
        myOutputs.add(new DataInputConnection(input, inputIndex, outputIndex));
    }
    @Override public void resetSpecific() { for (var child : children.values()) if (child instanceof LogicBase logic) logic.resetSpecific(); }
    public void isolateDevelopmentPersistence() { for (var child : children.values()) { if (child instanceof mdn.jh.automation.io.logic.components.number.Counter counter) counter.isolateDevelopmentPersistence(); if (child instanceof CustomComponent custom) custom.isolateDevelopmentPersistence(); } }
    @Override public void shutdownRuntime() { ready = false; for (var child : children.values()) {
        if (child instanceof LogicBase logic) logic.shutdownRuntime();
        if (child instanceof DataSource source && source.getMyDataSourceDevice() != null) {
            source.getMyDataSourceDevice().removeDataSource(source); source.setMyDataSourceHandler(null);
        }
    } }
    @Override public void deleteThis() { shutdownRuntime(); super.deleteThis(); for (var child : children.values()) { if (child instanceof LogicBase logic) logic.deleteThis(); if (child instanceof DataSource source) source.deleteThis(); } }
    public JSONObject snapshotGraph() throws Exception {
        JSONObject copy = new JSONObject(graph.toString()); JSONArray nodes = new JSONArray();
        JSONArray originals = graph.getJSONArray("nodes");
        for (int i = 0; i < originals.length(); i++) {
            JSONObject entry = new JSONObject(originals.getJSONObject(i).toString());
            entry.put("xml", xml(settingsOnly(child(entry.getInt("key"))))); nodes.put(entry);
        }
        copy.put("nodes", nodes); return copy;
    }
    @Override public Node getStorageXML(Document document) {
        Element root = (Element)super.getStorageXML(document), stored = document.createElement("CustomGraph");
        try { stored.setTextContent(snapshotGraph().toString()); } catch (Exception e) { throw new IllegalStateException("Cannot save custom component", e); }
        root.appendChild(stored); return root;
    }
    private boolean load(Node node, boolean fresh) throws Exception {
        // Initialize ports before restoring overrides from the base component.
        Element graphNode = null;
        for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling())
            if (child instanceof Element && "CustomGraph".equals(child.getNodeName())) graphNode = (Element)child;
        if (graphNode == null) throw new IllegalArgumentException("Custom component graph missing");
        initialize(new JSONObject(graphNode.getTextContent()), fresh);
        return super.initDataComponent(node);
    }
    @Override public boolean initDataComponent(Node node) throws Exception { return load(node, false); }

    private static final class Bridge extends LogicBase {
        private DataOutputIF value;
        private final int type;
        Bridge(int type) { super(type, new InputDefinition[0], new LogicComponentDescription("Internal input")); this.type = type; }
        void forward(DataOutputIF value) { this.value = value; updateMyOutputs(); }
        @Override public void resetSpecific() { }
        @Override protected void calculateMyActualState() { }
        @Override public int getDataTypeOutput() { return type; }
        @Override public boolean isDataValid() { return value != null && value.isDataValid(); }
        @Override public boolean getOutputAsBoolean() { return value != null && value.getOutputAsBoolean(); }
        @Override public double getOutputAsNumber() { return value == null ? 0 : value.getOutputAsNumber(); }
        @Override public String getOutputAsString() { return value == null ? "" : value.getOutputAsString(); }
    }
    private final class Observer extends LogicBase {
        Observer(int type) { super(type, new InputDefinition[]{new InputDefinition("Value", type)}, new LogicComponentDescription("Internal output")); }
        @Override public void resetSpecific() { }
        @Override public int getDataTypeOutput() { return myActualOutputState.getDataTypeOutput(); }
        @Override protected void calculateMyActualState() { if (ready) CustomComponent.this.updateMyOutputs(); }
    }
}
