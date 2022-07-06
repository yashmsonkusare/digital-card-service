package io.mosip.digitalcard.service;

import io.mosip.digitalcard.dto.DigitalCardStatusResponseDto;
import io.mosip.kernel.core.websub.model.EventModel;
/**
 * The Service DigitalCardService.
 *
 * @author Dhanendra
 */

public interface DigitalCardService {
    boolean generateDigitalCard(EventModel eventModel);

    DigitalCardStatusResponseDto getDigitalCard(String rid);

    boolean initiateCredentialRequest(EventModel eventModel);
}
