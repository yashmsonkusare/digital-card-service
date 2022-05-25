package io.mosip.kernel.pdfcardgenerator.dto;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Credentials Request DTO.
 * 
 * @author Mahammed Taheer
 * @since 1.2.1
*/

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "Model representing credential request.")
public class CredentialRequestDto {
    
    /**
	 * Identity Registration Id
	*/
    private String regId;

    /**
	 * The Credential Type
	*/
    private String credentialType;

    /**
	 * The Issuer of credentials
	*/
	private String issuer;
    
    /**
	 * The Recepiant of credentials
	*/
    private String  recepiant;
	
    /**
	 * The user of credentials
	*/
    private String user;
	
    /**
	 * Attribute encryption flag
	*/
    private boolean encrypt;
	
    /**
	 * The encryption key to encrypt the attributes.
	*/
    private String encryptionKey;
}
