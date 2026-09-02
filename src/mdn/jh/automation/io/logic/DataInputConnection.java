package mdn.jh.automation.io.logic;

import java.io.Serializable;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import mdn.jh.automation.io.DataInputIF;
import mdn.jh.automation.storage.Storeable;

public class DataInputConnection implements Serializable, Storeable {

	private static final long serialVersionUID = -8039360313024496340L;
	DataInputIF dataInput = null;
	int inputConnectionID = -1;

	public DataInputConnection(DataInputIF dataInput, int inputConnectionID) {
		this.dataInput = dataInput;
		this.inputConnectionID = inputConnectionID;
	}

	public DataInputIF getDataInput() {
		return dataInput;
	}

	/**
	 * The input number starting with 0
	 * 
	 * @return
	 */
	public int getInputConnectionID() {
		return inputConnectionID;
	}

	@Override
	public Node getStorageXML(Document doc) {

		Element rootElement = doc.createElement("DataInputConnection");
		rootElement.setAttribute("id", ""+dataInput.getId());
		rootElement.setAttribute("inputConnectionID", "" + inputConnectionID);	
			
		return rootElement;
	}

	@Override
	public boolean initDataComponent(Node node) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

}
