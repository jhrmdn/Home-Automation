package mdn.jh.automation.device.modbus.datasink;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class ModbusDataSinkDetail extends JPanel {

	private static final long serialVersionUID = 1292593771062062469L;
	ModbusDataSink modbusDataSink = null;
	private JPanel panel;
	private JLabel lblNewLabel;
	private JTextField textFieldType;
	private JLabel lblNewLabel_1;
	private JTextField textFieldAddress;

	public ModbusDataSinkDetail(ModbusDataSink modbusDataSink) {
		super();
		this.modbusDataSink = modbusDataSink;
		initialize();
		setData();
	}

	private void setData() {
		if (modbusDataSink == null)
			return;
		getTextFieldAddress().setText(Integer.toString(modbusDataSink.getAddress()));
		getTextFieldType().setText(modbusDataSink.getTypeAsString());

	}

	private void initialize() {
		setLayout(new BorderLayout(0, 0));
		add(getPanel(), BorderLayout.NORTH);
	}

	/*
	 * public ModbusDataSinkDetail(LayoutManager layout) { super(layout); // TODO
	 * Auto-generated constructor stub }
	 * 
	 * public ModbusDataSinkDetail(boolean isDoubleBuffered) {
	 * super(isDoubleBuffered); // TODO Auto-generated constructor stub }
	 * 
	 * public ModbusDataSinkDetail(LayoutManager layout, boolean isDoubleBuffered) {
	 * super(layout, isDoubleBuffered); // TODO Auto-generated constructor stub }
	 */

	private JPanel getPanel() {
		if (panel == null) {
			panel = new JPanel();
			GridBagLayout gbl_panel = new GridBagLayout();
			gbl_panel.columnWidths = new int[] { 0, 0, 0 };
			gbl_panel.rowHeights = new int[] { 0, 0, 0 };
			gbl_panel.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
			gbl_panel.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
			panel.setLayout(gbl_panel);
			GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
			gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
			gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
			gbc_lblNewLabel.gridx = 0;
			gbc_lblNewLabel.gridy = 0;
			panel.add(getLblNewLabel(), gbc_lblNewLabel);
			GridBagConstraints gbc_textFieldType = new GridBagConstraints();
			gbc_textFieldType.fill = GridBagConstraints.HORIZONTAL;
			gbc_textFieldType.insets = new Insets(0, 0, 5, 0);
			gbc_textFieldType.gridx = 1;
			gbc_textFieldType.gridy = 0;
			panel.add(getTextFieldType(), gbc_textFieldType);
			GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
			gbc_lblNewLabel_1.anchor = GridBagConstraints.EAST;
			gbc_lblNewLabel_1.insets = new Insets(0, 0, 0, 5);
			gbc_lblNewLabel_1.gridx = 0;
			gbc_lblNewLabel_1.gridy = 1;
			panel.add(getLblNewLabel_1(), gbc_lblNewLabel_1);
			GridBagConstraints gbc_textFieldAddress = new GridBagConstraints();
			gbc_textFieldAddress.fill = GridBagConstraints.HORIZONTAL;
			gbc_textFieldAddress.gridx = 1;
			gbc_textFieldAddress.gridy = 1;
			panel.add(getTextFieldAddress(), gbc_textFieldAddress);
		}
		return panel;
	}

	private JLabel getLblNewLabel() {
		if (lblNewLabel == null) {
			lblNewLabel = new JLabel("Type");
		}
		return lblNewLabel;
	}

	private JTextField getTextFieldType() {
		if (textFieldType == null) {
			textFieldType = new JTextField();
			textFieldType.setEditable(false);
			textFieldType.setColumns(10);
		}
		return textFieldType;
	}

	private JLabel getLblNewLabel_1() {
		if (lblNewLabel_1 == null) {
			lblNewLabel_1 = new JLabel("Address");
		}
		return lblNewLabel_1;
	}

	private JTextField getTextFieldAddress() {
		if (textFieldAddress == null) {
			textFieldAddress = new JTextField();
			textFieldAddress.setEditable(false);
			textFieldAddress.setColumns(10);
		}
		return textFieldAddress;
	}
}
