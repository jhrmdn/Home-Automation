package mdn.jh.automation.devices.fritz.datasink;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class FritzActionDetailsPanel extends JPanel {

	private static final long serialVersionUID = 8770167982783566455L;
	private JPanel panel;
	private JLabel lblNewLabel;
	private JPanel panel_1;
	private JLabel lblNewLabel_1;
	private JTextField textFieldAIN;
	FritzDataSink fritzDataSink;

	public FritzActionDetailsPanel(FritzDataSink fritzDataSink) {

		this.fritzDataSink = fritzDataSink;

		initialize();
		if (fritzDataSink == null)
			return;

		setData();
	}

	private void setData() {
		getTextFieldAIN().setText(fritzDataSink.getIdentifierOriginal());

	}

	private void initialize() {
		setLayout(new BorderLayout(0, 0));
		add(getPanel(), BorderLayout.NORTH);
		add(getPanel_1(), BorderLayout.CENTER);
	}

	private JPanel getPanel() {
		if (panel == null) {
			panel = new JPanel();
			panel.add(getLblNewLabel());
		}
		return panel;
	}

	private JLabel getLblNewLabel() {
		if (lblNewLabel == null) {
			lblNewLabel = new JLabel("Fritz Action");
		}
		return lblNewLabel;
	}

	private JPanel getPanel_1() {
		if (panel_1 == null) {
			panel_1 = new JPanel();
			GridBagLayout gbl_panel_1 = new GridBagLayout();
			gbl_panel_1.columnWidths = new int[] { 0, 0, 0 };
			gbl_panel_1.rowHeights = new int[] { 0, 0 };
			gbl_panel_1.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
			gbl_panel_1.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
			panel_1.setLayout(gbl_panel_1);
			GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
			gbc_lblNewLabel_1.insets = new Insets(0, 0, 0, 5);
			gbc_lblNewLabel_1.anchor = GridBagConstraints.EAST;
			gbc_lblNewLabel_1.gridx = 0;
			gbc_lblNewLabel_1.gridy = 0;
			panel_1.add(getLblNewLabel_1(), gbc_lblNewLabel_1);
			GridBagConstraints gbc_textFieldAIN = new GridBagConstraints();
			gbc_textFieldAIN.fill = GridBagConstraints.HORIZONTAL;
			gbc_textFieldAIN.gridx = 1;
			gbc_textFieldAIN.gridy = 0;
			panel_1.add(getTextFieldAIN(), gbc_textFieldAIN);
		}
		return panel_1;
	}

	private JLabel getLblNewLabel_1() {
		if (lblNewLabel_1 == null) {
			lblNewLabel_1 = new JLabel("AIN");
		}
		return lblNewLabel_1;
	}

	private JTextField getTextFieldAIN() {
		if (textFieldAIN == null) {
			textFieldAIN = new JTextField();
			textFieldAIN.setEditable(false);
			textFieldAIN.setColumns(10);
		}
		return textFieldAIN;
	}
}
