package io.mosip.digitalcard.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.digitalcard.constant.ApiName;
import io.mosip.digitalcard.constant.UinCardType;
import io.mosip.digitalcard.dto.ErrorDTO;
import io.mosip.digitalcard.dto.PDFSignatureRequestDto;
import io.mosip.digitalcard.dto.SignatureResponseDto;
import io.mosip.digitalcard.exception.DigitalCardServiceException;
import io.mosip.digitalcard.service.UinCardGenerator;
import io.mosip.digitalcard.util.DigitalCardRepoLogger;
import io.mosip.digitalcard.util.RestClient;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.pdfgenerator.exception.PDFGeneratorException;
import io.mosip.kernel.core.pdfgenerator.spi.PDFGenerator;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.kernel.pdfgenerator.itext.constant.PDFGeneratorExceptionCodeConstant;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * The Class UinCardGeneratorImpl.
 * 
 * @author Dhanendra.
 */
@Component
public class UinCardGeneratorImpl implements UinCardGenerator<byte[]> {

	/** The pdf generator. */
	@Autowired
	private PDFGenerator pdfGenerator;

	/** The print logger. */
	private Logger printLogger = DigitalCardRepoLogger.getLogger(UinCardGeneratorImpl.class);

	private static final String DATETIME_PATTERN = "mosip.digitalcard.service.datetime.pattern";


	@Value("${mosip.print.service.uincard.lowerleftx}")
	private int lowerLeftX;

	@Value("${mosip.print.service.uincard.lowerlefty}")
	private int lowerLeftY;

	@Value("${mosip.print.service.uincard.upperrightx}")
	private int upperRightX;

	@Value("${mosip.print.service.uincard.upperrighty}")
	private int upperRightY;

	@Value("${mosip.print.service.uincard.signature.reason}")
	private String reason;

	@Autowired
	private Environment env;


	ObjectMapper mapper = new ObjectMapper();
	
	
	@Autowired
	private RestClient restApiClient;


	@Override
	public byte[] generateUinCard(InputStream in, UinCardType type, String password) {
		printLogger.debug("UinCardGeneratorImpl::generateUinCard()::entry");
        byte[] pdfSignatured=null;
		ByteArrayOutputStream out = null;
		try {
			out = (ByteArrayOutputStream) pdfGenerator.generate(in);
			PDFSignatureRequestDto request = new PDFSignatureRequestDto(lowerLeftX, lowerLeftY, upperRightX,
					upperRightY, reason, 1, password);
			request.setApplicationId("KERNEL");
		  	request.setReferenceId("SIGN");
			request.setData(Base64.encodeBase64String(out.toByteArray()));
		  	DateTimeFormatter format = DateTimeFormatter.ofPattern(env.getProperty(DATETIME_PATTERN));
			LocalDateTime localdatetime = LocalDateTime
					.parse(DateUtils.getUTCCurrentDateTimeString(env.getProperty(DATETIME_PATTERN)), format);

		  	request.setTimeStamp(DateUtils.getUTCCurrentDateTimeString());
			RequestWrapper<PDFSignatureRequestDto> requestWrapper = new RequestWrapper<>();

			requestWrapper.setRequest(request);
			requestWrapper.setRequesttime(localdatetime);
			ResponseWrapper<?> responseWrapper;
			SignatureResponseDto signatureResponseDto;

			 responseWrapper= restApiClient.postApi(ApiName.PDFSIGN, null, "",""
					 ,MediaType.APPLICATION_JSON,requestWrapper, ResponseWrapper.class);


			if (responseWrapper.getErrors() != null && !responseWrapper.getErrors().isEmpty()) {
				ServiceError error = responseWrapper.getErrors().get(0);
			    throw new DigitalCardServiceException(error.getMessage());
			}
			signatureResponseDto = mapper.readValue(mapper.writeValueAsString(responseWrapper.getResponse()),
					SignatureResponseDto.class);

			pdfSignatured = Base64.decodeBase64(signatureResponseDto.getData());

		} catch (Exception e) {
			printLogger.error(e.getMessage()
							+ ExceptionUtils.getStackTrace(e));
			throw new PDFGeneratorException(PDFGeneratorExceptionCodeConstant.PDF_EXCEPTION.getErrorCode(),
					e.getMessage() + ExceptionUtils.getStackTrace(e));
		}
		printLogger.debug("UinCardGeneratorImpl::generateUinCard()::exit");

		return pdfSignatured;
	}

}
