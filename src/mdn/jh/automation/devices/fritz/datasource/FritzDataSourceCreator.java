package mdn.jh.automation.devices.fritz.datasource;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import mdn.jh.automation.devices.fritz.FritzSmartHomeDevices2JTree;
import mdn.jh.automation.devices.fritz.FritzDeviceMutableTreeNode;
import mdn.jh.automation.gui.DataSourceCreator;
import mdn.jh.automation.gui.ParameterEditor;
import mdn.jh.automation.io.converter.ConvertValueToBoolean;
import mdn.jh.automation.io.converter.ConvertValueToNumber;
import mdn.jh.automation.io.converter.Converter;
import mdn.jh.automation.io.source.DataSource;
import mdn.jh.automation.io.source.WrongDataTypeException;

public class FritzDataSourceCreator extends DataSourceCreator {

	private static final long serialVersionUID = 106290544823462481L;

	FritzDataSource newDataSource = null;
	private JPanel panelSelection;
	// private final ButtonGroup buttonGroup = new ButtonGroup();
	private FritzSmartHomeDevices2JTree tree;
	private Converter dataConverter = null;

	private JPanel panel_1;
	private JPanel panelSourceParameter;
	private JLabel lblNewLabel;
	private JTextField tfParameterIn1;
	private JLabel lblParameter;
	private JLabel lblNewLabel_1_1;
	private JTextField tfParameterIn2;
	private JTextField tfParameterIn3;
	private JLabel lblNewLabel_1;

	private JPanel panelSelectConverter;
	private JLabel lblNewLabel_3;
	private JComboBox<Converter> comboBoxSelectConverter;
	private ParameterEditor parameterEditor;
	// private Device selectedDevice = null;
	private FritzDataSourceInputParameter dataSourceInputParameter = null;

	public FritzDataSourceCreator() {
		initDataConverter();
		initialize();
	}

	public FritzDataSourceCreator(LayoutManager layout) {
		super(layout);
		// TODO Auto-generated constructor stub
	}

	public FritzDataSourceCreator(boolean isDoubleBuffered) {
		super(isDoubleBuffered);
		// TODO Auto-generated constructor stub
	}

	public FritzDataSourceCreator(LayoutManager layout, boolean isDoubleBuffered) {
		super(layout, isDoubleBuffered);
		// TODO Auto-generated constructor stub
	}

	public void setFritzSource(FritzDataSourceHandler dataSourceHandler) {
		getScrollPane().setViewportView(getFritzSourceTree());
		getFritzSourceTree().update(dataSourceHandler);
	}

//	public DataSourceStringInput getSelectedDataSource() {
//		return newDataSource;
//	}

	/**
	 * Exception if not all data are selected
	 */
	@Override
	public DataSource getDataSource() throws Exception {
		if (dataSourceInputParameter == null) {
			throw new Exception("Please select an input from the tree on the left");
		}

		if (dataConverter == null) {
			throw new Exception("Please select a data converter");
		}

		FritzDataSource newDataSource = new FritzDataSource();

		try {
			newDataSource.setDataConverter(dataConverter);
			newDataSource.setDataSourceInputParameter(dataSourceInputParameter);
		} catch (WrongDataTypeException e) {
			throw new Exception("Datatype of Source is fixed (" + newDataSource.getDataTypeOutput()
					+ ") - DataConverter has: " + dataConverter.getDataTypeOutput());
		}
		return newDataSource;
	}

	private void setNewDataConverter(Converter conv) {

		dataConverter = conv;

		updateDataConverter();

	}

	DataSourceCreator sourceSelector = null;
	private JPanel panel_2;
	private JScrollPane scrollPane;

	private void updateDataConverter() {
		getParameterEditor().setObjectToEdit(dataConverter);
	}

