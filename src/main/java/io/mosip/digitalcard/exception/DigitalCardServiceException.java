package io.mosip.digitalcard.exception;

import io.mosip.digitalcard.constant.DigitalCardServiceErrorCodes;
import io.mosip.kernel.core.exception.BaseCheckedException;
import io.mosip.kernel.core.exception.BaseUncheckedException;

public class DigitalCardServiceException extends BaseUncheckedException {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public DigitalCardServiceException() {
        super(DigitalCardServiceErrorCodes.DATASHARE_EXCEPTION.getErrorCode(),
                DigitalCardServiceErrorCodes.DATASHARE_EXCEPTION.getErrorMessage());
    }

    public DigitalCardServiceException(String message) {
        super(DigitalCardServiceErrorCodes.DATASHARE_EXCEPTION.getErrorCode(),
                message);
    }

    public DigitalCardServiceException(Throwable e) {
        super(DigitalCardServiceErrorCodes.DATASHARE_EXCEPTION.getErrorCode(),
                DigitalCardServiceErrorCodes.DATASHARE_EXCEPTION.getErrorMessage(), e);
    }
    public DigitalCardServiceException(String errorCode,String message) {
        super(DigitalCardServiceErrorCodes.DATASHARE_EXCEPTION.getErrorCode(),
                message);
    }
    public DigitalCardServiceException(String errorMessage, Throwable t) {
        super(DigitalCardServiceErrorCodes.DATASHARE_EXCEPTION.getErrorCode(), errorMessage, t);
    }
}
