package mdn.jh.automation.corefunctions;

import javax.swing.JPanel;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import mdn.jh.automation.device.DataSourceHandler;
import mdn.jh.automation.io.source.DataSource;
import mdn.jh.automation.io.DataComponentStub;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class CoreDataSource extends DataSource {

	private static final long serialVersionUID = 9015354993479294189L;
	private String valueType = "boolean";
	private String value = "false";
	private boolean valid = true;
    private Double minimum, maximum;

	public CoreDataSource() {
	}

	public CoreDataSource(String valueType, String value) throws Exception {
		configure(valueType, value);
	}

	public synchronized void configure(String type, String newValue) throws Exception {
        configure(type, newValue, minimum, maximum);
    }

    public synchronized void configure(String type, String newValue, Double newMinimum, Double newMaximum) throws Exception {
		if ("number".equals(type)) type = "float";
		if ("boolean".equals(type)) {
			if (!("true".equalsIgnoreCase(newValue) || "false".equalsIgnoreCase(newValue)))
				throw new IllegalArgumentException("Boolean value must be true or false");
			newValue = Boolean.toString(Boolean.parseBoolean(newValue));
		} else if ("integer".equals(type)) {
			newValue = Integer.toString(Integer.parseInt(newValue));
		} else if ("float".equals(type)) {
			float parsed = Float.parseFloat(newValue);
			if (!Float.isFinite(parsed)) throw new IllegalArgumentException("Float value must be finite");
			newValue = Float.toString(parsed);
		} else {
			throw new IllegalArgumentException("Unknown fixed value type: " + type);
		}
        newMinimum = numericLimit(type, newMinimum);
        newMaximum = numericLimit(type, newMaximum);
        if (newMinimum != null && newMaximum != null && newMaximum < newMinimum)
            throw new IllegalArgumentException("Maximum must be at least minimum");
        if (!"boolean".equals(type)) {
            double number = Double.parseDouble(newValue);
            if (newMinimum != null && number < newMinimum || newMaximum != null && number > newMaximum)
                throw new IllegalArgumentException("Fixed value is outside the configured limits");
        }
        minimum = newMinimum;
        maximum = newMaximum;
		valueType = type;
		value = newValue;
		valid = true;
		setStatusMessage("Fixed " + type + " value");
		updateMyOutputs();
	}

	private static Double numericLimit(String type, Double limit) {
        if (limit == null) return null;
        if ("boolean".equals(type) || !Double.isFinite(limit))
            throw new IllegalArgumentException("Limits require finite numeric values");
        if ("integer".equals(type)) {
            if (limit != Math.rint(limit) || limit < Integer.MIN_VALUE || limit > Integer.MAX_VALUE)
                throw new IllegalArgumentException("Integer limits must be 32-bit whole numbers");
            return limit;
        }
        float rounded = limit.floatValue();
        if (!Float.isFinite(rounded)) throw new IllegalArgumentException("Float limits must be finite");
        return limit;
    }

    public Double getMinimum() { return minimum; }
    public Double getMaximum() { return maximum; }

	public String getValueType() { return valueType; }
	public String getValue() { return value; }

	public CoreDataSource(DataSourceHandler dataSourceDevice) {
	//	super(dataSourceDevice);
		// TODO Auto-generated constructor stub
	}

	public CoreDataSource(Node node, DataSourceHandler dataSourceDevice) throws Exception {
	//	super(node, dataSourceDevice);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean getOutputAsBoolean() {
		return "boolean".equals(valueType) ? Boolean.parseBoolean(value) : getOutputAsNumber() != 0;
	}

	@Override
	public double getOutputAsNumber() {
		// TODO Auto-generated method stub
		return "boolean".equals(valueType) ? (getOutputAsBoolean() ? 1 : 0) : Double.parseDouble(value);
	}

	@Override
	public String getOutputAsString() {
		// TODO Auto-generated method stub
		return value;
	}

	@Override
	public Node getStorageXML(Document doc) {
		Element root = getStorage(doc);
		Element constant = doc.createElement("Constant"); constant.setAttribute("type", valueType); if (minimum != null) constant.setAttribute("minimum", minimum.toString()); if (maximum != null) constant.setAttribute("maximum", maximum.toString()); constant.appendChild(doc.createTextNode(value)); root.appendChild(constant);
		return root;
	}

	@Override
	public JPanel getSpecificDetailsPanel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getDataTypeOutput() {
		// TODO Auto-generated method stub
		return "boolean".equals(valueType) ? DataComponentStub.TYPE_BOOLEAN_IO : DataComponentStub.TYPE_DOUBLE_IO;
	}

	@Override
	public boolean isDataValid() {
		// TODO Auto-generated method stub
		return valid;
	}

	@Override
	public boolean initDataComponent(Node node) throws Exception {
		initDataSource(node);
		NodeList children = node.getChildNodes();
		for (int i=0; i<children.getLength(); i++) if ("Constant".equals(children.item(i).getNodeName())) {
			String storedType = children.item(i).getAttributes().getNamedItem("type").getNodeValue();
			Element constant = (Element) children.item(i);
            configure(storedType, constant.getTextContent(),
                constant.hasAttribute("minimum") ? Double.valueOf(constant.getAttribute("minimum")) : null,
                constant.hasAttribute("maximum") ? Double.valueOf(constant.getAttribute("maximum")) : null);
		}
		return true;
	}


}
