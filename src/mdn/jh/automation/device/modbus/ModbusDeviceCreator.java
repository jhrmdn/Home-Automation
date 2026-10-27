package mdn.jh.automation.device.modbus;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import mdn.jh.automation.device.Device;
import mdn.jh.automation.device.DeviceCreator;

public class ModbusDeviceCreator extends DeviceCreator {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7542101901163244745L;
	private JPanel panel;
	private JLabel lblNewLabel;
	private JPanel panel_1;
	private JLabel lblNewLabel_1;
	private JTextField tfIP_hostname;
	private JLabel lblNewLabel_2;
	private JTextField tfPort;
	private JLabel lblNewLabel_3;
	private JPanel panel_2;
	private JButton btnNewButton;
	private JLabel label;
	private JLabel label_8;
	boolean check = false;
	private ModbusDevice modbusDevice = null;
	private JTextField tfSlaveID;

	public ModbusDeviceCreator() {
		initialize();
		setDeviceName("Modbus Device");
	}

//	@Override
//	public boolean deviceIsValid() {
//		return check;
//	}

	@Override
	public Device getCreatedDevice() throws Exception {
		if (!check())
			throw new Exception("Please Check the connection data");
		return modbusDevice;
	}

	private boolean check() {
		getLabel_8().setText("check in progress...");
		revalidate();

		int port = 0;
		int slaveID = -1;

		try {
			port = Integer.valueOf(getTfPort().getText());
			slaveID = Integer.valueOf(getTfSlaveID().getText());

		} catch (Exception e) {
			getLabel_8().setText("Port/slaveID must be numbers >=0");
			return false;
		}

		if (slaveID < 0) {
			getLabel_8().setText("Port/slaveID must be numbers >=0");
			return false;
		}

		if (modbusDevice == null) {
			modbusDevice = new ModbusDevice();
		}
		modbusDevice.setConnectionData(getTfIP_hostname().getText(), port, slaveID);

		check = modbusDevice.isConnected();
		if (check) {
			getLabel_8().setText("Connection OK");
			return true;
		} else {
			getLabel_8().setText("Connection Failed. Please check connection data");
			return false;
		}

	}

	private void initialize() {
		add(getPanel_3(), BorderLayout.NORTH);
		add(getPanel_1_1(), BorderLayout.CENTER);
		add(getPanel_2_1(), BorderLayout.SOUTH);
	}

	private JPanel getPanel_3() {
		if (panel == null) {
			panel = new JPanel();
			panel.add(getLblNewLabel_5());
		}
		return panel;
	}

	private JLabel getLblNewLabel_5() {
		if (lblNewLabel == null) {
			lblNewLabel = new JLabel("Create New Modbus Client");
		}
		return lblNewLabel;
	}

