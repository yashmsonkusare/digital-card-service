package io.mosip.kernel.pdfcardgenerator.websub.impl;

import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.websub.spi.PublisherClient;
import io.mosip.kernel.core.websub.spi.SubscriptionClient;
import io.mosip.kernel.core.websub.spi.SubscriptionExtendedClient;
import io.mosip.kernel.pdfcardgenerator.constant.PdfCardGenConstants;
import io.mosip.kernel.pdfcardgenerator.constant.PdfCardGenErrorCodes;
import io.mosip.kernel.pdfcardgenerator.exception.PdfCardWebSubException;
import io.mosip.kernel.pdfcardgenerator.logger.PdfCardGeneratorLogger;
import io.mosip.kernel.pdfcardgenerator.websub.spi.WebSubEventSubcriber;
import io.mosip.kernel.pdfcardgenerator.websub.spi.WebSubEventTopicRegistrar;
import io.mosip.kernel.websub.api.exception.WebSubClientException;
import io.mosip.kernel.websub.api.model.FailedContentRequest;
import io.mosip.kernel.websub.api.model.FailedContentResponse;
import io.mosip.kernel.websub.api.model.SubscriptionChangeRequest;
import io.mosip.kernel.websub.api.model.SubscriptionChangeResponse;
import io.mosip.kernel.websub.api.model.UnsubscriptionRequest;

/**
 * Abstact class to subscribe & register for websub event.
 * @author Mahammed Taheer
 * @since 1.2.1
 */

@Component
public abstract class AbstractWebSubEventsInitializer implements WebSubEventTopicRegistrar, WebSubEventSubcriber {
    
    private static final Logger LOGGER = PdfCardGeneratorLogger.getLogger(AbstractWebSubEventsInitializer.class);


    /** The subscription client. */
	@Autowired
	protected SubscriptionClient<SubscriptionChangeRequest, UnsubscriptionRequest, SubscriptionChangeResponse> subscriptionClient;
	
	/** The subscription extended client. */
	@Autowired
	protected SubscriptionExtendedClient<FailedContentResponse, FailedContentRequest> subscriptionExtendedClient;

	/** The publisher. */
	@Autowired
	protected PublisherClient<String, Object, HttpHeaders> publisher;

    /**
	 * Subscribe.
	 *
	 * @param enableTester the enable tester
	 */
	@Override
	public void subscribe(Supplier<Boolean> enableTester) {
		if(enableTester == null || enableTester.get()) {
			try {
				doSubscribe();
			} catch (WebSubClientException e) {
				LOGGER.error(PdfCardGenConstants.SESSION_ID, PdfCardGenConstants.SUBSCRIBE,  this.getClass().getSimpleName(), ExceptionUtils.getStackTrace(e));
				throw new PdfCardWebSubException(PdfCardGenErrorCodes.SUBSCRIBE_ERROR.getErrorCode(), 
						PdfCardGenErrorCodes.SUBSCRIBE_ERROR.getErrorMessage(), e);
			}
		} else {
			LOGGER.info(PdfCardGenConstants.SESSION_ID, PdfCardGenConstants.SUBSCRIBE,  this.getClass().getSimpleName(), 
                    "This websub subscriber is disabled.");
		}
	}
	
	
	
	/**
	 * Register.
	 *
	 * @param enableTester the enable tester
	 */
	@Override
	public void register(Supplier<Boolean> enableTester) {
		if(enableTester == null || enableTester.get()) {
			doRegister();
		} else {
			LOGGER.info(PdfCardGenConstants.SESSION_ID, PdfCardGenConstants.REGISTER,  this.getClass().getSimpleName(), 
                    "This websub registrar is disabled.");
		}
	}
	
    /**
	 * Do subscribe.
	 */
	protected abstract void doSubscribe();

	/**
	 * Do register.
	 */
	protected abstract void doRegister();

}
