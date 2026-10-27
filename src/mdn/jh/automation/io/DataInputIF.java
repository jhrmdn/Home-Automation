package mdn.jh.automation.io;

import mdn.jh.automation.io.logic.InputDefinition;
import mdn.jh.automation.storage.Storeable;

public interface DataInputIF extends RecursionCheckIF, Storeable {

	/**
	 * Sets and input to the component
	 * @param dataOutput        The data from the sending component
	 * @param dataIDFromOutput  The id of the "sending" component
	 * @param connectionInputID The input number used
	 */
	public void setConnection(DataOutputIF dataOutput, int dataIDFromOutput, int connectionInputNumber) throws RecursionException;
	/**
	 * The DataComponent unique ID
	 * @return
	 */
	public int getId();
	
	/**
	 * Informs that an update has happened on the input id
	 */
	public void update(int connectionInputNumber);
	
	/**
	 * The Datatype defined in class 'Data'
	 * 
	 * @return
	 */
//	public int getDataTypeInput();

	public InputDefinition[] getInputDefinitions();

	/**
	 * To state that the input is in connected
	 * 
	 * @param inputNumber
	 * @param state
	 */
	//public void setInputInUse(int inputNumber, boolean state);
	public void unlinkInput(int inputNumber);
	
	public boolean isInputInUse(int inputNumber);

	/** Returns whether a real component connection occupies this input. */
	default boolean isInputConnected(int inputNumber) { return isInputInUse(inputNumber); }

	public DataOutputIF getInputData(int inputNumber);
	
	
	
}
