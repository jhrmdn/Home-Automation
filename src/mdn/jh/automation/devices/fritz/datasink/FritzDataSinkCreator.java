package mdn.jh.automation.devices.fritz.datasink;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;

import org.w3c.dom.Document;

import mdn.jh.automation.devices.fritz.FritzBoxDevice;
import mdn.jh.automation.devices.fritz.datasource.FritzDataSourceInputParameter;
import mdn.jh.automation.gui.DataSinkCreator;
import mdn.jh.automation.io.sink.DataSink;

public class FritzDataSinkCreator extends DataSinkCreator {
	private FritzDevice2JTreeAction tree;
	private static final long serialVersionUID = -4171091614948619204L;
	private JScrollPane scrollPane;
	private FritzDataSourceInputParameter dataSourceInputParameter = null;
	private JPanel panel;
	private JRadioButton rdbtnOnOff;
	private JLabel lblNewLabel;
	private FritzBoxDevice fritzBox = null;
	private Component horizontalStrut;
	private Component horizontalStrut_1;
	private JPanel panelSourceParameter;
	private JPanel panel_1;
	public void setFritzSource(Document dom, FritzBoxDevice fritzBox) {
		this.fritzBox = fritzBox;
		getScrollPane().setViewportView(getFritzSourceTree());
		getFritzSourceTree().update(dom);
	}

	@Override
	public DataSink getDataSink() {
		FritzDataSink dataSink = null;
		if (dataSourceInputParameter == null)
			return null;

		if (getRdbtnOnOff().isSelected()) {
			dataSink = new FritzSimpleOnOff();
		}

		if (dataSink == null) {
			return null;
		}

		dataSink.setFritzBox(fritzBox);
		dataSink.setIdentifier(dataSourceInputParameter.getParameter1());
		dataSink.setName(dataSourceInputParameter.getParameter2());

		return dataSink;
	}

	public FritzDataSourceInputParameter getInputParameter() {
		return dataSourceInputParameter;
	}

