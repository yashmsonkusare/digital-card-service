package io.mosip.digitalcard.service.impl;

import io.mosip.digitalcard.dto.CreateInsuranceDto;
import io.mosip.digitalcard.dto.RegistrySearchRequestDto;
import io.mosip.digitalcard.exception.DigitalCardServiceException;
import io.mosip.digitalcard.service.SunbirdVCIService;
import io.mosip.digitalcard.util.DigitalCardRepoLogger;
import io.mosip.digitalcard.util.RestClient;
import io.mosip.digitalcard.util.TokenIDGenerator;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.DateUtils;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * The SunbirdVCIServiceImpl.
 *
 * @author Dhanendra
 */
@Service
public class SunbirdVCIServiceImpl implements SunbirdVCIService {

    Logger logger = DigitalCardRepoLogger.getLogger(SunbirdVCIServiceImpl.class);

    @Autowired
    private TokenIDGenerator tokenIDGenerator;
    @Value("${mosip.digitalcard.sunbird.vci.registry.url}")
    private String registryURL;

    @Value("${mosip.digitalcard.sunbird.vci.search.registry.url}")
    private String searchRegistryURL;

    @Value("${mosip.digitalcard.sunbird.vci.policy.Name}")
    private String policyName;

    @Value("${mosip.digitalcard.sunbird.vci.policy.expiry}")
    private String policyExpiry;

    @Value("${mosip.digitalcard.sunbird.vci.policy.issuedOn}")
    private String policyIssuedOn;

    @Value("${mosip.digitalcard.sunbird.vci.policy.id-prefix}")
    private String policyIdPrefix;

    @Value("${mosip.digitalcard.sunbird.vci.policy.search.max.count:15}")
    private int maxCount;

    @Value("${mosip.digitalcard.sunbird.vci.partner.id}")
    private String partnerId;

    @Value("${mosip.digitalcard.sunbird.auth.partner.id}")
    private String authpartnerId;

    @Value("${mosip.digitalcard.sunbird.vci.request.benefits}")
    private List<String> sunbirdVciBenefits;

    @Value("${mosip.digitalcard.service.datetime.pattern}")
    private String dateTimePattern;

    @Value("${mosip.digitalcard.service.policy.expiry.days:365}")
    private int policyExpiryDays;


    @Autowired
    RestClient restClient;

    @Override
    public void createRegistry(Map<String,Object> identity) {
        CreateInsuranceDto createInsuranceDto=new CreateInsuranceDto();
            try {
                createIssuanceRequest(identity,createInsuranceDto);
                String response=restClient.postApi(registryURL,null,"","", MediaType.APPLICATION_JSON,createInsuranceDto,String.class);
                identity.put("policyNumber",createInsuranceDto.getPolicyNumber());
                logger.info("Created Registry for UIN: {}",identity.get("UIN"));
            }  catch (Exception e) {
                logger.error("Failed Creating Registry for UIN: {}",identity.get("UIN"));
                throw new DigitalCardServiceException(e);
            }
    }

    @Override
    public String generatePolicyNumber(String policyIdPrefix) {
        int count=0;
        String policyNumber=null;
        try {
            while (count<=maxCount) {
                Random rnd = new Random();
                int number = rnd.nextInt(999999);
                policyNumber = policyIdPrefix +String.format("%06d", number);
                Map<String, Map<String, String>> filters=new HashMap<>();
                Map<String,String> filter=new HashMap<>();
                RegistrySearchRequestDto registrySearchRequestDto=new RegistrySearchRequestDto();
                registrySearchRequestDto.setOffset(0);
                registrySearchRequestDto.setLimit(1);
                filter.put("eq",policyNumber);
                filters.put("policyNumber",filter);
                registrySearchRequestDto.setFilters(filters);
                String response=restClient.postApi(searchRegistryURL,null,"","", MediaType.APPLICATION_JSON,registrySearchRequestDto,String.class);
                if (!response.contains("osid")) {
                    break;
                }
                count++;
            }
        }  catch (Exception e) {
            logger.error("Error while searching : {}",policyNumber);
            throw new DigitalCardServiceException(e);
        }
        return policyNumber;
    }

    private void createIssuanceRequest(Map<String,Object> identity,CreateInsuranceDto createInsuranceDto) throws JSONException {
        DateTimeFormatter format = DateTimeFormatter.ofPattern(dateTimePattern);
        LocalDateTime localdatetime = LocalDateTime
                .parse(DateUtils.getUTCCurrentDateTimeString(dateTimePattern), format);

        createInsuranceDto.setFullName((String) identity.get("fullName_eng"));
        createInsuranceDto.setDob(convertDateOfBirth((String) identity.get("dateOfBirth")));
        createInsuranceDto.setEmail((String) identity.get("email"));
        createInsuranceDto.setGender(formatGender((String) identity.get("gender_eng")));
        createInsuranceDto.setMobile((String) identity.get("phone"));
        createInsuranceDto.setBenefits(sunbirdVciBenefits);
        createInsuranceDto.setPolicyName(policyName);
        createInsuranceDto.setPolicyExpiresOn(String.valueOf(localdatetime.plusDays(policyExpiryDays))+"Z");
        createInsuranceDto.setPolicyIssuedOn(String.valueOf(localdatetime)+"Z");
        createInsuranceDto.setPolicyNumber(generatePolicyNumber(policyIdPrefix));
        createInsuranceDto.setPsut(generatePsut((String) identity.get("UIN"),partnerId));
    }
    public String formatGender(String gender){
        if(gender.equalsIgnoreCase("MLE")){
            return "Male";
        } else if (gender.equalsIgnoreCase("FLE")) {
            return "Female";
        }else if (gender.equalsIgnoreCase("OTH")) {
            return "Others";
        }
        return gender;
    }
    public String convertDateOfBirth(String dateOfBirth){
        java.util.Date date = new Date(dateOfBirth);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        return formatter.format(date);
    }

   private String generatePsut(String individualId,String partnerId){
        String token= tokenIDGenerator.generateTokenID(individualId,authpartnerId);
        return tokenIDGenerator.generateTokenID(token,partnerId);
    }


}
