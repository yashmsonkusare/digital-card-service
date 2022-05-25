package io.mosip.kernel.pdfcardgenerator.websub.spi;

import java.util.function.Supplier;

/**
 * Interface to subscribe to websub event.
 * @author Mahammed Taheer
 * @since 1.2.1
 */
public interface WebSubEventSubcriber {
    
    /**
	 * subscribe to websub topic.
	 *
	 * @param enableTester the enable tester
    */
	void subscribe(Supplier<Boolean> enableTester);
}
