package mdn.jh.automation.io.converter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import mdn.jh.automation.io.DataComponentStub;

public class ConvertValueToString extends Converter {

	private static final long serialVersionUID = -1685993672593819912L;
/*
	double scaleFactorBefore = 1;
	double scaleFactorAfter = 1;
	double offsetBefore = 0;
	double offsetAfter = 0;
	*/
	
	String result = null;
	
	boolean valid = false;

	public ConvertValueToString() {
		super(DataComponentStub.TYPE_STRING_IO);
	}



	@Override
	public void setValue(String value) {
		
		valid = true;
		result = value;
	}

	/**
	 * Usually not to be used. if string value is "true" (case ignored) then true is returned otherwise false.
	 */
	@Override
	public boolean getOutputAsBoolean() {
		if(result==null)return false;
		return "true".equals(result.toLowerCase());
	}

	/**
	 * @return Tries to make a number - if not 0 is returned. Usually not to be used. Better to use converter fro number.
	 */
	@Override
	public double getOutputAsNumber() {
		if(result==null)return 0;
		try {
			return Double.valueOf(result);
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	@Override
	public String getOutputAsString() {
		return result;
	}

	@Override
	protected void initSpecific(Node node) throws Exception {
		//NodeList nl = node.getChildNodes();
		
		// TODO regular expression possible....
		/* Later on to be initialized.
		Node temp = null;
		for (int i = 0; i < nl.getLength(); i++) {
			temp = nl.item(i);
			if ("scaleFactorBefore".equals(temp.getNodeName())) {
				scaleFactorBefore = Double.valueOf(temp.getTextContent());
			}
			if ("scaleFactorAfter".equals(temp.getNodeName())) {
				scaleFactorAfter = Double.valueOf(temp.getTextContent());
			}
			if ("offsetBefore".equals(temp.getNodeName())) {
				offsetBefore = Double.valueOf(temp.getTextContent());
			}
			if ("offsetAfter".equals(temp.getNodeName())) {
				offsetAfter = Double.valueOf(temp.getTextContent());
			}
		}
		*/

	}

	@Override
	protected Node getSpecificStorageXML(Document doc) {

		Element rootElement = null;
		rootElement = doc.createElement("Settings");

		/*
		Element scaleFactorBefore = doc.createElement("scaleFactorBefore");
		Element scaleFactorAfter = doc.createElement("scaleFactorAfter");
		Element offsetBefore = doc.createElement("offsetBefore");
		Element offsetAfter = doc.createElement("offsetAfter");

		scaleFactorBefore.appendChild(doc.createTextNode(Double.toString(this.scaleFactorBefore)));
		scaleFactorAfter.appendChild(doc.createTextNode(Double.toString(this.scaleFactorAfter)));
		offsetBefore.appendChild(doc.createTextNode(Double.toString(this.offsetBefore)));
		offsetAfter.appendChild(doc.createTextNode(Double.toString(this.offsetAfter)));

		rootElement.appendChild(scaleFactorBefore);
		rootElement.appendChild(scaleFactorAfter);

		rootElement.appendChild(offsetBefore);
		rootElement.appendChild(offsetAfter);
		*/

		return rootElement;

	}

	@Override
	public boolean isDataValid() {
		return valid;
	}

}
