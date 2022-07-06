package io.mosip.digitalcard.service;

import io.mosip.digitalcard.constant.UinCardType;
import io.mosip.kernel.core.websub.model.EventModel;

public interface PDFCardService {

	/**
	 *  The PDFCardService
	 * @param credential
	 * @param credentialType
	 * @param requestId
	 * @param cardType
	 * @param isPasswordProtected
	 * @return
	 */
	public boolean generatePDFCard(String credential, String credentialType, String requestId, UinCardType cardType, boolean isPasswordProtected);

}