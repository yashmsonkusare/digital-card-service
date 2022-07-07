package io.mosip.digitalcard.service;

import io.mosip.digitalcard.dto.DigitalCardStatusResponseDto;
import io.mosip.kernel.core.websub.model.EventModel;
/**
 * The Service DigitalCardService.
 *
 * @author Dhanendra
 */

public interface DigitalCardService {
    boolean generateDigitalCard(String credential, String credentialType,String dataShareUrl,String eventId,String transactionId);

    DigitalCardStatusResponseDto getDigitalCard(String rid);

    boolean initiateCredentialRequest(EventModel eventModel);
}
