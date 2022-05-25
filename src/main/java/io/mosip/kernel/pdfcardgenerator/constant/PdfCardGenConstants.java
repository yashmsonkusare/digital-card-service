package io.mosip.kernel.pdfcardgenerator.constant;

/**
 * Constants for PDF Card Generator Service.
 * 
 * @author Mahammed Taheer
 * @since 1.2.1
 *
 */
public interface PdfCardGenConstants {


	String SESSION_ID = "PdfCardSessionId";
    
	/**
	 * The constant INVALID_REQUEST
	 */
	String INVALID_REQUEST = "Input Data should not be null or empty";

	String SUBSCRIBE = "subscribe";

	String REGISTER = "register";

	String WEBSUB_HELPER = "WebSubHelper";

	String IDCU_SUCCESS_RESPONSE = "Received Event and Successfully Requested Credentials.";

	String REGISTRATION_ID = "registration_id";

	String ID_HASH = "id_hash";

	String IDCU_SERVICE_IMPL = "IDCUServiceImpl";

	String REST_HELPER = "RestHelper";
    
}