	private FritzDevice2JTreeAction getFritzSourceTree() {
		if (tree == null) {
			tree = new FritzDevice2JTreeAction();
			tree.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					setFritzParameter(e.getSource());
				}
			});
		}
		return tree;
	}

	private void updateInputParameter() {
	

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
	
	private void setFritzParameter(Object mutableTreeNode) {
		FritzDevice2JTreeAction mt = (FritzDevice2JTreeAction) mutableTreeNode;
		FritzDeviceMutableTreeNodeAction lastTreeElement = (FritzDeviceMutableTreeNodeAction) mt
				.getLastSelectedPathComponent();
		if (lastTreeElement == null)
			return;

		dataSourceInputParameter = new FritzDataSourceInputParameter();
		dataSourceInputParameter.setParameter1(lastTreeElement.getUin());
		dataSourceInputParameter.setParameter2(lastTreeElement.getName());
		dataSourceInputParameter.setParameter3(lastTreeElement.getFunctionbitmask());
		updateInputParameter();
		updateDone();
	}

	public FritzDataSinkCreator() {
		// TODO Auto-generated constructor stub
		initialize();
	}

	private void initialize() {
		setLayout(new BorderLayout(0, 0));
		add(getScrollPane());
		add(getPanel(), BorderLayout.EAST);
		add(getPanelSourceParameter(), BorderLayout.SOUTH);
	}

	public FritzDataSinkCreator(LayoutManager layout) {
		super(layout);
		// TODO Auto-generated constructor stub
	}

	public FritzDataSinkCreator(boolean isDoubleBuffered) {
		super(isDoubleBuffered);
		// TODO Auto-generated constructor stub
	}

	public FritzDataSinkCreator(LayoutManager layout, boolean isDoubleBuffered) {
		super(layout, isDoubleBuffered);
		// TODO Auto-generated constructor stub
	}

	private JScrollPane getScrollPane() {
		if (scrollPane == null) {
			scrollPane = new JScrollPane();
		}
		return scrollPane;
	}
	private JLabel lblNewLabel_1_1;
	private JTextField tfParameterIn1;
	private JTextField tfParameterIn2;
	private JTextField tfParameterIn3;
	private JLabel lblNewLabel_1;
	private JLabel lblParameter2;
	private JLabel lblNewLabel1;
	
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
			GridBagConstraints gbc_lblNewLabel1 = new GridBagConstraints();
			gbc_lblNewLabel1.insets = new Insets(0, 0, 5, 5);
			gbc_lblNewLabel1.anchor = GridBagConstraints.EAST;
			gbc_lblNewLabel1.gridx = 0;
			gbc_lblNewLabel1.gridy = 1;
			
			panelSourceParameter.add(getLblNewLabel1(), gbc_lblNewLabel1);
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
			panelSourceParameter.add(getLblParameter2(), gbc_lblParameter);
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
	
	private JLabel getLblNewLabel1() {
		if (lblNewLabel1 == null) {
			lblNewLabel1 = new JLabel("Parameter 1");
		}
		return lblNewLabel1;
	}
	
	private JLabel getLblParameter2() {
		if (lblParameter2 == null) {
			lblParameter2 = new JLabel("Parameter 2");
		}
		return lblParameter2;
	}
	
	private JTextField getTfParameterIn1() {
		if (tfParameterIn1 == null) {
			tfParameterIn1 = new JTextField();
			tfParameterIn1.setEditable(false);
			tfParameterIn1.setColumns(10);
		}
		return tfParameterIn1;
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
	
	private JPanel getPanel() {
		if (panel == null) {
			panel = new JPanel();
			panel.setBorder(new LineBorder(new Color(0, 0, 0), 2));
			GridBagLayout gbl_panel = new GridBagLayout();
			gbl_panel.columnWidths = new int[] { 0, 0, 65, 0 };
			gbl_panel.rowHeights = new int[] { 14, 23, 0 };
			gbl_panel.columnWeights = new double[] { 0.0, 0.0, 0.0, Double.MIN_VALUE };
			gbl_panel.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
			panel.setLayout(gbl_panel);
			GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
			gbc_lblNewLabel.anchor = GridBagConstraints.WEST;
			gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
			gbc_lblNewLabel.gridx = 1;
			gbc_lblNewLabel.gridy = 0;
			panel.add(getLblNewLabel(), gbc_lblNewLabel);
			GridBagConstraints gbc_horizontalStrut = new GridBagConstraints();
			gbc_horizontalStrut.insets = new Insets(0, 0, 0, 5);
			gbc_horizontalStrut.gridx = 0;
			gbc_horizontalStrut.gridy = 1;
			panel.add(getHorizontalStrut(), gbc_horizontalStrut);
			GridBagConstraints gbc_rdbtnOnOff = new GridBagConstraints();
			gbc_rdbtnOnOff.insets = new Insets(0, 0, 0, 5);
			gbc_rdbtnOnOff.anchor = GridBagConstraints.WEST;
			gbc_rdbtnOnOff.gridx = 1;
			gbc_rdbtnOnOff.gridy = 1;
			panel.add(getRdbtnOnOff(), gbc_rdbtnOnOff);
			GridBagConstraints gbc_horizontalStrut_1 = new GridBagConstraints();
			gbc_horizontalStrut_1.gridx = 2;
			gbc_horizontalStrut_1.gridy = 1;
			panel.add(getHorizontalStrut_1(), gbc_horizontalStrut_1);
		}
		return panel;
	}

	private JRadioButton getRdbtnOnOff() {
		if (rdbtnOnOff == null) {
			rdbtnOnOff = new JRadioButton("On / Off");
			rdbtnOnOff.setMnemonic('1');
		}
		return rdbtnOnOff;
	}

	private JLabel getLblNewLabel() {
		if (lblNewLabel == null) {
			lblNewLabel = new JLabel("Function");
		}
		return lblNewLabel;
	}

	private Component getHorizontalStrut() {
		if (horizontalStrut == null) {
			horizontalStrut = Box.createHorizontalStrut(20);
		}
		return horizontalStrut;
	}

	private Component getHorizontalStrut_1() {
		if (horizontalStrut_1 == null) {
			horizontalStrut_1 = Box.createHorizontalStrut(20);
		}
		return horizontalStrut_1;
	}
	private JPanel getPanel_1() {
		if (panel_1 == null) {
			panel_1 = new JPanel();
		}
		return panel_1;
	}
}
