package io.mosip.kernel.pdfcardgenerator.util;

import org.springframework.stereotype.Component;

import io.mosip.kernel.pdfcardgenerator.dto.CredentialRequestDto;
import io.mosip.kernel.pdfcardgenerator.dto.StandardRequestDto;

/**
 * Utility class to build REST API calls Request Objects. 
 * 
 * @author Mahammed Taheer
 *
 * @since 1.2.1
 */

@Component
public class PDFGeneratorRestUtils {


    public StandardRequestDto<CredentialRequestDto> buildCredentialGenRequest(String registrationId) {

        
        return null;
    }
    
}
