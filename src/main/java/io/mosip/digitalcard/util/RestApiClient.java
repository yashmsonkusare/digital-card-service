package io.mosip.digitalcard.util;

import java.io.IOException;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.net.ssl.SSLContext;

import io.mosip.digitalcard.constant.ApiName;
import io.mosip.digitalcard.dto.Metadata;
import io.mosip.digitalcard.dto.SecretKeyRequest;
import io.mosip.digitalcard.dto.TokenRequestDTO;
import io.mosip.digitalcard.exception.ApisResourceAccessException;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.DateUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * The Class RestApiClient.
 *
 * @author Rishabh Keshari
 */
@Component
public class RestApiClient {

	/** The logger. */

	Logger logger = DigitalCardRepoLogger.getLogger(RestApiClient.class);

	/** The builder. */
	@Autowired
	RestTemplateBuilder builder;

	@Autowired
	Environment environment;

	private static final String AUTHORIZATION = "Authorization=";


	/**
	 * Gets the api. *
	 * 
	 * @param              <T> the generic type
	 * @param getURI       the get URI
	 * @param responseType the response type
	 * @return the api
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public <T> T getApi(URI uri, Class<?> responseType) throws Exception {
		RestTemplate restTemplate;
		T result = null;
		try {
			restTemplate = getRestTemplate();
			result = (T) restTemplate.exchange(uri, HttpMethod.GET, setRequestHeader(null, null), responseType)
					.getBody();
		} catch (Exception e) {
			logger.error( e.getMessage() + ExceptionUtils.getStackTrace(e));
			throw e;
		}
		return result;
	}
	public <T> T postApi(String url, List<String> pathsegments, String queryParamName, String queryParamValue,
						 MediaType mediaType, Object requestType, Class<?> responseClass) throws Exception {
		T result = null;
		RestTemplate restTemplate;
		UriComponentsBuilder builder = null;
		builder = UriComponentsBuilder.fromUriString(url);
		if (!((pathsegments == null) || (pathsegments.isEmpty()))) {
			for (String segment : pathsegments) {
				if (!((segment == null) || (("").equals(segment)))) {
					builder.pathSegment(segment);
				}
			}

		}
		if (!((queryParamName == null) || (("").equals(queryParamName)))) {
			String[] queryParamNameArr = queryParamName.split(",");
			String[] queryParamValueArr = queryParamValue.split(",");

			for (int i = 0; i < queryParamNameArr.length; i++) {
				builder.queryParam(queryParamNameArr[i], queryParamValueArr[i]);
			}
		}
		logger.info("RestApiClient::postApi()::entry uri : {}",url);
		try {
		restTemplate = getRestTemplate();
		result = (T) restTemplate.postForObject(builder.toUriString(), setRequestHeader(requestType, mediaType),
				responseClass);
		} catch (Exception e) {
			logger.error( e.getMessage() + ExceptionUtils.getStackTrace(e));
			throw e;
		}
		return result;
	}

	/**
	 * Post api.
	 *
	 * @param <T>             the generic type
	 * @param apiName         the api name
	 * @param pathsegments    the pathsegments
	 * @param queryParamName  the query param name
	 * @param queryParamValue the query param value
	 * @param mediaType       the media type
	 * @param requestType     the request type
	 * @param responseClass   the response class
	 * @return the t
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public <T> T postApi(ApiName apiName, List<String> pathsegments, String queryParamName, String queryParamValue,
						 MediaType mediaType, Object requestType, Class<?> responseClass) throws Exception {
		T result = null;
		RestTemplate restTemplate;
		String apiHostIpPort = environment.getProperty(apiName.name());
		UriComponentsBuilder builder = null;
		if (apiHostIpPort != null)
			builder = UriComponentsBuilder.fromUriString(apiHostIpPort);
		if (builder != null) {

			if (!((pathsegments == null) || (pathsegments.isEmpty()))) {
				for (String segment : pathsegments) {
					if (!((segment == null) || (("").equals(segment)))) {
						builder.pathSegment(segment);
					}
				}

			}
			if (!((queryParamName == null) || (("").equals(queryParamName)))) {
				String[] queryParamNameArr = queryParamName.split(",");
				String[] queryParamValueArr = queryParamValue.split(",");

				for (int i = 0; i < queryParamNameArr.length; i++) {
					builder.queryParam(queryParamNameArr[i], queryParamValueArr[i]);
				}
			}
			try {
				restTemplate = getRestTemplate();
				logger.info("RestApiClient::postApi()::entry uri : {}",apiHostIpPort);
				result = (T) restTemplate.postForObject(builder.toUriString(), setRequestHeader(requestType, mediaType),
						responseClass);
			} catch (Exception e) {
				logger.error( e.getMessage() + ExceptionUtils.getStackTrace(e));
				throw e;
			}

		}
		return result;
	}


	public RestTemplate getRestTemplate() throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
		logger.info( Arrays.asList(environment.getActiveProfiles()).toString());
		if (Arrays.stream(environment.getActiveProfiles()).anyMatch("dev-k8"::equals)) {
			logger.info(
					Arrays.asList(environment.getActiveProfiles()).toString());
			return new RestTemplate();
		} else {
			TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;

			SSLContext sslContext = org.apache.http.ssl.SSLContexts.custom()
					.loadTrustMaterial(null, acceptingTrustStrategy).build();

			SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext);

			CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(csf).build();

			HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();

			requestFactory.setHttpClient(httpClient);
			return new RestTemplate(requestFactory);
		}

	}

	/**
	 * this method sets token to header of the request
	 *
	 * @param requestType
	 * @param mediaType
	 * @return
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	private HttpEntity<Object> setRequestHeader(Object requestType, MediaType mediaType) throws IOException {
		MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
		headers.add("Cookie", getToken());
		if (mediaType != null) {
			headers.add("Content-Type", mediaType.toString());
		}
		if (requestType != null) {
			try {
				HttpEntity<Object> httpEntity = (HttpEntity<Object>) requestType;
				HttpHeaders httpHeader = httpEntity.getHeaders();
				Iterator<String> iterator = httpHeader.keySet().iterator();
				while (iterator.hasNext()) {
					String key = iterator.next();
					if (!(headers.containsKey("Content-Type") && key == "Content-Type"))
						headers.add(key, httpHeader.get(key).get(0));
				}
				return new HttpEntity<Object>(httpEntity.getBody(), headers);
			} catch (ClassCastException e) {
				return new HttpEntity<Object>(requestType, headers);
			}
		} else
			return new HttpEntity<Object>(headers);
	}

	/**
	 * This method gets the token for the user details present in config server.
	 *
	 * @return
	 * @throws IOException
	 */
	public String getToken() throws IOException {
		String token = System.getProperty("token");
		boolean isValid = false;

		/*if (StringUtils.isNotEmpty(token)) {

			isValid = TokenHandlerUtil.isValidBearerToken(token, environment.getProperty("token.request.issuerUrl"),
					environment.getProperty("token.request.clientId"));


		}*/
		if (!isValid) {
		TokenRequestDTO<SecretKeyRequest> tokenRequestDTO = new TokenRequestDTO<SecretKeyRequest>();
		tokenRequestDTO.setId(environment.getProperty("digitalcard.token.request.id"));
		tokenRequestDTO.setMetadata(new Metadata());

		tokenRequestDTO.setRequesttime(DateUtils.getUTCCurrentDateTimeString());
		// tokenRequestDTO.setRequest(setPasswordRequestDTO());
		tokenRequestDTO.setRequest(setSecretKeyRequestDTO());
		tokenRequestDTO.setVersion(environment.getProperty("digitalcard.token.request.version"));

		Gson gson = new Gson();
		HttpClient httpClient = HttpClientBuilder.create().build();
		// HttpPost post = new
		// HttpPost(environment.getProperty("PASSWORDBASEDTOKENAPI"));
		HttpPost post = new HttpPost(environment.getProperty("KEYBASEDTOKENAPI"));
		try {
			StringEntity postingString = new StringEntity(gson.toJson(tokenRequestDTO));
			post.setEntity(postingString);
			post.setHeader("Content-type", "application/json");
			HttpResponse response = httpClient.execute(post);
			org.apache.http.HttpEntity entity = response.getEntity();
			String responseBody = EntityUtils.toString(entity, "UTF-8");
			Header[] cookie = response.getHeaders("Set-Cookie");
			token = response.getHeaders("Set-Cookie")[0].getValue();
				System.setProperty("token", token.substring(14, token.indexOf(';')));
			return token.substring(0, token.indexOf(';'));
		} catch (IOException e) {
			logger.error( e.getMessage() + ExceptionUtils.getStackTrace(e));
			throw e;
			}
		}
		return AUTHORIZATION + token;
	}

	private SecretKeyRequest setSecretKeyRequestDTO() {
		SecretKeyRequest request = new SecretKeyRequest();
		request.setAppId(environment.getProperty("digitalcard.token.request.appid"));
		request.setClientId(environment.getProperty("digitalcard.token.request.clientId"));
		request.setSecretKey(environment.getProperty("digitalcard.token.request.secretKey"));
		return request;
	}


}
