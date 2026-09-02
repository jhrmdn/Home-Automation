package mdn.jh.automation.io.logic.components.converter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import mdn.jh.automation.io.DataComponentStub;
import mdn.jh.automation.io.logic.InputDefinition;
import mdn.jh.automation.io.logic.LogicBase;
import mdn.jh.automation.io.logic.LogicComponentDescription;

/** Maps exact strings or regular expressions to numeric output values. */
public class StringListConverter extends LogicBase {
	private static final long serialVersionUID = 1L;
	private static final InputDefinition[] INPUTS = { new InputDefinition("String input", DataComponentStub.TYPE_STRING_IO) };
	private final List<Mapping> mappings = new ArrayList<>();
	private double defaultValue;

	public StringListConverter() {
		super(DataComponentStub.TYPE_DOUBLE_IO, INPUTS, new LogicComponentDescription("String list converter"));
		setName("String list converter");
		setHelptext("Maps a String input to a number. Entries are checked from top to bottom and the first match wins. Exact entries are case-sensitive. Regular expressions must match the complete input. The default value is used when no entry matches.");
		calculateMyActualState();
	}

	public synchronized void configure(double defaultValue, List<Mapping> mappings) {
		if (!Double.isFinite(defaultValue)) throw new IllegalArgumentException("Default value must be finite");
		if (mappings == null) throw new IllegalArgumentException("Mappings are required");
		List<Mapping> validated = new ArrayList<>();
		for (Mapping mapping : mappings) validated.add(validate(mapping));
		this.defaultValue = defaultValue; this.mappings.clear(); this.mappings.addAll(validated);
		calculateMyActualState(); updateMyOutputs();
	}

	private Mapping validate(Mapping mapping) {
		if (mapping == null || mapping.input() == null || mapping.input().isEmpty()) throw new IllegalArgumentException("Mapping input must not be empty");
		if (!Double.isFinite(mapping.output())) throw new IllegalArgumentException("Mapping output must be finite");
		if (mapping.regex()) try { Pattern.compile(mapping.input()); } catch (PatternSyntaxException exception) { throw new IllegalArgumentException("Invalid regular expression: " + mapping.input()); }
		return mapping;
	}

	public synchronized double getDefaultValue() { return defaultValue; }
	public synchronized List<Mapping> getMappings() { return Collections.unmodifiableList(new ArrayList<>(mappings)); }
	@Override public int getDataTypeOutput() { return TYPE_DOUBLE_IO; }

	@Override protected synchronized void calculateMyActualState() {
		double result = defaultValue;
		if (myInputValues[0] != null) {
			String input = myInputValues[0].getOutputAsString();
			for (Mapping mapping : mappings) if (mapping.matches(input)) { result = mapping.output(); break; }
		}
		myActualOutputState.setValue(result);
		myActualOutputState.setDataValid(myInputValues[0] == null || myInputValues[0].isDataValid());
	}

	@Override public void resetSpecific() { calculateMyActualState(); updateMyOutputs(); }

	@Override public synchronized Node getStorageXML(Document doc) {
		Element root = (Element) super.getStorageXML(doc), config = doc.createElement("StringListConverter");
		config.setAttribute("defaultValue", Double.toString(defaultValue));
		for (Mapping mapping : mappings) { Element entry = doc.createElement("Mapping"); entry.setAttribute("input", mapping.input()); entry.setAttribute("regex", Boolean.toString(mapping.regex())); entry.setAttribute("output", Double.toString(mapping.output())); config.appendChild(entry); }
		root.appendChild(config); return root;
	}

	@Override public synchronized boolean initDataComponent(Node node) throws Exception {
		if (!super.initDataComponent(node)) return false;
		NodeList children = node.getChildNodes();
		for (int index = 0; index < children.getLength(); index++) if ("StringListConverter".equals(children.item(index).getNodeName())) {
			Node config = children.item(index); List<Mapping> restored = new ArrayList<>(); NodeList entries = config.getChildNodes();
			for (int entryIndex = 0; entryIndex < entries.getLength(); entryIndex++) if ("Mapping".equals(entries.item(entryIndex).getNodeName())) { Node entry = entries.item(entryIndex); restored.add(new Mapping(entry.getAttributes().getNamedItem("input").getNodeValue(), Boolean.parseBoolean(entry.getAttributes().getNamedItem("regex").getNodeValue()), Double.parseDouble(entry.getAttributes().getNamedItem("output").getNodeValue()))); }
			configure(Double.parseDouble(config.getAttributes().getNamedItem("defaultValue").getNodeValue()), restored);
		}
		return true;
	}

	public record Mapping(String input, boolean regex, double output) {
		boolean matches(String value) { return regex ? Pattern.matches(input, value == null ? "" : value) : input.equals(value); }
	}
}
