package io.mosip.digitalcard.service;

import io.mosip.digitalcard.dto.DigitalCardStatusResponseDto;
import io.mosip.kernel.core.websub.model.EventModel;

import java.util.Map;

/**
 * The Service DigitalCardService.
 *
 * @author Dhanendra
 */

public interface DigitalCardService {
    void generateDigitalCard(String credential, String credentialType, String dataShareUrl, String eventId, String transactionId, Map<String,Object> additionalAttribute);

    DigitalCardStatusResponseDto getDigitalCard(String rid);

    void initiateCredentialRequest(String rid,String ridHash);
}
