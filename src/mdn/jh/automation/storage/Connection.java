package mdn.jh.automation.storage;

import mdn.jh.automation.io.DataConnectionIF;

public class Connection {

	DataConnectionIF source;
	int targetID;
	int targetInputID;
    private int outputIndex;

	public Connection(DataConnectionIF source, int targetID, int targetInputID) {
		this.targetID = targetID;
		this.targetInputID = targetInputID;
		this.source = source;
	}

    public Connection(DataConnectionIF source, int targetID, int targetInputID, int outputIndex) {
        this(source, targetID, targetInputID); this.outputIndex = outputIndex;
    }
    public int getOutputIndex() { return outputIndex; }
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
