package mdn.jh.automation.devices.xml.datasource;

import java.util.Iterator;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import mdn.jh.automation.device.DataSourceHandler;
import mdn.jh.automation.devices.xml.XmlDevice;
import mdn.jh.automation.io.source.DataSource;

public class XmlDataSourceHandler extends DataSourceHandler {

	private static final long serialVersionUID = -5505172732029096173L;
	XmlDevice xmlDevice = null;

	public XmlDataSourceHandler(XmlDevice xmlDevice) {
		this.xmlDevice = xmlDevice;
	}

	public XmlDataSourceHandler(XmlDevice xmlDevice, Node node) throws Exception {
		this.xmlDevice = xmlDevice;
		initDataComponent(node);
	}

	@Override
	public void update() {
	
		
		if (xmlDevice == null)
			return;
		
		Document document = null;
		XMLDataSource dataSource = null;
		Iterator<DataSource> it = null;
		try {
			document = xmlDevice.getXMLDocument();
		} catch (Exception e) {
			it = myDataSources.iterator();
			while (it.hasNext()) {
				dataSource = (XMLDataSource) it.next();
				dataSource.setValid(false);
				dataSource.updateValue(null);
				return;
			}
		}

		it = myDataSources.iterator();
		while (it.hasNext()) {
			dataSource = (XMLDataSource) it.next();
			dataSource.setValid(true);
			dataSource.updateValue(getNodeValue(document, dataSource.getXpath()));
		}
	}

	private String getNodeValue(Document doc, String xpathString) {
		if (doc == null || xpathString == null) {
			return null;
		}
		XPathFactory xpathFactory = XPathFactory.newInstance();
		XPath xpath = xpathFactory.newXPath();

		XPathExpression expr;
		try {
			expr = xpath.compile(xpathString);
			return (String) expr.evaluate(doc, XPathConstants.STRING);
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	protected String getDataSourceDetail() {
		return "XML";
	}

}
