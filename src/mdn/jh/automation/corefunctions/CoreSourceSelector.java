package mdn.jh.automation.corefunctions;

import java.awt.BorderLayout;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import mdn.jh.automation.gui.DataSourceCreator;
import mdn.jh.automation.io.source.DataSource;

public class CoreSourceSelector extends DataSourceCreator {

	private static final long serialVersionUID = -369627759421458626L;
	private JPanel panel;
	private JPanel panel_1;
	private JPanel panel_2;
	private JLabel lblNewLabel;
	private JRadioButton rdbtnNewRadioButton;
	private JRadioButton rdbtnNewRadioButton_1;
	private JButton btnConstBool;
	private JLabel lblNewLabel_1;
	private JTextField textField;
	private JButton btnConstNumber;
	private final ButtonGroup buttonGroup = new ButtonGroup();

	public CoreSourceSelector() {

		initialize();
	}
	
	@Override
	public DataSource getDataSource() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	private void initialize() {
		setLayout(new BorderLayout(0, 0));
		add(getPanel(), BorderLayout.CENTER);
	}

	/*
	@Override
	public FritzDataSourceInputParameter getInputParameter() {
		return new FritzDataSourceInputParameter();
	}
*/
	private JPanel getPanel() {
		if (panel == null) {
			panel = new JPanel();
			panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
			panel.add(getPanel_1());
			panel.add(getPanel_2());
		}
		return panel;
	}
	private JPanel getPanel_1() {
		if (panel_1 == null) {
			panel_1 = new JPanel();
			panel_1.add(getLblNewLabel());
			panel_1.add(getRdbtnNewRadioButton());
			panel_1.add(getRdbtnNewRadioButton_1());
			panel_1.add(getBtnConstBool());
		}
		return panel_1;
	}
	private JPanel getPanel_2() {
		if (panel_2 == null) {
			panel_2 = new JPanel();
			panel_2.add(getLblNewLabel_1());
			panel_2.add(getTextField());
			panel_2.add(getBtnConstNumber());
		}
		return panel_2;
	}
	private JLabel getLblNewLabel() {
		if (lblNewLabel == null) {
			lblNewLabel = new JLabel("Const Boolean");
		}
		return lblNewLabel;
	}
	private JRadioButton getRdbtnNewRadioButton() {
		if (rdbtnNewRadioButton == null) {
			rdbtnNewRadioButton = new JRadioButton("true");
			buttonGroup.add(rdbtnNewRadioButton);
			rdbtnNewRadioButton.setSelected(true);
		}
		return rdbtnNewRadioButton;
	}
	private JRadioButton getRdbtnNewRadioButton_1() {
		if (rdbtnNewRadioButton_1 == null) {
			rdbtnNewRadioButton_1 = new JRadioButton("false");
			buttonGroup.add(rdbtnNewRadioButton_1);
		}
		return rdbtnNewRadioButton_1;
	}
	private JButton getBtnConstBool() {
		if (btnConstBool == null) {
			btnConstBool = new JButton("Select");
		}
		return btnConstBool;
	}
	private JLabel getLblNewLabel_1() {
		if (lblNewLabel_1 == null) {
			lblNewLabel_1 = new JLabel("Const Number");
		}
		return lblNewLabel_1;
	}
	private JTextField getTextField() {
		if (textField == null) {
			textField = new JTextField();
			textField.setColumns(10);
		}
		return textField;
	}
	private JButton getBtnConstNumber() {
		if (btnConstNumber == null) {
			btnConstNumber = new JButton("Select");
		}
		return btnConstNumber;
	}

}
