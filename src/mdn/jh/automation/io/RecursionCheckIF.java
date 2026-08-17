package mdn.jh.automation.io;

public interface RecursionCheckIF {

	/**
	 * Shall return true if a recursion is detected
	 * @param startID
	 * @return
	 */
	boolean checkForRecursions(int startID);

}