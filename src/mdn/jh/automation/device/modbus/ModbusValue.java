package mdn.jh.automation.device.modbus;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ModbusValue {

	protected int start = -1;
	protected boolean valid = false;
	protected long timestamp = 0;
	protected String timestamp_string=null;

	public String getTimestamp_string() {
		return timestamp_string;
	}

	private void setTimestamp_string(String timestamp_string) {
		this.timestamp_string = timestamp_string;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
		SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss_SSS");
		setTimestamp_string(format.format( new Date(timestamp)));
	}

	public ModbusValue() {
		super();
		
	}

	public long getTimestamp() {
		return timestamp;
	}

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public boolean isValid() {
		return valid;
	}

	public void setValid(boolean valid) {
		this.valid = valid;
	}

}