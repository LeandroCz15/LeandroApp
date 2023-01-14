package io.imagekit.sdk.exceptions;

import io.imagekit.sdk.models.ResponseMetaData;

public class ConflictException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String message;
	private String help;
	private ResponseMetaData responseMetaData;

	public ConflictException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace,
			String message1, String help, ResponseMetaData responseMetaData) {
		super(message, cause, enableSuppression, writableStackTrace);
		this.message = message1;
		this.help = help;
		this.responseMetaData = responseMetaData;
	}

	@Override
	public String toString() {
		return "ConflictException{" + "message='" + message + '\'' + ", help='" + help + '\'' + ", responseMetaData="
				+ responseMetaData + '}';
	}
}
