package io.mosip.digitalcard.service;

import io.mosip.kernel.core.websub.model.EventModel;

public interface PDFCardService {
    
	/**
	 * Get the card
	 * 
	 * 
	 * @param eventModel
	 * @return
	 * @throws Exception
	 */
	public boolean generateCard(EventModel eventModel) throws Exception;

	// Map<String, byte[]> getDocuments(String credentialSubject, String sign,
	// String cardType,
	// boolean isPasswordProtected);
	
}