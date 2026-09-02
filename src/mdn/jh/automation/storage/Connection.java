package mdn.jh.automation.storage;

import mdn.jh.automation.io.DataConnectionIF;

public class Connection {

	DataConnectionIF source;
	int targetID;
	int targetInputID;

	public Connection(DataConnectionIF source, int targetID, int targetInputID) {
		this.targetID = targetID;
		this.targetInputID = targetInputID;
		this.source = source;
	}

	public DataConnectionIF getSource() {
		return source;
	}

	public int getTarget() {
		return targetID;
	}

	public int getTargetInputID() {
		return targetInputID;
	}

}
