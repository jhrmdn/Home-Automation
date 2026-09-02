package mdn.jh.automation.devices.xml;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import mdn.jh.automation.device.Device;
import mdn.jh.automation.device.DeviceCreator;

public class XmlDeviceCreator extends DeviceCreator {

	private static final long serialVersionUID = 6589029193986037330L;
	private JPanel panel;
	private JPanel panel_1;
	private JLabel lblNewLabel;
	private JRadioButton rdbtnWeb;
	private JRadioButton rdbtnFile;
	private final ButtonGroup buttonGroup = new ButtonGroup();
	private JLabel lblNewLabel_1;
	private JPanel panel_2;
	private JLabel lblNewLabel_2;
	private JTextField txtURL;
	private JPanel panel_3;
	private JLabel lblNewLabel_3;
	private JRadioButton rdbtnHTTP;
	private JRadioButton rdbtnHTTPS;
	private final ButtonGroup buttonGroup_1 = new ButtonGroup();

	public XmlDeviceCreator() {
		initialize();
		setDeviceName("XML");
	}

	private void initialize() {
		setLayout(new BorderLayout(0, 0));
		add(getPanel());
	}

	@Override
	public Device getCreatedDevice() throws Exception {
		int type = XmlDevice.XML_type_webserver;
		if (rdbtnFile.isSelected())
			type = XmlDevice.XML_type_file;

		String url = getTxtURL().getText();
		if (url == null || "".equals(url)) {
			throw new Exception("Please select URL / File");
		}
		boolean https = rdbtnHTTPS.isSelected();

		XmlDevice device = new XmlDevice(type, url, https);

		
		return device;
	}

	private JPanel getPanel() {
		if (panel == null) {
			panel = new JPanel();
			GridBagLayout gbl_panel = new GridBagLayout();
			gbl_panel.columnWidths = new int[] { 0, 0 };
			gbl_panel.rowHeights = new int[] { 0, 0, 0, 0, 0, 0 };
			gbl_panel.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
			gbl_panel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
			panel.setLayout(gbl_panel);
			GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
			gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 0);
			gbc_lblNewLabel_1.gridx = 0;
			gbc_lblNewLabel_1.gridy = 0;
			panel.add(getLblNewLabel_1(), gbc_lblNewLabel_1);
			GridBagConstraints gbc_panel_1 = new GridBagConstraints();
			gbc_panel_1.insets = new Insets(0, 0, 5, 0);
			gbc_panel_1.fill = GridBagConstraints.BOTH;
			gbc_panel_1.gridx = 0;
			gbc_panel_1.gridy = 1;
			panel.add(getPanel_1(), gbc_panel_1);
			GridBagConstraints gbc_panel_2 = new GridBagConstraints();
			gbc_panel_2.insets = new Insets(0, 0, 5, 0);
			gbc_panel_2.fill = GridBagConstraints.BOTH;
			gbc_panel_2.gridx = 0;
			gbc_panel_2.gridy = 2;
			panel.add(getPanel_2(), gbc_panel_2);
			GridBagConstraints gbc_panel_3 = new GridBagConstraints();
			gbc_panel_3.fill = GridBagConstraints.BOTH;
			gbc_panel_3.gridx = 0;
			gbc_panel_3.gridy = 4;
			panel.add(getPanel_3(), gbc_panel_3);
		}
		return panel;
	}

	private JPanel getPanel_1() {
		if (panel_1 == null) {
			panel_1 = new JPanel();
			panel_1.add(getLblNewLabel());
			panel_1.add(getRdbtnWeb());
			panel_1.add(getRdbtnFile());
		}
		return panel_1;
	}

	private JLabel getLblNewLabel() {
		if (lblNewLabel == null) {
			lblNewLabel = new JLabel("Type");
		}
		return lblNewLabel;
	}

	private JRadioButton getRdbtnWeb() {
		if (rdbtnWeb == null) {
			rdbtnWeb = new JRadioButton("Web");
			rdbtnWeb.setSelected(true);
			buttonGroup.add(rdbtnWeb);
		}
		return rdbtnWeb;
	}

	private JRadioButton getRdbtnFile() {
		if (rdbtnFile == null) {
			rdbtnFile = new JRadioButton("File");
			buttonGroup.add(rdbtnFile);
		}
		return rdbtnFile;
	}

	private JLabel getLblNewLabel_1() {
		if (lblNewLabel_1 == null) {
			lblNewLabel_1 = new JLabel("XML Device Creator");
			lblNewLabel_1.setFont(new Font("Tahoma", Font.PLAIN, 14));
		}
		return lblNewLabel_1;
	}

	private JPanel getPanel_2() {
		if (panel_2 == null) {
			panel_2 = new JPanel();
			GridBagLayout gbl_panel_2 = new GridBagLayout();
			gbl_panel_2.columnWidths = new int[] { 20, 86, 0 };
			gbl_panel_2.rowHeights = new int[] { 20, 0 };
			gbl_panel_2.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
			gbl_panel_2.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
			panel_2.setLayout(gbl_panel_2);
			GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
			gbc_lblNewLabel_2.anchor = GridBagConstraints.WEST;
			gbc_lblNewLabel_2.insets = new Insets(0, 0, 0, 5);
			gbc_lblNewLabel_2.gridx = 0;
			gbc_lblNewLabel_2.gridy = 0;
			panel_2.add(getLblNewLabel_2(), gbc_lblNewLabel_2);
			GridBagConstraints gbc_txtURL = new GridBagConstraints();
			gbc_txtURL.fill = GridBagConstraints.HORIZONTAL;
			gbc_txtURL.anchor = GridBagConstraints.NORTH;
			gbc_txtURL.gridx = 1;
			gbc_txtURL.gridy = 0;
			panel_2.add(getTxtURL(), gbc_txtURL);
		}
		return panel_2;
	}

	private JLabel getLblNewLabel_2() {
		if (lblNewLabel_2 == null) {
			lblNewLabel_2 = new JLabel("URL");
		}
		return lblNewLabel_2;
	}

	private JTextField getTxtURL() {
		if (txtURL == null) {
			txtURL = new JTextField();
			txtURL.setText("http://192.168.17.54/heizung/UBAMonitorFast.xml");
			txtURL.setColumns(10);
		}
		return txtURL;
	}

	private JPanel getPanel_3() {
		if (panel_3 == null) {
			panel_3 = new JPanel();
			panel_3.add(getLblNewLabel_3());
			panel_3.add(getRdbtnHTTP());
			panel_3.add(getRdbtnHTTPS());
		}
		return panel_3;
	}

	private JLabel getLblNewLabel_3() {
		if (lblNewLabel_3 == null) {
			lblNewLabel_3 = new JLabel("Web Type");
		}
		return lblNewLabel_3;
	}

	private JRadioButton getRdbtnHTTP() {
		if (rdbtnHTTP == null) {
			rdbtnHTTP = new JRadioButton("HTTP");
			buttonGroup_1.add(rdbtnHTTP);
			rdbtnHTTP.setSelected(true);
		}
		return rdbtnHTTP;
	}

	private JRadioButton getRdbtnHTTPS() {
		if (rdbtnHTTPS == null) {
			rdbtnHTTPS = new JRadioButton("HTTPS");
			buttonGroup_1.add(rdbtnHTTPS);
		}
		return rdbtnHTTPS;
	}
}
