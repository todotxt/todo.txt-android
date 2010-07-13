package com.todotxt.todotxttouch;

public class TodoException extends Exception {

	private static final long serialVersionUID = 2160630991596963352L;

	public TodoException(String msg) {
		super(msg);
	}

	public TodoException(String msg, Throwable t) {
		super(msg, t);
	}

}
