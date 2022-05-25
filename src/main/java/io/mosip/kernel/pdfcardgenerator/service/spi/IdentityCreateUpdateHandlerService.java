package io.mosip.kernel.pdfcardgenerator.service.spi;

import io.mosip.kernel.pdfcardgenerator.model.WebSubPushDataModel;

/**
 * Service interface to handle the received Create/Update Event from IDRepo service. 
 * Initiates a request to credential request generator for latest credentials.
 * 
 * @author Mahammed Taheer
 *
 * @since 1.2.1
 */

public interface IdentityCreateUpdateHandlerService {
    

    /**
     * To handle the Identity Create/Update event received from WebSub.
     * 
     * @param eventDataModel the WebSub Event Data Model
     * @return The String
    */
	String handleIdentityEvent(WebSubPushDataModel eventDataModel);
}
