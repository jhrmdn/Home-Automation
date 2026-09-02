package mdn.jh.automation.devices.database;

import java.awt.BorderLayout;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Types;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import mdn.jh.automation.io.DataComponentStub;
import mdn.jh.automation.io.DataOutputIF;
import mdn.jh.automation.io.logic.InputDefinition;
import mdn.jh.automation.io.sink.DataSink;

public class DatabaseDataSink extends DataSink {
    private static final long serialVersionUID = 1L;
    private transient DatabaseDevice device;
    private String table = "", cronExpression = "* * * * *";
    private boolean schedulerEnabled = true;
    private String[] columnNames = new String[0];
    private int[] sqlTypes = new int[0];
    private transient CronSchedule schedule = new CronSchedule(cronExpression);
    private long lastExecutedMinute = Long.MIN_VALUE;
    private transient long lastObservedMinute = Long.MIN_VALUE;
    private transient boolean previousTrigger;
    private boolean valid = true;
    private String lastWriteDetails = "Never written";
    private static final DateTimeFormatter SHORT_TIME = DateTimeFormatter.ofPattern("dd.MM.yy HH:mm");

    public DatabaseDataSink() { super(new InputDefinition[0]); }
    public DatabaseDataSink(InputDefinition[] inputs) { super(inputs); }
    public void configure(DatabaseDevice device, String table, String[] columnNames, int[] sqlTypes, String cronExpression) {
        configure(device, table, columnNames, sqlTypes, cronExpression, true);
    }
    public void configure(DatabaseDevice device, String table, String[] columnNames, int[] sqlTypes, String cronExpression, boolean schedulerEnabled) {
        this.device = device; this.table = table; this.columnNames = columnNames.clone(); this.sqlTypes = sqlTypes.clone();
        this.cronExpression = cronExpression; this.schedulerEnabled = schedulerEnabled; this.schedule = new CronSchedule(cronExpression);
        setStatusMessage(table + (schedulerEnabled ? " @ " + cronExpression : " (event trigger only)"));
    }
    public void setDevice(DatabaseDevice device) { this.device = device; }
    public String getTable() { return table; }
    public String getCronExpression() { return cronExpression; }
    public boolean isSchedulerEnabled() { return schedulerEnabled; }
    public void setSchedulerEnabled(boolean enabled) { setSchedule(enabled, cronExpression); }
    public String getLastWriteDetails() { return lastWriteDetails; }
    public void setSchedule(boolean enabled, String expression) {
        CronSchedule validated = new CronSchedule(expression); schedulerEnabled = enabled; cronExpression = validated.getExpression(); schedule = validated;
        setStatusMessage(table + (enabled ? " @ " + cronExpression : " (event trigger only)"));
    }

    @Override public void updateCycle() {
        ZonedDateTime now = ZonedDateTime.now(); long minute = now.toEpochSecond() / 60;
        boolean trigger = triggerValue();
        boolean eventDue = trigger && !previousTrigger;
        previousTrigger = trigger;
        boolean minuteBoundary = lastObservedMinute != Long.MIN_VALUE && minute != lastObservedMinute;
        lastObservedMinute = minute;
        boolean scheduleDue = schedulerEnabled && minuteBoundary && minute != lastExecutedMinute && schedule().matches(now);
        if (!eventDue && !scheduleDue) return;
        if (scheduleDue) lastExecutedMinute = minute;
        try { insertConnectedValues(); valid = true; lastWriteDetails = "Successfully written at " + now; myOutput.setValue("written " + SHORT_TIME.format(now)); myOutput.setDataValid(true); }
        catch (Exception error) { valid = false; lastWriteDetails = "Write failed at " + now + ": " + error.getMessage(); myOutput.setValue("write failed " + SHORT_TIME.format(now)); myOutput.setDataValid(false); }
    }
    private boolean triggerValue() {
        int input = columnNames.length;
        return input < getInputDefinitions().length && isInputInUse(input) && getInputData(input) != null
                && getInputData(input).isDataValid() && getInputData(input).getOutputAsBoolean();
    }
    private CronSchedule schedule() { if (schedule == null) schedule = new CronSchedule(cronExpression); return schedule; }
    private void insertConnectedValues() throws Exception {
        if (device == null) throw new IllegalStateException("Database device is not assigned");
        String quote;
        try (Connection connection = device.openConnection()) { quote = connection.getMetaData().getIdentifierQuoteString().trim(); if (quote.isEmpty()) quote = "`";
            StringBuilder names = new StringBuilder(), values = new StringBuilder(); java.util.List<Integer> connected = new java.util.ArrayList<Integer>();
            for (int i = 0; i < columnNames.length; i++) if (isInputInUse(i) && getInputData(i) != null) {
                if (names.length() > 0) { names.append(','); values.append(','); } names.append(quote).append(columnNames[i].replace(quote, quote + quote)).append(quote); values.append('?'); connected.add(i);
            }
            if (connected.isEmpty()) return;
            String safeTable = quote + table.replace(quote, quote + quote) + quote;
            try (PreparedStatement statement = connection.prepareStatement("INSERT INTO " + safeTable + " (" + names + ") VALUES (" + values + ")")) {
                for (int parameter = 0; parameter < connected.size(); parameter++) bind(statement, parameter + 1, connected.get(parameter)); statement.executeUpdate();
            }
        }
    }
    private void bind(PreparedStatement statement, int parameter, int input) throws Exception {
        DataOutputIF value = getInputData(input); int type = sqlTypes[input];
        if (!value.isDataValid()) { statement.setNull(parameter, type); return; }
        if (type == Types.BOOLEAN || type == Types.BIT) statement.setBoolean(parameter, value.getOutputAsBoolean());
        else if (getInputDefinitions()[input].getDataType() == DataComponentStub.TYPE_DOUBLE_IO) statement.setDouble(parameter, value.getOutputAsNumber());
        else statement.setObject(parameter, value.getOutputAsString(), type);
    }
    @Override public boolean isDataValid() { return valid; }
    @Override public JPanel getSpecificDetailsPanel() { JPanel panel = new JPanel(new BorderLayout()); panel.add(new JLabel("Table: " + table + " | cron: " + cronExpression), BorderLayout.NORTH); return panel; }

