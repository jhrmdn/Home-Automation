package mdn.jh.automation.io.logic;

import java.io.Serializable;
import java.util.Iterator;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import mdn.jh.automation.io.DataComponentStub;
import mdn.jh.automation.io.DataConnectionIF;
import mdn.jh.automation.io.DataInputIF;
import mdn.jh.automation.io.DataOutputIF;
import mdn.jh.automation.io.DataValue;
import mdn.jh.automation.io.ComponentInputView;
import mdn.jh.automation.io.ComponentOutputView;
import mdn.jh.automation.io.RecursionCheckIF;
import mdn.jh.automation.io.RecursionException;
import mdn.jh.automation.storage.Store;



public abstract class LogicBase extends DataComponentStub
		implements DataProcessorIF, DataConnectionIF, DataOutputIF, RecursionCheckIF, DataInputIF, Serializable {

	private static final long serialVersionUID = 7085763113536443716L;
	protected Vector<DataInputConnection> myOutputs = new Vector<DataInputConnection>();
	protected int maximumNumberOfInputs = 1;
	protected DataValue myActualOutputState = null;
	private InputDefinition[] inputDefinitions = new InputDefinition[] {};
	protected DataOutputIF[] myInputValues = null;
	private boolean[] usedInputs = null;
	private static final ThreadLocal<Set<LogicBase>> VALIDITY_PATH = ThreadLocal.withInitial(HashSet::new);
	private static final ThreadLocal<Set<Integer>> RECURSION_PATH = ThreadLocal.withInitial(HashSet::new);
	private static final ThreadLocal<Set<LogicBase>> UPDATE_PATH = ThreadLocal.withInitial(HashSet::new);
	LogicComponentDescription logicComponentDescription = null;

	public LogicBase() {
		super();
	}
	
	
	public LogicBase(boolean withNewID) {
		super(withNewID);
	}
	
	public abstract int getDataTypeOutput();

	@Override
	public boolean isDataValid() {
		Set<LogicBase> path = VALIDITY_PATH.get();
		if (!path.add(this)) return false;
		try {
		if (myInputValues == null) {
			return false;
		}

		for (int i = 0; i < myInputValues.length; i++) {
			if (myInputValues[i] != null) {
				if (!myInputValues[i].isDataValid() && isInputInUse(i)) {
					return false;
				}
			}
		}
		return true;
		} finally {
			path.remove(this);
			if (path.isEmpty()) VALIDITY_PATH.remove();
		}
	}

	public String getDataTypeOutputAsString() {
		return getTypeAsString(getDataTypeOutput());

	}

	@Override
	public DataOutputIF getInputData(int inputNumber) {
		return myInputValues[inputNumber];
	}

	@Override protected synchronized void onInputOverrideChanged(int input) {
		if(input<0||input>=myInputValues.length)return;
		if(hasInputOverride(input)&&myInputValues[input]==null){DataValue value=new DataValue();value.changeDataType(inputDefinitions[input].getDataType());value.setDataValid(false);myInputValues[input]=new ComponentInputView(this,input,value);}
		else if(!hasInputOverride(input)&&!usedInputs[input])myInputValues[input]=null;
		calculateMyActualState();updateMyOutputs();
	}

	/**
	 * 
	 * @param inputDefinitions The array of input names
	 */

	public LogicBase(int dataTypeOutput, InputDefinition[] inputDefinitions,
			LogicComponentDescription logicComponentDescription) {
		this.logicComponentDescription = logicComponentDescription;
		switch (dataTypeOutput) {
		case TYPE_DOUBLE_IO:
			myActualOutputState = new DataValue(0.0);
			break;

		case TYPE_BOOLEAN_IO:
			myActualOutputState = new DataValue(false);
			break;

		default:
			myActualOutputState = new DataValue(null);
		}

		int length = 0;
		if (inputDefinitions != null) {
			length = inputDefinitions.length;
			this.inputDefinitions = inputDefinitions;
		}
		myInputValues = new DataOutputIF[length];
		usedInputs = new boolean[length];
		
		
	}

	@Override
	public void unlinkInput(int inputNumber) {
		setInputInUse(inputNumber, false);

	}
	// public abstract LogicBase(Node node);

	public void deleteThis() {
		Iterator<DataInputConnection> it = myOutputs.iterator();
		DataInputConnection temp = null;
		while (it.hasNext()) {
			temp = it.next();
			temp.getDataInput().unlinkInput(temp.getInputConnectionID());
		}
		myOutputs = null;
	}

	public LogicComponentDescription getLogicComponentDescription() {
		return logicComponentDescription;
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
		if (checkForRecursions(dataIDFromOutput)) {
			throw new RecursionException();
		}

		myInputValues[connectionInputNumber] = new ComponentInputView(this, connectionInputNumber, dataOutput);
		setInputInUse(connectionInputNumber, true);
		calculateMyActualState();
		updateOutputValidity();
		updateMyOutputs();
		return;
	}

	@Override
	public void addInputConnection(DataInputIF input, int inputConnectionID) throws RecursionException {
		if (input == null)
			return;
		updateOutputValidity();

		input.setConnection(new ComponentOutputView(this, this), getId(), inputConnectionID);
		myOutputs.add(new DataInputConnection(input, inputConnectionID));

	}

	@Override
	public void update(int connectionInputNumber) {
		Set<LogicBase> path = UPDATE_PATH.get();
		if (!path.add(this)) return;
		try {
			calculateMyActualState();
			updateOutputValidity();
			updateMyOutputs();
		} finally {
			path.remove(this);
			if (path.isEmpty()) UPDATE_PATH.remove();
		}
	}

	/** Allows a logic family to keep the value forwarded to connected inputs in sync. */
	protected void updateOutputValidity() { }

	protected void updateMyOutputs() {
		if (myOutputs == null)
			return;
		Iterator<DataInputConnection> ite = myOutputs.iterator();
		DataInputConnection temp = null;
		while (ite.hasNext()) {
			temp = ite.next();
			DataInputIF in = temp.getDataInput();
			if (in != null) {
				in.update(temp.getInputConnectionID());
				// in.setConnection(myActualOutputState, getId(), temp.getInputConnectionID());
			}
		}
		fireUpdate();
		//setMyRawValue(myActualOutputState.getOutputAsString());
	}

	public DataValue getMyState() {
		return myActualOutputState;
	}

	public Vector<DataInputConnection> getMyOutputs() {
		return myOutputs;
	}

	/**
	 * Recalculate the actual state and write to myActualOutputState
	 */
	protected abstract void calculateMyActualState();

	/**
	 * My maximum number of inputs
	 */
	public InputDefinition[] getInputDefinitions() {
		return inputDefinitions;
	}

	private void setInputInUse(int inputNumber, boolean state) {
		if (inputNumber >= usedInputs.length || inputNumber < 0) {
			return;
		}
		usedInputs[inputNumber] = state;
		if (!state) {
			myInputValues[inputNumber] = null;
		}
	}

	@Override
	public boolean isInputInUse(int inputNumber) {
		if (inputNumber >= usedInputs.length || inputNumber < 0) {
			return false;
		}
		return usedInputs[inputNumber] || hasInputOverride(inputNumber);
	}

	@Override public boolean isInputConnected(int inputNumber) { return inputNumber>=0&&inputNumber<usedInputs.length&&usedInputs[inputNumber]; }

	/**
	 * If true a recursion is detected. The input connection shall not be added.
	 * 
	 * @return
	 */
	@Override
	public boolean checkForRecursions(int idStartpoint) {
		Set<Integer> path = RECURSION_PATH.get();
		if (!path.add(getId())) return true;
		try {
		Iterator<DataInputConnection> it = myOutputs.iterator();
		DataInputConnection temp = null;
		int i = getId();
		if (i == idStartpoint) {
			return true;
		}
		while (it.hasNext()) {
			temp = it.next();
			if (temp.getDataInput().checkForRecursions(idStartpoint)) return true;
		}
		return false;
		} finally {
			path.remove(getId());
			if (path.isEmpty()) RECURSION_PATH.remove();
		}

	}

	@Override
	public void reset() {
		resetSpecific();
	}

	/**
	 * Logic unit depending reset - if necessary
	 */
	public abstract void resetSpecific();

	/** Stops transient schedulers when a complete configuration is unloaded. */
	public void shutdownRuntime() { }

	@Override
	public boolean getOutputAsBoolean() {
		if (myActualOutputState == null)
			return false;
		return myActualOutputState.getOutputAsBoolean();
	}

	@Override
	public double getOutputAsNumber() {
		if (myActualOutputState == null)
			return 0;
		return myActualOutputState.getOutputAsNumber();
	}

	@Override
	public String getOutputAsString() {
		if (myActualOutputState == null)
			return null;
		return myActualOutputState.getOutputAsString();
	}

	@Override
	public Node getStorageXML(Document doc) {
		Element rootElement = doc.createElement("LogicBase");
		rootElement.setAttribute("id", "" + getId());
		rootElement.setAttribute("class", "" + this.getClass().getCanonicalName());
		rootElement.appendChild(getDataComponentBaseXML(doc));
		Element outputs = doc.createElement("Outputs");
		Iterator<DataInputConnection> it = myOutputs.iterator();
		while (it.hasNext()) {
			outputs.appendChild(it.next().getStorageXML(doc));
		}

		rootElement.appendChild(outputs);
		return rootElement;
	}

	@Override
	public boolean initDataComponent(Node node) throws Exception {
		if (node == null)
			return false;
		String name = node.getNodeName();
		if (!"LogicBase".equals(name)) {
			return false;
		}
		String nodeID = node.getAttributes().getNamedItem("id").getNodeValue();
		overrideID(Integer.valueOf(nodeID));

		NodeList nl = node.getChildNodes();
		Node temp = null;
		for (int i = 0; i < nl.getLength(); i++) {
			temp = nl.item(i);

			if ("DataComponent".equals(temp.getNodeName())) {
				super.initDataComponent(temp);
			}

			if ("Outputs".equals(temp.getNodeName())) {
				NodeList nlOut = temp.getChildNodes();
				Node tempOut = null;
				for (int j = 0; j < nlOut.getLength(); j++) {
					tempOut = nlOut.item(j);
					if ("DataInputConnection".equals(tempOut.getNodeName())) {
						Node id = tempOut.getAttributes().getNamedItem("id");
						Node inputConnectionID = tempOut.getAttributes().getNamedItem("inputConnectionID");
						if (id == null || inputConnectionID == null) {
							throw new Exception(
									"DataInputConnection: XML-Storage: id or inputConnectionID missing. Logic Component:"
											+ logicComponentDescription.name + " ID:" + getId());
						}
						Store.addConnection(this, Integer.valueOf(id.getNodeValue()),
								Integer.valueOf(inputConnectionID.getNodeValue()));
					}
				}
			}

			/*
			 * if ("DataInputConnection".equals(temp.getNodeName())) { Node id =
			 * temp.getAttributes().getNamedItem("id"); Node inputConnectionID =
			 * temp.getAttributes().getNamedItem("inputConnectionID"); if (id == null ||
			 * inputConnectionID == null) { throw new Exception(
			 * "DataInputConnection: XML-Storage: id or inputConnectionID missing. Logic Component:"
			 * + logicComponentDescription.name + " ID:" + getId()); }
			 * Store.addConnection(this, Integer.valueOf(id.getNodeValue()),
			 * Integer.valueOf(inputConnectionID.getNodeValue())); }
			 */

		}

		return true;
	}

}
