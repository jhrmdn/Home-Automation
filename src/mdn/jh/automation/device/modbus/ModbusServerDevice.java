package mdn.jh.automation.device.modbus;

import java.net.InetAddress;
import java.util.logging.Level;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import com.intelligt.modbus.jlibmodbus.data.DataHolder;
import com.intelligt.modbus.jlibmodbus.data.ModbusCoils;
import com.intelligt.modbus.jlibmodbus.data.ModbusHoldingRegisters;
import com.intelligt.modbus.jlibmodbus.slave.ModbusSlave;
import com.intelligt.modbus.jlibmodbus.slave.ModbusSlaveFactory;
import com.intelligt.modbus.jlibmodbus.tcp.TcpParameters;
import mdn.jh.automation.Main;
import mdn.jh.automation.corefunctions.CoreDataSinkHandler;
import mdn.jh.automation.corefunctions.CoreDataSourceHandler;
import mdn.jh.automation.device.DataSinkHandler;
import mdn.jh.automation.device.DataSourceHandler;
import mdn.jh.automation.device.Device;
import mdn.jh.automation.gui.DataSinkCreator;
import mdn.jh.automation.gui.DataSourceCreator;

/** The application's single built-in Modbus TCP server. */
public class ModbusServerDevice extends Device {
    private int port = 5020, unitId = 1;
    private boolean autostart;
    private transient ModbusSlave slave;
    private transient Thread serverThread;
    private transient String serverError = "";

    public ModbusServerDevice() { super(TYPE_MODBUS_SERVER); initializeHandlers(); }
    private void initializeHandlers() { if (dataSourceHandler == null) dataSourceHandler = new CoreDataSourceHandler(); if (dataSinkHandler == null) dataSinkHandler = new CoreDataSinkHandler(); }
    public synchronized int getPort() { return port; }
    public synchronized int getUnitId() { return unitId; }
    public synchronized boolean isAutostart() { return autostart; }
    public synchronized String getServerError() { return serverError; }
    public synchronized boolean isServerRunning() { return slave != null && slave.isListening(); }
    public synchronized void configure(int port, int unitId, boolean autostart) {
        if (port < 1 || port > 65535) throw new IllegalArgumentException("Port must be between 1 and 65535");
        if (unitId < 0 || unitId > 247) throw new IllegalArgumentException("Unit ID must be between 0 and 247");
        if (isServerRunning() && (this.port != port || this.unitId != unitId)) throw new IllegalStateException("Stop the Modbus server before changing port or unit ID");
        this.port = port; this.unitId = unitId; this.autostart = autostart;
    }
    public synchronized void startServer() throws Exception {
        if (isServerRunning() || (serverThread != null && serverThread.isAlive())) return;
        TcpParameters tcp = new TcpParameters(); tcp.setHost(InetAddress.getByName("0.0.0.0")); tcp.setPort(port); tcp.setKeepAlive(true);
        ModbusSlave created = ModbusSlaveFactory.createModbusSlaveTCP(tcp); created.setServerAddress(unitId);
        DataHolder data = new DataHolder(); data.setCoils(new ModbusCoils(10000)); data.setDiscreteInputs(new ModbusCoils(10000));
        data.setHoldingRegisters(new ModbusHoldingRegisters(10000)); data.setInputRegisters(new ModbusHoldingRegisters(10000)); created.setDataHolder(data);
        slave = created; serverError = "";
        serverThread = new Thread(() -> { try { created.listen(); } catch (Exception error) { synchronized (ModbusServerDevice.this) { serverError = error.getMessage(); } Main.getLogger().log(Level.WARNING, "Built-in Modbus server stopped with an error", error); } }, "modbus-tcp-server");
        serverThread.setDaemon(true); serverThread.start();
        for (int i = 0; i < 20 && !created.isListening() && serverThread.isAlive(); i++) Thread.sleep(25);
        if (!created.isListening() && !serverError.isEmpty()) throw new Exception(serverError);
    }
    public synchronized void stopServer() throws Exception { if (slave != null) slave.shutdown(); slave = null; serverThread = null; }
    @Override public void startUpdateThreads() { super.startUpdateThreads(); if (autostart) try { startServer(); } catch (Exception e) { serverError = e.getMessage(); Main.getLogger().log(Level.WARNING, "Unable to autostart built-in Modbus server", e); } }
    @Override public void stopUpdateThreads() { try { stopServer(); } catch (Exception e) { Main.getLogger().log(Level.WARNING, "Unable to stop built-in Modbus server", e); } super.stopUpdateThreads(); }
    @Override public DataSourceHandler getDataSourceHandler() { initializeHandlers(); return dataSourceHandler; }
    @Override public DataSinkHandler getDataSinkHandler() { initializeHandlers(); return dataSinkHandler; }
    @Override public DataSourceCreator getDataSourceCreator() { return null; }
    @Override public DataSinkCreator getDataSinkCreator() { return null; }
    @Override public String getName() { return "Built-in Modbus TCP Server"; }
    @Override protected Node getSpecificStorage(Document doc) { Element root = doc.createElement("ModbusServer"); root.setAttribute("port", Integer.toString(port)); root.setAttribute("unitId", Integer.toString(unitId)); root.setAttribute("autostart", Boolean.toString(autostart)); return root; }
    @Override public boolean initSpecific(Node node) throws Exception { NodeList children = node.getChildNodes(); for (int i=0;i<children.getLength();i++) if ("ModbusServer".equals(children.item(i).getNodeName())) { Node config=children.item(i); configure(Integer.parseInt(config.getAttributes().getNamedItem("port").getNodeValue()),Integer.parseInt(config.getAttributes().getNamedItem("unitId").getNodeValue()),Boolean.parseBoolean(config.getAttributes().getNamedItem("autostart").getNodeValue())); } initializeHandlers(); return true; }
}
