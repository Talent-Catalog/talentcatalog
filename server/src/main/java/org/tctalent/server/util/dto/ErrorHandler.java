/*
 * Copyright (c) 2024 Talent Catalog.
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

import javax.security.auth.login.AccountLockedException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.tctalent.server.exception.InvalidCredentialsException;
import org.tctalent.server.exception.InvalidSessionException;
import org.tctalent.server.exception.PasswordExpiredException;
import org.tctalent.server.exception.ReCaptchaInvalidException;
import org.tctalent.server.exception.ServiceException;
import org.tctalent.server.exception.UserDeactivatedException;
import org.tctalent.server.logging.LogBuilder;
import org.tctalent.server.service.db.email.EmailHelper;


@ControllerAdvice
@Slf4j
public class ErrorHandler {
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
        LogBuilder.builder(log)
            .action("ServiceException")
            .message("Processing : ServiceException: " + ex)
            .logError();

        return new ErrorDTO(ex.getErrorCode(), ex.getMessage());
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ResponseBody
    public ErrorDTO processInvalidCredentialsException(InvalidCredentialsException ex) {
        LogBuilder.builder(log)
            .action("InvalidCredentialsException")
            .message("Processing : InvalidCredentialsException: " + ex)
            .logInfo();

        return new ErrorDTO("invalid_credentials", ex.getMessage());
    }

    @ExceptionHandler(AccountLockedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ResponseBody
    public ErrorDTO processAccountLockedException(AccountLockedException ex) {
        LogBuilder.builder(log)
            .action("AccountLockedException")
            .message("Processing : AccountLockedException: " + ex)
            .logInfo();

        return new ErrorDTO("account_locked", ex.getMessage());
    }

    @ExceptionHandler(UserDeactivatedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ResponseBody
    public ErrorDTO processUserDeactivatedExceptionException(UserDeactivatedException ex) {
        LogBuilder.builder(log)
            .action("UserDeactivatedException")
            .message("Processing : UserDeactivatedException: " + ex)
            .logInfo();

        return new ErrorDTO("user_deactivated", ex.getMessage());
    }

    @ExceptionHandler(PasswordExpiredException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ResponseBody
    public ErrorDTO processPasswordExpiredException(PasswordExpiredException ex) {
        LogBuilder.builder(log)
            .action("PasswordExpiredException")
            .message("Processing : PasswordExpiredException: " + ex)
            .logInfo();

        return new ErrorDTO("password_expired", ex.getMessage());
    }

    @ExceptionHandler(ReCaptchaInvalidException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ResponseBody
    public ErrorDTO processReCaptchaInvalidException(ReCaptchaInvalidException ex) {
        LogBuilder.builder(log)
            .action("ReCaptchaInvalidException")
            .message("Processing : ReCaptchaInvalidException: " + ex)
            .logInfo();

        return new ErrorDTO("recaptcha_invalid", ex.getMessage());
    }

    /**
     * Handles InvalidSessionException by returning HTTP 401 (Unauthorized).
     * <p>
     * This exception is thrown when a user attempts to access a protected resource
     * but has no active session (i.e., is not logged in). While InvalidSessionException
     * extends ServiceException (which would normally return 400 Bad Request), this
     * specific handler catches it and returns 401 Unauthorized, which is the semantically
     * correct HTTP status code for authentication failures.
     * <p>
     * This distinguishes it from InvalidCredentialsException, which is used when
     * incorrect credentials are provided during login. InvalidSessionException is
     * used when a session is missing or expired during API calls.
     * <p>
     * <b>Frontend Integration:</b> The Angular error interceptors in the admin-portal and
     * candidate-portal automatically detect 401 responses and trigger user logout and
     * redirect to the login page. This handler will handle users with expired or invalid sessions
     * so they are properly logged out and can re-authenticate. See:
     * <ul>
     *   <li>ui/admin-portal/src/app/services/error.interceptor.ts</li>
     *   <li>ui/candidate-portal/src/app/services/error.interceptor.ts</li>
     *   <li>ui/admin-portal/src/app/services/auth-expiry.interceptor.ts</li>
     * </ul>
     * <p>
     * <b>Thrown from:</b>
     * <ul>
     *   <li>CASI module: ServicesAdminController (assignToCandidate, assignToList)</li>
     *   <li>UserServiceImpl: fetchLoggedInUser() when no active session exists</li>
     *   <li>SavedSearchServiceImpl: various methods requiring authentication</li>
     *   <li>JobServiceImpl: methods requiring logged-in user</li>
     *   <li>Various service implementations when authService.getLoggedInUser() returns empty</li>
     * </ul>
     *
     * @param ex the InvalidSessionException that was thrown
     * @return ErrorDTO with error code "invalid_session" and the exception message
     */
    @ExceptionHandler(InvalidSessionException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ResponseBody
    public ErrorDTO processInvalidSessionException(InvalidSessionException ex) {
        LogBuilder.builder(log)
            .action("InvalidSessionException")
            .message("Processing : InvalidSessionException: " + ex)
            .logInfo();

        return new ErrorDTO(ex.getErrorCode(), ex.getMessage());
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public ErrorDTO processNoHandlerFoundException(NoHandlerFoundException ex) {
        //Don't need exception traceback - this is probably just robots probing the site
        LogBuilder.builder(log)
            .action("NoHandlerFoundException")
            .message("Processing : NoHandlerFoundException: " + ex)
            .logError();

        return new ErrorDTO("handler_not_found", ex.getMessage());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public ErrorDTO processSpringDataRestResourceNotFoundException(ResourceNotFoundException ex) {
        //Don't need exception traceback - this is just Spring Data Rest trying to fetch
        //an entity that doesn't exist on the DB. Throwing an exception in that case is normal
        //and expected behaviour.
        LogBuilder.builder(log)
            .action("SpringDataRest.ResourceNotFoundException")
            .message("Processing : ResourceNotFoundException: " + ex)
            .logInfo();

        return new ErrorDTO("resource_not_found", ex.getMessage());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public ErrorDTO processHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException ex) {
        //Don't need exception traceback - this is probably just robots probing the site
        LogBuilder.builder(log)
            .action("HttpRequestMethodNotSupportedException")
            .message("Processing : HttpRequestMethodNotSupportedException: " + ex)
            .logError();

        return new ErrorDTO("unsupported_http_request", ex.getMessage());
    }

    @ExceptionHandler(NullPointerException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorDTO processNullException(NullPointerException ex) {
        final String code = "null_exception";
        LogBuilder.builder(log)
            .action("NullException")
            .message(code)
            .logError(ex);

        if(!beenSent) {
            try{
                emailHelper.sendAlert(code, ex);
                beenSent = true;
            } catch (Exception e) {
                LogBuilder.builder(log)
                    .action("NullException")
                    .message("Error sending null exception email")
                    .logError(e);
            }
        }
        return new ErrorDTO(code, ex.toString());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorDTO processOtherException(Exception ex) {
        final String code = "unexpected_exception";
        LogBuilder.builder(log)
            .action("OtherException")
            .message(code)
            .logError(ex);

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
