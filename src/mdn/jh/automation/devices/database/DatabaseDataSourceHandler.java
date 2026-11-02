package mdn.jh.automation.devices.database;

import mdn.jh.automation.device.DataSourceHandler;

public class DatabaseDataSourceHandler extends DataSourceHandler {
    private static final long serialVersionUID = 1L;
    private final DatabaseDevice device;
    public DatabaseDataSourceHandler(DatabaseDevice device) { this.device = device; }
    @Override public void update() { }
    @Override protected String getDataSourceDetail() { return device.getName(); }
}
