package io.mosip.kernel.pdfcardgenerator.model;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
/**
 * WebSub Push Data Model to get data from WebSub.
 * 
 * @author Mahammed Taheer
 * @since 1.2.1
*/

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "Model representing push data from WebSub.")
public class WebSubPushDataModel {
    
    /** 
     * The Websub publisher name. 
    */
    private String publisher;
    
    /** 
     * The Websub topic used for publishing data. 
    */
    private String topic;
    
    /** 
     * The Websub data published on time in String. 
    */
    private String publishedOn;

    /** 
     * The Actual published data as an event. 
    */
    private EventDataModel eventDataModel;

}
