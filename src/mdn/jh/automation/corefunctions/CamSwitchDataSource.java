package mdn.jh.automation.corefunctions;

import java.util.Arrays;
import org.w3c.dom.*;
import mdn.jh.automation.io.logic.DataInputConnection;

/** Manual Boolean cam switch with an independently configurable row for each position. */
public class CamSwitchDataSource extends SelectorSwitchDataSource {
    private static final long serialVersionUID=1L;
    private String[] positionLabels={"", ""};
    private boolean[][] matrix={{false,false},{true,true}};

    public CamSwitchDataSource(){setName("Cam Switch");setHelptext("Select a position to activate its configured Boolean outputs. Each matrix row may enable any combination of outputs, including none. Position labels, matrix and selection are saved.");updateStatus();}
    public synchronized void configureCam(String[] positions,String[] outputs,boolean[][] incoming){
        String[] newPositions=validateLabels(positions,"positions"),newOutputs=validateLabels(outputs,"outputs");
        if(incoming==null||incoming.length!=newPositions.length)throw new IllegalArgumentException("One matrix row per switch position is required");
        boolean[][] validated=new boolean[incoming.length][];
        for(int row=0;row<incoming.length;row++){
            if(incoming[row]==null||incoming[row].length!=newOutputs.length)throw new IllegalArgumentException("Each row must match the output count");
            validated[row]=Arrays.copyOf(incoming[row],incoming[row].length);
        }
        for(DataInputConnection edge:myOutputs)if(edge.getOutputIndex()>=newOutputs.length)throw new IllegalArgumentException("Disconnect removed outputs before reducing the output count");
        positionLabels=newPositions;labels=newOutputs;matrix=validated;selectedIndex=Math.min(selectedIndex,newPositions.length-1);
        updateStatus();updateMyOutputs();
    }
    private static String[] validateLabels(String[] input,String kind){
        if(input==null||input.length<2||input.length>8)throw new IllegalArgumentException("Select between 2 and 8 "+kind);
        String[] result=new String[input.length];
        for(int i=0;i<input.length;i++){result[i]=input[i]==null?"":input[i].trim();if(result[i].length()>200)throw new IllegalArgumentException("Labels may contain at most 200 characters");}
        return result;
    }
    @Override public synchronized void configure(String[] outputs){
        // Keep output-only callers from silently replacing the cam matrix.
        String[] validatedOutputs=validateLabels(outputs,"outputs");
        boolean[][] resized=new boolean[matrix.length][validatedOutputs.length];
        for(int row=0;row<resized.length;row++)System.arraycopy(matrix[row],0,resized[row],0,Math.min(matrix[row].length,resized[row].length));
        configureCam(positionLabels,outputs,resized);
    }
    public synchronized String[] getPositionLabels(){return Arrays.copyOf(positionLabels,positionLabels.length);}
    public synchronized boolean[][] getMatrix(){boolean[][] copy=new boolean[matrix.length][];for(int i=0;i<copy.length;i++)copy[i]=Arrays.copyOf(matrix[i],matrix[i].length);return copy;}
    @Override public synchronized void select(int index){
        if(index<0||index>=positionLabels.length)throw new IllegalArgumentException("Unknown cam switch position");
        selectedIndex=index;updateStatus();updateMyOutputs();
    }
    @Override protected synchronized boolean outputActive(int index){return matrix!=null&&selectedIndex<matrix.length&&index<matrix[selectedIndex].length&&matrix[selectedIndex][index];}
    @Override protected synchronized void updateStatus(){
        if(positionLabels==null){super.updateStatus();return;}
        setStatusMessage("Position "+(selectedIndex+1)+(positionLabels[selectedIndex].isEmpty()?"":" · "+positionLabels[selectedIndex]));
    }
    @Override public synchronized Node getStorageXML(Document document){
        Element root=getStorage(document),settings=document.createElement("CamSwitch");
        settings.setAttribute("name",getName()==null?"Cam Switch":getName());settings.setAttribute("selectedIndex",Integer.toString(selectedIndex));
        for(String label:labels){Element output=document.createElement("Output");output.setTextContent(label);settings.appendChild(output);}
        for(int row=0;row<positionLabels.length;row++){
            Element position=document.createElement("Position");position.setAttribute("label",positionLabels[row]);
            for(boolean state:matrix[row]){Element bit=document.createElement("State");bit.setTextContent(Boolean.toString(state));position.appendChild(bit);}
            settings.appendChild(position);
        }
        root.appendChild(settings);return root;
    }
    @Override public synchronized boolean initDataComponent(Node node)throws Exception{
        if(!(node instanceof Element root)||!"DataSource".equals(root.getNodeName()))return false;
        Element settings=(Element)root.getElementsByTagName("CamSwitch").item(0);
        if(settings==null)throw new IllegalArgumentException("Cam Switch settings are missing");
        NodeList outputs=settings.getElementsByTagName("Output"),positions=settings.getElementsByTagName("Position");
        String[] names=new String[positions.getLength()],outputNames=new String[outputs.getLength()];boolean[][] states=new boolean[names.length][];
        for(int i=0;i<outputNames.length;i++)outputNames[i]=outputs.item(i).getTextContent();
        for(int row=0;row<names.length;row++){
            Element position=(Element)positions.item(row);names[row]=position.getAttribute("label");NodeList bits=position.getElementsByTagName("State");states[row]=new boolean[bits.getLength()];
            for(int col=0;col<bits.getLength();col++){String value=bits.item(col).getTextContent();if(!"true".equals(value)&&!"false".equals(value))throw new IllegalArgumentException("Invalid matrix Boolean");states[row][col]=Boolean.parseBoolean(value);}
        }
        configureCam(names,outputNames,states);initDataSource(node);setName(settings.getAttribute("name"));select(Integer.parseInt(settings.getAttribute("selectedIndex")));return true;
    }
}
