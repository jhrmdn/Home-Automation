package mdn.jh.automation.webserver;

import org.json.JSONException;
import org.json.JSONObject;

import mdn.jh.automation.io.source.DataSource;

public class DataPointSource extends DataPoint {
	//TODO - switch from DataOutputIF
	DataSource dataSource = null;	
	
	//DataComponentStub myDataProvider = null;
	
	@Override
	public JSONObject getJSONData() {

		JSONObject o = getJSONBaseData();
	//	System.out.println("DataPointSource:"+dataSource.getSourceName() );
		if(dataSource==null)return o;
		
		try {
			o.put("name", dataSource.getSourceName());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return o;
	}

	/**
	 * 
	 * @param htmlElementID    The id of the receiving HTML Element
	 * @param dataComponentID  The id of the DataComponentStub
	 * @param dataType         Type of Data taken from DataComponentStub number (1),
	 *                         bool (2) ,string(3 - default)
	 * @param connectionNumber For later use. The number of the connection
	 */
	/*
	public DataPointSource(String htmlElementID, long dataComponentID, int dataType, int connectionNumber) {
		super();
		this.htmlElementID = htmlElementID;
		this.dataComponentID = dataComponentID;
		this.dataType = dataType;
		dataProvider = Main.getMySmartHomeHandler().getDataOutputInterface(dataComponentID);
		 
		
	}
*/
	public DataPointSource(String htmlElementID, long dataComponentID, DataSource dataSource) {
		super(htmlElementID,dataComponentID,dataSource);
		fillData(dataSource);
	}
	
	private void fillData(DataSource source) {
		System.out.println("fillData: "+source);
		this.dataSource = source;
		if (source==null)return;
		
		
	}


}
