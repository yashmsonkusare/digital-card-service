package io.mosip.kernel.pdfcardgenerator.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
/**
 * Identity Model for the registration Id & UIN Hash data.
 * 
 * @author Mahammed Taheer
 * @since 1.2.1
*/

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IdentityModel {
    

     /** 
     * The Identity Registration Id . 
    */
    private String registrationId;

    /** 
     * The Identity IdHash. 
    */
    private String idHash;
}
