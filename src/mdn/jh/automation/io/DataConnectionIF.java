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
    default void addInputConnection(DataInputIF input, int inputIndex, int outputIndex) throws WrongDataTypeException, RecursionException {
        if (outputIndex != 0) throw new IllegalArgumentException("Invalid output index");
        addInputConnection(input, inputIndex);
    }
    default int getOutputCount() { return 1; }
    default String getOutputName(int index) { return "Output"; }
    default DataOutputIF getOutputPort(int index) {
        if (index != 0) throw new IllegalArgumentException("Invalid output index");
        return new ComponentOutputView((DataComponentStub)this, (DataOutputIF)this);
    }
	int getId();

}
