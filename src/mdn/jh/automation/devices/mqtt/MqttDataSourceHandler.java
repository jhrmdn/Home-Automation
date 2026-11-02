package mdn.jh.automation.devices.mqtt;
import org.w3c.dom.Node;import mdn.jh.automation.device.DataSourceHandler;
public class MqttDataSourceHandler extends DataSourceHandler{private static final long serialVersionUID=1L;private final MqttBrokerDevice device;public MqttDataSourceHandler(MqttBrokerDevice d){device=d;}public MqttDataSourceHandler(MqttBrokerDevice d,Node n)throws Exception{this(d);initDataComponent(n);}@Override public void update(){device.refreshSubscriptions();}@Override protected String getDataSourceDetail(){return device.getName();}}
