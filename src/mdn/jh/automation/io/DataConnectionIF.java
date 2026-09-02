package mdn.jh.automation.io;

import mdn.jh.automation.io.source.WrongDataTypeException;

public interface DataConnectionIF {

	/**
	 * The backward information for a sending component to know which is the receiving component and the port
	 * @param input
	 * @param inputConnectionID The number of the input connection socket starting
	 *                          with one e.g. 1,2....
	 * @throws WrongDataTypeException
	 */
	void addInputConnection(DataInputIF input, int inputConnectionID) throws WrongDataTypeException,RecursionException;
	int getId();

}
