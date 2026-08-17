package mdn.jh.automation.device.modbus.datasource;

import java.awt.LayoutManager;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.JTextField;

public class ModbusSourceDetailsPanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8456222280739043204L;
	private ModbusDataSource myDataSource = null;
	private JPanel panel;
	private JLabel lblNewLabel;
	private JLabel lblNewLabel_1;
	private JTextField txtType;
	private JTextField txtAddress;

	public void setModbusDataSource(ModbusDataSource modbusDataSource) {
		this.myDataSource = modbusDataSource;
		if (myDataSource == null)
			return;

		getTxtAddress().setText(Integer.toString(modbusDataSource.getAddress()));
		getTxtType().setText(modbusDataSource.getTypeAsString());

	}

	public ModbusSourceDetailsPanel() {
		// TODO Auto-generated constructor stub
		initialize();
	}

	private void initialize() {
		setLayout(new BorderLayout(0, 0));
		add(getPanel(), BorderLayout.CENTER);
	}

	public ModbusSourceDetailsPanel(LayoutManager layout) {
		super(layout);
		// TODO Auto-generated constructor stub
	}

	public ModbusSourceDetailsPanel(boolean isDoubleBuffered) {
		super(isDoubleBuffered);
		// TODO Auto-generated constructor stub
	}

	public ModbusSourceDetailsPanel(LayoutManager layout, boolean isDoubleBuffered) {
		super(layout, isDoubleBuffered);
		// TODO Auto-generated constructor stub
	}

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
			GridBagConstraints gbc_txtType = new GridBagConstraints();
			gbc_txtType.insets = new Insets(0, 0, 5, 0);
			gbc_txtType.fill = GridBagConstraints.HORIZONTAL;
			gbc_txtType.gridx = 1;
			gbc_txtType.gridy = 0;
			panel.add(getTxtType(), gbc_txtType);
			GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
			gbc_lblNewLabel_1.anchor = GridBagConstraints.EAST;
			gbc_lblNewLabel_1.insets = new Insets(0, 0, 0, 5);
			gbc_lblNewLabel_1.gridx = 0;
			gbc_lblNewLabel_1.gridy = 1;
			panel.add(getLblNewLabel_1(), gbc_lblNewLabel_1);
			GridBagConstraints gbc_txtAddress = new GridBagConstraints();
			gbc_txtAddress.fill = GridBagConstraints.HORIZONTAL;
			gbc_txtAddress.gridx = 1;
			gbc_txtAddress.gridy = 1;
			panel.add(getTxtAddress(), gbc_txtAddress);
		}
		return panel;
	}

	private JLabel getLblNewLabel() {
		if (lblNewLabel == null) {
			lblNewLabel = new JLabel("Type");
		}
		return lblNewLabel;
	}

	private JLabel getLblNewLabel_1() {
		if (lblNewLabel_1 == null) {
			lblNewLabel_1 = new JLabel("Address");
		}
		return lblNewLabel_1;
	}

	private JTextField getTxtType() {
		if (txtType == null) {
			txtType = new JTextField();
			txtType.setEditable(false);
			txtType.setColumns(10);
		}
		return txtType;
	}

	private JTextField getTxtAddress() {
		if (txtAddress == null) {
			txtAddress = new JTextField();
			txtAddress.setEditable(false);
			txtAddress.setColumns(10);
		}
		return txtAddress;
	}
}
