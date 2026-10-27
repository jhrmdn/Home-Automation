package mdn.jh.automation.devices.database;

import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

/** Add/remove combobox rows allow '*' or multiple values for every cron field. */
public class CronEditorPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private final FieldEditor[] fields = { new FieldEditor("Minute", 0, 59), new FieldEditor("Hour", 0, 23),
            new FieldEditor("Day", 1, 31), new FieldEditor("Month", 1, 12), new FieldEditor("Weekday", 0, 7) };
    public CronEditorPanel() { setLayout(new GridLayout(1, 5, 8, 0)); for (FieldEditor field : fields) add(field); }
    public String getExpression() {
        StringBuilder result = new StringBuilder();
        for (FieldEditor field : fields) { if (result.length() > 0) result.append(' '); result.append(field.value()); }
        return new CronSchedule(result.toString()).getExpression();
    }
    public void setExpression(String expression) {
        String[] values = new CronSchedule(expression).getExpression().split(" ");
        for (int i = 0; i < fields.length; i++) fields[i].setValue(values[i]);
    }
    private static class FieldEditor extends JPanel {
        private static final long serialVersionUID = 1L;
        private final JPanel rows = new JPanel(new GridLayout(0, 1));
        private final String[] options;
        FieldEditor(String title, int min, int max) {
            setLayout(new java.awt.BorderLayout()); add(new JLabel(title), java.awt.BorderLayout.NORTH); add(rows, java.awt.BorderLayout.CENTER);
            options = new String[max - min + 2]; options[0] = "*"; for (int i = min; i <= max; i++) options[i - min + 1] = String.valueOf(i);
            JButton add = new JButton("+"); add.addActionListener(e -> addRow("*")); add(add, java.awt.BorderLayout.SOUTH); addRow("*");
        }
        private void addRow(String selected) {
            JPanel row = new JPanel(); JComboBox<String> box = new JComboBox<String>(options); box.setSelectedItem(selected);
            JButton remove = new JButton("-"); remove.addActionListener(e -> { if (rows.getComponentCount() > 1) { rows.remove(row); rows.revalidate(); rows.repaint(); } });
            row.add(box); row.add(remove); rows.add(row); rows.revalidate();
        }
        String value() {
            List<String> selected = new ArrayList<String>();
            for (java.awt.Component component : rows.getComponents()) {
                @SuppressWarnings("unchecked") JComboBox<String> box = (JComboBox<String>) ((JPanel) component).getComponent(0);
                String value = box.getSelectedItem().toString(); if ("*".equals(value)) return "*"; if (!selected.contains(value)) selected.add(value);
            }
            return String.join(",", selected);
        }
        void setValue(String value) {
            rows.removeAll(); for (String item : value.split(",")) addRow(item); rows.revalidate(); rows.repaint();
        }
    }
}
