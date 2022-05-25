package io.mosip.kernel.pdfcardgenerator.model;


import java.util.Map;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
/**
 * Event Data Model for WebSub actual event data.
 * 
 * @author Mahammed Taheer
 * @since 1.2.1
*/

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "Model representing Credential Event Data recevied from WebSub.")
public class EventDataModel {
    
    /** 
     * The Event Data Id. 
    */
    private String id;

    /** 
     * The Event Data Transaction Id. 
    */
    private String transactionId;

    /** 
     * The Event Type. 
    */
    private EventType eventType;

    /** 
     * The Event Timestamp. 
    */
    private String timestamp;

    /** 
     * The Event Data share URI. 
    */
    private String dataShareUri;

    /** 
     * The Actual Event Data in Map. 
    */
    private Map<String, Object> data;
}