    @Override public Node getStorageXML(Document doc) {
        Element root = getStorageDataSink(doc); Element config = doc.createElement("DatabaseSink"); config.setAttribute("table", table); config.setAttribute("cron", cronExpression); config.setAttribute("schedulerEnabled", String.valueOf(schedulerEnabled));
        for (int i = 0; i < columnNames.length; i++) { Element column = doc.createElement("Column"); column.setAttribute("name", columnNames[i]); column.setAttribute("sqlType", String.valueOf(sqlTypes[i])); config.appendChild(column); }
        root.appendChild(config); return root;
    }
    @Override public void initSpecific(Node node) {
        Node config = null; NodeList children = node.getChildNodes(); for (int i = 0; i < children.getLength(); i++) if ("DatabaseSink".equals(children.item(i).getNodeName())) config = children.item(i);
        if (config == null) { valid = false; return; }
        table = config.getAttributes().getNamedItem("table").getNodeValue(); cronExpression = config.getAttributes().getNamedItem("cron").getNodeValue();
        Node enabled = config.getAttributes().getNamedItem("schedulerEnabled"); schedulerEnabled = enabled == null || Boolean.parseBoolean(enabled.getNodeValue());
        java.util.List<String> names = new java.util.ArrayList<String>(); java.util.List<Integer> types = new java.util.ArrayList<Integer>(); NodeList columns = config.getChildNodes();
        for (int i = 0; i < columns.getLength(); i++) if ("Column".equals(columns.item(i).getNodeName())) { names.add(columns.item(i).getAttributes().getNamedItem("name").getNodeValue()); types.add(Integer.valueOf(columns.item(i).getAttributes().getNamedItem("sqlType").getNodeValue())); }
        columnNames = names.toArray(new String[0]); sqlTypes = new int[types.size()]; InputDefinition[] definitions = new InputDefinition[types.size() + 1];
        for (int i = 0; i < types.size(); i++) { sqlTypes[i] = types.get(i); definitions[i] = new InputDefinition(columnNames[i], mapType(sqlTypes[i])); }
        definitions[types.size()] = new InputDefinition("Write trigger", DataComponentStub.TYPE_BOOLEAN_IO, "A rising edge inserts one row");
        defineInputs(definitions); schedule = new CronSchedule(cronExpression); setStatusMessage(table + (schedulerEnabled ? " @ " + cronExpression : " (event trigger only)"));
    }
    private int mapType(int sqlType) {
        if (sqlType == Types.BOOLEAN || sqlType == Types.BIT) return DataComponentStub.TYPE_BOOLEAN_IO;
        switch (sqlType) { case Types.TINYINT: case Types.SMALLINT: case Types.INTEGER: case Types.BIGINT: case Types.FLOAT: case Types.REAL: case Types.DOUBLE: case Types.NUMERIC: case Types.DECIMAL: return DataComponentStub.TYPE_DOUBLE_IO; default: return DataComponentStub.TYPE_STRING_IO; }
    }
}
