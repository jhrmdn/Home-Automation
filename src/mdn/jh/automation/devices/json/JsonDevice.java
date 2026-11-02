package mdn.jh.automation.devices.json;

import java.net.URI;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.json.JSONTokener;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import mdn.jh.automation.Main;
import mdn.jh.automation.device.DataSinkHandler;
import mdn.jh.automation.device.DataSourceHandler;
import mdn.jh.automation.device.Device;
import mdn.jh.automation.devices.json.datasource.JsonDataSourceHandler;
import mdn.jh.automation.devices.xml.datasink.XmlDataSinkHandler;
import mdn.jh.automation.gui.DataSinkCreator;
import mdn.jh.automation.gui.DataSourceCreator;

public class JsonDevice extends Device {
	public static final int TYPE_FILE=1, TYPE_WEB=2;
	private int jsonType=TYPE_WEB; private String location="";
	public JsonDevice(){super(TYPE_JSON);}
	public JsonDevice(int type,String location)throws Exception{this();this.jsonType=type;this.location=location;getJsonDocument();}
	public Object getJsonDocument() throws Exception {String text;if(jsonType==TYPE_FILE)text=Files.readString(Path.of(location),StandardCharsets.UTF_8);else try(InputStream input=URI.create(location).toURL().openStream()){text=new String(input.readAllBytes(),StandardCharsets.UTF_8);}return new JSONTokener(text).nextValue();}
	@Override public String getName(){return (jsonType==TYPE_FILE?"JSON File: ":"JSON Web: ")+location;}
	@Override public DataSourceHandler getDataSourceHandler(){if(dataSourceHandler==null)dataSourceHandler=new JsonDataSourceHandler(this);return dataSourceHandler;}
	@Override public DataSinkHandler getDataSinkHandler(){if(dataSinkHandler==null)dataSinkHandler=new XmlDataSinkHandler();return dataSinkHandler;}
	@Override public DataSourceCreator getDataSourceCreator(){return null;}
	@Override public DataSinkCreator getDataSinkCreator(){return null;}
	@Override protected Node getSpecificStorage(Document doc){Element root=doc.createElement("JSON");root.setAttribute("type",Integer.toString(jsonType));root.setAttribute("location",location);return root;}
	@Override public boolean initSpecific(Node node)throws Exception{Node json=null;NodeList children=node.getChildNodes();for(int i=0;i<children.getLength();i++)if("JSON".equals(children.item(i).getNodeName())){json=children.item(i);break;}if(json==null)return false;NamedNodeMap attributes=json.getAttributes();jsonType=Integer.parseInt(attributes.getNamedItem("type").getNodeValue());location=attributes.getNamedItem("location").getNodeValue();NodeList nested=json.getChildNodes();for(int i=0;i<nested.getLength();i++)if("DataSources".equals(nested.item(i).getNodeName()))dataSourceHandler=new JsonDataSourceHandler(this,nested.item(i));return true;}
}
