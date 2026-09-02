package mdn.jh.automation.io.source;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JPanel;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import mdn.jh.automation.device.DataSourceHandler;
import mdn.jh.automation.io.DataComponentStub;
import mdn.jh.automation.io.DataConnectionIF;
import mdn.jh.automation.io.DataInputIF;
import mdn.jh.automation.io.DataOutputIF;
import mdn.jh.automation.io.ComponentOutputView;
import mdn.jh.automation.io.logic.DataInputConnection;
import mdn.jh.automation.storage.Store;
import mdn.jh.automation.storage.Storeable;

public abstract class DataSource extends DataComponentStub
		implements DataConnectionIF, DataOutputIF, Serializable, Storeable {

	private static final long serialVersionUID = 5065593374270701886L;
	String name = null;
	String sourceName = null;
	protected Vector<DataInputConnection> myOutputs = new Vector<DataInputConnection>();
	DataSourceHandler myDataSourceHandler = null;
//	private DataOutputIF dataOutputValue = null;

	public abstract JPanel getSpecificDetailsPanel();

	public DataSource() {
		super();
	}

	public DataSource(boolean setNewID) {
		super(setNewID);
	}

	public abstract int getDataTypeOutput();

	public DataSourceHandler getMyDataSourceDevice() {
		return myDataSourceHandler;
	}

	public void setMyDataSourceHandler(DataSourceHandler dataSourceHandler) {
		this.myDataSourceHandler = dataSourceHandler;
	}

	public String getDataTypeOutputAsString() {
		return getTypeAsString(getDataTypeOutput());

	}

	public String getSourceName() {
		return sourceName;
	}

	public void setSourceName(String sourceName) {
		this.sourceName = sourceName;
	}

	/**
	 * Returns true if the datatype cannot be changed anymore because the system has
	 * DataOutputListener
	 * 
	 * @return
	 */
	public boolean isDataTypeLocked() {
		return myUpdateListener.size() > 0;
	}

	public String getName() {
		return name;
	}

	/**
	 * The name for display
	 * 
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}

	public void deleteThis() {

		Iterator<DataInputConnection> it = myOutputs.iterator();
		DataInputConnection temp = null;
		while (it.hasNext()) {
			temp = it.next();
			temp.getDataInput().unlinkInput(temp.getInputConnectionID());
		}
		myOutputs = null;

	}

	@Override
	public void addInputConnection(DataInputIF input, int inputConnectionID) throws WrongDataTypeException {
		if (input == null)
			return;
		if (input.getInputDefinitions()[inputConnectionID].getDataType() != getDataTypeOutput()) {
			throw new WrongDataTypeException(
					"Input Type:" + input.getInputDefinitions()[inputConnectionID].getDataType() + ". My Type: "
							+ getDataTypeOutput());
		}

		try {

			// input.setConnection(dataOutputValue, getId(), inputConnectionID);
			input.setConnection(new ComponentOutputView(this, this), getId(), inputConnectionID);
		} catch (Exception e) {
			// do nothing. The input will not be connected. But shall not be possible for a
			// DataSource.
		}

		myOutputs.add(new DataInputConnection(input, inputConnectionID));
	}

	protected void updateMyOutputs() {
		fireUpdate();
		if (myOutputs == null)
			return;
		Iterator<DataInputConnection> ite = myOutputs.iterator();
		DataInputConnection temp = null;
		while (ite.hasNext()) {

			temp = ite.next();
			DataInputIF in = temp.getDataInput();
			if (in != null) {
				in.update(temp.getInputConnectionID());
			}
		}
	}

	public Vector<DataInputConnection> getMyOutputs() {
		return myOutputs;
	}

	protected Element getStorage(Document doc) {
		Element rootElement = null;
		rootElement = doc.createElement("DataSource");
		// rootElement.setAttribute("id", "" + getId());
		rootElement.setAttribute("class", this.getClass().getCanonicalName());

		rootElement.appendChild(getDataComponentBaseXML(doc));
		if (myOutputs != null) {
			Element outputs = doc.createElement("Outputs");
			Iterator<DataInputConnection> it = myOutputs.iterator();
			while (it.hasNext()) {
				outputs.appendChild(it.next().getStorageXML(doc));
			}
			rootElement.appendChild(outputs);
		}

		return rootElement;

	}

	protected boolean initDataSource(Node node) throws Exception {
		if (node == null)
			return false;
		String name = node.getNodeName();
		if (!"DataSource".equals(name)) {
			throw new Exception("Creating - Expected Node: DataSource - Found:" + name);
		}

		// String nodeID = node.getAttributes().getNamedItem("id").getNodeValue();
		// overrideID(Integer.valueOf(nodeID));

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
											+ getName() + " ID:" + getId());
						}
						Store.addConnection(this, Integer.valueOf(id.getNodeValue()),
								Integer.valueOf(inputConnectionID.getNodeValue()));

//						Store.addConnection(this, getId(),Integer.valueOf(inputConnectionID.getNodeValue()));

					}
				}
			}
		}

		return true;
	}

}
