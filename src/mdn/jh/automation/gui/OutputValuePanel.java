package mdn.jh.automation.gui;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LayoutManager;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import mdn.jh.automation.io.DataComponentStub;
import mdn.jh.automation.io.DataOutputIF;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import javax.swing.BoxLayout;

public class OutputValuePanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2428022963297067463L;
	private JLabel lblNewLabel;
	private JLabel lblNewLabel_1;
	private JLabel lblNewLabel_2;
	private JTextField tfString;
	private JTextField textFieldNumber;
	private JTextField textFieldBoolean;
	DataOutputIF dataOutputIF = null;
	private JLabel lblNewLabel_3;
	private JLabel lblOutputType;
	private JPanel panelOutValues;

	public OutputValuePanel() {
		// TODO Auto-generated constructor stub
		initialize();
	}

	private void initialize() {
		setLayout(new BorderLayout(0, 0));
		add(getPanelOutValues(), BorderLayout.NORTH);
	}

	public void setDataOutput(DataOutputIF dataOutputIF) {
		this.dataOutputIF = dataOutputIF;
		updateValues();
	}

	private void updateValues() {
		if (dataOutputIF == null)
			return;

		int type = dataOutputIF.getDataTypeOutput();
		//highlight the used type
		switch (type) {
		case DataComponentStub.TYPE_DOUBLE_IO:
			getTextFieldNumber().setBackground(Color.green);
			break;
		case DataComponentStub.TYPE_BOOLEAN_IO:
			getTextFieldBoolean().setBackground(Color.green);
			break;
		case DataComponentStub.TYPE_STRING_IO:
			getTfString().setBackground(Color.green);
			break;

		}

		String typeName = DataComponentStub.getTypeAsString(type) + " (" + type + ")";
		getLblOutputType().setText(typeName);
		getTfString().setText(dataOutputIF.getOutputAsString());
		getTextFieldNumber().setText("" + dataOutputIF.getOutputAsNumber());
		getTextFieldBoolean().setText(dataOutputIF.getOutputAsString());
				
	}

	public OutputValuePanel(LayoutManager layout) {
		super(layout);
		// TODO Auto-generated constructor stub
	}

	public OutputValuePanel(boolean isDoubleBuffered) {
		super(isDoubleBuffered);
		// TODO Auto-generated constructor stub
	}

	public OutputValuePanel(LayoutManager layout, boolean isDoubleBuffered) {
		super(layout, isDoubleBuffered);
		// TODO Auto-generated constructor stub
	}

	private JLabel getLblNewLabel() {
		if (lblNewLabel == null) {
			lblNewLabel = new JLabel("String");
		}
		return lblNewLabel;
	}

	private JLabel getLblNewLabel_1() {
		if (lblNewLabel_1 == null) {
			lblNewLabel_1 = new JLabel("Number");
		}
		return lblNewLabel_1;
	}

	private JLabel getLblNewLabel_2() {
		if (lblNewLabel_2 == null) {
			lblNewLabel_2 = new JLabel("Boolean");
		}
		return lblNewLabel_2;
	}

	private JTextField getTfString() {
		if (tfString == null) {
			tfString = new JTextField();
			tfString.setEditable(false);
			tfString.setColumns(10);
		}
		return tfString;
	}

	private JTextField getTextFieldNumber() {
		if (textFieldNumber == null) {
			textFieldNumber = new JTextField();
			textFieldNumber.setEditable(false);
			textFieldNumber.setColumns(10);
		}
		return textFieldNumber;
	}

	private JTextField getTextFieldBoolean() {
		if (textFieldBoolean == null) {
			textFieldBoolean = new JTextField();
			textFieldBoolean.setEditable(false);
			textFieldBoolean.setColumns(10);
		}
		return textFieldBoolean;
	}

	private JLabel getLblNewLabel_3() {
		if (lblNewLabel_3 == null) {
			lblNewLabel_3 = new JLabel("Output Type");
		}
		return lblNewLabel_3;
	}

	private JLabel getLblOutputType() {
		if (lblOutputType == null) {
			lblOutputType = new JLabel("Unknown");
			lblOutputType.setHorizontalAlignment(SwingConstants.LEFT);
		}
		return lblOutputType;
	}
	private JPanel getPanelOutValues() {
		if (panelOutValues == null) {
			panelOutValues = new JPanel();
			GridBagLayout gbl_panelOutValues = new GridBagLayout();
			gbl_panelOutValues.columnWidths = new int[]{188};
			gbl_panelOutValues.rowHeights = new int[]{0};
			gbl_panelOutValues.columnWeights = new double[]{0.0};
			gbl_panelOutValues.rowWeights = new double[]{Double.MIN_VALUE};
			panelOutValues.setLayout(gbl_panelOutValues);
			GridBagConstraints gbc_lblNewLabel_3 = new GridBagConstraints();
			gbc_lblNewLabel_3.anchor = GridBagConstraints.WEST;
			gbc_lblNewLabel_3.insets = new Insets(0, 0, 5, 5);
			gbc_lblNewLabel_3.gridx = 0;
			gbc_lblNewLabel_3.gridy = 0;
			panelOutValues.add(getLblNewLabel_3(), gbc_lblNewLabel_3);
			GridBagConstraints gbc_lblOutputType = new GridBagConstraints();
			gbc_lblOutputType.anchor = GridBagConstraints.WEST;
			gbc_lblOutputType.insets = new Insets(0, 0, 5, 0);
			gbc_lblOutputType.gridx = 1;
			gbc_lblOutputType.gridy = 0;
			panelOutValues.add(getLblOutputType(), gbc_lblOutputType);
			GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
			gbc_lblNewLabel.anchor = GridBagConstraints.WEST;
			gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
			gbc_lblNewLabel.gridx = 0;
			gbc_lblNewLabel.gridy = 1;
			panelOutValues.add(getLblNewLabel(), gbc_lblNewLabel);
			GridBagConstraints gbc_tfString = new GridBagConstraints();
			gbc_tfString.anchor = GridBagConstraints.WEST;
			gbc_tfString.insets = new Insets(0, 0, 5, 0);
			gbc_tfString.gridx = 1;
			gbc_tfString.gridy = 1;
			panelOutValues.add(getTfString(), gbc_tfString);
			GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
			gbc_lblNewLabel_1.anchor = GridBagConstraints.WEST;
			gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
			gbc_lblNewLabel_1.gridx = 0;
			gbc_lblNewLabel_1.gridy = 2;
			panelOutValues.add(getLblNewLabel_1(), gbc_lblNewLabel_1);
			GridBagConstraints gbc_textFieldNumber = new GridBagConstraints();
			gbc_textFieldNumber.anchor = GridBagConstraints.WEST;
			gbc_textFieldNumber.insets = new Insets(0, 0, 5, 0);
			gbc_textFieldNumber.gridx = 1;
			gbc_textFieldNumber.gridy = 2;
			panelOutValues.add(getTextFieldNumber(), gbc_textFieldNumber);
			GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
			gbc_lblNewLabel_2.anchor = GridBagConstraints.WEST;
			gbc_lblNewLabel_2.insets = new Insets(0, 0, 5, 5);
			gbc_lblNewLabel_2.gridx = 0;
			gbc_lblNewLabel_2.gridy = 3;
			panelOutValues.add(getLblNewLabel_2(), gbc_lblNewLabel_2);
			GridBagConstraints gbc_textFieldBoolean = new GridBagConstraints();
			gbc_textFieldBoolean.anchor = GridBagConstraints.WEST;
			gbc_textFieldBoolean.insets = new Insets(0, 0, 5, 0);
			gbc_textFieldBoolean.gridx = 1;
			gbc_textFieldBoolean.gridy = 3;
			panelOutValues.add(getTextFieldBoolean(), gbc_textFieldBoolean);
			
			
		}
		return panelOutValues;
	}
}
