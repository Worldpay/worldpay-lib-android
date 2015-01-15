package com.worldpay;

import java.io.Serializable;

/**
 * Contains library generic errors
 * 
 */
public class WorldpayError implements Serializable {
	private static final long serialVersionUID = 3929710223879734507L;

	/**
	 * Generic unxpected error
	 */
	public final static int ERROR_LIBRARY_UNEXPECTED = 1;

	/**
	 * The device seems that does not have network.
	 */
	public final static int ERROR_NO_NETWORK = 10;

	/**
	 * A json error occured while creating json for the request
	 */
	public final static int ERROR_CREATING_REQUEST_JSON = 100;

	public final static int ERROR_RESPONSE_UNKNOWN = 200;
	/**
	 * Connection error while receiving response
	 */
	public final static int ERROR_RESPONSE_CONNECTION = 201;
	
	/**
	 * Json error while trying to parse json response from error.
	 */
	public final static int ERROR_RESPONSE_MALFORMED_JSON = 202;

	private int code = 0;
	private String message;

	protected WorldpayError(int code, String message) {
		setError(code, message);
	}

	protected WorldpayError() {
	}

	public int getCode() {
		return code;
	}

	protected void setCode(int code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	protected void setMessage(String message) {
		this.message = message;
	}

	protected void setError(int code, String message) {
		this.code = code;
		this.message = message;
	}

}
