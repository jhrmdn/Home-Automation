package mdn.jh.automation.devices.database;

import java.awt.BorderLayout;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import mdn.jh.automation.gui.DataSinkCreator;
import mdn.jh.automation.io.DataComponentStub;
import mdn.jh.automation.io.logic.InputDefinition;
import mdn.jh.automation.io.sink.DataSink;

public class DatabaseDataSinkCreator extends DataSinkCreator {
    private static final long serialVersionUID = 1L;
    private final DatabaseDevice device;
    private final JComboBox<String> tables = new JComboBox<String>();
    private final CronEditorPanel schedule = new CronEditorPanel();
    private final JLabel columns = new JLabel("Select a table");
    private List<DatabaseColumn> selectedColumns = new ArrayList<DatabaseColumn>();

    public DatabaseDataSinkCreator(DatabaseDevice device) {
        this.device = device; setLayout(new BorderLayout(8, 8));
        JPanel north = new JPanel(); north.add(new JLabel("Table")); north.add(tables);
        JButton refresh = new JButton("Refresh tables / inputs"); refresh.addActionListener(e -> refresh()); north.add(refresh);
        tables.addActionListener(e -> refreshColumns()); add(north, BorderLayout.NORTH); add(schedule, BorderLayout.CENTER); add(columns, BorderLayout.SOUTH); refresh();
    }
    private void refresh() {
        Object old = tables.getSelectedItem();
        try { tables.removeAllItems(); for (String table : device.listTables()) tables.addItem(table); if (old != null) tables.setSelectedItem(old); refreshColumns(); }
        catch (Exception error) { columns.setText("Refresh failed: " + error.getMessage()); }
    }
    private void refreshColumns() {
        Object table = tables.getSelectedItem(); if (table == null) { selectedColumns.clear(); columns.setText("No table selected"); return; }
        try (Connection connection = device.openConnection(); ResultSet rs = connection.getMetaData().getColumns(device.getDatabase(), null, table.toString(), "%")) {
            List<DatabaseColumn> result = new ArrayList<DatabaseColumn>(); StringBuilder text = new StringBuilder("Inputs: ");
            while (rs.next()) { DatabaseColumn column = new DatabaseColumn(rs.getString("COLUMN_NAME"), rs.getInt("DATA_TYPE")); result.add(column); if (result.size() > 1) text.append(", "); text.append(column.name); }
            selectedColumns = result; columns.setText(text.toString()); updateDone();
        } catch (Exception error) { columns.setText("Input refresh failed: " + error.getMessage()); }
    }
    @Override public DataSink getDataSink() throws Exception {
        Object table = tables.getSelectedItem(); if (table == null || selectedColumns.isEmpty()) throw new Exception("Select and refresh a table");
        InputDefinition[] inputs = new InputDefinition[selectedColumns.size() + 1]; int[] sqlTypes = new int[selectedColumns.size()]; String[] names = new String[selectedColumns.size()];
        for (int i = 0; i < selectedColumns.size(); i++) { DatabaseColumn c = selectedColumns.get(i); names[i] = c.name; sqlTypes[i] = c.sqlType; inputs[i] = new InputDefinition(c.name, mapType(c.sqlType)); }
        inputs[selectedColumns.size()] = new InputDefinition("Write trigger", DataComponentStub.TYPE_BOOLEAN_IO, "A rising edge inserts one row");
        DatabaseDataSink sink = new DatabaseDataSink(inputs); sink.configure(device, table.toString(), names, sqlTypes, schedule.getExpression()); return sink;
    }
    private int mapType(int sqlType) {
        if (sqlType == Types.BOOLEAN || sqlType == Types.BIT) return DataComponentStub.TYPE_BOOLEAN_IO;
        switch (sqlType) { case Types.TINYINT: case Types.SMALLINT: case Types.INTEGER: case Types.BIGINT: case Types.FLOAT: case Types.REAL: case Types.DOUBLE: case Types.NUMERIC: case Types.DECIMAL: return DataComponentStub.TYPE_DOUBLE_IO; default: return DataComponentStub.TYPE_STRING_IO; }
    }
    private static class DatabaseColumn { final String name; final int sqlType; DatabaseColumn(String name, int sqlType) { this.name = name; this.sqlType = sqlType; } }
}
