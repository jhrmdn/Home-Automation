package mdn.jh.automation.device.modbus;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class DataCellRenderer extends DefaultTableCellRenderer {

	private static final long serialVersionUID = -7504184270011411876L;

	ModbusDataTable modbusClient = null;

	public DataCellRenderer(ModbusDataTable modbusClient) {
		this.modbusClient = modbusClient;
	}

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int col) {

		String v = null;
		if (value == null) {
			v = new String();
		} else {
			v = value.toString();
		}

		if (!modbusClient.is_modbus_type_16_bit()) {

			JCheckBox c = new JCheckBox();

			if (v.equals("true")) {
				c.setSelected(true);
				c.setOpaque(true);
				c.setBackground(Color.GREEN);
			}
			if (v.equals("false")) {
				c.setOpaque(true);
				c.setBackground(Color.RED);
			}

			return c;

		} else {

			JLabel label = new JLabel(v);
			if (col > 1) {

				int address = modbusClient.startAdress + row * 16 + col - 1;
				if (!modbusClient.isStartWith1()) {
					address--;
				}
				label.setToolTipText("Address: " + address);
			}
			return label;

		}

	}

}
