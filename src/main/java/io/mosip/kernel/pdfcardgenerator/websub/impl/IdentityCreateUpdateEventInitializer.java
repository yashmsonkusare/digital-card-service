package io.mosip.kernel.pdfcardgenerator.websub.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.pdfcardgenerator.constant.PdfCardGenConstants;
import io.mosip.kernel.pdfcardgenerator.logger.PdfCardGeneratorLogger;
import io.mosip.kernel.websub.api.model.SubscriptionChangeRequest;

/**
 * Base class to subscribe & register for Identity Create/Update websub event.
 * @author Mahammed Taheer
 * @since 1.2.1
 */


@Component
public class IdentityCreateUpdateEventInitializer extends AbstractWebSubEventsInitializer {

    private static final Logger LOGGER = PdfCardGeneratorLogger.getLogger(AbstractWebSubEventsInitializer.class);

    @Value("${mosip.pdfcard.generate.websub.hubURL}")
	private String webSubHubUrl;

	@Value("${mosip.pdfcard.generate.identity.change.websub.secret}")
	private String webSubSecret;

	@Value("${mosip.pdfcard.generate.identity.change.websub.callBackUrl}")
	private String webSubCallbackUrl;

	@Value("${mosip.pdfcard.generate.identity.change.websub.topic}")
	private String webSubTopic;

    
    @Override
    protected void doSubscribe() {
        LOGGER.info(PdfCardGenConstants.SESSION_ID, PdfCardGenConstants.SUBSCRIBE, this.getClass().getSimpleName(), 
                    "Trying Websub Subscribtion for Identity CreateUpdate Event.");
        SubscriptionChangeRequest subscriptionRequest = new SubscriptionChangeRequest();
        subscriptionRequest.setCallbackURL(webSubCallbackUrl);
        subscriptionRequest.setHubURL(webSubHubUrl);
        subscriptionRequest.setSecret(webSubSecret);
        subscriptionRequest.setTopic(webSubTopic);
        subscriptionClient.subscribe(subscriptionRequest);
        LOGGER.info(PdfCardGenConstants.SESSION_ID, PdfCardGenConstants.SUBSCRIBE, this.getClass().getSimpleName(), 
                    "Websub Subscribtion Completed for Identity CreateUpdate Event.");
    }

    @Override
    protected void doRegister() {
        LOGGER.info(PdfCardGenConstants.SESSION_ID, PdfCardGenConstants.SUBSCRIBE, this.getClass().getSimpleName(), 
                    "Trying Websub Topic Registration for Identity CreateUpdate Event.");
        publisher.registerTopic(webSubTopic, webSubHubUrl);
        LOGGER.info(PdfCardGenConstants.SESSION_ID, PdfCardGenConstants.SUBSCRIBE, this.getClass().getSimpleName(), 
                    "Websub Topic Registration Completed for Identity CreateUpdate Event.");
    }
    
}
