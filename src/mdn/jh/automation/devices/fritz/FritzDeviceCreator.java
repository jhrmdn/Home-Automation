package mdn.jh.automation.devices.fritz;

import mdn.jh.automation.device.Device;
import mdn.jh.automation.device.DeviceCreator;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import javax.swing.JLabel;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.JTextField;
import javax.swing.JPasswordField;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class FritzDeviceCreator extends DeviceCreator {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7542101901163244745L;
	private JPanel panel;
	private JLabel lblNewLabel;
	private JPanel panel_1;
	private JLabel lblNewLabel_1;
	private JTextField tfFritzIP;
	private JLabel lblNewLabel_2;
	private JTextField tfUsername;
	private JLabel lblNewLabel_3;
	private JPasswordField tfPasswordField;
	private JPanel panel_2;
	private JButton btnNewButton;
	private JLabel label;
	private JLabel label_8;
	boolean check = false;

	public FritzDeviceCreator() {
		initialize();
		setDeviceName("Fritz Box");
	}

//	@Override
//	public boolean deviceIsValid() {
//		return check;
//	}

	@Override
	public Device getCreatedDevice() throws Exception {
		if (!check()) {
			throw new Exception("Please make a check first");
		}
		return box;
	}

	private FritzBoxDevice box = null;

	private boolean check() {
		getLabel_8().setText("check in progress...");
		String pw = new String(getTfPasswordField().getPassword());
		if (box == null) {
			box = new FritzBoxDevice(getTfFritzIP().getText(), getTfUsername().getText(), pw);
		} else {
			box.setConnectionData(getTfFritzIP().getText(), getTfUsername().getText(), pw);
		}

		check = box.checkConnectionOK();
		if (check) {
			getLabel_8().setText("Connection OK");
			return true;
		} else {
			getLabel_8().setText("Connection Failed");
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
			lblNewLabel = new JLabel("Create New Fritz Box");
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
			GridBagConstraints gbc_tfFritzIP = new GridBagConstraints();
			gbc_tfFritzIP.fill = GridBagConstraints.HORIZONTAL;
			gbc_tfFritzIP.insets = new Insets(0, 0, 5, 0);
			gbc_tfFritzIP.gridx = 1;
			gbc_tfFritzIP.gridy = 0;
			panel_1.add(getTfFritzIP(), gbc_tfFritzIP);
			GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
			gbc_lblNewLabel_2.anchor = GridBagConstraints.EAST;
			gbc_lblNewLabel_2.insets = new Insets(0, 0, 5, 5);
			gbc_lblNewLabel_2.gridx = 0;
			gbc_lblNewLabel_2.gridy = 1;
			panel_1.add(getLblNewLabel_2_1(), gbc_lblNewLabel_2);
			GridBagConstraints gbc_tfUsername = new GridBagConstraints();
			gbc_tfUsername.fill = GridBagConstraints.HORIZONTAL;
			gbc_tfUsername.insets = new Insets(0, 0, 5, 0);
			gbc_tfUsername.gridx = 1;
			gbc_tfUsername.gridy = 1;
			panel_1.add(getTfUsername(), gbc_tfUsername);
			GridBagConstraints gbc_lblNewLabel_3 = new GridBagConstraints();
			gbc_lblNewLabel_3.anchor = GridBagConstraints.EAST;
			gbc_lblNewLabel_3.insets = new Insets(0, 0, 5, 5);
			gbc_lblNewLabel_3.gridx = 0;
			gbc_lblNewLabel_3.gridy = 2;
			panel_1.add(getLblNewLabel_3_1(), gbc_lblNewLabel_3);
			GridBagConstraints gbc_tfPasswordField = new GridBagConstraints();
			gbc_tfPasswordField.fill = GridBagConstraints.HORIZONTAL;
			gbc_tfPasswordField.insets = new Insets(0, 0, 5, 0);
			gbc_tfPasswordField.gridx = 1;
			gbc_tfPasswordField.gridy = 2;
			panel_1.add(getTfPasswordField(), gbc_tfPasswordField);
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

	private JTextField getTfFritzIP() {
		if (tfFritzIP == null) {
			tfFritzIP = new JTextField();
			tfFritzIP.setColumns(10);
		}
		return tfFritzIP;
	}

	private JLabel getLblNewLabel_2_1() {
		if (lblNewLabel_2 == null) {
			lblNewLabel_2 = new JLabel("Username");
		}
		return lblNewLabel_2;
	}

	private JTextField getTfUsername() {
		if (tfUsername == null) {
			tfUsername = new JTextField();
			tfUsername.setColumns(10);
		}
		return tfUsername;
	}

	private JLabel getLblNewLabel_3_1() {
		if (lblNewLabel_3 == null) {
			lblNewLabel_3 = new JLabel("Password");
		}
		return lblNewLabel_3;
	}

	private JPasswordField getTfPasswordField() {
		if (tfPasswordField == null) {
			tfPasswordField = new JPasswordField();
		}
		return tfPasswordField;
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

}
