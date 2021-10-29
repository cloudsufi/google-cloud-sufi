package com.google.adapter.util;

import com.google.adapter.beans.ErrorDetails;
import com.google.adapter.constants.AdapterConstants;
import com.google.adapter.constants.ErrorCodes;
import com.sap.conn.jco.JCoException;
import com.sap.conn.jco.JCoRuntimeException;

import java.util.Objects;


public class ExceptionUtils {

    private final ReadErrorPropertyFile errorPropertyFile = ReadErrorPropertyFile.getInstance("error_mapping.json");


    /**
     * Convert error details in actual format
     * @param exception Exception
     * @param errorMessage Jco error message
     * @return ErrorDetails instance
     */
    public ErrorDetails convertErrorDetails(Exception exception, String errorMessage) {
        ErrorDetails errorDetails = null;

        if (exception instanceof JCoException) {
//            JCoException jCoException = (JCoException) exception.getCause();
            return fetchErrorDetails(exception, errorMessage);
        }

        if (exception instanceof JCoRuntimeException) {
            errorDetails = fetchJcoRuntimeExceptionDetails((JCoRuntimeException)exception, errorMessage);
        } else {
            errorDetails = fetchOtherErrorDetails(exception, errorMessage);
        }
        return errorDetails;
    }

    /**
     * Return exception in specified format
     *
     * @param jCoException if jco exception group is not present
     * @return error details in correct format
     */
    private ErrorDetails fetchErrorDetails(Exception jCoException, String errorMessage) {

        ErrorDetails errorDetails = null;
        if (ErrorCodes.byCode().contains(((JCoException)jCoException).getGroup() + "")) {
            errorDetails = errorPropertyFile.getErrorDetails(String.valueOf(((JCoException)jCoException).getGroup()));
            if (Objects.nonNull(errorMessage)) {
                errorDetails.setSecondaryMessage(errorMessage.trim());
            }
        }
        return errorDetails;
    }

    /**
     * Return exception in specified format
     *
     * @param exception if jco exception group is not present
     * @return error details in correct format
     */
    private ErrorDetails fetchOtherErrorDetails(Exception exception, String errorMessage) {
        ErrorDetails errorDetails = null;

        if (ErrorCodes.byCode().contains(exception.getMessage() + "")) {
            errorDetails = errorPropertyFile.getErrorDetails(String.valueOf(exception.getMessage()));
            errorDetails.setSecondaryCode(exception.getMessage());
            if (Objects.nonNull(errorMessage) &&
                    (exception.getMessage().equals(String.valueOf(AdapterConstants.JCO_INVALID_PARAMETER_ERROR_CODE)) ||
                            exception.getMessage().equals(String.valueOf(AdapterConstants.DESTINATION_EXCEPTION_120)))) {
                errorDetails.setSecondaryMessage(errorMessage.trim());
            }
        } else {
            errorDetails = errorPropertyFile.getErrorDetails(String.valueOf(AdapterConstants.JCO_CONNECTION_ERROR));
            errorDetails.setSecondaryCode(String.valueOf(AdapterConstants.JCO_CONNECTION_ERROR));
        }
        return errorDetails;
    }

    private ErrorDetails fetchJcoRuntimeExceptionDetails(JCoRuntimeException exception, String errorMessage) {
        ErrorDetails errorDetails = null;

        if (ErrorCodes.byCode().contains(String.valueOf(exception.getGroup()))) {
            errorDetails = errorPropertyFile.getErrorDetails(String.valueOf(exception.getGroup()));
            errorDetails.setSecondaryCode(String.valueOf(exception.getGroup()));
        } else {
            errorDetails = errorPropertyFile.getErrorDetails(String.valueOf(AdapterConstants.JCO_RUNTIME_EXCEPTION));
            errorDetails.setSecondaryCode(String.valueOf(exception.getGroup()));
        }

        if (Objects.nonNull(errorMessage)) {
            errorDetails.setSecondaryMessage(errorMessage.trim());
        } else {
            errorDetails.setSecondaryMessage(exception.getMessage().trim());
        }
        return errorDetails;
    }
}
