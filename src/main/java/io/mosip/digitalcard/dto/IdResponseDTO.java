package io.mosip.digitalcard.dto;

import io.mosip.kernel.core.http.ResponseWrapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class IdResponseDTO extends ResponseWrapper<IdentityResponseDTO> {

}