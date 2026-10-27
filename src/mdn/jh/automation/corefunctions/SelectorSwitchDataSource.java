package mdn.jh.automation.corefunctions;

import java.util.Arrays;
import javax.swing.JPanel;
import org.w3c.dom.*;
import mdn.jh.automation.io.*;
import mdn.jh.automation.io.logic.DataInputConnection;
import mdn.jh.automation.io.source.DataSource;
import mdn.jh.automation.io.source.WrongDataTypeException;

/** One-hot Boolean source: exactly one selected output is true. */
public class SelectorSwitchDataSource extends DataSource {
    private static final long serialVersionUID = 1L;
    protected String[] labels = {"", ""};
    protected int selectedIndex;

    public SelectorSwitchDataSource(){setName("Selector Switch");updateStatus();setHelptext("Select one of 2–8 Boolean outputs. Only the selected output is true; all others are false. Positions can be named and selected in Automation or the dashboard.");}
    public synchronized void configure(String[] incoming){
        if(incoming==null||incoming.length<2||incoming.length>8)throw new IllegalArgumentException("Select between 2 and 8 outputs");
        String[] validated=new String[incoming.length];
        for(int i=0;i<incoming.length;i++){
            validated[i]=incoming[i]==null?"":incoming[i].trim();
            if(validated[i].length()>200)throw new IllegalArgumentException("Output labels may contain at most 200 characters");
        }
        for(DataInputConnection connection:myOutputs)
            if(connection.getOutputIndex()>=validated.length)throw new IllegalArgumentException("Disconnect removed outputs before reducing the output count");
        labels=validated;selectedIndex=Math.min(selectedIndex,labels.length-1);updateStatus();updateMyOutputs();
    }
    public synchronized String[] getOutputLabels(){return Arrays.copyOf(labels,labels.length);}
    public synchronized int getSelectedIndex(){return selectedIndex;}
    public synchronized void select(int index){
        checkIndex(index);selectedIndex=index;updateStatus();updateMyOutputs();
    }
    private void checkIndex(int index){if(index<0||index>=labels.length)throw new IllegalArgumentException("Unknown output position");}
    protected void updateStatus(){setStatusMessage("Output "+(selectedIndex+1)+(labels[selectedIndex].isEmpty()?"":" · "+labels[selectedIndex]));}
    @Override public synchronized int getOutputCount(){return labels.length;}
    @Override public synchronized String getOutputName(int index){checkIndex(index);return (index+1)+(labels[index].isEmpty()?"":" · "+labels[index]);}
    @Override public synchronized DataOutputIF getOutputPort(int index){checkIndex(index);return new OutputPort(index);}
    @Override public void addInputConnection(DataInputIF input,int inputIndex)throws WrongDataTypeException {addInputConnection(input,inputIndex,0);}
    @Override public synchronized void addInputConnection(DataInputIF input,int inputIndex,int outputIndex)throws WrongDataTypeException{
        DataOutputIF port=getOutputPort(outputIndex);
        if(inputIndex<0||inputIndex>=input.getInputDefinitions().length||input.getInputDefinitions()[inputIndex].getDataType()!=TYPE_BOOLEAN_IO)
            throw new WrongDataTypeException("Selector Switch outputs require a Boolean input");
        try{input.setConnection(port,getId(),inputIndex);}catch(RecursionException e){throw new IllegalArgumentException("Recursive connection",e);}
        myOutputs.add(new DataInputConnection(input,inputIndex,outputIndex));
    }
    protected synchronized boolean outputActive(int index){return selectedIndex==index;}
    @Override public int getDataTypeOutput(){return TYPE_BOOLEAN_IO;}
    @Override public synchronized boolean getOutputAsBoolean(){return outputActive(0);}
    @Override public double getOutputAsNumber(){return getOutputAsBoolean()?1:0;}
    @Override public String getOutputAsString(){return Boolean.toString(getOutputAsBoolean());}
    @Override public boolean isDataValid(){return true;}
    @Override public JPanel getSpecificDetailsPanel(){return null;}
    // A single component-wide override could activate two outputs, breaking one-hot semantics.
    @Override public void setOutputOverride(String value,boolean persistent){throw new IllegalArgumentException("Use the Selector Switch position instead of an output override");}
    @Override public synchronized Node getStorageXML(Document document){
        Element root=getStorage(document),settings=document.createElement("SelectorSwitch");
        settings.setAttribute("name",getName()==null?"Selector Switch":getName());
        settings.setAttribute("selectedIndex",Integer.toString(selectedIndex));
        for(String label:labels){Element output=document.createElement("Output");output.setTextContent(label);settings.appendChild(output);}
        root.appendChild(settings);return root;
    }
    @Override public synchronized boolean initDataComponent(Node node)throws Exception{
        if(!(node instanceof Element root)||!"DataSource".equals(root.getNodeName()))return false;
        Element settings=(Element)root.getElementsByTagName("SelectorSwitch").item(0);
        if(settings==null)throw new IllegalArgumentException("Selector Switch settings are missing");
        NodeList outputs=settings.getElementsByTagName("Output");String[] restored=new String[outputs.getLength()];
        for(int i=0;i<restored.length;i++)restored[i]=outputs.item(i).getTextContent();
        configure(restored);initDataSource(node);setName(settings.getAttribute("name"));
        select(Integer.parseInt(settings.getAttribute("selectedIndex")));return true;
    }
    private final class OutputPort implements DataOutputIF,java.io.Serializable {
        private static final long serialVersionUID=1L;
        private final int index;
        OutputPort(int index){this.index=index;}
        @Override public boolean getOutputAsBoolean(){synchronized(SelectorSwitchDataSource.this){return outputActive(index);}}
        @Override public double getOutputAsNumber(){return getOutputAsBoolean()?1:0;}
        @Override public String getOutputAsString(){return Boolean.toString(getOutputAsBoolean());}
        @Override public int getDataTypeOutput(){return TYPE_BOOLEAN_IO;}
        @Override public boolean isDataValid(){return true;}
    }
}
