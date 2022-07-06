package io.mosip.digitalcard.service.impl;

import io.mosip.digitalcard.constant.DigitalCardServiceErrorCodes;
import io.mosip.digitalcard.constant.UinCardType;
import io.mosip.digitalcard.controller.DigitalCardController;
import io.mosip.digitalcard.dto.CredentialRequestDto;
import io.mosip.digitalcard.dto.CredentialResponse;
import io.mosip.digitalcard.dto.DigitalCardStatusResponseDto;
import io.mosip.digitalcard.entity.DigitalCardTransactionEntity;
import io.mosip.digitalcard.exception.DigitalCardServiceException;
import io.mosip.digitalcard.repositories.DigitalCardTransactionRepository;
import io.mosip.digitalcard.service.DigitalCardService;
import io.mosip.digitalcard.service.PDFCardService;
import io.mosip.digitalcard.util.*;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.websub.model.EventModel;
import io.mosip.vercred.CredentialsVerifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
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

    @Autowired
    private CredentialUtil credentialUtil;

    @Autowired
    Utility utility;

    @Autowired
    RestClient restClient;

    @Autowired
    private EncryptionUtil encryptionUtil;

    @Autowired
    private CredentialsVerifier credentialsVerifier;

    @Autowired
    DigitalCardTransactionRepository digitalCardTransactionRepository;

    @Value("${mosip.digitalcard.verify.credentials.flag:true}")
    private boolean verifyCredentialsFlag;

    @Value("${mosip.digitalcard.credentials.request.initiate.flag:true}")
    private boolean isInitiateFlag;

    @Value("${mosip.digitalcard.pdf.password.enable.flag:true}")
    private boolean pdfPasswordFlag;

    Logger logger = DigitalCardRepoLogger.getLogger(DigitalCardController.class);

    public boolean generateDigitalCard(EventModel eventModel) {
        String credential = null;
        boolean isGenerated = false;
        String decryptedCredential=null;
        try {
            if (eventModel.getEvent().getDataShareUri() == null || eventModel.getEvent().getDataShareUri().isEmpty()) {
                credential = eventModel.getEvent().getData().get("credential").toString();
            } else {
                String dataShareUrl = eventModel.getEvent().getDataShareUri();
                URI dataShareUri = URI.create(dataShareUrl);
                credential = restClient.getForObject(dataShareUrl, String.class);
            }
            String ecryptionPin = null;
            decryptedCredential = encryptionUtil.decryptData(credential);
            if (verifyCredentialsFlag){
                logger.info("Configured received credentials to be verified. Flag {}", verifyCredentialsFlag);
                boolean verified =credentialsVerifier.verifyCredentials(decryptedCredential);
                if (!verified) {
                    logger.error("Received Credentials failed in verifiable credential verify method. So, digital card is not getting generated." +
                            " Id: {}, Transaction Id: {}", eventModel.getEvent().getId(), eventModel.getEvent().getTransactionId());
                    return false;
                }
            }
            isGenerated=pdfCardServiceImpl.generatePDFCard(decryptedCredential,
                    eventModel.getEvent().getData().get("credentialType").toString(),
                    eventModel.getEvent().getTransactionId(), UinCardType.PDF,true);
        }catch (Exception e){
            logger.error(DigitalCardServiceErrorCodes.DIGITAL_CARD_NOT_GENERATED.getErrorMessage() , e);
            isGenerated = false;
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
            if(isInitiateFlag && digitalCardTransactionEntity==null) {
                CredentialResponse credentialResponse = credentialUtil.reqCredential(credentialRequestDto);
                saveTransactionDetails(credentialResponse, null);
            }
            throw new DigitalCardServiceException(DigitalCardServiceErrorCodes.DIGITAL_CARD_NOT_CREATED.getErrorCode(),DigitalCardServiceErrorCodes.DIGITAL_CARD_NOT_CREATED.getErrorMessage());
        } catch (Exception e) {
            throw new DigitalCardServiceException(DigitalCardServiceErrorCodes.DIGITAL_CARD_NOT_GENERATED.getErrorCode(),DigitalCardServiceErrorCodes.DIGITAL_CARD_NOT_GENERATED.getErrorMessage());
        }
    }

    @Override
    public boolean initiateCredentialRequest(EventModel eventModel) {
        boolean isCreated=false;
        String pdfByteString = null;
        CredentialRequestDto credentialRequestDto = new CredentialRequestDto();
        credentialRequestDto.setCredentialType(credentialType);
        credentialRequestDto.setIssuer(partnerId);
        credentialRequestDto.setId(eventModel.getEvent().getData().get("registration_id").toString());
        try {
            CredentialResponse credentialResponse = credentialUtil.reqCredential(credentialRequestDto);
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
