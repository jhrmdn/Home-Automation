package mdn.jh.automation.devices.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import mdn.jh.automation.device.DataSinkHandler;
import mdn.jh.automation.device.DataSourceHandler;
import mdn.jh.automation.device.Device;
import mdn.jh.automation.gui.DataSinkCreator;
import mdn.jh.automation.gui.DataSourceCreator;

public class DatabaseDevice extends Device {
    private String host = "localhost", username = "", password = "", database = "";
    private int port = 3306;

    public DatabaseDevice() { super(TYPE_DATABASE); initializeHandlers(); }
    public DatabaseDevice(Node node) throws Exception { super(TYPE_DATABASE, false); initDataComponent(node); }
    private void initializeHandlers() { dataSourceHandler = new DatabaseDataSourceHandler(this); dataSinkHandler = new DatabaseDataSinkHandler(this); }

    public void setConnectionData(String host, int port, String username, String password, String database) {
        this.host = host; this.port = port; this.username = username; this.password = password; this.database = database == null ? "" : database;
    }
    public Connection openConnection() throws SQLException {
        String url = "jdbc:mariadb://" + host + ":" + port + "/" + database;
        return DriverManager.getConnection(url, username, password);
    }
    public List<String> listDatabases() throws SQLException {
        List<String> result = new ArrayList<String>();
        try (Connection connection = DriverManager.getConnection("jdbc:mariadb://" + host + ":" + port + "/", username, password);
             ResultSet rs = connection.getMetaData().getCatalogs()) { while (rs.next()) result.add(rs.getString(1)); }
        return result;
    }
    public List<String> listTables() throws SQLException {
        List<String> result = new ArrayList<String>();
        try (Connection connection = openConnection(); ResultSet rs = connection.getMetaData().getTables(database, null, "%", new String[] { "TABLE" })) {
            while (rs.next()) result.add(rs.getString("TABLE_NAME"));
        }
        return result;
    }
    public String getDatabase() { return database; }
    @Override public DataSourceHandler getDataSourceHandler() { if (dataSourceHandler == null) dataSourceHandler = new DatabaseDataSourceHandler(this); return dataSourceHandler; }
    @Override public DataSinkHandler getDataSinkHandler() { if (dataSinkHandler == null) dataSinkHandler = new DatabaseDataSinkHandler(this); return dataSinkHandler; }
    @Override public DataSourceCreator getDataSourceCreator() { return null; }
    @Override public DataSinkCreator getDataSinkCreator() { return new DatabaseDataSinkCreator(this); }
    @Override public String getName() { return "MariaDB/MySQL: " + database + "@" + host + ":" + port; }

    @Override protected Node getSpecificStorage(Document doc) {
        Element root = doc.createElement("Database"); root.setAttribute("host", host); root.setAttribute("port", String.valueOf(port));
        root.setAttribute("username", username); root.setAttribute("password", password); root.setAttribute("catalog", database); return root;
    }
    @Override public boolean initSpecific(Node node) throws Exception {
        Node config = null; NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) if ("Database".equals(children.item(i).getNodeName())) config = children.item(i);
        if (config == null) return false;
        NamedNodeMap a = config.getAttributes(); setConnectionData(a.getNamedItem("host").getNodeValue(), Integer.parseInt(a.getNamedItem("port").getNodeValue()),
                a.getNamedItem("username").getNodeValue(), a.getNamedItem("password").getNodeValue(), a.getNamedItem("catalog").getNodeValue());
        NodeList nested = config.getChildNodes(); initializeHandlers();
        for (int i = 0; i < nested.getLength(); i++) {
            if ("DataSources".equals(nested.item(i).getNodeName())) dataSourceHandler.initDataComponent(nested.item(i));
            if ("DataSinks".equals(nested.item(i).getNodeName())) dataSinkHandler = new DatabaseDataSinkHandler(this, nested.item(i));
        }
        return true;
    }
}
