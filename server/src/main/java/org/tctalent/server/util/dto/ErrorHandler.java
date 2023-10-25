/*
 * Copyright (c) 2023 Talent Beyond Boundaries.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.util.dto;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.tctalent.server.exception.InvalidCredentialsException;
import org.tctalent.server.exception.PasswordExpiredException;
import org.tctalent.server.exception.ReCaptchaInvalidException;
import org.tctalent.server.exception.ServiceException;
import org.tctalent.server.exception.UserDeactivatedException;
import org.tctalent.server.service.db.email.EmailHelper;

import javax.security.auth.login.AccountLockedException;


@ControllerAdvice
public class ErrorHandler {

    private static final Logger log = LoggerFactory.getLogger(ErrorHandler.class);
    private final EmailHelper emailHelper;
    private boolean beenSent = false;

    @Autowired
    public ErrorHandler(EmailHelper emailHelper){
        this.emailHelper = emailHelper;
    }

    @ExceptionHandler(ServiceException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorDTO processServiceException(ServiceException ex) {
        log.error("Processing ServiceException: " + ex);
        return new ErrorDTO(ex.getErrorCode(), ex.getMessage());
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ResponseBody
    public ErrorDTO processInvalidCredentialsException(InvalidCredentialsException ex) {
        log.info("Processing : InvalidCredentialsException: " + ex);
        return new ErrorDTO("invalid_credentials", ex.getMessage());
    }

    @ExceptionHandler(AccountLockedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ResponseBody
    public ErrorDTO processAccountLockedException(AccountLockedException ex) {
        log.info("Processing : AccountLockedException: " + ex);
        return new ErrorDTO("account_locked", ex.getMessage());
    }

    @ExceptionHandler(UserDeactivatedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ResponseBody
    public ErrorDTO processUserDeactivatedExceptionException(UserDeactivatedException ex) {
        log.info("Processing : UserDeactivatedException: " + ex);
        return new ErrorDTO("user_deactivated", ex.getMessage());
    }

    @ExceptionHandler(PasswordExpiredException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ResponseBody
    public ErrorDTO processPasswordExpiredException(PasswordExpiredException ex) {
        log.info("Processing : PasswordExpiredException: " + ex);
        return new ErrorDTO("password_expired", ex.getMessage());
    }

    @ExceptionHandler(ReCaptchaInvalidException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ResponseBody
    public ErrorDTO processReCaptchaInvalidException(ReCaptchaInvalidException ex) {
        log.info("Processing : ReCaptchaInvalidException: " + ex);
        return new ErrorDTO("recaptcha_invalid", ex.getMessage());
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public ErrorDTO processNoHandlerFoundException(NoHandlerFoundException ex) {
        //Don't need exception traceback - this is probably just robots probing the site
        log.error("Processing NoHandlerFoundException: " + ex);
        return new ErrorDTO("handler_not_found", ex.getMessage());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public ErrorDTO processHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException ex) {
        //Don't need exception traceback - this is probably just robots probing the site
        log.error("Processing HttpRequestMethodNotSupportedException: " + ex);
        return new ErrorDTO("unsupported_http_request", ex.getMessage());
    }

    @ExceptionHandler(NullPointerException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorDTO processNullException(NullPointerException ex) {
        final String code = "null_exception";
        log.error(code, ex);
        if(!beenSent) {
            try{
                emailHelper.sendAlert(code, ex);
                beenSent = true;
            } catch (Exception e) {
                log.error("Error sending null exception email", e);
            }
        }
        return new ErrorDTO(code, ex.toString());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorDTO processOtherException(Exception ex) {
        final String code = "unexpected_exception";
        log.error(code, ex);
        return new ErrorDTO(code, ex.toString());
    }

    @Getter
    public static class ErrorDTO {

        private final long timestamp;
        private final String code;
        private final String message;
        private Object data;

        public ErrorDTO(String code, String message) {
            this.code = code;
            this.message = message;
            this.timestamp = System.currentTimeMillis();
        }

        public void setData(Object data) {
            this.data = data;
        }
    }

}
