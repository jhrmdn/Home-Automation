package mdn.jh.automation.devices.database;

import java.util.Iterator;
import org.w3c.dom.Node;
import mdn.jh.automation.device.DataSinkHandler;
import mdn.jh.automation.io.sink.DataSink;

public class DatabaseDataSinkHandler extends DataSinkHandler {
    private static final long serialVersionUID = 1L;
    private final DatabaseDevice device;
    public DatabaseDataSinkHandler(DatabaseDevice device) { this.device = device; }
    public DatabaseDataSinkHandler(DatabaseDevice device, Node node) throws Exception { this(device); initDataComponent(node); }
    @Override public boolean initDataComponent(Node node) throws Exception {
        boolean result = super.initDataComponent(node);
        for (Iterator<DataSink> it = getMyDataSinks().iterator(); it.hasNext();) ((DatabaseDataSink) it.next()).setDevice(device);
        return result;
    }
    @Override protected String getDataSinkDetail() { return device.getName(); }
}
