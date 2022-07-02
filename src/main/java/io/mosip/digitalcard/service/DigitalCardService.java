package io.mosip.digitalcard.service;

import io.mosip.kernel.core.websub.model.EventModel;
/**
 * The Service DigitalCardService.
 *
 * @author Dhanendra
 */

public interface DigitalCardService {
    boolean generateDigitalCard(EventModel eventModel);

    byte[] getDigitalCard(String rid,String idHash);

    boolean createDigitalCard(EventModel eventModel);
}
