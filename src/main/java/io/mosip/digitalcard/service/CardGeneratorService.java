package io.mosip.digitalcard.service;


import org.json.JSONObject;

public interface CardGeneratorService {

	/**
	 * The PDFCardService
	 *
	 * @param credential
	 * @param credentialType
	 * @param requestId
	 * @param isPasswordProtected
	 * @return
	 */
	public byte[] generateCard(JSONObject decryptedCredentialJson, String credentialType, String requestId, boolean isPasswordProtected);

}