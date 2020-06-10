package org.tbbtalent.server.util.dto;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.tbbtalent.server.exception.InvalidCredentialsException;
import org.tbbtalent.server.exception.ServiceException;


@ControllerAdvice
public class ErrorHandler {

    private static final Logger log = LoggerFactory.getLogger(ErrorHandler.class);


    @ExceptionHandler(ServiceException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorDTO processServiceException(ServiceException ex) {
        log.error("Processing ServiceException: " + ex);
        ErrorDTO errorDTO = new ErrorDTO(ex.getErrorCode(), ex.getMessage());
        return errorDTO;
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorDTO processInvalidCredentialsException(InvalidCredentialsException ex) {
        log.info("Processing : InvalidCredentialsException: " + ex);
        return new ErrorDTO("invalid_credentials", ex.getMessage());
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public ErrorDTO processNoHandlerFoundException(NoHandlerFoundException ex) {
        log.error("Processing NoHandlerFoundException: " + ex);
        return new ErrorDTO("handler_not_found", ex.getMessage());
    }    
    
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorDTO processOtherException(Exception ex) {
        final String code = "unexpected_exception";
        log.error(code, ex);
        log.info(ex.toString());
        return new ErrorDTO(code, ex.toString());
    }    

    //-------------------------------------------------------------------------

    private class ErrorDTO {

        private long timestamp;
        private String code;
        private String message;
        private Object data;

        public ErrorDTO(String code, String message) {
            this.code = code;
            this.message = message;
            this.timestamp = System.currentTimeMillis();
        }

        public long getTimestamp() {
            return timestamp;
        }

        public String getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }

        public Object getData() {
            return data;
        }

        public void setData(Object data) {
            this.data = data;
        }
    }

    //-------------------------------------------------------------------------

    private class ValidationErrorDTO extends ErrorDTO {

        private List<FieldErrorDTO> fieldErrors = new ArrayList<>();

        public ValidationErrorDTO(String code, String message) {
            super(code, message);
        }

        public void addFieldError(String path, String code, String message) {
            FieldErrorDTO error = new FieldErrorDTO(path, code, message);
            fieldErrors.add(error);
        }

        public List<FieldErrorDTO> getFieldErrors() {
            return fieldErrors;
        }
    }

    //-------------------------------------------------------------------------

    private class FieldErrorDTO {

        private String field;
        private String code;
        private String message;

        public FieldErrorDTO(String field, String code, String message) {
            this.field = field;
            this.code = code;
            this.message = message;
        }

        public String getField() {
            return field;
        }

        public String getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }
    }
}
