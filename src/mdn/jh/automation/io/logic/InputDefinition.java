package mdn.jh.automation.io.logic;

import java.io.Serializable;

import mdn.jh.automation.io.DataComponentStub;

public class InputDefinition implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8903280240968539785L;
	private String name = null;
	private String helpText = null;
	private int dataType=DataComponentStub.TYPE_NOT_SET;
	
	public int getDataType() {
		return dataType;
	}

	/**
	 * 
	 * @param value
	 * @param inputComponentSenderID
	 * @param name                   The name of the input
	 */
	public InputDefinition(String name, int dataType) {
		this(name,dataType,null);
		
	}

	public InputDefinition(String name,int dataType, String helpText) {
		this.name = name;
		this.dataType=dataType;
	}

	public String getName() {
		return name;
	}

	public String getHelpText() {
		return helpText;
	}

	public void setHelpText(String helpText) {
		this.helpText = helpText;
	}

}
