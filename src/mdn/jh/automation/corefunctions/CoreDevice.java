package mdn.jh.automation.corefunctions;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import mdn.jh.automation.device.DataSinkHandler;
import mdn.jh.automation.device.DataSourceHandler;
import mdn.jh.automation.device.Device;
import mdn.jh.automation.gui.DataSinkCreator;
import mdn.jh.automation.gui.DataSourceCreator;

/**
 * For To keep constant datasources
 * 
 * @author jhrib
 */
public class CoreDevice extends Device {

	// private CoreDataSourceHandler dataSourceHandler = new
	// CoreDataSourceHandler();
	// private CoreDataSinkHandler dataSinkHandler = new CoreDataSinkHandler();

	public CoreDevice() {
		super(Device.TYPE_CORE);
		// TODO Auto-generated constructor stub
	}

	public CoreDevice(int type, boolean createNewDeviceID) {
		super(type, createNewDeviceID);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean initSpecific(Node node) throws Exception {
		NodeList children = node.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) if ("CoreDevice".equals(children.item(i).getNodeName())) {
			NodeList coreChildren = children.item(i).getChildNodes();
			for (int j = 0; j < coreChildren.getLength(); j++) {
				if ("DataSources".equals(coreChildren.item(j).getNodeName())) { dataSourceHandler = new CoreDataSourceHandler(); dataSourceHandler.initDataComponent(coreChildren.item(j)); }
				if ("DataSinks".equals(coreChildren.item(j).getNodeName())) { dataSinkHandler = new CoreDataSinkHandler(); dataSinkHandler.initDataComponent(coreChildren.item(j)); }
			}
		}
		return true;
	}

	@Override
	public Node getSpecificStorage(Document doc) {
		Element rootElement = null;
		rootElement = doc.createElement("CoreDevice");
		//rootElement.setAttribute("deviceID", "" + getDeviceID());
		// rootElement.appendChild(dataSourceHandler.getStorageXML(doc));
		// rootElement.appendChild(dataSinkHandler.getStorageXML(doc));

		return rootElement;
	}

	@Override
	public DataSourceHandler getDataSourceHandler() {
		if (dataSourceHandler == null) {
			dataSourceHandler = new CoreDataSourceHandler();

		}
		return dataSourceHandler;
	}

	/*
	 * @Override public void removeDataSource(DataSource dataSource) { // TODO
	 * Auto-generated method stub
	 * 
	 * }
	 */
	@Override
	public DataSourceCreator getDataSourceCreator() {
		return new CoreSourceSelector();
	}

	@Override
	public DataSinkCreator getDataSinkCreator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		return "Core";
	}

	@Override
	public DataSinkHandler getDataSinkHandler() {
		if (dataSinkHandler == null) {
			dataSinkHandler = new CoreDataSinkHandler();

		}
		return dataSinkHandler;
	}

}
