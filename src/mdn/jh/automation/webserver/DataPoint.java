package mdn.jh.automation.webserver;

import org.json.JSONException;
import org.json.JSONObject;

import mdn.jh.automation.io.DataComponentStub;
import mdn.jh.automation.io.DataOutputIF;

public abstract class DataPoint {

	protected String htmlElementID = null;

	public abstract JSONObject getJSONData();

	protected long dataComponentID = -1;
	int connectionNumber = 0;
	protected int dataType = DataComponentStub.TYPE_STRING_IO;
	protected DataOutputIF dataProvider = null;

	public DataPoint() {
		super();
	}

	protected JSONObject getJSONBaseData() {

		boolean valid = true;
		JSONObject o = new JSONObject();
		if (dataProvider==null)return o;
		
		
		try {
			o.put("id", htmlElementID);

			if (dataProvider != null) {
				switch (dataType) {
				case 1:
					o.put("data", dataProvider.getOutputAsNumber());
					break;
				case 2:
					o.put("data", dataProvider.getOutputAsBoolean());
				default:
					o.put("data", dataProvider.getOutputAsString());
				}
				if (!dataProvider.isDataValid()) {
					valid = false;
				}
			} else {
				o.put("data", "");
				valid = false;
			}
			
			o.put("valid", valid);
			
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return o;
	}
	
	public DataPoint(String htmlElementID, long dataComponentID,DataOutputIF dataProvider) {
		this.htmlElementID=htmlElementID;
		this.dataComponentID=dataComponentID;
		this.dataProvider = dataProvider;
	}
}