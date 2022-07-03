package io.mosip.digitalcard.service.impl;

import io.mosip.digitalcard.constant.DigitalCardServiceErrorCodes;
import io.mosip.digitalcard.controller.DigitalCardController;
import io.mosip.digitalcard.dto.CredentialRequestDto;
import io.mosip.digitalcard.dto.CredentialResponse;
import io.mosip.digitalcard.dto.DigitalCardStatusResponseDto;
import io.mosip.digitalcard.entity.DigitalCardTransactionEntity;
import io.mosip.digitalcard.exception.DigitalCardServiceException;
import io.mosip.digitalcard.repositories.DigitalCardTransactionRepository;
import io.mosip.digitalcard.service.DigitalCardService;
import io.mosip.digitalcard.service.PDFCardService;
import io.mosip.digitalcard.util.CredentialUtil;
import io.mosip.digitalcard.util.DigitalCardRepoLogger;
import io.mosip.digitalcard.util.RestClient;
import io.mosip.digitalcard.util.Utility;
import io.mosip.kernel.core.logger.spi.Logger;
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
    private PDFCardService pdfCardServiceImpl;

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

    Logger logger = DigitalCardRepoLogger.getLogger(DigitalCardController.class);

    @Override
    public boolean generateDigitalCard(EventModel eventModel) {
        boolean isGenerated;
        try {
            isGenerated=pdfCardServiceImpl.generateCard(eventModel);
        } catch (Exception e) {
            throw new DigitalCardServiceException(DigitalCardServiceErrorCodes.DIGITAL_CARD_NOT_GENERATED.getErrorCode(),DigitalCardServiceErrorCodes.DIGITAL_CARD_NOT_GENERATED.getErrorMessage());
        }
        return isGenerated;
    }

    @Override
    public DigitalCardStatusResponseDto getDigitalCard(String rid) {
        String pdfByteString=null;
        CredentialRequestDto credentialRequestDto=new CredentialRequestDto();
        DigitalCardStatusResponseDto digitalCardStatusResponseDto=new DigitalCardStatusResponseDto();
        credentialRequestDto.setCredentialType(credentialType);
        credentialRequestDto.setIssuer(partnerId);
        credentialRequestDto.setId(rid);
        try {
            DigitalCardTransactionEntity digitalCardTransactionEntity=digitalCardTransactionRepository.findByRID(rid);
            if(digitalCardTransactionEntity!=null && digitalCardTransactionEntity.getDataShareUrl()!=null){
                digitalCardStatusResponseDto.setId(digitalCardTransactionEntity.getrid());
                digitalCardStatusResponseDto.setStatusCode(digitalCardTransactionEntity.getStatusCode());
                digitalCardStatusResponseDto.setUrl(digitalCardTransactionEntity.getDataShareUrl());
                return digitalCardStatusResponseDto;
            }
            CredentialResponse credentialResponse=credentialUtil.reqCredential(credentialRequestDto);
            credentialRequestId=credentialResponse.getRequestId();
            saveTransactionDetails(credentialResponse,null);
            throw new DigitalCardServiceException(DigitalCardServiceErrorCodes.DIGITAL_CARD_NOT_CREATED.getErrorCode(),DigitalCardServiceErrorCodes.DIGITAL_CARD_NOT_CREATED.getErrorMessage());
        } catch (Exception e) {
            throw new DigitalCardServiceException(DigitalCardServiceErrorCodes.DIGITAL_CARD_NOT_GENERATED.getErrorCode(),DigitalCardServiceErrorCodes.DIGITAL_CARD_NOT_GENERATED.getErrorMessage());
        }
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
            logger.error(DigitalCardServiceErrorCodes.DIGITAL_CARD_NOT_GENERATED.getErrorMessage(),e);
        }
        return isCreated;
    }


    private void saveTransactionDetails(CredentialResponse credentialResponse, String idHash){
        DigitalCardTransactionEntity digitalCardEntity=new DigitalCardTransactionEntity();
        digitalCardEntity.setrid(credentialResponse.getId());
        digitalCardEntity.setUinSaltedHash(idHash);
        digitalCardEntity.setCredentialId(credentialResponse.getRequestId());
        digitalCardEntity.setCreateDateTime(LocalDateTime.now());
        digitalCardEntity.setCreatedBy(Utility.getUser());
        digitalCardEntity.setStatusCode("NEW");
        digitalCardTransactionRepository.save(digitalCardEntity);

    }
}