	private JPanel getPanel_1_1() {
		if (panel_1 == null) {
			panel_1 = new JPanel();
			GridBagLayout gbl_panel_1 = new GridBagLayout();
			gbl_panel_1.columnWidths = new int[] { 0, 0, 0 };
			gbl_panel_1.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0 };
			gbl_panel_1.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
			gbl_panel_1.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
			panel_1.setLayout(gbl_panel_1);
			GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
			gbc_lblNewLabel_1.anchor = GridBagConstraints.EAST;
			gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
			gbc_lblNewLabel_1.gridx = 0;
			gbc_lblNewLabel_1.gridy = 0;
			panel_1.add(getLblNewLabel_1_1(), gbc_lblNewLabel_1);
			GridBagConstraints gbc_tfIP_hostname = new GridBagConstraints();
			gbc_tfIP_hostname.fill = GridBagConstraints.HORIZONTAL;
			gbc_tfIP_hostname.insets = new Insets(0, 0, 5, 0);
			gbc_tfIP_hostname.gridx = 1;
			gbc_tfIP_hostname.gridy = 0;
			panel_1.add(getTfIP_hostname(), gbc_tfIP_hostname);
			GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
			gbc_lblNewLabel_2.anchor = GridBagConstraints.EAST;
			gbc_lblNewLabel_2.insets = new Insets(0, 0, 5, 5);
			gbc_lblNewLabel_2.gridx = 0;
			gbc_lblNewLabel_2.gridy = 1;
			panel_1.add(getLblNewLabel_2_1(), gbc_lblNewLabel_2);
			GridBagConstraints gbc_tfPort = new GridBagConstraints();
			gbc_tfPort.fill = GridBagConstraints.HORIZONTAL;
			gbc_tfPort.insets = new Insets(0, 0, 5, 0);
			gbc_tfPort.gridx = 1;
			gbc_tfPort.gridy = 1;
			panel_1.add(getTfPort(), gbc_tfPort);
			GridBagConstraints gbc_lblNewLabel_3 = new GridBagConstraints();
			gbc_lblNewLabel_3.anchor = GridBagConstraints.EAST;
			gbc_lblNewLabel_3.insets = new Insets(0, 0, 5, 5);
			gbc_lblNewLabel_3.gridx = 0;
			gbc_lblNewLabel_3.gridy = 2;
			panel_1.add(getLblNewLabel_3_1(), gbc_lblNewLabel_3);
			GridBagConstraints gbc_tfSlaveID = new GridBagConstraints();
			gbc_tfSlaveID.insets = new Insets(0, 0, 5, 0);
			gbc_tfSlaveID.fill = GridBagConstraints.HORIZONTAL;
			gbc_tfSlaveID.gridx = 1;
			gbc_tfSlaveID.gridy = 2;
			panel_1.add(getTfSlaveID(), gbc_tfSlaveID);
			GridBagConstraints gbc_label = new GridBagConstraints();
			gbc_label.insets = new Insets(0, 0, 0, 5);
			gbc_label.anchor = GridBagConstraints.EAST;
			gbc_label.gridx = 0;
			gbc_label.gridy = 6;
			panel_1.add(getLabel_7(), gbc_label);
		}
		return panel_1;
	}

	private JLabel getLblNewLabel_1_1() {
		if (lblNewLabel_1 == null) {
			lblNewLabel_1 = new JLabel("IP / Hostname");
		}
		return lblNewLabel_1;
	}

	private JTextField getTfIP_hostname() {
		if (tfIP_hostname == null) {
			tfIP_hostname = new JTextField();
			tfIP_hostname.setColumns(10);
		}
		return tfIP_hostname;
	}

	private JLabel getLblNewLabel_2_1() {
		if (lblNewLabel_2 == null) {
			lblNewLabel_2 = new JLabel("Port");
		}
		return lblNewLabel_2;
	}

	private JTextField getTfPort() {
		if (tfPort == null) {
			tfPort = new JTextField();
			tfPort.setColumns(10);
		}
		return tfPort;
	}

	private JLabel getLblNewLabel_3_1() {
		if (lblNewLabel_3 == null) {
			lblNewLabel_3 = new JLabel("Device ID");
		}
		return lblNewLabel_3;
	}

	private JPanel getPanel_2_1() {
		if (panel_2 == null) {
			panel_2 = new JPanel();
			panel_2.add(getBtnNewButton_1());
			panel_2.add(getLabel_8());
		}
		return panel_2;
	}

	private JButton getBtnNewButton_1() {
		if (btnNewButton == null) {
			btnNewButton = new JButton("Check");
			btnNewButton.setVisible(false);
			btnNewButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					check();
				}
			});
		}
		return btnNewButton;
	}

	private JLabel getLabel_7() {
		if (label == null) {
			label = new JLabel((String) null);
		}
		return label;
	}

	private JLabel getLabel_8() {
		if (label_8 == null) {
			label_8 = new JLabel("-");
			label_8.setVisible(false);
		}
		return label_8;
	}

	private JTextField getTfSlaveID() {
		if (tfSlaveID == null) {
			tfSlaveID = new JTextField();
			tfSlaveID.setColumns(10);
		}
		return tfSlaveID;
	}

}
