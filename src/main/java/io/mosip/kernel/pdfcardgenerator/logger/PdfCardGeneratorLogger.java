package io.mosip.kernel.pdfcardgenerator.logger;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.logger.logback.factory.Logfactory;

/**
 * PDF Card Generator logger.
 * 
 * @author Mahammed Taheer
 * @since 1.2.1
 *
 */
public class PdfCardGeneratorLogger {
    
    /**
	 * Instantiates a new logger.
	 */
	private PdfCardGeneratorLogger() {
	}

	/**
	 * Method to get the rolling file logger for the class provided.
	 *
	 * @param clazz the clazz
	 * @return the logger
	 */
	public static Logger getLogger(Class<?> clazz) {
		return Logfactory.getSlf4jLogger(clazz);
	}
}
