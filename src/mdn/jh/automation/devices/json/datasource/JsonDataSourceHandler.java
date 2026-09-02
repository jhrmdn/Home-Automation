package mdn.jh.automation.devices.json.datasource;

import java.util.Iterator;
import org.json.JSONArray;import org.json.JSONObject;import org.w3c.dom.Node;
import mdn.jh.automation.device.DataSourceHandler;import mdn.jh.automation.devices.json.JsonDevice;import mdn.jh.automation.io.source.DataSource;

public class JsonDataSourceHandler extends DataSourceHandler {
	private static final long serialVersionUID=1L;private final JsonDevice device;
	public JsonDataSourceHandler(JsonDevice device){this.device=device;}public JsonDataSourceHandler(JsonDevice device,Node node)throws Exception{this(device);initDataComponent(node);}
	@Override public void update(){try{Object document=device.getJsonDocument();Iterator<DataSource> iterator=myDataSources.iterator();while(iterator.hasNext()){JsonDataSource source=(JsonDataSource)iterator.next();Object value=resolve(document,source.getJsonPath());source.updateValue(value==JSONObject.NULL?"":String.valueOf(value));source.setValid(true);}}catch(Exception error){for(DataSource source:myDataSources)((JsonDataSource)source).setValid(false);}}
	private Object resolve(Object value,String pointer)throws Exception{if(pointer==null||pointer.isEmpty()||"/".equals(pointer))return value;for(String token:pointer.substring(1).split("/")){token=token.replace("~1","/").replace("~0","~");if(value instanceof JSONObject)value=((JSONObject)value).get(token);else if(value instanceof JSONArray)value=((JSONArray)value).get(Integer.parseInt(token));else throw new IllegalArgumentException("JSON path does not resolve to a value");}return value;}
	@Override protected String getDataSourceDetail(){return device.getName();}
}
