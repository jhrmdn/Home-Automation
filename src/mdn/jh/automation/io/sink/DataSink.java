package mdn.jh.automation.io.sink;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import mdn.jh.automation.io.DataComponentStub;
import mdn.jh.automation.io.DataInputIF;
import mdn.jh.automation.io.DataOutputIF;
import mdn.jh.automation.io.DataValue;
import mdn.jh.automation.io.RecursionException;
import mdn.jh.automation.io.logic.InputDefinition;
import mdn.jh.automation.storage.Storeable;

public abstract class DataSink extends DataComponentStub implements DataInputIF, Storeable {

	private static final long serialVersionUID = 8219362314589570292L;

	private InputDefinition[] inputDefinitions = null;
	private boolean[] usedInputs = null;
	protected DataOutputIF[] myInputValues = null;
	String sinkName = null;
	String outputValue = null;
	protected DataValue myOutput = new DataValue();

	/*
	public String getOutputValue() {
		return outputValue;
	}
*/
	public DataOutputIF getOutputDataValue() {
		return myOutput;
	}

	/*
	protected void setOutputValue(String outputValue) {
		this.outputValue = outputValue;
	}
	*/

	protected DataSink(InputDefinition[] inputDefinitions) {
		super();
		defineInputs(inputDefinitions);
	}

	/** Allows metadata-driven sinks to restore their input list from storage. */
	protected final void defineInputs(InputDefinition[] inputDefinitions) {
		int length = 0;
		if (inputDefinitions != null) {
			length = inputDefinitions.length;
			this.inputDefinitions = inputDefinitions;
		} else {
			this.inputDefinitions = new InputDefinition[] {};
		}
		myInputValues = new DataOutputIF[length];
		usedInputs = new boolean[length];
	}

	public abstract void updateCycle();

	/**
	 * Will be called after each update cycle of the inputs. Then the actual input
	 * value can be used to trigger an output, action, or other....
	 */
	public void executeActions() {
		updateCycle();
		fireUpdate();

	}

	public String getSinkName() {
		return sinkName;
	}

	public void setSinkName(String sinkName) {
		this.sinkName = sinkName;
	}

	private void setInputInUse(int inputNumber, boolean state) {
		if (inputNumber > usedInputs.length || inputNumber < 0) {
			return;
		}
		usedInputs[inputNumber] = state;
		if (!state) {
			myInputValues[inputNumber] = null;
		}
	}

	@Override
	public boolean isInputInUse(int inputNumber) {
		if (inputNumber > usedInputs.length || inputNumber < 0) {
			return false;
		}
		return usedInputs[inputNumber];
	}

	@Override
	public void unlinkInput(int inputNumber) {
		setInputInUse(inputNumber, false);
	}

	/**
	 * @return if the value could be set successful
	 * @throws RecursionException
	 */
	@Override
	public void setConnection(DataOutputIF dataOutput, int dataIDFromOutput, int connectionInputNumber)
			throws RecursionException {
		if (getInputDefinitions() == null)
			return;
		if (connectionInputNumber < 0 || connectionInputNumber > getInputDefinitions().length) {
			return;
		}
		// if (checkForRecursions(dataIDFromOutput)) {
		// throw new RecursionException();
		// }

		myInputValues[connectionInputNumber] = dataOutput;
		setInputInUse(connectionInputNumber, true);
		return;
	}

	@Override
	public boolean checkForRecursions(int startID) {
		return false;
	}

	@Override
	public void update(int connectionInputNumber) {
		// First do nothing here. Usually all is done when updateCycleEnd() is called
	}

	public InputDefinition[] getInputDefinitions() {
		return inputDefinitions;
	}

	@Override
	public DataOutputIF getInputData(int inputNumber) {
		return myInputValues[inputNumber];
	}

	public abstract void initSpecific(Node node);

	protected Element getStorageDataSink(Document doc) {
		Element rootElement = null;
		rootElement = doc.createElement("DataSink");
		rootElement.setAttribute("id", "" + getId());
		rootElement.setAttribute("class", this.getClass().getCanonicalName());

		rootElement.appendChild(getDataComponentBaseXML(doc));
		return rootElement;
	}

}
