package mdn.jh.automation.devices.shelly;

import javax.swing.JPanel;
import org.w3c.dom.*;
import mdn.jh.automation.io.DataComponentStub;
import mdn.jh.automation.io.source.DataSource;

public class ShellyDataSource extends DataSource {
    private static final long serialVersionUID=1L;
    private int channel;
    private String componentKey="";
    private boolean analog, valid;
    private double value;

    public ShellyDataSource() {}
    public ShellyDataSource(int channel) { this(channel,"input:"+channel,false); }
    public ShellyDataSource(int channel,String key,boolean analog) {
        this.channel=channel;this.componentKey=key;this.analog=analog;
        setStatusMessage("Physical input "+key);
    }
    public int getChannel(){return channel;}
    public String getComponentKey(){return componentKey.isEmpty()?"input:"+channel:componentKey;}
    public void updateValue(boolean value){updateValue(value?1d:0d);}
    public void updateValue(double value){this.value=value;valid=Double.isFinite(value);updateMyOutputs();}
    public void invalidate(){valid=false;}
    @Override public boolean getOutputAsBoolean(){return value!=0;}
    @Override public double getOutputAsNumber(){return value;}
    @Override public String getOutputAsString(){return analog?Double.toString(value):Boolean.toString(value!=0);}
    @Override public int getDataTypeOutput(){return analog?DataComponentStub.TYPE_DOUBLE_IO:DataComponentStub.TYPE_BOOLEAN_IO;}
    @Override public boolean isDataValid(){return valid;}
    @Override public JPanel getSpecificDetailsPanel(){return null;}
    @Override public Node getStorageXML(Document doc){
        Element root=getStorage(doc),input=doc.createElement("ShellyInput");
        input.setAttribute("channel",Integer.toString(channel));input.setAttribute("key",getComponentKey());input.setAttribute("analog",Boolean.toString(analog));root.appendChild(input);return root;
    }
    @Override public boolean initDataComponent(Node node)throws Exception{
        initDataSource(node);NodeList children=node.getChildNodes();
        for(int i=0;i<children.getLength();i++)if("ShellyInput".equals(children.item(i).getNodeName())){
            Element input=(Element)children.item(i);channel=Integer.parseInt(input.getAttribute("channel"));componentKey=input.getAttribute("key");analog=Boolean.parseBoolean(input.getAttribute("analog"));
        }
        setStatusMessage("Physical input "+getComponentKey());return true;
    }
}
