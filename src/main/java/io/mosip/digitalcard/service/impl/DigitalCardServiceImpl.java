package io.mosip.digitalcard.service.impl;

import io.mosip.digitalcard.constant.DigitalCardServiceErrorCodes;
import io.mosip.digitalcard.dto.CredentialRequestDto;
import io.mosip.digitalcard.dto.CredentialResponse;
import io.mosip.digitalcard.dto.CredentialStatusResponse;
import io.mosip.digitalcard.entity.DigitalCardTransactionEntity;
import io.mosip.digitalcard.exception.DigitalCardServiceException;
import io.mosip.digitalcard.repositories.DigitalCardTransactionRepository;
import io.mosip.digitalcard.service.DigitalCardService;
import io.mosip.digitalcard.service.PrintService;
import io.mosip.digitalcard.util.CredentialUtil;
import io.mosip.digitalcard.util.RestClient;
import io.mosip.digitalcard.util.Utility;
import io.mosip.kernel.core.websub.model.EventModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

/**
 * The DigitalCardServiceImpl.
 *
 * @author Dhanendra
 */
@Service
public class DigitalCardServiceImpl implements DigitalCardService {

    @Autowired
    private PrintService printServiceImpl;

    @Value("${mosip.digitalcard.credential.request.partner.id}")
    private String partnerId;

    @Value("${mosip.digitalcard.credential.type}")
    private String credentialType;

    @Value("${mosip.digitalcard.fixedRate.in.milliseconds}")
    private int delaySeconds;

    @Autowired
    private CredentialUtil credentialUtil;

    @Autowired
    Utility utility;

    @Autowired
    RestClient restClient;

    @Autowired
    DigitalCardTransactionRepository digitalCardTransactionRepository;

    String credentialRequestId=null;

    @Override
    public boolean generateDigitalCard(EventModel eventModel) {
        boolean isPrinted;
        try {
            isPrinted=printServiceImpl.generateCard(eventModel);
        } catch (Exception e) {
            throw new DigitalCardServiceException(DigitalCardServiceErrorCodes.DIGITAL_CARD_NOT_GENERATED.getErrorCode(),DigitalCardServiceErrorCodes.DIGITAL_CARD_NOT_GENERATED.getErrorMessage());
        }
        return isPrinted;
    }

    @Override
    public byte[] getDigitalCard(String rid,String idHash) {
        String pdfByteString=null;
        CredentialRequestDto credentialRequestDto=new CredentialRequestDto();
        credentialRequestDto.setCredentialType(credentialType);
        credentialRequestDto.setIssuer(partnerId);
        credentialRequestDto.setId(rid);
        try {
            DigitalCardTransactionEntity digitalCardTransactionEntity=digitalCardTransactionRepository.findByRID(rid);
            if(digitalCardTransactionEntity!=null && digitalCardTransactionEntity.getDataShareUrl()!=null){
                pdfByteString = restClient.getForObject(digitalCardTransactionEntity.getDataShareUrl(), String.class);
                return pdfByteString.getBytes(StandardCharsets.UTF_8);
            }
            CredentialResponse credentialResponse=credentialUtil.reqCredential(credentialRequestDto);
            credentialRequestId=credentialResponse.getRequestId();
            saveTransactionDetails(credentialResponse,idHash);
            throw new DigitalCardServiceException(DigitalCardServiceErrorCodes.DIGITAL_CARD_NOT_CREATED.getErrorCode(),DigitalCardServiceErrorCodes.DIGITAL_CARD_NOT_CREATED.getErrorMessage());
        } catch (Exception e) {
            throw new DigitalCardServiceException(DigitalCardServiceErrorCodes.DIGITAL_CARD_NOT_GENERATED.getErrorCode(),DigitalCardServiceErrorCodes.DIGITAL_CARD_NOT_GENERATED.getErrorMessage());
        }
    }
    private CredentialStatusResponse checkStatus(String credentialReqId){
        CredentialStatusResponse credentialStatusResponse=new CredentialStatusResponse();
        credentialStatusResponse=credentialUtil.getStatus(credentialReqId);
        return credentialStatusResponse;
    }

    @Override
    public boolean createDigitalCard(EventModel eventModel) {
        boolean isCreated=false;
        String pdfByteString = null;
        CredentialRequestDto credentialRequestDto = new CredentialRequestDto();
        credentialRequestDto.setCredentialType(credentialType);
        credentialRequestDto.setIssuer(partnerId);
        credentialRequestDto.setId(eventModel.getEvent().getData().get("registration_id").toString());
        try {
            CredentialResponse credentialResponse = credentialUtil.reqCredential(credentialRequestDto);
            credentialRequestId = credentialResponse.getRequestId();
            saveTransactionDetails(credentialResponse, eventModel.getEvent().getData().get("id_hash").toString());
            isCreated=true;
        } catch (DigitalCardServiceException e) {
            isCreated=false;
            throw new DigitalCardServiceException(DigitalCardServiceErrorCodes.DIGITAL_CARD_NOT_GENERATED.getErrorCode(),DigitalCardServiceErrorCodes.DIGITAL_CARD_NOT_GENERATED.getErrorMessage());
        }
        return isCreated;
    }


    private void saveTransactionDetails(CredentialResponse credentialResponse, String idHash){
        DigitalCardTransactionEntity digitalCardEntity=new DigitalCardTransactionEntity();
        digitalCardEntity.setrid(credentialResponse.getId());
        digitalCardEntity.setUinSaltedHash(idHash);
        digitalCardEntity.setCredentialId(credentialResponse.getId());
        digitalCardEntity.setCreateDateTime(LocalDateTime.now());
        digitalCardEntity.setCreatedBy(Utility.getUser());
        digitalCardEntity.setStatusCode("NEW");
        digitalCardTransactionRepository.save(digitalCardEntity);

    }
}