	private void initDataConverter() {
		getComboBoxSelectConverter().addItem(null);
		getComboBoxSelectConverter().addItem(new ConvertValueToBoolean());
		getComboBoxSelectConverter().addItem(new ConvertValueToNumber());

		getComboBoxSelectConverter().addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent event) {
				if (event.getStateChange() == ItemEvent.SELECTED) {
					Converter item = (Converter) event.getItem();
					setNewDataConverter(item);
				}
			}
		});
		// updateDataConverter();
	}

	/*
	 * private void createDataSource() { if (selectedDevice == null) { return; }
	 * 
	 * // getBtnSelectDatasource().setEnabled(false); //
	 * getBtnOk().setEnabled(true); getComboBoxSelectConverter().setEnabled(true);
	 * // getBtnSelectDatasource().setEnabled(false); //
	 * getComboBoxDevices().setEnabled(false); newDataSource = new
	 * FritzDataSource(); // getLblId().setText("ID: " + newDataSource.getId());
	 * newDataSource.setDataSourceInputParameter(dataSourceInputParameter);
	 * 
	 * }
	 */
	/*
	 * private void storeAndClose() {
	 * 
	 * if (selectedDevice == null) { JOptionPane.showMessageDialog(this,
	 * "Device not selected", "Error", JOptionPane.OK_OPTION); return; }
	 * 
	 * if (newDataSource == null) { JOptionPane.showMessageDialog(this,
	 * "Error when creating. newDataSource is null", "Warning",
	 * JOptionPane.OK_OPTION); return; }
	 * 
	 * if (dataConverter == null) { JOptionPane.showMessageDialog(this,
	 * "Please select a Data Converter first.", "Warning", JOptionPane.OK_OPTION);
	 * return; }
	 * 
	 * 
	 * if (selectedDevice.getType() == Device.TYPE_FRITZ_BOX) { if
	 * (dataSourceInputParameter.getParameter1() == null) {
	 * JOptionPane.showMessageDialog(this,
	 * "Please select the Element from the tree as Input Parameter.", "Warning",
	 * JOptionPane.OK_OPTION); return; }
	 * 
	 * }
	 * 
	 * 
	 * selectedDevice.getDataSourceHandler().addDataSource(newDataSource); //
	 * Main.getMySmartHomeHandler().registerDataSource(newDataSource);
	 * 
	 * this.setVisible(false); }
	 */
	private void updateInputParameter() {
		if (newDataSource != null) {
			newDataSource.setDataSourceInputParameter(dataSourceInputParameter);
		}

		if (dataSourceInputParameter == null) {
			getTfParameterIn1().setText(null);
			getTfParameterIn2().setText(null);
			getTfParameterIn3().setText(null);
			return;
		}

		getTfParameterIn1().setText(dataSourceInputParameter.getParameter1());
		getTfParameterIn2().setText(dataSourceInputParameter.getParameter2());
		getTfParameterIn3().setText(dataSourceInputParameter.getParameter3());
	}

	private void initialize() {
		setSize(new Dimension(1316, 862));
		setPreferredSize(new Dimension(800, 600));
		setLayout(new BorderLayout(0, 0));
		add(getPanelSelection(), BorderLayout.CENTER);
		add(getPanel_1(), BorderLayout.EAST);
	}

	private FritzSmartHomeDevices2JTree getFritzSourceTree() {
		if (tree == null) {
			tree = new FritzSmartHomeDevices2JTree();
			tree.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					setFritzParameter(e.getSource());
				}
			});
		}
		return tree;
	}

	/**
	 * Parameter
	 * 1: Xpath to data value
	 * 2: Name
	 * 3: Xpath check present for isValid function
	 * @param mutableTreeNode
	 */
	private void setFritzParameter(Object mutableTreeNode) {
		FritzSmartHomeDevices2JTree mt = (FritzSmartHomeDevices2JTree) mutableTreeNode;
		FritzDeviceMutableTreeNode lastTreeElement = (FritzDeviceMutableTreeNode) mt.getLastSelectedPathComponent();
		if (lastTreeElement == null)
			return;
		
		if ("".equals(lastTreeElement.getMyXPath())) {
		//	return;
		}
			
		dataSourceInputParameter = new FritzDataSourceInputParameter();
		dataSourceInputParameter.setParameter1(lastTreeElement.getMyXPath());
		dataSourceInputParameter.setParameter2(lastTreeElement.getName());
		dataSourceInputParameter.setParameter3("devicelist/device[@identifier='" + lastTreeElement.getUin() + "']/present[1]");
	
		updateInputParameter();
		updateDone();
		// updateInputParameter();
	}

	private JPanel getPanelSelection() {
		if (panelSelection == null) {
			panelSelection = new JPanel();
			panelSelection.setPreferredSize(new Dimension(350, 10));
			panelSelection.setLayout(new BorderLayout(0, 0));
			panelSelection.add(getScrollPane(), BorderLayout.CENTER);
		}
		return panelSelection;
	}

	private JPanel getPanel_1() {
		if (panel_1 == null) {
			panel_1 = new JPanel();
			panel_1.setLayout(new BorderLayout(0, 0));
			panel_1.add(getPanel_2(), BorderLayout.EAST);

		}
		return panel_1;
	}

	private JPanel getPanelSourceParameter() {
		if (panelSourceParameter == null) {
			panelSourceParameter = new JPanel();
			panelSourceParameter.setAlignmentY(Component.TOP_ALIGNMENT);
			panelSourceParameter.setPreferredSize(new Dimension(10, 150));
			GridBagLayout gbl_panelSourceParameter = new GridBagLayout();
			gbl_panelSourceParameter.columnWidths = new int[] { 0, 0, 0 };
			gbl_panelSourceParameter.rowHeights = new int[] { 0, 0, 0, 0, 0 };
			gbl_panelSourceParameter.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
			gbl_panelSourceParameter.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
			panelSourceParameter.setLayout(gbl_panelSourceParameter);
			GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
			gbc_lblNewLabel_1.gridwidth = 2;
			gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
			gbc_lblNewLabel_1.gridx = 0;
			gbc_lblNewLabel_1.gridy = 0;
			panelSourceParameter.add(getLblNewLabel_1(), gbc_lblNewLabel_1);
			GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
			gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
			gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
			gbc_lblNewLabel.gridx = 0;
			gbc_lblNewLabel.gridy = 1;
			panelSourceParameter.add(getLblNewLabel(), gbc_lblNewLabel);
			GridBagConstraints gbc_tfParameterIn1 = new GridBagConstraints();
			gbc_tfParameterIn1.insets = new Insets(0, 0, 5, 0);
			gbc_tfParameterIn1.fill = GridBagConstraints.HORIZONTAL;
			gbc_tfParameterIn1.gridx = 1;
			gbc_tfParameterIn1.gridy = 1;
			panelSourceParameter.add(getTfParameterIn1(), gbc_tfParameterIn1);
			GridBagConstraints gbc_lblParameter = new GridBagConstraints();
			gbc_lblParameter.anchor = GridBagConstraints.EAST;
			gbc_lblParameter.insets = new Insets(0, 0, 5, 5);
			gbc_lblParameter.gridx = 0;
			gbc_lblParameter.gridy = 2;
			panelSourceParameter.add(getLblParameter(), gbc_lblParameter);
			GridBagConstraints gbc_tfParameterIn2 = new GridBagConstraints();
			gbc_tfParameterIn2.insets = new Insets(0, 0, 5, 0);
			gbc_tfParameterIn2.fill = GridBagConstraints.HORIZONTAL;
			gbc_tfParameterIn2.gridx = 1;
			gbc_tfParameterIn2.gridy = 2;
			panelSourceParameter.add(getTfParameterIn2(), gbc_tfParameterIn2);
			GridBagConstraints gbc_lblNewLabel_1_1 = new GridBagConstraints();
			gbc_lblNewLabel_1_1.anchor = GridBagConstraints.EAST;
			gbc_lblNewLabel_1_1.insets = new Insets(0, 0, 0, 5);
			gbc_lblNewLabel_1_1.gridx = 0;
			gbc_lblNewLabel_1_1.gridy = 3;
			panelSourceParameter.add(getLblNewLabel_1_1(), gbc_lblNewLabel_1_1);
			GridBagConstraints gbc_tfParameterIn3 = new GridBagConstraints();
			gbc_tfParameterIn3.fill = GridBagConstraints.HORIZONTAL;
			gbc_tfParameterIn3.gridx = 1;
			gbc_tfParameterIn3.gridy = 3;
			panelSourceParameter.add(getTfParameterIn3(), gbc_tfParameterIn3);
		}
		return panelSourceParameter;
	}

	private JLabel getLblNewLabel() {
		if (lblNewLabel == null) {
			lblNewLabel = new JLabel("Parameter 1");
		}
		return lblNewLabel;
	}

	private JTextField getTfParameterIn1() {
		if (tfParameterIn1 == null) {
			tfParameterIn1 = new JTextField();
			tfParameterIn1.setEditable(false);
			tfParameterIn1.setColumns(10);
		}
		return tfParameterIn1;
	}

	private JLabel getLblParameter() {
		if (lblParameter == null) {
			lblParameter = new JLabel("Parameter 2");
		}
		return lblParameter;
	}

	private JLabel getLblNewLabel_1_1() {
		if (lblNewLabel_1_1 == null) {
			lblNewLabel_1_1 = new JLabel("Parameter 3");
		}
		return lblNewLabel_1_1;
	}

	private JTextField getTfParameterIn2() {
		if (tfParameterIn2 == null) {
			tfParameterIn2 = new JTextField();
			tfParameterIn2.setEditable(false);
			tfParameterIn2.setColumns(10);
		}
		return tfParameterIn2;
	}

	private JTextField getTfParameterIn3() {
		if (tfParameterIn3 == null) {
			tfParameterIn3 = new JTextField();
			tfParameterIn3.setEditable(false);
			tfParameterIn3.setColumns(10);
		}
		return tfParameterIn3;
	}

	private JLabel getLblNewLabel_1() {
		if (lblNewLabel_1 == null) {
			lblNewLabel_1 = new JLabel("Input Parameter");
		}
		return lblNewLabel_1;
	}

	private JPanel getPanelSelectConverter() {
		if (panelSelectConverter == null) {
			panelSelectConverter = new JPanel();
			GridBagLayout gbl_panelSelectConverter = new GridBagLayout();
			gbl_panelSelectConverter.columnWidths = new int[] { 160, 81, 0 };
			gbl_panelSelectConverter.rowHeights = new int[] { 22, 0 };
			gbl_panelSelectConverter.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
			gbl_panelSelectConverter.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
			panelSelectConverter.setLayout(gbl_panelSelectConverter);
			GridBagConstraints gbc_lblNewLabel_3 = new GridBagConstraints();
			gbc_lblNewLabel_3.anchor = GridBagConstraints.WEST;
			gbc_lblNewLabel_3.insets = new Insets(0, 0, 0, 5);
			gbc_lblNewLabel_3.gridx = 0;
			gbc_lblNewLabel_3.gridy = 0;
			panelSelectConverter.add(getLblNewLabel_3(), gbc_lblNewLabel_3);
			GridBagConstraints gbc_comboBoxSelectConverter = new GridBagConstraints();
			gbc_comboBoxSelectConverter.fill = GridBagConstraints.HORIZONTAL;
			gbc_comboBoxSelectConverter.anchor = GridBagConstraints.NORTH;
			gbc_comboBoxSelectConverter.gridx = 1;
			gbc_comboBoxSelectConverter.gridy = 0;
			panelSelectConverter.add(getComboBoxSelectConverter(), gbc_comboBoxSelectConverter);
		}
		return panelSelectConverter;
	}

	private JLabel getLblNewLabel_3() {
		if (lblNewLabel_3 == null) {
			lblNewLabel_3 = new JLabel("Select Converter");
		}
		return lblNewLabel_3;
	}

	private JComboBox<Converter> getComboBoxSelectConverter() {
		if (comboBoxSelectConverter == null) {
			comboBoxSelectConverter = new JComboBox<Converter>();

		}
		return comboBoxSelectConverter;
	}

	private ParameterEditor getParameterEditor() {
		if (parameterEditor == null) {
			parameterEditor = new ParameterEditor();
		}
		return parameterEditor;
	}

	private JPanel getPanel_2() {
		if (panel_2 == null) {
			panel_2 = new JPanel();
			panel_2.setLayout(new BoxLayout(panel_2, BoxLayout.Y_AXIS));
			panel_2.add(getPanelSourceParameter());
			panel_2.add(getPanelSelectConverter());
			panel_2.add(getParameterEditor());
		}
		return panel_2;
	}

	private JScrollPane getScrollPane() {
		if (scrollPane == null) {
			scrollPane = new JScrollPane();
		}
		return scrollPane;
	}

}
