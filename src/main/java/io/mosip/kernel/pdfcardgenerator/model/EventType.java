package io.mosip.kernel.pdfcardgenerator.model;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Credential Data Type.
 * 
 * @author Mahammed Taheer
 * @since 1.2.1
*/

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "Model representing Credential Event Type.")
public class EventType {
    
    /** 
     * The Credential Type namespace. 
    */
    private String namespace;

    /** 
     * The Credential Type name. 
    */
    private String name;
}
