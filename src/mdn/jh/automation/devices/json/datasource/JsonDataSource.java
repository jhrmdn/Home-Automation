package mdn.jh.automation.devices.json.datasource;

import java.lang.reflect.Constructor;
import javax.swing.JPanel;
import org.w3c.dom.Document;import org.w3c.dom.Element;import org.w3c.dom.Node;import org.w3c.dom.NodeList;
import mdn.jh.automation.io.converter.Converter;import mdn.jh.automation.io.source.DataSource;import mdn.jh.automation.io.source.WrongDataTypeException;

public class JsonDataSource extends DataSource {
	private static final long serialVersionUID=1L;private Converter converter;private String jsonPath="";private boolean valid;
	public String getJsonPath(){return jsonPath;}public void setJsonPath(String path){jsonPath=path;}public void setValid(boolean value){valid=value;}
	public void setDataConverter(Converter value)throws WrongDataTypeException{if(isDataTypeLocked())throw new WrongDataTypeException();converter=value;}
	public void updateValue(String value){if(converter==null)return;converter.setValue(value);updateMyOutputs();}
	@Override public boolean getOutputAsBoolean(){return converter!=null&&converter.getOutputAsBoolean();}@Override public double getOutputAsNumber(){return converter==null?0:converter.getOutputAsNumber();}@Override public String getOutputAsString(){return converter==null?null:converter.getOutputAsString();}@Override public int getDataTypeOutput(){return converter.getDataTypeOutput();}@Override public boolean isDataValid(){return valid;}@Override public JPanel getSpecificDetailsPanel(){return null;}
	@Override public Node getStorageXML(Document doc){Element root=getStorage(doc),path=doc.createElement("JsonPath");path.appendChild(doc.createCDATASection(jsonPath));root.appendChild(path);if(converter!=null)root.appendChild(converter.getStorageXML(doc));return root;}
	@Override public boolean initDataComponent(Node node)throws Exception{if(!"DataSource".equals(node.getNodeName()))throw new Exception("Expected DataSource");initDataSource(node);NodeList children=node.getChildNodes();for(int i=0;i<children.getLength();i++){Node child=children.item(i);if("JsonPath".equals(child.getNodeName()))jsonPath=child.getTextContent();if("DataConverter".equals(child.getNodeName())){Class<?> clazz=Class.forName(child.getAttributes().getNamedItem("class").getNodeValue());Constructor<?> constructor=clazz.getConstructor();converter=(Converter)constructor.newInstance();converter.initDataComponent(child);}}return true;}
}
