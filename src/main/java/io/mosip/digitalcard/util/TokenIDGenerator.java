package io.mosip.digitalcard.util;

import io.mosip.kernel.core.util.HMACUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigInteger;

@Component
public class TokenIDGenerator {

	@Value("${mosip.digitalcard.tokenid.uin.salt}")
	private String uinSalt;

	@Value("${mosip.kernel.tokenid.length}")
	private int tokenIDLength;

	@Value("${mosip.digitalcard.tokenid.partnercode.salt}")
	private String partnerCodeSalt;

	public String generateTokenID(String uin, String partnerCode) {
		String uinHash = HMACUtils.digestAsPlainText(HMACUtils.generateHash((uin + uinSalt).getBytes()));
		String hash = HMACUtils
				.digestAsPlainText(HMACUtils.generateHash((partnerCodeSalt + partnerCode + uinHash).getBytes()));
		return new BigInteger(hash.getBytes()).toString().substring(0, tokenIDLength);
	}

}
