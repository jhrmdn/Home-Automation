package mdn.jh.automation.io;

import java.awt.Rectangle;
import java.io.Serializable;
import java.util.Iterator;
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
	protected Rectangle bounds = null;
	String statusMessage = null;

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
		rootElement.setAttribute("id", "" + getId());
		if (name != null) rootElement.setAttribute("name", name);

		if (bounds != null) {
			Element visualization = doc.createElement("Visualization");
			visualization.setAttribute("x_position", Double.valueOf(bounds.getX()).toString());
			visualization.setAttribute("y_position", Double.valueOf(bounds.getY()).toString());
			visualization.setAttribute("width", Double.valueOf(bounds.getWidth()).toString());
			visualization.setAttribute("height", Double.valueOf(bounds.getHeight()).toString());
			rootElement.appendChild(visualization);
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
