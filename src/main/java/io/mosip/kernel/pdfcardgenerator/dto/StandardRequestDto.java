package io.mosip.kernel.pdfcardgenerator.dto;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AccessLevel;

/**
 * A Standard MOSIP format Request DTO.
 * 
 * @author Mahammed Taheer
 * @since 1.2.1
*/

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "Model representing for the standard MOSIP Format.")
public class StandardRequestDto<T> {

    /**
	 * Standard Request Id
	 */
	@ApiModelProperty(value = "request id", position = 1)
	private String id;
    
	/**
	 * Standard Request version
	 */
	@ApiModelProperty(value = "request version", position = 2)
	private String version;
	
    /**
	 * Request Date Time
	 */
	@ApiModelProperty(value = "request time", position = 3)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	@Setter(AccessLevel.NONE)
	@Getter(AccessLevel.NONE)
	private Date requesttime;

	/**
	 * The Actual Request Object
	 */
	@ApiModelProperty(value = "Actual request", position = 4)
	private T request;
	
	public Date getRequesttime() {
		return requesttime!=null ? new Date(requesttime.getTime()):null;
	}

	public void setRequesttime(Date requesttime) {
		this.requesttime =requesttime!=null ? new Date(requesttime.getTime()):null;
	}
    
}
