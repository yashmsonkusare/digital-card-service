package io.mosip.kernel.pdfcardgenerator.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.mosip.kernel.pdfcardgenerator.exception.PdfCardWebSubException;
import io.mosip.kernel.pdfcardgenerator.helper.WebSubEventDataHelper;
import io.mosip.kernel.pdfcardgenerator.model.EventDataModel;
import io.mosip.kernel.pdfcardgenerator.model.IdentityModel;
import io.mosip.kernel.pdfcardgenerator.model.WebSubPushDataModel;
import io.mosip.kernel.pdfcardgenerator.service.spi.IdentityCreateUpdateHandlerService;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.pdfcardgenerator.constant.PdfCardGenConstants;
import io.mosip.kernel.pdfcardgenerator.constant.PdfCardGenErrorCodes;
import io.mosip.kernel.pdfcardgenerator.exception.PdfCardWebSubException;
import io.mosip.kernel.pdfcardgenerator.logger.PdfCardGeneratorLogger;

/**
 * Service implementation to handle the received Create/Update Event from IDRepo service. 
 * Initiates a request to credential request generator for latest credentials.
 * 
 * @author Mahammed Taheer
 *
 * @since 1.2.1
 */

@Service
public class IdentityCreateUpdateHandlerServiceImpl implements IdentityCreateUpdateHandlerService {

    private static final Logger LOGGER = PdfCardGeneratorLogger.getLogger(WebSubEventDataHelper.class);

    @Autowired
    private WebSubEventDataHelper webSubEventDataHelper;


    /*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.keymanager.service.IdentityCreateUpdateHandlerService#handleIdentityEvent(io.mosip.
     * kernel.pdfcardgenerator.model.WebSubPushDataModel)
	 */
    @Override
    public String handleIdentityEvent(WebSubPushDataModel wsEventDataModel) {
        
        try {

            LOGGER.info(PdfCardGenConstants.SESSION_ID, PdfCardGenConstants.IDCU_SERVICE_IMPL, "", 
                    "Start Identity Create/Update Websub event data processing.");

            EventDataModel eventData = wsEventDataModel.getEventDataModel();
            IdentityModel identityModel = webSubEventDataHelper.getRegistrationIdFromEventData(eventData);

            

        } catch(PdfCardWebSubException websubException) {

        }
        return null;
    }
    
}
