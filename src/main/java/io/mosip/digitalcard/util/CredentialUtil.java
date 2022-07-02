package io.mosip.digitalcard.util;

import io.mosip.digitalcard.constant.ApiName;
import io.mosip.digitalcard.dto.CredentialRequestDto;
import io.mosip.digitalcard.dto.CredentialResponse;
import io.mosip.digitalcard.dto.CredentialResponseDto;
import io.mosip.digitalcard.dto.CredentialStatusResponse;
import io.mosip.digitalcard.exception.ApisResourceAccessException;
import io.mosip.digitalcard.exception.DigitalCardServiceException;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class CredentialUtil {

    @Autowired
    private RestClient restClient;

    @Autowired
    Utility utility;

    public CredentialResponse reqCredential(CredentialRequestDto dto) throws DigitalCardServiceException {
        CredentialResponseDto credentialResponseDto=new CredentialResponseDto();
        CredentialResponse credentialResponse=new CredentialResponse();
        RequestWrapper<CredentialRequestDto> requestDto = new RequestWrapper<>();
        try {
            requestDto.setRequest(dto);
            requestDto.setRequesttime(LocalDateTime.now());
            ResponseWrapper<CredentialResponse> responseDto=restClient.postApi(ApiName.CREDENTIAL_REQ_URL,null,"","", MediaType.APPLICATION_JSON,requestDto,ResponseWrapper.class);
            credentialResponse=utility.readValue(utility.writeValueAsString(responseDto.getResponse()),
                    CredentialResponse.class);
        } catch (ApisResourceAccessException | IOException e) {
            throw new DigitalCardServiceException(e);
        }
        return credentialResponse;
    }

    public CredentialStatusResponse getStatus(String requestId) throws DigitalCardServiceException {
        ResponseWrapper<CredentialStatusResponse> responseDto = null;
        CredentialStatusResponse credentialStatusResponse = new CredentialStatusResponse();
        List<String> pathsegments=new ArrayList<>();
        pathsegments.add(requestId);
        try {
            responseDto =restClient.getApi(ApiName.CREDENTIAL_STATUS_URL,pathsegments,"","",ResponseWrapper.class);
            credentialStatusResponse=utility.readValue(utility.writeValueAsString(responseDto.getResponse()),
                    CredentialStatusResponse.class);
        }  catch (Exception e) {
            throw new DigitalCardServiceException(e);
        }
        return credentialStatusResponse;
    }
}
