package mdn.jh.automation.webserver;

import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import mdn.jh.automation.Main;

public class DataProviderSession {
	private static GregorianCalendar cal = null;

	// TODO Improve for secure session ids
	private static synchronized long createSessionID() {
		cal = new GregorianCalendar();
		long sessionid = cal.getTimeInMillis();
		try {
			Thread.sleep(1);
		} catch (Exception e) {
			// TODO: handle exception
		}
		return sessionid;

	}

	private long sessionID;
	// Session Timeout in msec
	private long sessionTimeout = 120000;
	private long lastSessionCall = 0;

	public long getSessionID() {
		return sessionID;
	}

	private Vector<DataPoint> dataPoints = new Vector<DataPoint>();

	public JSONObject getData() {
		JSONObject json = new JSONObject();
		lastSessionCall = cal.getTimeInMillis();
		System.out.println("GetJSONData: " + lastSessionCall);
		return json;
	}

	public String getDataupdateAsJSON() {
		JSONArray arr = new JSONArray();
		Iterator<DataPoint> it = dataPoints.iterator();
		JSONObject temp = null;
		while (it.hasNext()) {
			temp = it.next().getJSONData();
			if (temp != null)
				arr.put(temp);
		}
		return arr.toString();
	}



	//TODO - Store List of DatasetIDs as xml
	public void loadDataSet(int datasetID) {
		
		
		DataPoint p = Main.getMySmartHomeHandler().getDataPoint(23, "1"); 
		dataPoints.add(p);
		
		p = Main.getMySmartHomeHandler().getDataPoint(25, "2"); 
		dataPoints.add(p);
	/*
		p = Main.getMySmartHomeHandler().getDataPoint(25, "3"); 
		dataPoints.add(p);
		*/
		
	}
	
	public String getDataModelAsJSON() {
		JSONArray arr = new JSONArray();
		Iterator<DataPoint> it = dataPoints.iterator();
		JSONObject temp = null;
		while (it.hasNext()) {
			temp = it.next().getJSONData();
			if (temp != null)
				arr.put(temp);
		}
		
		JSONObject jsonObject= null;
		jsonObject=new JSONObject();
		try {
			jsonObject.append("DataPointList", arr);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return jsonObject.toString();
	}

	
	
	
	
	/**
	 * 
	 * @param jsonRequest The request from javascript webpage with the datapoints
	 *                    (html-element ids) and belonging datapoints from server
	 *                    (DataOutputIF /Input). Generated in display.js
	 */
	public DataProviderSession() {

		/*
		try {

			JSONArray jsonArray = jsonRequest.getJSONArray("datarequest");
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject o = jsonArray.getJSONObject(i);
				// String id = o.getString(getJSONData());

				long dataComponentID = o.getLong("dc");
				String htmlComponentID = o.getString("id");
				int dataType = o.getInt("type");
				DataPoint p = new DataPoint(htmlComponentID, dataComponentID, dataType);
				dataPoints.add(p);
				System.out.println("Create DataPoint:" + jsonArray.getJSONObject(i));
			}
			

		} catch (Exception e) {
			//System.out.println("Error Parse Request");
			//e.printStackTrace();
			//sessionID = -1;
			//return;
		}
		*/
		// TODO
		loadDataSet(0);
		sessionID = createSessionID();
		//System.out.println("Create Session:"+sessionID);
	}

}
