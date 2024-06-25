package io.mosip.digitalcard.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
//import springfox.documentation.builders.ApiInfoBuilder;
//import springfox.documentation.builders.PathSelectors;
//import springfox.documentation.builders.RequestHandlerSelectors;
//import springfox.documentation.service.ApiInfo;
//import springfox.documentation.spi.DocumentationType;
//import springfox.documentation.spring.web.plugins.Docket;
//import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

/**
 * Configuration class for swagger config
 * 
 * @author Dhanendra
 * @since 1.0.0
 *
 */
//@Configuration
//@EnableSwagger2
public class SwaggerConfig {

	/**
	 * Digital Card service Version
	 */
	private static final String DIGITALCARD_SERVICE_VERSION = "1.0";
	/**
	 * Application Title
	 */
	private static final String TITLE = "Digital Card Service";
	/**
	 * Digital Card Service
	 */
	private static final String DISCRIBTION = "Digital Card Service to print card";

	@Value("${application.env.local:false}")
	private Boolean localEnv;

	@Value("${swagger.base-url:#{null}}")
	private String swaggerBaseUrl;

	@Value("${server.port:8096}")
	private int serverPort;

	String proto = "http";
	String host = "localhost";
	int port = -1;
	String hostWithPort = "localhost:8080";

	/**
	 * Produces {@link ApiInfo}
	 * 
	 * @return {@link ApiInfo}
	 */
//	private ApiInfo apiInfo() {
//		return new ApiInfoBuilder().title(TITLE).description(DISCRIBTION).version(DIGITALCARD_SERVICE_VERSION).build();
//	}

	/**
	 * Produce Docket bean
	 * 
	 * @return Docket bean
	 */
//	@Bean
//	public Docket api() {
//		boolean swaggerBaseUrlSet = false;
//		if (!localEnv && swaggerBaseUrl != null && !swaggerBaseUrl.isEmpty()) {
//			try {
//				proto = new URL(swaggerBaseUrl).getProtocol();
//				host = new URL(swaggerBaseUrl).getHost();
//				port = new URL(swaggerBaseUrl).getPort();
//				if (port == -1) {
//					hostWithPort = host;
//				} else {
//					hostWithPort = host + ":" + port;
//				}
//				swaggerBaseUrlSet = true;
//			} catch (MalformedURLException e) {
//
//			}
//		}
//
//		Docket docket = new Docket(DocumentationType.SWAGGER_2).apiInfo(apiInfo())
//				.groupName(TITLE).select().apis(RequestHandlerSelectors.any())
//				.paths(PathSelectors.regex("(?!/(error).*).*")).build();
//		if (swaggerBaseUrlSet) {
//			docket.protocols(protocols()).host(hostWithPort);
//		}
//
//		return docket;
//	}

	private Set<String> protocols() {
		Set<String> protocols = new HashSet<>();
		protocols.add(proto);
		return protocols;
	}
}
