package mdn.jh.automation.io.converter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import mdn.jh.automation.io.DataComponentStub;

public class ConvertValueToBoolean extends Converter {

	private static final long serialVersionUID = 4249270801660464745L;
	double decisionLimit = 1;
	boolean result = false;
	int decisionType = 1;
	boolean valid = false;

	public ConvertValueToBoolean() {
		super(DataComponentStub.TYPE_BOOLEAN_IO);
	}

	public double getDecisionLimit() {
		return decisionLimit;
	}

	public void setDecisionLimit(double decisionLimit) {
		this.decisionLimit = decisionLimit;
	}

	public int getDecisionType() {
		return decisionType;
	}

	/**
	 * 1=MoreEqual 2=More 3=LessEqual 4=Less
	 * 
	 */
	public void setDecisionType(int decisionType) {
		if (decisionType < 1 || decisionType > 4) {
			return;
		}
		this.decisionType = decisionType;
	}

	/**
	 * Value NULL=false; Value not number = false; Value number: acc. to decision -
	 * true acc. to decision or if the string is "true" (case insensitive)
	 */
	@Override
	public void setValue(String value) {
		if (value == null) {
			result = false;
			return;
		}

		if (value.toLowerCase().equals("true")) {
			result = true;
			valid = true;
			return;
		}

		double v = 0;
		try {
			v = Double.valueOf(value);
			valid = true;
		} catch (NumberFormatException e) {
			result = false;
			valid = false;
			return;
		}
		switch (decisionType) {
		case 1:
			result = v >= decisionLimit;
			break;
		case 2:
			result = v > decisionLimit;
			break;
		case 3:
			result = v <= decisionLimit;
			break;
		case 4:
			result = v < decisionLimit;
			break;
		}

	}

	@Override
	public String getOutputAsString() {

		if (result) {
			return "true";
		}
		return "false";
	}

	@Override
	public boolean getOutputAsBoolean() {
		return result;
	}

	@Override
	public double getOutputAsNumber() {

		if (result)
			return 1;
		else
			return 0;
	}

	@Override
	protected void initSpecific(Node node) throws Exception {
		NodeList nl = node.getChildNodes();
		Node temp = null;
		for (int i = 0; i < nl.getLength(); i++) {
			temp = nl.item(i);
			if ("decisionLimit".equals(temp.getNodeName())) {
				decisionLimit = Double.valueOf(temp.getTextContent());
			}
			if ("decisionType".equals(temp.getNodeName())) {
				decisionType = Integer.valueOf(temp.getTextContent());
			}
		}
	}

	@Override
	protected Node getSpecificStorageXML(Document doc) {
		Element rootElement = null;
		rootElement = doc.createElement("Settings");
		Element decisionLimit = doc.createElement("decisionLimit");
		Element decisionType = doc.createElement("decisionType");
		decisionLimit.appendChild(doc.createTextNode(Double.toString(this.decisionLimit)));
		decisionType.appendChild(doc.createTextNode(Integer.toString(this.decisionType)));
		rootElement.appendChild(decisionLimit);
		rootElement.appendChild(decisionType);

		return rootElement;
	}

	@Override
	public boolean isDataValid() {
		return valid;
	}

}
