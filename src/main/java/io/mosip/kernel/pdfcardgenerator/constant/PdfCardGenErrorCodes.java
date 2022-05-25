package io.mosip.kernel.pdfcardgenerator.constant;

public enum PdfCardGenErrorCodes {
    
    SUBSCRIBE_ERROR("KER-PCG-001", "Not able to do subscribe for the event."),
	REGISTRATION_ID_NOT_FOUND("KER-PCG-002", "Registration Id not found in the event data."),
	ID_HASH_NOT_FOUND("KER-PCG-003", "Id Hash not found in the event data.");

    /**
	 * The error code
	 */
	private final String errorCode;
	/**
	 * The error message
	 */
	private final String errorMessage;

	/**
	 * Constructor to set error code and message
	 * 
	 * @param errorCode    the error code
	 * @param errorMessage the error message
	 */
	private PdfCardGenErrorCodes(final String errorCode, final String errorMessage) {
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
	}

	/**
	 * Function to get error code
	 * 
	 * @return {@link #errorCode}
	 */
	public String getErrorCode() {
		return errorCode;
	}

	/**
	 * Function to get the error message
	 * 
	 * @return {@link #errorMessage}
	 */
	public String getErrorMessage() {
		return errorMessage;
	}
}
