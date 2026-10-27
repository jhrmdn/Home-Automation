package mdn.jh.automation.devices.database;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import mdn.jh.automation.device.Device;
import mdn.jh.automation.device.DeviceCreator;

public class DatabaseDeviceCreator extends DeviceCreator {
    private static final long serialVersionUID = 1L;
    private final JTextField host = new JTextField("localhost");
    private final JTextField port = new JTextField("3306");
    private final JTextField username = new JTextField();
    private final JPasswordField password = new JPasswordField();
    private final JComboBox<String> databases = new JComboBox<String>();
    private final JLabel status = new JLabel("Enter connection data, then load databases");

    public DatabaseDeviceCreator() {
        setDeviceName("MariaDB / MySQL Database");
        JPanel fields = new JPanel(new GridLayout(0, 2, 8, 8));
        fields.add(new JLabel("IP / hostname")); fields.add(host);
        fields.add(new JLabel("Port")); fields.add(port);
        fields.add(new JLabel("Username")); fields.add(username);
        fields.add(new JLabel("Password")); fields.add(password);
        fields.add(new JLabel("Database")); fields.add(databases);
        JButton refresh = new JButton("Connect / refresh databases");
        refresh.addActionListener(event -> refreshDatabases());
        JPanel south = new JPanel(new BorderLayout()); south.add(refresh, BorderLayout.WEST); south.add(status, BorderLayout.CENTER);
        add(fields, BorderLayout.NORTH); add(south, BorderLayout.SOUTH);
    }

    private DatabaseDevice configuredDevice(String catalog) {
        DatabaseDevice result = new DatabaseDevice();
        result.setConnectionData(host.getText().trim(), Integer.parseInt(port.getText().trim()), username.getText(), new String(password.getPassword()), catalog);
        return result;
    }
    private void refreshDatabases() {
        try {
            List<String> names = configuredDevice("").listDatabases(); databases.removeAllItems();
            for (String name : names) databases.addItem(name);
            status.setText(names.size() + " database(s) found");
        } catch (Exception error) { status.setText("Connection failed: " + error.getMessage()); }
    }
    @Override public Device getCreatedDevice() throws Exception {
        Object selected = databases.getSelectedItem();
        if (selected == null) throw new Exception("Connect and select a database first");
        DatabaseDevice result = configuredDevice(selected.toString());
        try (java.sql.Connection ignored = result.openConnection()) { return result; }
    }
}
