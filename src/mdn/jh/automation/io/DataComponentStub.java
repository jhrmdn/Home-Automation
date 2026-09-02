package mdn.jh.automation.io;

import java.awt.Rectangle;
import java.io.Serializable;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Vector;
import java.util.logging.Level;

import javax.swing.JPanel;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import mdn.jh.automation.Main;

public abstract class DataComponentStub implements Serializable {
	private static final long serialVersionUID = -4517787552740749774L;
	private static int idCounter = 1;

	public static final int TYPE_NOT_SET = 0;
	public static final int TYPE_DOUBLE_IO = 1;
	public static final int TYPE_BOOLEAN_IO = 2;
	public static final int TYPE_STRING_IO = 3;

	private int id = -1;
//	private String myRawValue = null;
	protected Vector<DataUpdateListenerIF> myUpdateListener = new Vector<DataUpdateListenerIF>();
	protected String name = null;
	protected String helptext = null;
	private String description = "";
	protected Rectangle bounds = null;
	String statusMessage = null;
	private String trueColor = "";
	private String falseColor = "";
	private final List<ValueColorRange> valueColorRanges = new ArrayList<ValueColorRange>();
	private String automationPageId = "automation-page-1";
	private String outputOverrideValue;
	private boolean outputOverridePersistent;
	private final Map<Integer, String> inputOverrideValues = new HashMap<>();
	private final Map<Integer, Boolean> inputOverridePersistent = new HashMap<>();

	/**
	 * 
	 * @return true if the data are valid (no timeout, etc...)
	 */
	public abstract boolean isDataValid();

	public Rectangle getBounds() {
		return bounds;
	}

	public void setBounds(Rectangle bounds) {
		this.bounds = bounds;
	}

	/**
	 * 
	 * @return The actual status string to be displayed
	 */
	public String getStatusMessage() {
		return statusMessage;
	}

	/**
	 * Sets the actual status output
	 * 
	 * @param statusMessage
	 */
	protected void setStatusMessage(String statusMessage) {
		this.statusMessage = statusMessage;
	}

	/**
	 * Optional - can be overwritten with a JPanel containing the specific
	 * information / setting options when the details panel is called.
	 * 
	 * @return a JPanel with the visualisation (also editable parameters). Default
	 *         is null (No display) If null is returned - no specific information
	 *         displayed
	 */
	public JPanel getSpecificDetailsPanel() {
		return null;
	}

	protected Node getDataComponentBaseXML(Document doc) {
		Element rootElement = null;
		rootElement = doc.createElement("DataComponent");
		rootElement.appendChild(doc.createElement("Helptext").appendChild(doc.createTextNode(helptext)));
		Element descriptionElement = doc.createElement("Description");
		descriptionElement.appendChild(doc.createTextNode(description));
		rootElement.appendChild(descriptionElement);
		rootElement.setAttribute("id", "" + getId());
		if (name != null) rootElement.setAttribute("name", name);
		rootElement.setAttribute("automationPageId", automationPageId);
		if (!trueColor.isEmpty() || !falseColor.isEmpty() || !valueColorRanges.isEmpty()) {
			Element colors = doc.createElement("OutputColors");
			if (!trueColor.isEmpty()) colors.setAttribute("true", trueColor);
			if (!falseColor.isEmpty()) colors.setAttribute("false", falseColor);
			for (ValueColorRange range : valueColorRanges) {
				Element item = doc.createElement("Range");
				item.setAttribute("minimum", Double.toString(range.minimum()));
				item.setAttribute("maximum", Double.toString(range.maximum()));
				item.setAttribute("color", range.color());
				colors.appendChild(item);
			}
			rootElement.appendChild(colors);
		}

		if (bounds != null) {
			Element visualization = doc.createElement("Visualization");
			visualization.setAttribute("x_position", Double.valueOf(bounds.getX()).toString());
			visualization.setAttribute("y_position", Double.valueOf(bounds.getY()).toString());
			visualization.setAttribute("width", Double.valueOf(bounds.getWidth()).toString());
			visualization.setAttribute("height", Double.valueOf(bounds.getHeight()).toString());
			rootElement.appendChild(visualization);
		}
		if (outputOverrideValue != null && outputOverridePersistent) {
			Element override = doc.createElement("OutputOverride"); override.setAttribute("value", outputOverrideValue); rootElement.appendChild(override);
		}
		for (Map.Entry<Integer,String> entry : inputOverrideValues.entrySet()) if (inputOverridePersistent.getOrDefault(entry.getKey(), false)) {
			Element override = doc.createElement("InputOverride"); override.setAttribute("input", Integer.toString(entry.getKey())); override.setAttribute("value", entry.getValue()); rootElement.appendChild(override);
		}
		return rootElement;
	}

