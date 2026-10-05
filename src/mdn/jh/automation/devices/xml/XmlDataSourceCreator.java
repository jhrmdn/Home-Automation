package mdn.jh.automation.devices.xml;

import java.awt.BorderLayout;
import java.awt.LayoutManager;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import org.w3c.dom.Document;

import mdn.jh.automation.devices.xml.datasource.Document2JTree;
import mdn.jh.automation.devices.xml.datasource.XMLDataSource;
import mdn.jh.automation.gui.DataSourceCreator;
import mdn.jh.automation.io.source.DataSource;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import mdn.jh.automation.device.DataConverterSelector;
import javax.swing.BoxLayout;

public class XmlDataSourceCreator extends DataSourceCreator {

	private static final long serialVersionUID = -3292543189600352374L;
	private JPanel panel;
	private JTextField textFieldSelectedXPath;
	private JScrollPane scrollPane;
	Document2JTree tree = null;
	private JPanel panel_1;
	private JPanel panel_2;
	private DataConverterSelector dataConverterSelector;
	private JLabel lblNewLabel_1;
	private JPanel panel_3;
	private JLabel lblNewLabel_2;

	public XmlDataSourceCreator() {
		// TODO Auto-generated constructor stub
		initialize();
	}

	private void initialize() {
		setLayout(new BorderLayout(0, 0));
		add(getPanel(), BorderLayout.SOUTH);
		add(getScrollPane(), BorderLayout.CENTER);
		add(getPanel_1(), BorderLayout.EAST);
	}

	@Override
	public DataSource getDataSource() throws Exception {
		String xPath = getTextFieldSelectedXPath().getText();
		
		if ("".equals(xPath) || xPath == null) {
			throw new Exception("No Xpath selected");
		}

		if (getDataConverterSelector().getSelectedConverter() == null) {
			throw new Exception("Please select Converter");
		}

		XMLDataSource dataSource = new XMLDataSource();
		dataSource.setXpath(tree.getSelectedXPath());
		dataSource.setDataConverter(getDataConverterSelector().getSelectedConverter());

		return dataSource;
	}

	private void leaveSelected() {

		if (tree == null)
			return;
		getTextFieldSelectedXPath().setText(tree.getSelectedXPath());
	}

	public void setDocument(Document document) {
		tree = new Document2JTree();
		tree.update(document);
		getScrollPane().setViewportView(tree);

		tree.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON3)
					leaveSelected();
			}
		});

	}

	public XmlDataSourceCreator(LayoutManager layout) {
		super(layout);
		// TODO Auto-generated constructor stub
	}

	public XmlDataSourceCreator(boolean isDoubleBuffered) {
		super(isDoubleBuffered);
		// TODO Auto-generated constructor stub
	}

	public XmlDataSourceCreator(LayoutManager layout, boolean isDoubleBuffered) {
		super(layout, isDoubleBuffered);
		// TODO Auto-generated constructor stub
	}

	private JPanel getPanel() {
		if (panel == null) {
			panel = new JPanel();
			GridBagLayout gbl_panel = new GridBagLayout();
			gbl_panel.columnWidths = new int[] { 74, 96, 0 };
			gbl_panel.rowHeights = new int[] { 20, 0 };
			gbl_panel.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
			gbl_panel.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
			panel.setLayout(gbl_panel);
		}
		return panel;
	}

	private JTextField getTextFieldSelectedXPath() {
		if (textFieldSelectedXPath == null) {
			textFieldSelectedXPath = new JTextField();
			textFieldSelectedXPath.setEditable(false);
			textFieldSelectedXPath.setColumns(10);
		}
		return textFieldSelectedXPath;
	}

	private JScrollPane getScrollPane() {
		if (scrollPane == null) {
			scrollPane = new JScrollPane();
		}
		return scrollPane;
	}

	private JPanel getPanel_1() {
		if (panel_1 == null) {
			panel_1 = new JPanel();
			panel_1.setLayout(new BoxLayout(panel_1, BoxLayout.Y_AXIS));
			panel_1.add(getPanel_2());
			panel_1.add(getPanel_3());
		}
		return panel_1;
	}

	private JPanel getPanel_2() {
		if (panel_2 == null) {
			panel_2 = new JPanel();
			GridBagLayout gbl_panel_2 = new GridBagLayout();
			gbl_panel_2.columnWidths = new int[] { 48, 302, 0 };
			gbl_panel_2.rowHeights = new int[] { 20, 0, 0 };
			gbl_panel_2.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
			gbl_panel_2.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
			panel_2.setLayout(gbl_panel_2);
			GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
			gbc_lblNewLabel_1.anchor = GridBagConstraints.WEST;
			gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
			gbc_lblNewLabel_1.gridx = 0;
			gbc_lblNewLabel_1.gridy = 0;
			panel_2.add(getLblNewLabel_1(), gbc_lblNewLabel_1);
			GridBagConstraints gbc_dataConverterSelector = new GridBagConstraints();
			gbc_dataConverterSelector.insets = new Insets(0, 0, 5, 0);
			gbc_dataConverterSelector.anchor = GridBagConstraints.NORTHWEST;
			gbc_dataConverterSelector.gridx = 1;
			gbc_dataConverterSelector.gridy = 0;
			panel_2.add(getDataConverterSelector(), gbc_dataConverterSelector);
			GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
			gbc_lblNewLabel_2.insets = new Insets(0, 0, 0, 5);
			gbc_lblNewLabel_2.gridx = 0;
			gbc_lblNewLabel_2.gridy = 1;
			panel_2.add(getLblNewLabel_2(), gbc_lblNewLabel_2);
			GridBagConstraints gbc_textFieldSelectedXPath = new GridBagConstraints();
			gbc_textFieldSelectedXPath.fill = GridBagConstraints.HORIZONTAL;
			gbc_textFieldSelectedXPath.gridx = 1;
			gbc_textFieldSelectedXPath.gridy = 1;
			panel_2.add(getTextFieldSelectedXPath(), gbc_textFieldSelectedXPath);
		}
		return panel_2;
	}

	private DataConverterSelector getDataConverterSelector() {
		if (dataConverterSelector == null) {
			dataConverterSelector = new DataConverterSelector();
		}
		return dataConverterSelector;
	}

	private JLabel getLblNewLabel_1() {
		if (lblNewLabel_1 == null) {
			lblNewLabel_1 = new JLabel("Converter");
		}
		return lblNewLabel_1;
	}

	private JPanel getPanel_3() {
		if (panel_3 == null) {
			panel_3 = new JPanel();
		}
		return panel_3;
	}

	private JLabel getLblNewLabel_2() {
		if (lblNewLabel_2 == null) {
			lblNewLabel_2 = new JLabel("Selected Xpath");
		}
		return lblNewLabel_2;
	}
}
