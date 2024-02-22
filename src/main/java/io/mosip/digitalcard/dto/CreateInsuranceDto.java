package io.mosip.digitalcard.dto;

import lombok.Data;

import java.util.List;

@Data
public class CreateInsuranceDto {

    private String policyNumber;
    private String policyName;
    private String policyExpiresOn;
    private String  policyIssuedOn;
    private String fullName;
    private String dob;
    private String  email;
    private String mobile;
    private String gender;
    private String psut;
    private List<String> benefits;
}