	public boolean initDataComponent(Node node) throws Exception {
		if (node == null)
			return false;
		String name = node.getNodeName();
		if (!"DataComponent".equals(name)) {
			throw new Exception("Creating - Expected Node: DataComponent - Found:" + name);
		}

		Node idn = node.getAttributes().getNamedItem("id");
		if (idn != null) {
			try {
				overrideID(Integer.valueOf(idn.getNodeValue()));

			} catch (Exception e) {
				Main.getLogger().log(Level.WARNING, "Wrong ID in configuration:" + idn.getNodeValue());
			}

		}
		Node nameNode = node.getAttributes().getNamedItem("name");
		if (nameNode != null) setName(nameNode.getNodeValue());
		Node pageNode = node.getAttributes().getNamedItem("automationPageId");
		if (pageNode != null && !pageNode.getNodeValue().isBlank()) automationPageId = pageNode.getNodeValue();

		NodeList nl = node.getChildNodes();
		Node temp = null;
		for (int i = 0; i < nl.getLength(); i++) {
			temp = nl.item(i);
			if ("Visualization".equals(temp.getNodeName())) {
				bounds = new Rectangle();

				bounds.setLocation(
						Double.valueOf(temp.getAttributes().getNamedItem("x_position").getNodeValue()).intValue(),
						Double.valueOf(temp.getAttributes().getNamedItem("y_position").getNodeValue()).intValue());
				bounds.setSize(Double.valueOf(temp.getAttributes().getNamedItem("width").getNodeValue()).intValue(),
						Double.valueOf(temp.getAttributes().getNamedItem("height").getNodeValue()).intValue());
			}
			if ("Description".equals(temp.getNodeName())) description = temp.getTextContent() == null ? "" : temp.getTextContent();
			if ("OutputColors".equals(temp.getNodeName())) {
				Node trueNode = temp.getAttributes().getNamedItem("true"), falseNode = temp.getAttributes().getNamedItem("false");
				trueColor = trueNode == null ? "" : normalizeColor(trueNode.getNodeValue());
				falseColor = falseNode == null ? "" : normalizeColor(falseNode.getNodeValue());
				valueColorRanges.clear();
				NodeList ranges = temp.getChildNodes();
				for (int j = 0; j < ranges.getLength(); j++) if ("Range".equals(ranges.item(j).getNodeName())) {
					Node range = ranges.item(j);
					valueColorRanges.add(new ValueColorRange(
							Double.parseDouble(range.getAttributes().getNamedItem("minimum").getNodeValue()),
							Double.parseDouble(range.getAttributes().getNamedItem("maximum").getNodeValue()),
							range.getAttributes().getNamedItem("color").getNodeValue()));
				}
			}
			if ("OutputOverride".equals(temp.getNodeName())) { Node value=temp.getAttributes().getNamedItem("value"); if(value!=null){outputOverrideValue=value.getNodeValue();outputOverridePersistent=true;} }
			if ("InputOverride".equals(temp.getNodeName())) { int input=Integer.parseInt(temp.getAttributes().getNamedItem("input").getNodeValue());String value=temp.getAttributes().getNamedItem("value").getNodeValue();setInputOverride(input,value,true); }

		}
		return true;

	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getHelptext() {
		return helptext;
	}

	public void setHelptext(String helptext) {
		this.helptext = helptext;
	}

	public synchronized String getDescription() { return description; }
	public synchronized void setDescription(String description) {
		if (description != null && description.length() > 10000) throw new IllegalArgumentException("Description must not exceed 10000 characters");
		this.description = description == null ? "" : description;
	}

	public synchronized String getAutomationPageId() { return automationPageId; }
	public synchronized void setAutomationPageId(String pageId) {
		if (pageId == null || pageId.isBlank() || pageId.length() > 80) throw new IllegalArgumentException("Invalid automation page ID");
		automationPageId = pageId;
	}

	public synchronized boolean hasOutputOverride() { return outputOverrideValue != null; }
	public synchronized String getOutputOverrideValue() { return outputOverrideValue; }
	public synchronized boolean isOutputOverridePersistent() { return outputOverridePersistent; }
	public synchronized void setOutputOverride(String value, boolean persistent) { if(value==null)throw new IllegalArgumentException("Override value is required");outputOverrideValue=value;outputOverridePersistent=persistent; }
	public synchronized void clearOutputOverride() { outputOverrideValue=null;outputOverridePersistent=false; }
	public synchronized boolean hasInputOverride(int input) { return inputOverrideValues.containsKey(input); }
	public synchronized String getInputOverrideValue(int input) { return inputOverrideValues.get(input); }
	public synchronized boolean isInputOverridePersistent(int input) { return inputOverridePersistent.getOrDefault(input,false); }
	public synchronized void setInputOverride(int input,String value,boolean persistent){if(input<0||value==null)throw new IllegalArgumentException("Invalid input override");inputOverrideValues.put(input,value);inputOverridePersistent.put(input,persistent);onInputOverrideChanged(input);}
	public synchronized void clearInputOverride(int input){inputOverrideValues.remove(input);inputOverridePersistent.remove(input);onInputOverrideChanged(input);}
	protected synchronized void restoreInputOverrides(){for(Integer input:new ArrayList<Integer>(inputOverrideValues.keySet()))onInputOverrideChanged(input);}
	protected void onInputOverrideChanged(int input) { }

	public synchronized String getTrueColor() { return trueColor; }
	public synchronized String getFalseColor() { return falseColor; }
	public synchronized List<ValueColorRange> getValueColorRanges() { return new ArrayList<ValueColorRange>(valueColorRanges); }

	public synchronized void setBooleanColors(String trueColor, String falseColor) {
		this.trueColor = normalizeColor(trueColor);
		this.falseColor = normalizeColor(falseColor);
	}

	public synchronized void setValueColorRanges(List<ValueColorRange> ranges) {
		valueColorRanges.clear();
		if (ranges != null) valueColorRanges.addAll(ranges);
	}

	private static String normalizeColor(String color) {
		if (color == null || color.isBlank()) return "";
		if (!color.matches("#[0-9a-fA-F]{6}")) throw new IllegalArgumentException("Color must use #RRGGBB format");
		return color.toLowerCase();
	}

	public record ValueColorRange(double minimum, double maximum, String color) implements Serializable {
		private static final long serialVersionUID = 1L;
		public ValueColorRange {
			if (!Double.isFinite(minimum) || !Double.isFinite(maximum) || maximum < minimum)
				throw new IllegalArgumentException("Color range maximum must be greater than or equal to minimum");
			color = normalizeColor(color);
			if (color.isEmpty()) throw new IllegalArgumentException("Color is required");
		}
	}

	/**
	 * Usually for Debugging purposes
	 * 
	 * @return
	 */

	/*
	 * public String getMyRawValue() { return myRawValue; }
	 * 
	 * protected void setMyRawValue(String myValue) { this.myRawValue = myValue;
	 * 
	 * }
	 */
	/**
	 * The type Double/Boolean/String
	 * 
	 * @param dataType
	 */
	public DataComponentStub() {
		this.id = getNewID();
	}

	/**
	 * Compatibility constructor. Restored components also receive a temporary ID;
	 * their stored ID replaces it during XML initialization.
	 * 
	 * @param setNewID retained for source and binary compatibility
	 */
	public DataComponentStub(boolean setNewID) {
		this.id = getNewID();
	}

	/**
	 * Returns a new component ID. Components restored from storage replace this
	 * temporary ID with their stored ID during initialization.
	 * 
	 * @return
	 */
	protected static synchronized int getNewID() {
		idCounter++;
		return idCounter;
	}

	/**
	 * For initialisation from stored file
	 * 
	 * @param id
	 */
	protected synchronized void overrideID(int overrideID) {
		if (overrideID > idCounter) {
			idCounter = overrideID;
		}
		this.id = overrideID;

	}

	public boolean subscribeUpdateListener(DataUpdateListenerIF listener) {
		return myUpdateListener.add(listener);
	}

	public boolean unSubscribeUpdateListener(DataUpdateListenerIF listener) {
		return myUpdateListener.remove(listener);
	}

	/**
	 * Can be called by inherited classes when an update has taken place
	 */
	protected void fireUpdate() {
		Iterator<DataUpdateListenerIF> it = myUpdateListener.iterator();
		while (it.hasNext()) {
			it.next().dataUpdateFired(this);
		}
	}

	public synchronized int getId() {
		// Repair legacy/deserialized instances that predate unconditional allocation.
		if (id < 0) id = getNewID();
		return id;
	}

	public static String getTypeAsString(int dataType) {
		if (dataType == TYPE_DOUBLE_IO)
			return "Number";
		if (dataType == TYPE_BOOLEAN_IO)
			return "Boolean";
		if (dataType == TYPE_STRING_IO)
			return "String";
		return "UNKNOWN";
	}

}
