package io.mosip.kernel.pdfcardgenerator.helper;

import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Component;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.pdfcardgenerator.constant.PdfCardGenConstants;
import io.mosip.kernel.pdfcardgenerator.constant.PdfCardGenErrorCodes;
import io.mosip.kernel.pdfcardgenerator.exception.PdfCardWebSubException;
import io.mosip.kernel.pdfcardgenerator.logger.PdfCardGeneratorLogger;
import io.mosip.kernel.pdfcardgenerator.model.EventDataModel;
import io.mosip.kernel.pdfcardgenerator.model.IdentityModel;

/**
 * Helper class to read the WebSub Event and return data based on datashare URI or direct data in the event. 
 * 
 * @author Mahammed Taheer
 *
 * @since 1.2.1
 */

@Component
public class WebSubEventDataHelper {
    
    private static final Logger LOGGER = PdfCardGeneratorLogger.getLogger(WebSubEventDataHelper.class);

    public IdentityModel getRegistrationIdFromEventData(EventDataModel eventData) {
        
        LOGGER.info(PdfCardGenConstants.SESSION_ID, PdfCardGenConstants.WEBSUB_HELPER, "", 
                "Reading Registration Id from websub event data.");
        Map<String, Object> eventDataMap = eventData.getData();
        
        String registrationId = (String) eventDataMap.get(PdfCardGenConstants.REGISTRATION_ID);

        if(Objects.isNull(registrationId) || registrationId.trim().length() == 0){
            LOGGER.error(PdfCardGenConstants.SESSION_ID, PdfCardGenConstants.WEBSUB_HELPER, "", 
            "Registration Id Not found in the Websub event data.");
            throw new PdfCardWebSubException(PdfCardGenErrorCodes.REGISTRATION_ID_NOT_FOUND.getErrorCode(), 
						PdfCardGenErrorCodes.REGISTRATION_ID_NOT_FOUND.getErrorMessage());
        }

        String idHash = (String) eventDataMap.get(PdfCardGenConstants.ID_HASH);

        if(Objects.isNull(idHash) || idHash.trim().length() == 0){
            LOGGER.error(PdfCardGenConstants.SESSION_ID, PdfCardGenConstants.WEBSUB_HELPER, "", 
            "Id Hash Not found in the Websub event data.");
            throw new PdfCardWebSubException(PdfCardGenErrorCodes.ID_HASH_NOT_FOUND.getErrorCode(), 
						PdfCardGenErrorCodes.ID_HASH_NOT_FOUND.getErrorMessage());
        }

        return new IdentityModel(registrationId, idHash);
    }
}
