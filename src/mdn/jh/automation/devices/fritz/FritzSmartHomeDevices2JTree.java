package mdn.jh.automation.devices.fritz;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Hashtable;
import java.util.Stack;
import java.util.Vector;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreeSelectionModel;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import mdn.jh.automation.devices.fritz.datasink.FritzCommandHandler;
import mdn.jh.automation.devices.fritz.datasource.FritzDataSourceHandler;

public class FritzSmartHomeDevices2JTree extends JTree {

	private static final long serialVersionUID = -1957396765821406043L;

	public FritzSmartHomeDevices2JTree() {
		// TODO Auto-generated constructor stub
		initialize();
	}

	private void mouseClick() {

		FritzDeviceMutableTreeNode node = (FritzDeviceMutableTreeNode) getLastSelectedPathComponent();
		if (node.getFunctionbitmask() != null) {
			int f = Integer.valueOf(node.getFunctionbitmask());

			boolean[] b = FritzCommandHandler.getFunctions(f);
			for (int i = 0; i < b.length; i++) {
				// System.out.println(""+ b[i]);
			}
		}
	}

	private void initialize() {
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON3)
					mouseClick();
			}
		});
	}

	public FritzSmartHomeDevices2JTree(Object[] value) {
		super(value);
		// TODO Auto-generated constructor stub
	}

	public FritzSmartHomeDevices2JTree(Vector<?> value) {
		super(value);
		// TODO Auto-generated constructor stub
	}

	public FritzSmartHomeDevices2JTree(Hashtable<?, ?> value) {
		super(value);
		// TODO Auto-generated constructor stub
	}

	public FritzSmartHomeDevices2JTree(TreeNode root) {
		super(root);
		// TODO Auto-generated constructor stub
	}

	public FritzSmartHomeDevices2JTree(TreeModel newModel) {
		super(newModel);
		// TODO Auto-generated constructor stub
	}

	public FritzSmartHomeDevices2JTree(TreeNode root, boolean asksAllowsChildren) {
		super(root, asksAllowsChildren);
		// TODO Auto-generated constructor stub
	}

	public void update(FritzDataSourceHandler fritzDevices) {
		updateTree(fritzDevices.getFritzDeviceList().getFirstChild());
		repaint();
	}

	private void updateTree(Node root) {
		// Take a DOM and convert it to a Tree model for the JTree
		FritzDeviceMutableTreeNode top = createTreeNode(root, "Start", null);
		DefaultTreeModel dtModel = new DefaultTreeModel(top);
		setModel(dtModel);
		this.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		this.setShowsRootHandles(true);
		this.setEditable(false);
	}

	/**
	 * 
	 * This takes a DOM Node and recurses through the children until each one is
	 * added to a DefaultMutableTreeNode. This can then be used by the JTree as a
	 * tree model. The second parameter can be used to provide more visual detail
	 * for debugging.
	 * 
	 */
	protected FritzDeviceMutableTreeNode createTreeNode(Node root, String deviceName, String topUIN) {
		FritzDeviceMutableTreeNode dmtNode = null;

		String type = getNodeType(root);

		String name = root.getNodeName();
		NamedNodeMap attributes = root.getAttributes();
		String newTopName = null;

		if ("device".equals(name)) {
			NodeList nl = root.getChildNodes();
			for (int i = 0; i < nl.getLength(); i++) {
				Node t = nl.item(i);
				if ("name".equals(t.getNodeName())) {
					name = t.getTextContent() + " - (" + name + ")";
					newTopName = t.getTextContent();
				}
			}
		}

		if (newTopName == null) {
			newTopName = deviceName;
		}

		String uin = topUIN;
		String functionBitMask = null;

		if (attributes != null) {

			Node n = attributes.getNamedItem("productname");
			if (n != null) {
				name = name + " - " + n.getNodeValue();
			}
			n = attributes.getNamedItem("identifier");
			if (n != null) {
				uin = n.getNodeValue();
				name = name + " - Identifier: " + n.getNodeValue();
			}
			n = attributes.getNamedItem("functionbitmask");
			if (n != null) {
				functionBitMask = n.getNodeValue();
			}
		}
		String value = root.getNodeValue();

		// Special case for TEXT_NODE, others are similar but not catered for here.

		dmtNode = new FritzDeviceMutableTreeNode(root.getNodeType() == Node.TEXT_NODE ? value : name);
		dmtNode.setMyXPath(getFullXPath(root));

		dmtNode.setName(newTopName);
		dmtNode.setFunctionbitmask(functionBitMask);
		dmtNode.setUin(uin);

		// Display the attributes if there are any
		NamedNodeMap attribs = root.getAttributes();
		if (attribs != null) {
			for (int i = 0; i < attribs.getLength(); i++) {
				Node attNode = attribs.item(i);
				String attName = attNode.getNodeName().trim();
				String attValue = attNode.getNodeValue().trim();

				if (attValue != null) {
					if (attValue.length() > 0) {
						//dmtNode.add(new DefaultMutableTreeNode("[Attribute] --> " + attName + "=\"" + attValue + "\""));
					}
				}
			}
		}

		// If there are any children and they are non-null then recurse...
		if (root.hasChildNodes()) {
			NodeList childNodes = root.getChildNodes();
			if (childNodes != null) {
				for (int k = 0; k < childNodes.getLength(); k++) {
					Node nd = childNodes.item(k);
					if (nd != null) {
						// A special case could be made for each Node type.
						if (nd.getNodeType() == Node.ELEMENT_NODE) {
							dmtNode.add(createTreeNode(nd, newTopName, uin));
						}

						// This is the default
						String data = nd.getNodeValue();
						if (data != null) {
							data = data.trim();
							if (!data.equals("\n") && !data.equals("\r\n") && data.length() > 0) {
								//dmtNode.add(createTreeNode(nd, newTopName, uin));
								dmtNode.add(createTreeNode(nd, "Don't use - select Node above", uin));
							}
						}
					}
				}
			}
		}
		return dmtNode;
	}

	public static String getFullXPath(Node n) {
		// abort early
		if (null == n)
			return null;

		// declarations
		Node parent = null;
		Stack<Node> hierarchy = new Stack<Node>();
		StringBuffer buffer = new StringBuffer();

		// push element on stack
		hierarchy.push(n);

		switch (n.getNodeType()) {
		case Node.ATTRIBUTE_NODE:
			parent = ((Attr) n).getOwnerElement();
			break;
		case Node.ELEMENT_NODE:
			parent = n.getParentNode();
			break;
		case Node.DOCUMENT_NODE:
			parent = n.getParentNode();
			break;
		default:

		}

		while (null != parent && parent.getNodeType() != Node.DOCUMENT_NODE) {
			// push on stack
			hierarchy.push(parent);

			// get parent of parent
			parent = parent.getParentNode();
		}

		// construct xpath
		Object obj = null;
		while (!hierarchy.isEmpty() && null != (obj = hierarchy.pop())) {
			Node node = (Node) obj;
			boolean handled = false;

			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element e = (Element) node;

				// is this the root element?
				if (buffer.length() == 0) {
					// root element - simply append element name
					buffer.append(node.getNodeName());
				} else {
					// child element - append slash and element name
					buffer.append("/");
					buffer.append(node.getNodeName());

					if (node.hasAttributes()) {
						// see if the element has a name or id attribute
						if (e.hasAttribute("identifier")) {
							// id attribute found - use that
							buffer.append("[@identifier='" + e.getAttribute("identifier") + "']");
							handled = true;
						} else if (e.hasAttribute("name")) {
							// name attribute found - use that
							buffer.append("[@name='" + e.getAttribute("name") + "']");
							handled = true;
						}
					}

					if (!handled) {
						// no known attribute we could use - get sibling index
						int prev_siblings = 1;
						Node prev_sibling = node.getPreviousSibling();
						while (null != prev_sibling) {
							if (prev_sibling.getNodeType() == node.getNodeType()) {
								if (prev_sibling.getNodeName().equalsIgnoreCase(node.getNodeName())) {
									prev_siblings++;
								}
							}
							prev_sibling = prev_sibling.getPreviousSibling();
						}
						buffer.append("[" + prev_siblings + "]");
					}
				}
			} else if (node.getNodeType() == Node.ATTRIBUTE_NODE) {
				buffer.append("/@");
				buffer.append(node.getNodeName());
			}
		}
		// return buffer
		return buffer.toString();
	}

	/**
	 * 
	 * This simple method returns a displayable string given a NodeType
	 * 
	 */
	public String getNodeType(Node node) {
		String type;

		switch (node.getNodeType()) {
		case Node.ELEMENT_NODE: {
			type = "Element";
			break;
		}
		case Node.ATTRIBUTE_NODE: {
			type = "Attribute";
			break;
		}
		case Node.TEXT_NODE: {
			type = "Text";
			break;
		}
		case Node.CDATA_SECTION_NODE: {
			type = "CData section";
			break;
		}
		case Node.ENTITY_REFERENCE_NODE: {
			type = "Entity reference";
			break;
		}
		case Node.ENTITY_NODE: {
			type = "Entity";
			break;
		}
		case Node.PROCESSING_INSTRUCTION_NODE: {
			type = "Processing instruction";
			break;
		}
		case Node.COMMENT_NODE: {
			type = "Comment";
			break;
		}
		case Node.DOCUMENT_NODE: {
			type = "Document";
			break;
		}
		case Node.DOCUMENT_TYPE_NODE: {
			type = "Document type";
			break;
		}
		case Node.DOCUMENT_FRAGMENT_NODE: {
			type = "Document fragment";
			break;
		}
		case Node.NOTATION_NODE: {
			type = "Notation";
			break;
		}
		default: {
			type = "Unknown, contact Sun!";
			break;
		}
		}
		return type;
	}

}
