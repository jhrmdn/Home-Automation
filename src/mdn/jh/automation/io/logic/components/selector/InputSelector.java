package mdn.jh.automation.io.logic.components.selector;

import java.util.Arrays;
import javax.swing.JPanel;
import org.w3c.dom.*;
import mdn.jh.automation.io.DataOutputIF;
import mdn.jh.automation.io.RecursionException;
import mdn.jh.automation.io.logic.*;

/** Configurable 2–8-position manual switch. Only the selected input determines output validity. */
public class InputSelector extends LogicBase {
    private static final long serialVersionUID = 1L;
    public static final int INPUT_COUNT = 5; // Default, also used when loading earlier configurations.
    public static final int MIN_INPUTS = 2, MAX_INPUTS = 8;
    private String valueType = "boolean";
    private String[] labels = {"", "", "", "", ""};
    private int selectedIndex;

    public InputSelector() {
        super(TYPE_BOOLEAN_IO, definitions(TYPE_BOOLEAN_IO, new String[]{"", "", "", "", ""}),
                new LogicComponentDescription("Input Selector"));
        setName("Input Selector");
        setHelptext("Routes one of 2–8 inputs of the same type to the output. Select a numbered position in Automation or the dashboard. Only the selected input must be connected and valid.");
        calculateMyActualState();
    }

    private static InputDefinition[] definitions(int type, String[] labels) {
        InputDefinition[] inputs = new InputDefinition[labels.length];
        for (int i=0;i<labels.length;i++) inputs[i]=new InputDefinition((i+1)+(labels[i].isEmpty()?"":" · "+labels[i]),type);
        return inputs;
    }
    private static int dataType(String type) {
        return switch(type) {
            case "boolean" -> TYPE_BOOLEAN_IO;
            case "number" -> TYPE_DOUBLE_IO;
            case "string" -> TYPE_STRING_IO;
            default -> throw new IllegalArgumentException("Input type must be Boolean, Number or String");
        };
    }
    public synchronized void configure(String type, String[] incomingLabels) {
        int nextType=dataType(type);
        if(incomingLabels==null||(incomingLabels.length<MIN_INPUTS||incomingLabels.length>MAX_INPUTS))throw new IllegalArgumentException("Select between 2 and 8 inputs");
        String[] validated=new String[incomingLabels.length];
        for(int i=0;i<incomingLabels.length;i++){
            validated[i]=incomingLabels[i]==null?"":incomingLabels[i].trim();
            if(validated[i].length()>200)throw new IllegalArgumentException("Input labels may contain at most 200 characters");
        }
        for(int i=validated.length;i<labels.length;i++)
            if(isInputInUse(i))throw new IllegalArgumentException("Disconnect removed inputs and clear their overrides before reducing the input count");
        if(!valueType.equals(type)){
            for(int i=0;i<labels.length;i++)if(isInputInUse(i))throw new IllegalArgumentException("Disconnect inputs and remove input overrides before changing the type");
            if(!getMyOutputs().isEmpty()||hasOutputOverride())throw new IllegalArgumentException("Disconnect the output and remove its override before changing the type");
            defineInputs(definitions(nextType,validated));
        }else if(validated.length!=labels.length){
            resizeInputs(definitions(nextType,validated));
        }
        valueType=type;labels=validated;selectedIndex=Math.min(selectedIndex,labels.length-1);
        calculateMyActualState();updateMyOutputs();
    }
    public synchronized String getValueType(){return valueType;}
    public synchronized String[] getInputLabels(){return Arrays.copyOf(labels,labels.length);}
    public synchronized int getSelectedIndex(){return selectedIndex;}
    public synchronized void select(int index){
        if(index<0||index>=labels.length)throw new IllegalArgumentException("Unknown input position");
        selectedIndex=index;calculateMyActualState();updateMyOutputs();
    }
    @Override public synchronized InputDefinition[] getInputDefinitions(){return definitions(getDataTypeOutput(),labels);}
    @Override public synchronized int getDataTypeOutput(){return dataType(valueType);}
    @Override public synchronized boolean isDataValid(){
        DataOutputIF input=myInputValues[selectedIndex];
        return input!=null&&isInputInUse(selectedIndex)&&input.getDataTypeOutput()==getDataTypeOutput()&&input.isDataValid();
    }
    @Override protected synchronized void calculateMyActualState(){
        DataOutputIF input=myInputValues[selectedIndex];
        myActualOutputState.changeDataType(getDataTypeOutput());
        switch(valueType){
            case "boolean" -> myActualOutputState.setValue(input!=null&&input.getOutputAsBoolean());
            case "number" -> myActualOutputState.setValue(input==null?0d:input.getOutputAsNumber());
            case "string" -> myActualOutputState.setValue(input==null?"":input.getOutputAsString());
        }
        updateOutputValidity();setStatusMessage("Input "+(selectedIndex+1)+(labels[selectedIndex].isEmpty()?"":" · "+labels[selectedIndex]));
    }
    @Override protected synchronized void updateOutputValidity(){myActualOutputState.setDataValid(isDataValid());}
    @Override public synchronized void setConnection(DataOutputIF output,int sourceId,int index)throws RecursionException{
        if(index<0||index>=labels.length)throw new IllegalArgumentException("Unknown input");
        if(output.getDataTypeOutput()!=getDataTypeOutput())throw new IllegalArgumentException("Input and output types must match");
        super.setConnection(output,sourceId,index);
    }
    @Override public synchronized void unlinkInput(int index){super.unlinkInput(index);onInputOverrideChanged(index);}
    @Override public synchronized void resetSpecific(){select(0);}
    @Override public JPanel getSpecificDetailsPanel(){return null;}
    @Override public synchronized Node getStorageXML(Document document){
        Element root=(Element)super.getStorageXML(document),settings=document.createElement("InputSelector");
        settings.setAttribute("valueType",valueType);settings.setAttribute("selectedIndex",Integer.toString(selectedIndex));
        for(String label:labels){Element input=document.createElement("Input");input.setTextContent(label);settings.appendChild(input);}
        root.appendChild(settings);return root;
    }
    @Override public synchronized boolean initDataComponent(Node node)throws Exception{
        if(!(node instanceof Element root)||!"LogicBase".equals(root.getNodeName()))return false;
        Element settings=(Element)root.getElementsByTagName("InputSelector").item(0);
        if(settings==null)throw new IllegalArgumentException("Input Selector settings are missing");
        NodeList inputs=settings.getElementsByTagName("Input");
        if(inputs.getLength()<MIN_INPUTS||inputs.getLength()>MAX_INPUTS)throw new IllegalArgumentException("Select between 2 and 8 inputs");
        String[] restored=new String[inputs.getLength()];for(int i=0;i<restored.length;i++)restored[i]=inputs.item(i).getTextContent();
        configure(settings.getAttribute("valueType"),restored);
        if(!super.initDataComponent(node))return false;
        select(Integer.parseInt(settings.getAttribute("selectedIndex")));return true;
    }
}
