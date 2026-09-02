package mdn.jh.automation;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.FileNotFoundException;

//Import other Java classes
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
//We are going to use JAXP's classes for DOM I/O
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

//Import the W3C DOM clas ses
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Displays XML in a JTree
 */

public class XML2JTree extends JPanel {

	private static final long serialVersionUID = -5319162133903349406L;
	private JTree jTree;
	private static JFrame frame;

	public static final int FRAME_WIDTH = 800;
	public static final int FRAME_HEIGHT = 600;

	public XML2JTree(Node root, boolean showDetails) {
		// Take a DOM and convert it to a Tree model for the JTree
		DefaultMutableTreeNode top = createTreeNode(root, showDetails);
		DefaultTreeModel dtModel = new DefaultTreeModel(top);

		// Create our JTree
		jTree = new JTree(dtModel);

		// We have no tree listener but this looks more natrual
		jTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		jTree.setShowsRootHandles(true);

		// If this were editable it would result in too much code
		// for this demo.
		jTree.setEditable(false);

		// Create a new JScrollPane but override one of the methods.
		JScrollPane jScroll = new JScrollPane() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			// This keeps the scrollpane a reasonable size
			public Dimension getPreferredSize() {
				return new Dimension(FRAME_WIDTH - 20, FRAME_HEIGHT - 40);
			}
		};

		// Wrap the JTree in a JScroll so that we can scroll it in the JSplitPane.
		jScroll.getViewport().add(jTree);

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add("Center", jScroll);
		add("Center", panel);
	}

	/**
	 * 
	 * This takes a DOM Node and recurses through the children until each one is
	 * added to a DefaultMutableTreeNode. This can then be used by the JTree as a
	 * tree model. The second parameter can be used to provide more visual detail
	 * for debugging.
	 * 
	 */
	protected DefaultMutableTreeNode createTreeNode(Node root, boolean showDetails) {
		DefaultMutableTreeNode dmtNode = null;

		String type = getNodeType(root);
		
		String name = root.getNodeName();
		NamedNodeMap attributes = root.getAttributes();
		if(attributes!=null) {
			Node n =attributes.getNamedItem("productname");
			if(n!=null) {
				name = name + " - " + n.getNodeValue();
			}
			n =attributes.getNamedItem("identifier");
			if(n!=null) {
				name = name + " - Identifier: " + n.getNodeValue();
			}
		}
		String value = root.getNodeValue();

		if (showDetails) {
			dmtNode = new DefaultMutableTreeNode("[" + type + "] --> " + name + " = " + value);
		} else {
			// Special case for TEXT_NODE, others are similar but not catered for here.
			dmtNode = new DefaultMutableTreeNode(root.getNodeType() == Node.TEXT_NODE ? value : name);
		}

		// Display the attributes if there are any
		NamedNodeMap attribs = root.getAttributes();
		if (attribs != null && showDetails) {
			for (int i = 0; i < attribs.getLength(); i++) {
				Node attNode = attribs.item(i);
				String attName = attNode.getNodeName().trim();
				String attValue = attNode.getNodeValue().trim();

				if (attValue != null) {
					if (attValue.length() > 0) {
						dmtNode.add(new DefaultMutableTreeNode("[Attribute] --> " + attName + "=\"" + attValue + "\""));
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
							dmtNode.add(createTreeNode(nd, showDetails));
						}

						// This is the default
						String data = nd.getNodeValue();
						if (data != null) {
							data = data.trim();
							if (!data.equals("\n") && !data.equals("\r\n") && data.length() > 0) {
								dmtNode.add(createTreeNode(nd, showDetails));
							}
						}
					}
				}
			}
		}
		return dmtNode;
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

	/**
	 * 
	 * A common point of exit...
	 * 
	 */

	private static void exit() {
		System.out.println("Graceful exit");
		System.exit(0);
	}

	/**
	 * 
	 * This main is simply to test this class
	 * 
	 */

	public static void main(String[] args) {
		Document doc = null;

		// Just check we have the right parameters first.
		if (args.length < 1) {
			System.out.println("Usage:\tXML2JTree filename.xml");
			return;
		}
		String filename = args[0];

		boolean showDetails = false;
		if (args.length == 2) {
			// If anything's there override the default
			showDetails = Boolean.valueOf(args[1]).booleanValue();
		}

		// Create a frame to "hold" our class
		frame = new JFrame("XML to JTree");

		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Dimension dim = toolkit.getScreenSize();
		int screenHeight = dim.height;
		int screenWidth = dim.width;

		// This should display a WIDTH x HEIGHT sized Frame in the middle of the screen
		frame.setBounds((screenWidth - FRAME_WIDTH) / 2, (screenHeight - FRAME_HEIGHT) / 2, FRAME_WIDTH, FRAME_HEIGHT);

		frame.setBackground(Color.lightGray);
		frame.getContentPane().setLayout(new BorderLayout());

		// Give our frame an icon when it's minimized.
		frame.setIconImage(toolkit.getImage("Wrox.gif"));

		// Add a WindowListener so that we can close the window
		WindowListener wndCloser = new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				exit();
			}
		};
		frame.addWindowListener(wndCloser);

		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setValidating(false); // Not important fro this demo

			DocumentBuilder db = dbf.newDocumentBuilder();
			doc = db.parse(filename);
		} catch (FileNotFoundException fnfEx) {
			// Display a "nice" warning message if the file isn't there.
			JOptionPane.showMessageDialog(frame, filename + " was not found", "Warning", JOptionPane.WARNING_MESSAGE);

			System.out.println();
			exit();
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(frame, ex.getMessage(), "Exception", JOptionPane.WARNING_MESSAGE);

			ex.printStackTrace();
			exit();
		}

		Node root = (Node) doc.getDocumentElement();

		frame.getContentPane().add(new XML2JTree(root, showDetails), BorderLayout.CENTER);

		frame.validate();
		frame.setVisible(true);
	}
}
