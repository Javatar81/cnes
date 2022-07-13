package org.cnes.jstore.store;

public class ReadingException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ReadingException() {
		
	}

	public ReadingException(String message) {
		super(message);
	}

	public ReadingException(Throwable cause) {
		super(cause);
	}

	public ReadingException(String message, Throwable cause) {
		super(message, cause);
	}

	public ReadingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
