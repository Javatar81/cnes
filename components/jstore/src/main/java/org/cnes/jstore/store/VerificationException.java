package org.cnes.jstore.store;

import org.cnes.jstore.model.Identifier;

public class VerificationException extends Exception {

	private static final long serialVersionUID = 1L;
	private final Identifier expected;
	private final Identifier actual;

	public VerificationException(Identifier expected, Identifier actual) {
		this.expected = expected;
		this.actual = actual;
	}

	public VerificationException(Identifier expected, Identifier actual, String message) {
		super(message + " " + getCauseMessage(expected, actual));
		this.expected = expected;
		this.actual = actual;
	}

	public VerificationException(Identifier expected, Identifier actual,  Throwable cause) {
		super(cause);
		this.expected = expected;
		this.actual = actual;
	}

	public VerificationException(Identifier expected, Identifier actual,  String message, Throwable cause) {
		super(message + " " + getCauseMessage(expected, actual), cause);
		this.expected = expected;
		this.actual = actual;
	}

	public VerificationException(Identifier expected, Identifier actual, String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message + " " + getCauseMessage(expected, actual), cause, enableSuppression, writableStackTrace);
		this.expected = expected;
		this.actual = actual;
	}

	public Identifier getExpected() {
		return expected;
	}

	public Identifier getActual() {
		return actual;
	}
	
	private static String getCauseMessage(Identifier expected, Identifier actual) {
		return String.format("Expected '%s' but was '%s'", expected, actual); 
	}
}
