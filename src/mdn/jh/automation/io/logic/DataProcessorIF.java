package mdn.jh.automation.io.logic;

import mdn.jh.automation.io.DataInputIF;
import mdn.jh.automation.storage.Storeable;

public interface DataProcessorIF extends Storeable, DataInputIF {
	/**
	 * To be called if a new cycle is started
	 */
	public void reset();

	public int getId();
}
