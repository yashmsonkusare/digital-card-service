package io.mosip.digitalcard.controller;

import io.mosip.digitalcard.dto.DataShareResponseDto;
import io.mosip.digitalcard.dto.DigitalCardStatusResponseDto;
import io.mosip.digitalcard.service.DigitalCardService;
import io.mosip.digitalcard.util.DigitalCardRepoLogger;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.CryptoUtil;
import io.mosip.kernel.core.websub.model.EventModel;
import io.mosip.kernel.websub.api.annotation.PreAuthenticateContentAndVerifyIntent;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;

@RestController
@Api(value = "This api generate Digital Card based on RID",tags = {"Digital Card"})
public class DigitalCardController {

    @Autowired
    DigitalCardService digitalCardServiceImpl;

    Logger logger = DigitalCardRepoLogger.getLogger(DigitalCardController.class);


    @PostMapping(path = "/idCreateEventHandle/callback/notifyStatus", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Request authenticated successfully") })
    @PreAuthenticateContentAndVerifyIntent(secret = "${mosip.digitalcard.websub.secret}", callback = "/v1/digitalcard/idCreateEventHandle/callback/notifyStatus", topic = "${mosip.digitalcard.generate.identity.create.websub.topic}")
    public ResponseEntity<?> identityCreateEvent(@RequestBody EventModel eventModel)  {
        logger.info("event recieved from websub id: {}, topic : {}",eventModel.getEvent().getId(),eventModel.getTopic());
        boolean isCreated=digitalCardServiceImpl.createDigitalCard(eventModel);
        logger.info("successfully created the digitalcard : {}",isCreated);
        return new ResponseEntity<>("request accepted.", HttpStatus.OK);
    }

    @PostMapping(path = "/idUpdateEventHandle/callback/notifyStatus", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Request authenticated successfully") })
    @PreAuthenticateContentAndVerifyIntent(secret = "${mosip.digitalcard.websub.secret}", callback = "/v1/digitalcard/idUpdateEventHandle/callback/notifyStatus", topic = "${mosip.digitalcard.generate.identity.update.websub.topic}")
    public ResponseEntity<?> identityUpdateEvent(@RequestBody EventModel eventModel)  {
        logger.info("event recieved from websub id: {}, topic : {}",eventModel.getEvent().getId(),eventModel.getTopic());
        boolean isCreated=digitalCardServiceImpl.createDigitalCard(eventModel);
        logger.info("successfully printed the digitalcard : {}",isCreated);
        return new ResponseEntity<>("request accepted.", HttpStatus.OK);
    }

    @PostMapping(path = "/credential/callback/notifyStatus", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Request authenticated successfully") })
    @PreAuthenticateContentAndVerifyIntent(secret = "${mosip.digitalcard.websub.secret}", callback = "/v1/digitalcard/credential/callback/notifyStatus", topic = "${mosip.digitalcard.generate.credential.websub.topic}")
    public ResponseEntity<?> credentialEvent(@RequestBody EventModel eventModel)  {
        logger.info("event recieved from websub id: {}, topic : {}",eventModel.getEvent().getId(),eventModel.getTopic());
        boolean isPrinted=digitalCardServiceImpl.generateDigitalCard(eventModel);
        logger.info("successfully printed the digitalcard : {}",isPrinted);
        return new ResponseEntity<>("request accepted.", HttpStatus.OK);
    }

    @GetMapping(path = "/{rid}")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Request authenticated successfully") })
    public ResponseWrapper<DigitalCardStatusResponseDto> getDigitalCard(@PathVariable("rid") String rid)  {
        ResponseWrapper<DigitalCardStatusResponseDto> digitalCardStatusResponseDtoResponseWrapper=new DataShareResponseDto();
        digitalCardStatusResponseDtoResponseWrapper.setResponse(digitalCardServiceImpl.getDigitalCard(rid));
        logger.info("successfully printed the digitalcard");
        return digitalCardStatusResponseDtoResponseWrapper;
    }
}