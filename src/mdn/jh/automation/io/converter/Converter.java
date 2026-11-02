package mdn.jh.automation.io.converter;

import java.io.Serializable;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import mdn.jh.automation.io.ConverterIF;
import mdn.jh.automation.io.DataComponentStub;
import mdn.jh.automation.storage.Storeable;

public abstract class Converter implements ConverterIF, Serializable, Storeable {
	private static final long serialVersionUID = 7049291257914828243L;
	private int dataType = DataComponentStub.TYPE_NOT_SET;

	/**
	 * Datatype as defined in class "Data"
	 * 
	 * @param dataType
	 */
	public Converter(int dataType) {
		this.dataType = dataType;
	}

	public Converter(Node node) {
		
	}

	@Override
	public int getDataTypeOutput() {
		return dataType;
	}

	@Override
	public String toString() {
		return this.getClass().getName();
	}

	@Override
	public boolean initDataComponent(Node node) throws Exception {
		if (node == null)
			return false;
		String name = node.getNodeName();
		if (!"DataConverter".equals(name)) {
			throw new Exception("Creating - Expected Node: DataConverter - Found:" + name);
		}
		initSpecific(node);
		
		
		
		return true;
	}

	protected abstract void initSpecific(Node node) throws Exception;

	protected abstract Node getSpecificStorageXML(Document doc);

	@Override
	public Node getStorageXML(Document doc) {
		Element rootElement = null;
		rootElement = doc.createElement("DataConverter");
		rootElement.setAttribute("class", "" + this.getClass().getCanonicalName());
		// rootElement.setAttribute("dataType", "" + "" + dataType);
		rootElement.appendChild(getSpecificStorageXML(doc));
		return rootElement;
	}

}
