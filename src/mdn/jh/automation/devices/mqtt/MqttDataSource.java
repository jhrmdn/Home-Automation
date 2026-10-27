package mdn.jh.automation.devices.mqtt;

import java.nio.charset.StandardCharsets;
import javax.swing.JPanel;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import mdn.jh.automation.io.DataComponentStub;
import mdn.jh.automation.io.source.DataSource;

public class MqttDataSource extends DataSource {
    private static final long serialVersionUID=1L; private String topic="",valueType="string";private int qos;private String value="";private boolean valid;
    public MqttDataSource(){} public MqttDataSource(String topic,int qos,String valueType){configure(topic,qos,valueType);}
    public void configure(String topic,int qos,String valueType){if(topic==null||topic.trim().isEmpty())throw new IllegalArgumentException("MQTT topic is required");if(qos<0||qos>2)throw new IllegalArgumentException("QoS must be 0, 1, or 2");if(!"boolean".equals(valueType)&&!"number".equals(valueType)&&!"string".equals(valueType))throw new IllegalArgumentException("Unknown MQTT value type");this.topic=topic.trim();this.qos=qos;this.valueType=valueType;setStatusMessage("Subscribed to "+topic+" (QoS "+qos+")");}
    public String getTopic(){return topic;}public int getQos(){return qos;}public String getValueType(){return valueType;}public void receive(byte[] payload){value=new String(payload,StandardCharsets.UTF_8);try{if("boolean".equals(valueType)&&!("true".equalsIgnoreCase(value)||"false".equalsIgnoreCase(value)||"1".equals(value)||"0".equals(value)))throw new Exception();if("number".equals(valueType))Double.parseDouble(value);valid=true;updateMyOutputs();}catch(Exception e){valid=false;}}
    public void invalidate(){valid=false;}@Override public boolean getOutputAsBoolean(){return "true".equalsIgnoreCase(value)||"1".equals(value);}@Override public double getOutputAsNumber(){try{return Double.parseDouble(value);}catch(Exception e){return 0;}}@Override public String getOutputAsString(){return value;}@Override public int getDataTypeOutput(){return "boolean".equals(valueType)?TYPE_BOOLEAN_IO:"number".equals(valueType)?TYPE_DOUBLE_IO:TYPE_STRING_IO;}@Override public boolean isDataValid(){return valid;}@Override public JPanel getSpecificDetailsPanel(){return null;}
    @Override public Node getStorageXML(Document doc){Element root=getStorage(doc),config=doc.createElement("MqttSource");config.setAttribute("topic",topic);config.setAttribute("qos",Integer.toString(qos));config.setAttribute("valueType",valueType);root.appendChild(config);return root;}@Override public boolean initDataComponent(Node node)throws Exception{initDataSource(node);NodeList list=node.getChildNodes();for(int i=0;i<list.getLength();i++)if("MqttSource".equals(list.item(i).getNodeName())){Node c=list.item(i);configure(c.getAttributes().getNamedItem("topic").getNodeValue(),Integer.parseInt(c.getAttributes().getNamedItem("qos").getNodeValue()),c.getAttributes().getNamedItem("valueType").getNodeValue());}return true;}
}
