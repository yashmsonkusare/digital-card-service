package io.mosip.digitalcard.service;

import org.json.JSONObject;

import java.util.Map;

public interface SunbirdVCIService {

    public void createRegistry(Map<String,Object> identity);

    public String searchPolicy(String policy);
}
