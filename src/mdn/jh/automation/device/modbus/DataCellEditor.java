package mdn.jh.automation.device.modbus;

import java.awt.Color;

import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.text.Caret;

public class DataCellEditor extends DefaultCellEditor {

	private static final long serialVersionUID = -8682596561186194867L;

	public DataCellEditor(JTextField textField) {
		super(textField);
		textField.setBackground(Color.GREEN);
		Caret ca = textField.getCaret();
		ca.setDot(1);
		
		this.setClickCountToStart(2);
	}

	public DataCellEditor(JCheckBox checkBox) {
		super(checkBox);
		// TODO Auto-generated constructor stub
	}

	public DataCellEditor(JComboBox<?> comboBox) {
		super(comboBox);
		// TODO Auto-generated constructor stub
	}

}
