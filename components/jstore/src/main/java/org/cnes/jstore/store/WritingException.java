package org.cnes.jstore.store;

public class WritingException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public WritingException() {
	}

	public WritingException(String message) {
		super(message);
	}

	public WritingException(Throwable cause) {
		super(cause);
	}

	public WritingException(String message, Throwable cause) {
		super(message, cause);
	}

	public WritingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
