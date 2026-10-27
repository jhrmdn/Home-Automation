package mdn.jh.automation.gui;

import mdn.jh.automation.io.DataConnectionIF;
import mdn.jh.automation.io.DataInputIF;

public interface ConnectionLinkListenerIF {

	public void setSourceComponent(DataConnectionIF source);

	public void setTargetComponent(DataInputIF target, int inputID);

	public void cancelSelection();
	
	/**
	 * 
	 * 
	 * 1=nothing selected, 2=sourceSelected
	 * 
	 * @return
	 */
	public int selectionState();

}
