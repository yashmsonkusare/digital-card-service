package io.mosip.digitalcard.service.impl;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.mosip.biometrics.util.ConvertRequestDto;
import io.mosip.biometrics.util.face.FaceDecoder;
import io.mosip.digitalcard.constant.DigitalCardServiceErrorCodes;
import io.mosip.digitalcard.constant.IdType;
import io.mosip.digitalcard.constant.PDFGeneratorExceptionCodeConstant;
import io.mosip.digitalcard.constant.UinCardType;
import io.mosip.digitalcard.dto.DataShareDto;
import io.mosip.digitalcard.dto.JsonValue;
import io.mosip.digitalcard.service.PDFCardService;
import io.mosip.digitalcard.websub.CredentialStatusEvent;
import io.mosip.digitalcard.websub.StatusEvent;
import io.mosip.digitalcard.exception.ApiNotAccessibleException;
import io.mosip.digitalcard.exception.DataShareException;
import io.mosip.digitalcard.exception.DigitalCardServiceException;
import io.mosip.digitalcard.exception.IdentityNotFoundException;
import io.mosip.digitalcard.repositories.DigitalCardTransactionRepository;
import io.mosip.digitalcard.service.UinCardGenerator;
import io.mosip.digitalcard.util.*;
import io.mosip.digitalcard.websub.WebSubSubscriptionHelper;
import io.mosip.kernel.core.cbeffutil.spi.CbeffUtil;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.pdfgenerator.exception.PDFGeneratorException;
import io.mosip.kernel.core.qrcodegenerator.exception.QrcodeGenerationException;
import io.mosip.kernel.core.qrcodegenerator.spi.QrCodeGenerator;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.kernel.core.websub.model.EventModel;
import io.mosip.kernel.qrcode.generator.zxing.constant.QrVersion;
import io.mosip.vercred.CredentialsVerifier;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;
import org.json.JSONException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URI;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class PDFCardServiceImpl implements PDFCardService {

	@Value("${mosip.digitalcard.websub.publish.topic:CREDENTIAL_STATUS_UPDATE}")
	private String topic;
	
	@Autowired
	private WebSubSubscriptionHelper webSubSubscriptionHelper;

	@Autowired
	private DataShareUtil dataShareUtil;

	@Autowired
	private RestClient restApiClient;

	/** The Constant FILE_SEPARATOR. */
	public static final String FILE_SEPARATOR = File.separator;

	/** The Constant VALUE. */
	private static final String VALUE = "value";

	@Value("${mosip.digitalcard.templateTypeCode:RPR_UIN_CARD_TEMPLATE}")
	private String uinCardTemplate;

	/** The Constant FACE. */
	private static final String FACE = "Face";

	/** The Constant UIN_TEXT_FILE. */
	private static final String UIN_TEXT_FILE = "textFile";

	/** The Constant APPLICANT_PHOTO. */
	private static final String APPLICANT_PHOTO = "ApplicantPhoto";

	/** The Constant QRCODE. */
	private static final String QRCODE = "QrCode";

	/** The Constant UINCARDPASSWORD. */
	private static final String UINCARDPASSWORD = "mosip.digitalcard.uincard.password";

	/** The print logger. */
	Logger printLogger = DigitalCardRepoLogger.getLogger(PDFCardServiceImpl.class);

	/** The template generator. */
	@Autowired
	private TemplateGenerator templateGenerator;

	/** The utilities. */
	@Autowired
	private Utility utility;

	@Autowired
	private EncryptionUtil encryptionUtil;

	/** The uin card generator. */
	@Autowired
	private UinCardGenerator<byte[]> uinCardGenerator;

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

	@Autowired
	private CredentialsVerifier credentialsVerifier;

	@Value("${mosip.digitalcard.datashare.partner.id}")
	private String partnerId;

	@Value("${mosip.digitalcard.datashare.policy.id}")
	private String policyId;

	@Value("${mosip.template-language}")
	private String templateLang;

	@Value("#{'${mosip.mandatory-languages:}'.concat('${mosip.optional-languages:}')}")
	private String supportedLang;

	@Value("${mosip.digitalcard.verify.credentials.flag:true}")
	private boolean verifyCredentialsFlag;

	@Value("${mosip.digitalcard.pdf.password.enable.flag:true}")
	private boolean pdfPasswordFlag;

	@Autowired
	private ObjectMapper objectMapper;

	public PDFCardServiceImpl() {
	}


	public boolean generateCard(EventModel eventModel) {
		String credential = null;
		boolean isPrinted = false;
		String decryptedCredential=null;
		try {
			if (eventModel.getEvent().getDataShareUri() == null || eventModel.getEvent().getDataShareUri().isEmpty()) {
				credential = eventModel.getEvent().getData().get("credential").toString();
			} else {
				String dataShareUrl = eventModel.getEvent().getDataShareUri();
				URI dataShareUri = URI.create(dataShareUrl);
				credential = restApiClient.getForObject(dataShareUrl, String.class);
			}
			String ecryptionPin = null;
			//eventModel.getEvent().getData().get("protectionKey").toString();
			decryptedCredential = encryptionUtil.decryptData(credential);
			if (verifyCredentialsFlag){
				printLogger.info("Configured received credentials to be verified. Flag {}", verifyCredentialsFlag);
				boolean verified =credentialsVerifier.verifyCredentials(decryptedCredential);
				if (!verified) {
					printLogger.error("Received Credentials failed in verifiable credential verify method. So, the credentials will not be printed." +
						" Id: {}, Transaction Id: {}", eventModel.getEvent().getId(), eventModel.getEvent().getTransactionId());
					return false;
				}
			}
			Map proofMap = new HashMap<String, String>();
			proofMap = (Map) eventModel.getEvent().getData().get("proof");
			byte[] pdfbytes = getDocuments(decryptedCredential,
					eventModel.getEvent().getData().get("credentialType").toString(), ecryptionPin,
					eventModel.getEvent().getTransactionId(), "UIN", true).get("uinPdf");
			isPrinted = true; 
		}catch (Exception e){
			printLogger.error(e.getMessage() , e);
			isPrinted = false;
		}
		return isPrinted;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.print.service.PrintService#
	 * getDocuments(io.mosip.registration.processor.core.constant.IdType,
	 * java.lang.String, java.lang.String, boolean)
	 */
	private Map<String, byte[]> getDocuments(String credential, String credentialType, String encryptionPin,
			String requestId,
			String cardType,
			boolean isPasswordProtected) {
		printLogger.debug("PrintServiceImpl::getDocuments()::entry");
		String credentialSubject;
		Map<String, byte[]> byteMap = new HashMap<>();
		String uin = null;
		String rid=null;
		String password = null;
		boolean isPhotoSet=false;
		String individualBio = null;
		Map<String, Object> attributes = new LinkedHashMap<>();
		boolean isTransactionSuccessful = false;
		String template = uinCardTemplate;
		byte[] pdfbytes = null;
		try {
			credentialSubject = getCrdentialSubject(credential);
			org.json.JSONObject decryptedJson =new org.json.JSONObject(credentialSubject);
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
				pdfbytes = uinCardGenerator.generateUinCard(uinArtifact, UinCardType.PDF,
						password);

			} else {
			if (!isPhotoSet) {
				printLogger.debug(DigitalCardServiceErrorCodes.APPLICANT_PHOTO_NOT_SET.name());
			}
			setTemplateAttributes(decryptedJson.toString(), attributes);
			attributes.put(IdType.UIN.toString(), uin);
			byte[] textFileByte = createTextFile(decryptedJson.toString());
			byteMap.put(UIN_TEXT_FILE, textFileByte);
			boolean isQRcodeSet = setQrCode(decryptedJson.toString(), attributes,isPhotoSet);
			rid=getRid(decryptedJson.get("id"));
			if (!isQRcodeSet) {
				printLogger.debug(DigitalCardServiceErrorCodes.QRCODE_NOT_SET.name());
			}
			// getting template and placing original valuespng
			InputStream uinArtifact = templateGenerator.getTemplate(template, attributes, templateLang);
			if (uinArtifact == null) {
				printLogger.error(DigitalCardServiceErrorCodes.TEM_PROCESSING_FAILURE.name());
				throw new DigitalCardServiceException(
						DigitalCardServiceErrorCodes.TEM_PROCESSING_FAILURE.getErrorCode(),DigitalCardServiceErrorCodes.TEM_PROCESSING_FAILURE.getErrorMessage());
			}

			pdfbytes = uinCardGenerator.generateUinCard(uinArtifact, UinCardType.PDF, password);
				InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(pdfbytes));
				  File pdfFile = new File("src/main/resources/uin.pdf");
				  OutputStream os = new FileOutputStream(pdfFile);
				  os.write(pdfbytes);
				  os.close();
		}
			printStatusUpdate(requestId, pdfbytes, credentialType,rid);
			isTransactionSuccessful = true;

		}
		catch (QrcodeGenerationException e) {

			printLogger.error(DigitalCardServiceErrorCodes.QRCODE_NOT_GENERATED.name() , e);
			throw new PDFGeneratorException(PDFGeneratorExceptionCodeConstant.PDF_EXCEPTION.getErrorCode(),
					e.getErrorText());

		}  catch (PDFGeneratorException e) {

			printLogger.error(DigitalCardServiceErrorCodes.PDF_NOT_GENERATED.name() ,e);
			throw new PDFGeneratorException(PDFGeneratorExceptionCodeConstant.PDF_EXCEPTION.getErrorCode(),
					e.getErrorText());

		}catch (Exception ex) {
			printLogger.error(ex.getMessage() ,ex);
			throw new PDFGeneratorException(PDFGeneratorExceptionCodeConstant.PDF_EXCEPTION.getErrorCode(),
					ex.getMessage() ,ex);

		}
		printLogger.debug("PrintServiceImpl::getDocuments()::exit");
		return byteMap;
	}

	private String getRid(Object id) {
		String rid= id.toString().split("/credentials/")[1];
		return rid;
	}

	/**
	 * Creates the text file.
	 *
	 * @param jsonString
	 *            the attributes
	 * @return the byte[]
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private byte[] createTextFile(String jsonString) throws Exception {

		LinkedHashMap<String, String> printTextFileMap = new LinkedHashMap<>();
		JSONObject demographicIdentity = objectMapper.readValue(jsonString, JSONObject.class);
		if (demographicIdentity == null)
			throw new IdentityNotFoundException(DigitalCardServiceErrorCodes.IDENTITY_NOT_FOUND.getErrorCode(),DigitalCardServiceErrorCodes.IDENTITY_NOT_FOUND.getErrorMessage());
		String printTextFileJson = utility.getPrintTextFileJson(utility.getConfigServerFileStorageURL(),
				utility.getPrintTextFile());
		JSONObject printTextFileJsonObject = objectMapper.readValue(printTextFileJson, JSONObject.class);
		Set<String> printTextFileJsonKeys = printTextFileJsonObject.keySet();
		for (String key : printTextFileJsonKeys) {
			String printTextFileJsonString = utility.getJSONValue(printTextFileJsonObject, key);
			for (String value : printTextFileJsonString.split(",")) {
				Object object = demographicIdentity.get(value);
				if (object instanceof ArrayList) {
					JSONArray node = utility.getJSONArray(demographicIdentity, value);
					JsonValue[] jsonValues = Utility.mapJsonNodeToJavaObject(JsonValue.class, node);
					for (JsonValue jsonValue : jsonValues) {
						/*
						 * if (jsonValue.getLanguage().equals(primaryLang)) printTextFileMap.put(value +
						 * "_" + primaryLang, jsonValue.getValue()); if
						 * (jsonValue.getLanguage().equals(secondaryLang)) printTextFileMap.put(value +
						 * "_" + secondaryLang, jsonValue.getValue());
						 */
						if (supportedLang.contains(jsonValue.getLanguage()))
							printTextFileMap.put(value + "_" + jsonValue.getLanguage(), jsonValue.getValue());

					}

				} else if (object instanceof LinkedHashMap) {
					JSONObject json = utility.getJSONObject(demographicIdentity, value);
					printTextFileMap.put(value, (String) json.get(VALUE));
				} else {
					printTextFileMap.put(value, (String) object);

				}
			}

		}

		Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
		String printTextFileString = gson.toJson(printTextFileMap);
		return printTextFileString.getBytes();
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
	private void setTemplateAttributes(String jsonString, Map<String, Object> attribute)
			throws Exception {
		try {
			JSONObject demographicIdentity = objectMapper.readValue(jsonString, JSONObject.class);
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
					Object object = demographicIdentity.get(value);
					if (object != null) {
						try {
						obj = new JSONParser().parse(object.toString());
						} catch (Exception e) {
							obj = object;
						}
					
					if (obj instanceof JSONArray) {
						// JSONArray node = JsonUtil.getJSONArray(demographicIdentity, value);
						JsonValue[] jsonValues = Utility.mapJsonNodeToJavaObject(JsonValue.class, (JSONArray) obj);
						for (JsonValue jsonValue : jsonValues) {
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
			printLogger.error("Error while parsing Json file" ,e);
			throw e;
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
				JsonValue[] jsonValues = Utility.mapJsonNodeToJavaObject(JsonValue.class, (JSONArray) obj);
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
	private String getParameter(JsonValue[] jsonValues, String langCode) {

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

	private void printStatusUpdate(String requestId, byte[] data, String credentialType, String rid)
			throws DataShareException, ApiNotAccessibleException, IOException, Exception {
		DataShareDto dataShareDto = null;
		dataShareDto = dataShareUtil.getDataShare(data, policyId, partnerId);
		CredentialStatusEvent creEvent = new CredentialStatusEvent();
		LocalDateTime currentDtime = DateUtils.getUTCCurrentDateTime();
		digitalCardTransactionRepository.updateTransactionDetails(rid,"AVAILABLE", dataShareDto.getUrl());
		StatusEvent sEvent = new StatusEvent();
		sEvent.setId(UUID.randomUUID().toString());
		sEvent.setRequestId(requestId);
		sEvent.setStatus("STORED");
		sEvent.setUrl(dataShareDto.getUrl());
		sEvent.setTimestamp(Timestamp.valueOf(currentDtime).toString());
		creEvent.setPublishedOn(LocalDateTime.now().toString());
		creEvent.setPublisher("PRINT_SERVICE");
		creEvent.setTopic(topic);
		creEvent.setEvent(sEvent);
		webSubSubscriptionHelper.printStatusUpdateEvent(topic, creEvent);
	}
}
	
