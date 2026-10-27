package mdn.jh.automation.webserver;

import org.json.JSONException;
import org.json.JSONObject;

import mdn.jh.automation.io.sink.DataSink;

public class DataPointSink extends DataPoint {
	//TODO - switch from DataOutputIF
	
	DataSink dataSink =null;
	

	@Override
	public JSONObject getJSONData() {
		
		JSONObject o = getJSONBaseData();
		if(dataSink==null)return o;
		
		try {
			o.put("name", dataSink.getName());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return o;
	}


	
	/**
	 * 
	 * @param htmlElementID    The id of the receiving HTML Element
	 * @param dataComponentID  The id of the DataComponentStub
	 *                         bool (2) ,string(3 - default)
	 * @param dataSink 			The underlying datasink
	 * dataType         Type of Data taken from DataComponentStub number (1),
	 */

	public DataPointSink(String htmlElementID, long dataComponentID, DataSink dataSink) {
		super(htmlElementID,dataComponentID,dataSink.getOutputDataValue());
		fillData(dataSink);
	}

	private void fillData(DataSink sink) {
		this.dataSink = sink;
		if (sink==null)return;
		
		//this.dataType=sink.get
		
	}
	
	
	/*
	public DataPointSink(String htmlElementID, long dataComponentID, int dataType) {
		this(htmlElementID, dataComponentID, dataType, 0);
	}
	public DataPointSink(String htmlElementID, long dataComponentID) {
		this(htmlElementID, dataComponentID, 3, 0);
	}*/
	

}
