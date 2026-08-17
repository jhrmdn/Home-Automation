package mdn.jh.automation.corefunctions;

import java.util.Iterator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import mdn.jh.automation.device.DataSourceHandler;
import mdn.jh.automation.io.source.DataSource;

public class CoreDataSourceHandler extends DataSourceHandler {

	private static final long serialVersionUID = -1135047597913765042L;

	@Override
	public boolean initDataComponent(Node node) throws Exception {
		return super.initDataComponent(node);
	}

	@Override
	public Node getStorageXML(Document doc) {
		Element rootElement = null;
		rootElement = doc.createElement("DataSources");
		// rootElement.setAttribute("type", "" + getDataSourceDeviceType());

		Iterator<DataSource> it = myDataSources.iterator();
		while (it.hasNext()) {
			rootElement.appendChild(it.next().getStorageXML(doc));
		}
		return rootElement;
	}

	@Override
	public void update() {
		// TODO Auto-generated method stub

	}

	@Override
	protected String getDataSourceDetail() {
		// TODO Auto-generated method stub
		return null;
	}

}
