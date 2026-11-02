package mdn.jh.automation.devices.fritz.datasink;

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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javax.swing.border.LineBorder;
import java.awt.Color;

public class FritzDevice2JTreeAction extends JTree {

	private static final long serialVersionUID = -1957396765821406043L;

	public FritzDevice2JTreeAction() {
		// TODO Auto-generated constructor stub
		initialize();
	}
	private void initialize() {
		setBorder(new LineBorder(new Color(0, 0, 0), 2));
	}

	public FritzDevice2JTreeAction(Object[] value) {
		super(value);
		// TODO Auto-generated constructor stub
	}

	public FritzDevice2JTreeAction(Vector<?> value) {
		super(value);
		// TODO Auto-generated constructor stub
	}

	public FritzDevice2JTreeAction(Hashtable<?, ?> value) {
		super(value);
		// TODO Auto-generated constructor stub
	}

	public FritzDevice2JTreeAction(TreeNode root) {
		super(root);
		// TODO Auto-generated constructor stub
	}

	public FritzDevice2JTreeAction(TreeModel newModel) {
		super(newModel);
		// TODO Auto-generated constructor stub
	}

	public FritzDevice2JTreeAction(TreeNode root, boolean asksAllowsChildren) {
		super(root, asksAllowsChildren);
		// TODO Auto-generated constructor stub
	}

	public void update(Document document) {
		updateTree(document.getFirstChild(), false);
		repaint();
	}

	private void updateTree(Node root, boolean showDetails) {
		// Take a DOM and convert it to a Tree model for the JTree
		FritzDeviceMutableTreeNodeAction top = createTreeNode(root, "Start");
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
	protected FritzDeviceMutableTreeNodeAction createTreeNode(Node root, String deviceName) {
		FritzDeviceMutableTreeNodeAction dmtNode = null;

		// String type = getNodeType(root);
		String uin=null;
		String name = root.getNodeName();
		String newTopName = null;
		String functionbitmask = null;

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

		NamedNodeMap attributes = root.getAttributes();
		if (attributes != null) {

			Node n = attributes.getNamedItem("productname");
			if (n != null) {
				name = name + " - " + n.getNodeValue();
			}
			n = attributes.getNamedItem("identifier");
			if (n != null) {
				name = name + " - Identifier: " + n.getNodeValue();
				uin=n.getNodeValue();
			}

			n = attributes.getNamedItem("functionbitmask");
			if (n != null) {
				functionbitmask = n.getNodeValue();
			}

		}
		String value = root.getNodeValue();

		// if (showDetails) {

		dmtNode = new FritzDeviceMutableTreeNodeAction(name + " = " + value);
		if (functionbitmask != null) {
			dmtNode.setFunctionbitmask(functionbitmask);
			dmtNode.add(new FritzDeviceMutableTreeNodeAction("Functionbitmask --> " + functionbitmask));
			dmtNode.setUin(uin);
		}
		dmtNode.setName(newTopName);

		if (root.hasChildNodes()) {
			NodeList childNodes = root.getChildNodes();
			if (childNodes != null) {
				for (int k = 0; k < childNodes.getLength(); k++) {

					Node nd = childNodes.item(k);
					if (nd != null) {
						
						FritzDeviceMutableTreeNodeAction fritzDeviceMutableTreeNodeAction = createTreeNode(nd,
								newTopName);

						if (fritzDeviceMutableTreeNodeAction.getFunctionbitmask() != null) {
							if (FritzCommandHandler
									.hasActionCommand(fritzDeviceMutableTreeNodeAction.getFunctionbitmask()))
								dmtNode.add(fritzDeviceMutableTreeNodeAction);
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
