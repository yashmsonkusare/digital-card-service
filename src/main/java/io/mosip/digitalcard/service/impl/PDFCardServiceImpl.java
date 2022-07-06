package io.mosip.digitalcard.service.impl;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.biometrics.util.ConvertRequestDto;
import io.mosip.biometrics.util.face.FaceDecoder;
import io.mosip.digitalcard.constant.*;
import io.mosip.digitalcard.dto.DataShareDto;
import io.mosip.digitalcard.dto.PDFSignatureRequestDto;
import io.mosip.digitalcard.dto.SignatureResponseDto;
import io.mosip.digitalcard.dto.SimpleType;
import io.mosip.digitalcard.service.PDFCardService;
import io.mosip.digitalcard.websub.CredentialStatusEvent;
import io.mosip.digitalcard.websub.StatusEvent;
import io.mosip.digitalcard.exception.ApiNotAccessibleException;
import io.mosip.digitalcard.exception.DataShareException;
import io.mosip.digitalcard.exception.DigitalCardServiceException;
import io.mosip.digitalcard.exception.IdentityNotFoundException;
import io.mosip.digitalcard.repositories.DigitalCardTransactionRepository;
import io.mosip.digitalcard.util.*;
import io.mosip.digitalcard.websub.WebSubSubscriptionHelper;
import io.mosip.kernel.core.cbeffutil.spi.CbeffUtil;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.pdfgenerator.exception.PDFGeneratorException;
import io.mosip.kernel.core.pdfgenerator.spi.PDFGenerator;
import io.mosip.kernel.core.qrcodegenerator.exception.QrcodeGenerationException;
import io.mosip.kernel.core.qrcodegenerator.spi.QrCodeGenerator;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.kernel.qrcode.generator.zxing.constant.QrVersion;
import io.mosip.vercred.CredentialsVerifier;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.JSONException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.io.*;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class PDFCardServiceImpl implements PDFCardService {


	/** The PDFServiceImpl logger. */
	Logger logger = DigitalCardRepoLogger.getLogger(PDFCardServiceImpl.class);

	private static final String DATETIME_PATTERN = "mosip.digitalcard.service.datetime.pattern";

	/** The Constant FILE_SEPARATOR. */
	public static final String FILE_SEPARATOR = File.separator;

	/** The Constant VALUE. */
	private static final String VALUE = "value";

	/** The Constant FACE. */
	private static final String FACE = "Face";

	/** The Constant APPLICANT_PHOTO. */
	private static final String APPLICANT_PHOTO = "ApplicantPhoto";

	/** The Constant QRCODE. */
	private static final String QRCODE = "QrCode";

	/** The Constant UINCARDPASSWORD. */
	private static final String UINCARDPASSWORD = "mosip.digitalcard.uincard.password";
	
	@Autowired
	private WebSubSubscriptionHelper webSubSubscriptionHelper;

	@Autowired
	private DataShareUtil dataShareUtil;

	@Autowired
	private RestClient restApiClient;

	/** The pdf generator. */
	@Autowired
	private PDFGenerator pdfGenerator;

	/** The template generator. */
	@Autowired
	private TemplateGenerator templateGenerator;

	/** The utilities. */
	@Autowired
	private Utility utility;

	@Autowired
	private EncryptionUtil encryptionUtil;

	/** The qr code generator. */
	@Autowired
	private QrCodeGenerator<QrVersion> qrCodeGenerator;

	/** The cbeffutil. */
	@Autowired
	private CbeffUtil cbeffutil;

	/** The env. */
	@Autowired
	private Environment env;

	@Autowired
	DigitalCardTransactionRepository digitalCardTransactionRepository;

	@Value("${mosip.digitalcard.websub.publish.topic:CREDENTIAL_STATUS_UPDATE}")
	private String topic;

	@Autowired
	private CredentialsVerifier credentialsVerifier;

	@Value("${mosip.digitalcard.datashare.partner.id}")
	private String partnerId;

	@Value("${mosip.digitalcard.datashare.policy.id}")
	private String policyId;

	@Value("${mosip.template-language}")
	private String templateLang;

	@Value("${mosip.supported-languages}")
	private String supportedLang;

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

	@Value("${mosip.digitalcard.templateTypeCode:RPR_UIN_CARD_TEMPLATE}")
	private String uinCardTemplate;


	@Autowired
	private ObjectMapper objectMapper;

	public PDFCardServiceImpl() {
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.digitalcard.service.PDFService#
	 */
	public boolean generatePDFCard(String credential, String credentialType,
			String requestId,
			UinCardType cardType,
			boolean isPasswordProtected) {
		logger.debug("PDFServiceImpl::getDocuments()::entry");
		boolean isGenerated=false;
		String uin = null;
		String rid=null;
		String password = null;
		boolean isPhotoSet=false;
		String individualBio = null;
		Map<String, Object> attributes = new LinkedHashMap<>();
		String template = uinCardTemplate;
		byte[] pdfbytes = null;
		try {
			org.json.JSONObject jsonObject = new org.json.JSONObject(credential);
			org.json.JSONObject decryptedJson = jsonObject.getJSONObject("credentialSubject");
			if(decryptedJson.has("biometrics")){
				individualBio = decryptedJson.getString("biometrics");
				String individualBiometric = new String(individualBio);
				isPhotoSet = setApplicantPhoto(individualBiometric, attributes);
				attributes.put("isPhotoSet",isPhotoSet);
			}
			uin = decryptedJson.getString("UIN");
			if (isPasswordProtected) {
				password = getPassword(uin);
			}
			if (credentialType.equalsIgnoreCase("qrcode")) {
				boolean isQRcodeSet = setQrCode(decryptedJson.toString(), attributes,isPhotoSet);
				InputStream uinArtifact = templateGenerator.getTemplate(template, attributes, templateLang);
				pdfbytes = generateUinCard(uinArtifact, cardType,
						password);
			} else {
			if (!isPhotoSet) {
				logger.debug(DigitalCardServiceErrorCodes.APPLICANT_PHOTO_NOT_SET.name());
			}
			setTemplateAttributes(decryptedJson, attributes);
			attributes.put(IdType.UIN.toString(), uin);
			boolean isQRcodeSet = setQrCode(decryptedJson.toString(), attributes,isPhotoSet);
			rid=getRid(decryptedJson.get("id"));
			if (!isQRcodeSet) {
				logger.debug(DigitalCardServiceErrorCodes.QRCODE_NOT_SET.name());
			}
			// getting template and placing original valuespng
			InputStream uinArtifact = templateGenerator.getTemplate(template, attributes, templateLang);
			if (uinArtifact == null) {
				logger.error(DigitalCardServiceErrorCodes.TEM_PROCESSING_FAILURE.name());
				throw new DigitalCardServiceException(
						DigitalCardServiceErrorCodes.TEM_PROCESSING_FAILURE.getErrorCode(),DigitalCardServiceErrorCodes.TEM_PROCESSING_FAILURE.getErrorMessage());
			}

			pdfbytes = generateUinCard(uinArtifact, cardType, password);
		}
			digitalCardStatusUpdate(requestId, pdfbytes, credentialType,rid);
			isGenerated=true;
		}
		catch (QrcodeGenerationException e) {
			isGenerated = false;
			logger.error(DigitalCardServiceErrorCodes.QRCODE_NOT_GENERATED.getErrorMessage(), e);
		}  catch (PDFGeneratorException e) {
			isGenerated = false;
			logger.error(DigitalCardServiceErrorCodes.PDF_NOT_GENERATED.getErrorMessage() ,e);
		}catch (Exception ex) {
			logger.error(PDFGeneratorExceptionCodeConstant.PDF_EXCEPTION.getErrorMessage() ,ex);
		}
		logger.debug("PDFServiceImpl::getDocuments()::exit");
		return isGenerated;
	}

	private String getRid(Object id) {
		String rid= id.toString().split("/credentials/")[1];
		return rid;
	}

	/**
	 * Sets the qr code.
	 *
	 * @param attributes   the attributes
	 * @return true, if successful
	 * @throws QrcodeGenerationException                          the qrcode
	 *                                                            generation
	 *                                                            exception
	 * @throws IOException                                        Signals that an
	 *                                                            I/O exception has
	 *                                                            occurred.
	 * @throws QrcodeGenerationException
	 */
	private boolean setQrCode(String qrString, Map<String, Object> attributes,boolean isPhotoSet)
			throws IOException, QrcodeGenerationException {
		boolean isQRCodeSet = false;
		JSONObject qrJsonObj = objectMapper.readValue(qrString, JSONObject.class);
		if(isPhotoSet) {
			qrJsonObj.remove("biometrics");
		}
		byte[] qrCodeBytes = qrCodeGenerator.generateQrCode(qrJsonObj.toString(), QrVersion.V30);
		if (qrCodeBytes != null) {
			String imageString = Base64.encodeBase64String(qrCodeBytes);
			attributes.put(QRCODE, "data:image/png;base64," + imageString);
			isQRCodeSet = true;
		}

		return isQRCodeSet;
	}

	/**
	 * Sets the applicant photo.
	 *
	 *            the response
	 * @param attributes
	 *            the attributes
	 * @return true, if successful
	 * @throws Exception
	 *             the exception
	 */
	private boolean setApplicantPhoto(String individualBio, Map<String, Object> attributes) throws Exception {
		ConvertRequestDto convertRequestDto = new ConvertRequestDto();
		String value = individualBio;
		boolean isPhotoSet = false;

		if (value != null) {
			CbeffToBiometricUtil util = new CbeffToBiometricUtil(cbeffutil);
			List<String> subtype = new ArrayList<>();
			byte[] photoByte = util.getImageBytes(value, FACE, subtype);
			convertRequestDto.setVersion("ISO19794_5_2011");
			convertRequestDto.setInputBytes(photoByte);
			if (photoByte != null) {
				byte[] data = FaceDecoder.convertFaceISOToImageBytes(convertRequestDto);
				String encodedData = StringUtils.newStringUtf8(Base64.encodeBase64(data, false));
				attributes.put(APPLICANT_PHOTO, "data:image/png;base64," + encodedData);
				isPhotoSet = true;
			}
		}
		return isPhotoSet;
	}

	/**
	 * Gets the artifacts.
	 *
	 * @param attribute    the attribute
	 * @return the artifacts
	 * @throws IOException    Signals that an I/O exception has occurred.
	 * @throws ParseException
	 */
	@SuppressWarnings("unchecked")
	private void setTemplateAttributes(org.json.JSONObject demographicIdentity, Map<String, Object> attribute)
			throws Exception {
		try {
			if (demographicIdentity == null)
				throw new IdentityNotFoundException(DigitalCardServiceErrorCodes.IDENTITY_NOT_FOUND.getErrorCode(),DigitalCardServiceErrorCodes.IDENTITY_NOT_FOUND.getErrorMessage());

			String mapperJsonString = utility.getIdentityMappingJson(utility.getConfigServerFileStorageURL(),
					utility.getIdentityJson());
			JSONObject mapperJson = objectMapper.readValue(mapperJsonString, JSONObject.class);
			JSONObject mapperIdentity = utility.getJSONObject(mapperJson,
					utility.getDemographicIdentity());

			List<String> mapperJsonKeys = new ArrayList<>(mapperIdentity.keySet());
			for (String key : mapperJsonKeys) {
				LinkedHashMap<String, String> jsonObject = utility.getJSONValue(mapperIdentity, key);
				Object obj = null;
				String values = jsonObject.get(VALUE);
				for (String value : values.split(",")) {
					// Object object = demographicIdentity.get(value);
					Object object = demographicIdentity.has(value)?demographicIdentity.get(value):null;
					if (object != null) {
						try {
							obj = new JSONParser().parse(object.toString());
						} catch (Exception e) {
							obj = object;
						}

						if (obj instanceof JSONArray) {
							// JSONArray node = JsonUtil.getJSONArray(demographicIdentity, value);
							SimpleType[] jsonValues = Utility.mapJsonNodeToJavaObject(SimpleType.class, (JSONArray) obj);
							for (SimpleType jsonValue : jsonValues) {
								if (supportedLang.contains(jsonValue.getLanguage()))
									attribute.put(value + "_" + jsonValue.getLanguage(), jsonValue.getValue());
							}

						} else if (object instanceof JSONObject) {
							JSONObject json = (JSONObject) object;
							attribute.put(value, (String) json.get(VALUE));
						} else {
							attribute.put(value, String.valueOf(object));
						}
					}

				}
			}
			} catch (JsonParseException | JsonMappingException | DigitalCardServiceException e) {
				logger.error("Error while parsing Json file" ,e);
			}

	}

	/**
	 * Gets the password.
	 *
	 * @param uin
	 *            the uin
	 * @return the password
	 *             the id repo app exception
	 * @throws NumberFormatException
	 *             the number format exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private String getPassword(String uin) throws Exception {
		JSONObject jsonObject = utility.retrieveIdrepoJson(uin);

		String[] attributes = env.getProperty(UINCARDPASSWORD).split("\\|");
		List<String> list = new ArrayList<>(Arrays.asList(attributes));

		Iterator<String> it = list.iterator();
		String uinCardPd = "";
		Object obj=null;
		while (it.hasNext()) {
			String key = it.next().trim();

			Object object = jsonObject.get(key);
			if (object != null) {
				try {
					obj = new JSONParser().parse(object.toString());
				} catch (Exception e) {
					obj = object;
				}
			}
			if (obj instanceof JSONArray) {
				// JSONArray node = JsonUtil.getJSONArray(demographicIdentity, value);
				SimpleType[] jsonValues = Utility.mapJsonNodeToJavaObject(SimpleType.class, (JSONArray) obj);
				uinCardPd = uinCardPd.concat(getParameter(jsonValues, templateLang).substring(0,4));
			} else if (object instanceof JSONObject) {
				JSONObject json = (JSONObject) object;
				uinCardPd = uinCardPd.concat((String) json.get(VALUE));
			} else {
				uinCardPd = uinCardPd.concat((String) object.toString().substring(0,4));
			}
		}
		return uinCardPd;
	}

	/**
	 * Gets the parameter.
	 *
	 * @param jsonValues
	 *            the json values
	 * @param langCode
	 *            the lang code
	 * @return the parameter
	 */
	private String getParameter(SimpleType[] jsonValues, String langCode) {

		String parameter = null;
		if (jsonValues != null) {
			for (int count = 0; count < jsonValues.length; count++) {
				String lang = jsonValues[count].getLanguage();
				if (langCode.contains(lang)) {
					parameter = jsonValues[count].getValue();
					break;
				}
			}
		}
		return parameter;
	}



	private String getCrdentialSubject(String crdential) throws JSONException {
		org.json.JSONObject jsonObject = new org.json.JSONObject(crdential);
		String credentialSubject = jsonObject.get("credentialSubject").toString();
		return credentialSubject;
	}

	private byte[] generateUinCard(InputStream in, UinCardType type, String password) {
		logger.debug("UinCardGeneratorImpl::generateUinCard()::entry");
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
					, MediaType.APPLICATION_JSON,requestWrapper, ResponseWrapper.class);


			if (responseWrapper.getErrors() != null && !responseWrapper.getErrors().isEmpty()) {
				ServiceError error = responseWrapper.getErrors().get(0);
				throw new DigitalCardServiceException(error.getMessage());
			}
			signatureResponseDto = objectMapper.readValue(objectMapper.writeValueAsString(responseWrapper.getResponse()),
					SignatureResponseDto.class);

			pdfSignatured = Base64.decodeBase64(signatureResponseDto.getData());

		} catch (Exception e) {
			logger.error(io.mosip.kernel.pdfgenerator.itext.constant.PDFGeneratorExceptionCodeConstant.PDF_EXCEPTION.getErrorMessage(),e.getMessage()
					+ ExceptionUtils.getStackTrace(e));
		}
		logger.debug("UinCardGeneratorImpl::generateUinCard()::exit");

		return pdfSignatured;
	}

	private void digitalCardStatusUpdate(String requestId, byte[] data, String credentialType, String rid)
			throws DataShareException, ApiNotAccessibleException, IOException, Exception {
		DataShareDto dataShareDto = null;
		dataShareDto = dataShareUtil.getDataShare(data, policyId, partnerId);
		CredentialStatusEvent creEvent = new CredentialStatusEvent();
		LocalDateTime currentDtime = DateUtils.getUTCCurrentDateTime();
		digitalCardTransactionRepository.updateTransactionDetails(rid,"AVAILABLE", dataShareDto.getUrl(),LocalDateTime.now(),Utility.getUser());
		StatusEvent sEvent = new StatusEvent();
		sEvent.setId(UUID.randomUUID().toString());
		sEvent.setRequestId(requestId);
		sEvent.setStatus("STORED");
		sEvent.setUrl(dataShareDto.getUrl());
		sEvent.setTimestamp(Timestamp.valueOf(currentDtime).toString());
		creEvent.setPublishedOn(LocalDateTime.now().toString());
		creEvent.setPublisher("DIGITAL_CARD_SERVICE");
		creEvent.setTopic(topic);
		creEvent.setEvent(sEvent);
		webSubSubscriptionHelper.digitalCardStatusUpdateEvent(topic, creEvent);
		logger.info("publish event for topic : {} and rid : {}",topic,rid);
	}
}
	
