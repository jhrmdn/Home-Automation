package mdn.jh.automation.devices.internalweb;

import org.w3c.dom.*;
import mdn.jh.automation.device.*;
import mdn.jh.automation.gui.DataSinkCreator;
import mdn.jh.automation.gui.DataSourceCreator;

/** API endpoints hosted by the application's existing HTTP server. */
public class InternalWebserverDevice extends Device {
    private volatile boolean apiEnabled;
    private volatile boolean autostart;

    public boolean isApiEnabled() { return apiEnabled; }
    public void setApiEnabled(boolean enabled) { apiEnabled = enabled; }
    public boolean isAutostart() { return autostart; }
    public void setAutostart(boolean enabled) { autostart = enabled; }
    @Override public void startUpdateThreads() { apiEnabled = autostart; super.startUpdateThreads(); }
    @Override public void stopUpdateThreads() { apiEnabled = false; super.stopUpdateThreads(); }

    public static boolean isWebApiEnabled() {
        var handler = mdn.jh.automation.Main.getMySmartHomeHandler();
        if (handler == null) return false;
        for (Device device : handler.getDevices())
            if (device instanceof InternalWebserverDevice) return ((InternalWebserverDevice) device).isApiEnabled();
        return false;
    }
    public InternalWebserverDevice() {
        super(TYPE_INTERNAL_WEBSERVER);
        dataSourceHandler = new DataSourceHandler() {
            @Override public void update() { }
            @Override protected String getDataSourceDetail() { return getName(); }
        };
        dataSinkHandler = new DataSinkHandler() {
            @Override protected String getDataSinkDetail() { return getName(); }
        };
    }

    @Override public String getName() { return "Internal Webserver"; }
    @Override public DataSourceHandler getDataSourceHandler() { return dataSourceHandler; }
    @Override public DataSinkHandler getDataSinkHandler() { return dataSinkHandler; }
    @Override public DataSourceCreator getDataSourceCreator() { return null; }
    @Override public DataSinkCreator getDataSinkCreator() { return null; }
    @Override protected Node getSpecificStorage(Document document) {
        Element element = document.createElement("InternalWebserver");
        element.setAttribute("autostart", Boolean.toString(autostart));
        return element;
    }
    @Override public boolean initSpecific(Node node) throws Exception {
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (!"InternalWebserver".equals(children.item(i).getNodeName())) continue;
            Element settings = (Element) children.item(i);
            // Previously these endpoints were always enabled; retain that on migration.
            autostart = !settings.hasAttribute("autostart") || Boolean.parseBoolean(settings.getAttribute("autostart"));
            NodeList handlers = children.item(i).getChildNodes();
            for (int j = 0; j < handlers.getLength(); j++) {
                Node handler = handlers.item(j);
                if ("DataSources".equals(handler.getNodeName())) dataSourceHandler.initDataComponent(handler);
                if ("DataSinks".equals(handler.getNodeName())) dataSinkHandler.initDataComponent(handler);
            }
            return true;
        }
        return false;
    }
}
