package io.mosip.kernel.pdfcardgenerator.helper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.pdfcardgenerator.constant.PdfCardGenConstants;
import io.mosip.kernel.pdfcardgenerator.logger.PdfCardGeneratorLogger;

/**
 * Helper class to send REST API calls to the required services. 
 * 
 * @author Mahammed Taheer
 *
 * @since 1.2.1
 */

@Component
public class RestHelper {

    private static final Logger LOGGER = PdfCardGeneratorLogger.getLogger(WebSubEventDataHelper.class);

    /**
	 * Reference for ${regCenter.url} from property file
	*/
	@Value("${mosip.pdfcard.generator.credential.generator.uri}")
	String credGenReqUri;
    
    @Qualifier("selfTokenWebClient")
    @Autowired
    private WebClient webClient; 
    
    public String sendCredentialGeneratorRequest(String registrationId) {

        LOGGER.info(PdfCardGenConstants.SESSION_ID, PdfCardGenConstants.REST_HELPER, "", 
                    "Send Request for Credential Generation Request. URI Configured: " + credGenReqUri);

    }

    private 
    
}
