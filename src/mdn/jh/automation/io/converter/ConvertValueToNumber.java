package mdn.jh.automation.io.converter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import mdn.jh.automation.io.DataComponentStub;

public class ConvertValueToNumber extends Converter {

	private static final long serialVersionUID = -1685993672593819912L;
	double scaleFactorBefore = 1;
	double scaleFactorAfter = 1;
	double offsetBefore = 0;
	double offsetAfter = 0;
	double result = 0;
	boolean valid = false;

	public ConvertValueToNumber() {
		super(DataComponentStub.TYPE_DOUBLE_IO);
	}


	public double getScaleFactorBefore() {
		return scaleFactorBefore;
	}

	public double getScaleFactorAfter() {
		return scaleFactorAfter;
	}

	public double getOffsetBefore() {
		return offsetBefore;
	}

	public double getOffsetAfter() {
		return offsetAfter;
	}

	/**
	 * see getProcessedValue() / Default=1
	 * 
	 * @param scaleFactorBefore
	 */
	public void setScaleFactorBefore(double scaleFactorBefore) {
		this.scaleFactorBefore = scaleFactorBefore;
	}

	/**
	 * see getProcessedValue() / Default=1
	 * 
	 * @param scaleFactorAfter
	 */

	public void setScaleFactorAfter(double scaleFactorAfter) {
		this.scaleFactorAfter = scaleFactorAfter;
	}

	/**
	 * see getProcessedValue() / Default=0
	 * 
	 * @param offsetBefore
	 */
	public void setOffsetBefore(double offsetBefore) {
		this.offsetBefore = offsetBefore;
	}

	/**
	 * see getProcessedValue() / Default=0
	 * 
	 * @param offsetAfter
	 */

	public void setOffsetAfter(double offsetAfter) {
		this.offsetAfter = offsetAfter;
	}

	@Override
	public void setValue(String value) {
		double v = 0;
		try {
			v = Double.valueOf(value);
		} catch (NumberFormatException e) {
			result = 0;
			valid = false;
		}

		valid = true;
		result = (v * scaleFactorBefore + offsetBefore) * scaleFactorAfter + offsetAfter;
	}

	/**
	 * Returns true if value <> 0
	 */
	@Override
	public boolean getOutputAsBoolean() {
		if (result != 0)
			return true;
		return false;
	}

	/**
	 * @return (value*scaleFactorBefore+offsetBefore)*scaleFactorAfter+offsetAfter
	 */
	@Override
	public double getOutputAsNumber() {
		return result;
	}

	@Override
	public String getOutputAsString() {
		return "" + result;
	}

	@Override
	protected void initSpecific(Node node) throws Exception {
		NodeList nl = node.getChildNodes();
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

	}

	@Override
	protected Node getSpecificStorageXML(Document doc) {

		Element rootElement = null;
		rootElement = doc.createElement("Settings");

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

		return rootElement;

	}

	@Override
	public boolean isDataValid() {
		return valid;
	}

}
