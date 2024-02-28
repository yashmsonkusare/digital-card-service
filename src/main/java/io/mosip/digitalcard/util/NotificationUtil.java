package io.mosip.digitalcard.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.digitalcard.constant.ApiName;
import io.mosip.digitalcard.constant.DigitalCardServiceErrorCodes;
import io.mosip.digitalcard.dto.NotificationResponseDto;
import io.mosip.digitalcard.exception.ApiNotAccessibleException;
import io.mosip.digitalcard.exception.ApisResourceAccessException;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;


/**
 * 
 * @author Nagarjuna
 *
 */
@Component
public class NotificationUtil {

	private Logger log = DigitalCardRepoLogger.getLogger(NotificationUtil.class);

	@Value("${mosip.digitalcard.email.resourse.url}")
	private String emailURL;

	@Autowired
	RestApiClient restApiClient;

	@Autowired
	private ObjectMapper mapper;

	/**
	 * Calls the email notifier api to send email notifications
	 * 
\	 * @param file
	 * @param subject
	 * @param body
	 * @return
	 * @throws IOException
	 */
	public void emailNotification(String emailId,
																	  MultipartFile file, String subject, String body) throws IOException, ApiNotAccessibleException {
		log.info("In emailNotification method of NotificationUtil service");
		HttpEntity<byte[]> doc = null;
		MultiValueMap<Object, Object> emailMap = new LinkedMultiValueMap<>();
		if (file != null) {
			LinkedMultiValueMap<String, String> pdfHeaderMap = new LinkedMultiValueMap<>();
			pdfHeaderMap.add("Content-disposition",
					"form-data; name=attachments; filename=" + file.getOriginalFilename());
			pdfHeaderMap.add("Content-type", "text/plain");
			doc = new HttpEntity<>(file.getBytes(), pdfHeaderMap);
			emailMap.add("attachments", doc);
		}

		ResponseWrapper<NotificationResponseDto> response = new ResponseWrapper<>();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);
		emailMap.add("mailContent", body);
		emailMap.add("mailSubject", subject);
		emailMap.add("mailTo", emailId);
		HttpEntity<MultiValueMap<Object, Object>> httpEntity = new HttpEntity<>(emailMap, headers);
		log.info("In emailNotification method of NotificationUtil service emailResourseUrl:", emailURL);
		NotificationResponseDto notifierResponse = new NotificationResponseDto();
		try {
			Map<String, Object> responseFromEmailAPI = restApiClient.postApi(emailURL, null, "", "",
					MediaType.MULTIPART_FORM_DATA, httpEntity, Map.class);
			notifierResponse = mapper.readValue(mapper.writeValueAsString(responseFromEmailAPI.get("response")),
					NotificationResponseDto.class);
			log.info("emailNotification sent status {}",notifierResponse.getStatus());
		} catch (Exception e) {
			log.error("Error occured while parsing the response of email notifier api.", e.getLocalizedMessage());
			throw new ApiNotAccessibleException(DigitalCardServiceErrorCodes.API_NOT_ACCESSIBLE_EXCEPTION.getErrorCode(),
					DigitalCardServiceErrorCodes.API_NOT_ACCESSIBLE_EXCEPTION.getErrorMessage());
		}
    }

	private LocalDateTime getCurrentResponseTime() {
		return DateUtils.getUTCCurrentDateTime();
	}
}