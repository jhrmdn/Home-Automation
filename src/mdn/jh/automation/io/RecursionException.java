package mdn.jh.automation.io;

public class RecursionException extends Exception {

	private static final long serialVersionUID = 7161519553828419236L;

	public RecursionException() {
		super("Recursion in connection found");
	}

	public RecursionException(String message) {
		super(message);
	
	}

	public RecursionException(Throwable cause) {
		super(cause);
	}

	public RecursionException(String message, Throwable cause) {
		super(message, cause);
	}

	public RecursionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
