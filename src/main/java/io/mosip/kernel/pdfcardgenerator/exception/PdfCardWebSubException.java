package io.mosip.kernel.pdfcardgenerator.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;

/**
 * Custom Exception Class to handle WebSub related exception in PDF Card Generation.
 * 
 * @author Mahammed Taheer
 * @since 1.2.1
 *
 */

public class PdfCardWebSubException extends BaseUncheckedException{
    /**
	 * Generated serial version id
	 */
	private static final long serialVersionUID = 8621530697947108810L;

	/**
	 * Constructor the initialize Handler exception
	 * 
	 * @param errorCode    The errorcode for this exception
	 * @param errorMessage The error message for this exception
	 */
	public PdfCardWebSubException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
	}

	/**
	 * @param errorCode    The errorcode for this exception
	 * @param errorMessage The error message for this exception
	 * @param rootCause    cause of the error occoured
	 */
	public PdfCardWebSubException(String errorCode, String errorMessage, Throwable rootCause) {
		super(errorCode, errorMessage, rootCause);
	}

    /**
	 * Instantiates a new PDF Card WebSub exception.
	 *
	 * @param rootCause the root cause
	 */
	public PdfCardWebSubException(BaseUncheckedException rootCause) {
		this(rootCause.getErrorCode(), rootCause.getErrorText(), rootCause);
	}

}
