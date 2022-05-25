package io.mosip.kernel.pdfcardgenerator.websub.spi;

import java.util.function.Supplier;

/**
 * The Interface to register websub topic.
 * @author Mahammed Taheer
 * @since 1.2.1
 * 
 */
public interface WebSubEventTopicRegistrar {
   
    /**
	 * Register to websub.
	 *
	 * @param enableTester the enable tester
	 */
	void register(Supplier<Boolean> enableTester);
}
