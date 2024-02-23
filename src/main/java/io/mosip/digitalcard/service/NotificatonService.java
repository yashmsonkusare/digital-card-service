package io.mosip.digitalcard.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import io.mosip.digitalcard.constant.DigitalCardServiceErrorCodes;
import io.mosip.digitalcard.exception.ApiNotAccessibleException;
import io.mosip.digitalcard.util.*;
import io.mosip.kernel.core.logger.spi.Logger;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@Component
public class NotificatonService {

	private Logger log = DigitalCardRepoLogger.getLogger(NotificatonService.class);

	@Autowired
	NotificationUtil notificationUtil;


	@Autowired
	private TemplateGenerator templateGenerator;

	@Autowired
	RestTemplate restTemplate;

	@Autowired
	private Utility utility;

	/**
	 * 
	 * @param emailSubTemplate
	 * @param emailBodyTemplate
	 */
	public void sendNotication(Map<String,Object> attributes, String emailSubTemplate,
								String emailBodyTemplate) throws ApiNotAccessibleException {
			try {
				String emailSubject = templateGenerator.getEmailContent(emailSubTemplate, attributes);
				String emailBody=templateGenerator.getEmailContent(emailBodyTemplate, attributes);
				notificationUtil.emailNotification((String) attributes.get("email"), null, emailSubject,
						emailBody);
			} catch (IOException e) {
				log.error("error occured while send notifications.", e.getLocalizedMessage());
				throw new ApiNotAccessibleException(DigitalCardServiceErrorCodes.API_NOT_ACCESSIBLE_EXCEPTION.getErrorCode(),
						DigitalCardServiceErrorCodes.API_NOT_ACCESSIBLE_EXCEPTION.getErrorMessage());
			} catch (Exception e) {
                throw new RuntimeException(e);
            }

    }

}