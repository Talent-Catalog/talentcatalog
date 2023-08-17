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

package org.tbbtalent.server.api.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.tbbtalent.server.exception.*;
import org.tbbtalent.server.request.LoginRequest;
import org.tbbtalent.server.response.JwtAuthenticationResponse;
import org.tbbtalent.server.service.db.CaptchaService;
import org.tbbtalent.server.service.db.UserService;
import org.tbbtalent.server.util.qr.EncodedQrImage;

import javax.security.auth.login.AccountLockedException;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author smalik
 */
@WebMvcTest(AuthAdminApi.class)
@AutoConfigureMockMvc
class AuthAdminApiTest extends ApiTestBase {
    private static final String BASE_PATH = "/api/admin/auth";

    private static final String JWT_ACCESS_TOKEN = "jwt-abc-def-123";
    private static final String JWT_TOKEN_TYPE = "Bearer";

    private static final String INVALID_CREDENTIALS_MESSAGE = "Invalid credentials";
    private static final String ACCOUNT_LOCKED_MESSAGE = "Account locked";
    private static final String USER_DEACTIVATED_MESSAGE = "Account deactivated";
    private static final String PASSWORD_EXPIRED_MESSAGE = "Password expired";
    private static final String QR_CODE_GEN_EXCEPTION_MESSAGE = "Error generating QR code";
    private static final String RECAPTCHA_INVALID_MESSAGE = "reCaptcha was not successfully validated";

    private static final String INVALID_CREDENTIALS_CODE = "invalid_credentials";
    private static final String ACCOUNT_LOCKED_EXCEPTION = "account_locked";
    private static final String USER_DEACTIVATED_CODE = "user_deactivated";
    private static final String PASSWORD_EXPIRED_CODE = "password_expired";
    private static final String QR_CODE_GEN_ERROR_CODE = "qr_error";
    private static final String RECAPTCHA_INVALID_CODE = "recaptcha";

    private LoginRequest loginRequest;

    @Autowired AuthAdminApi controller;
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean UserService userService;
    @MockBean CaptchaService captchaService;

    @BeforeEach
    public void setUp() {
        configureAuthentication();

        loginRequest = new LoginRequest();
        loginRequest.setUsername("sadat");
        loginRequest.setPassword("password");
    }

    @Test
    public void testWebOnlyContextLoads() {
        assertThat(controller).isNotNull();
    }

    @Test
    @DisplayName("login succeeds - bypassing MFA and Captcha")
    void loginSucceeds() throws Exception {
        user.setUsingMfa(false);
        loginRequest.setReCaptchaV3Token(null);

        given(userService.login(any(LoginRequest.class)))
                .willReturn(new JwtAuthenticationResponse(JWT_ACCESS_TOKEN, user));

        doLoginAndVerifyResponse();

        verify(userService, never()).mfaVerify(any());
        verify(captchaService, never()).processCaptchaV3Token(any(), any());
    }

    @Test
    @DisplayName("login fails - using MFA with no secret configured - for initial login attempt")
    void loginFailsUsingMfaWithNoSecret() throws Exception {
        user.setUsingMfa(true);
        user.setMfaSecret(null);

        given(userService.login(any(LoginRequest.class)))
                .willReturn(new JwtAuthenticationResponse(JWT_ACCESS_TOKEN, user));

        doLoginAndVerifyResponse();

        verify(userService, never()).mfaVerify(any());
        verify(captchaService, never()).processCaptchaV3Token(any(), any());
    }

    @Test
    @DisplayName("login succeeds - using MFA with valid secret")
    void loginSucceedsUsingMfaWithValidSecretConfigured() throws Exception {
        user.setUsingMfa(true);
        user.setMfaSecret("valid-mfa-secret");

        given(userService.login(any(LoginRequest.class)))
                .willReturn(new JwtAuthenticationResponse(JWT_ACCESS_TOKEN, user));

        doLoginAndVerifyResponse();

        verify(userService).mfaVerify(any());
        verify(captchaService, never()).processCaptchaV3Token(any(), any());
    }

    @Test
    @DisplayName("login succeeds - using reCaptcha with valid token")
    void loginSucceedsUsingValidRecaptchaToken() throws Exception {
        user.setUsingMfa(false);
        loginRequest.setReCaptchaV3Token("valid-recaptcha-token");

        given(userService.login(any(LoginRequest.class)))
                .willReturn(new JwtAuthenticationResponse(JWT_ACCESS_TOKEN, user));

        doLoginAndVerifyResponse();

        verify(userService, never()).mfaVerify(any());
        verify(captchaService).processCaptchaV3Token(any(), any());
    }

    private void doLoginAndVerifyResponse() throws Exception {
        mockMvc.perform(post(BASE_PATH + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest))
                        .accept(MediaType.APPLICATION_JSON))

                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accessToken", is(JWT_ACCESS_TOKEN)))
                .andExpect(jsonPath("$.tokenType", is(JWT_TOKEN_TYPE)))

                .andExpect(jsonPath("$.user.username", is(user.getUsername())))
                .andExpect(jsonPath("$.user.email", is(user.getEmail())))
                .andExpect(jsonPath("$.user.role", is(user.getRole().toString())))
                .andExpect(jsonPath("$.user.firstName", is(user.getFirstName())))
                .andExpect(jsonPath("$.user.lastName", is(user.getLastName())));
    }

    @ParameterizedTest
    @MethodSource("provideLoginExceptionsAndCodes")
    @DisplayName("login fails - user service throws an exception")
    void loginFailsUserServiceException(Throwable loginException, String errorCode) throws Exception {
        given(userService.login(any(LoginRequest.class))).willThrow(loginException);
        doLoginAndVerifyFails(errorCode, loginException.getMessage());
    }

    private static Stream<Arguments> provideLoginExceptionsAndCodes() {
        return Stream.of(
                Arguments.of(new InvalidCredentialsException(INVALID_CREDENTIALS_MESSAGE), INVALID_CREDENTIALS_CODE),
                Arguments.of(new AccountLockedException(ACCOUNT_LOCKED_MESSAGE), ACCOUNT_LOCKED_EXCEPTION),
                Arguments.of(new UserDeactivatedException(USER_DEACTIVATED_MESSAGE), USER_DEACTIVATED_CODE),
                Arguments.of(new PasswordExpiredException(PASSWORD_EXPIRED_MESSAGE), PASSWORD_EXPIRED_CODE)
        );
    }

    @Test
    @DisplayName("login fails - using MFA with invalid secret")
    void loginFailsUsingMfaWithInvalidSecret() throws Exception {
        user.setUsingMfa(true);
        user.setMfaSecret("invalid-mfa-secret");

        given(userService.login(any(LoginRequest.class)))
                .willReturn(new JwtAuthenticationResponse(JWT_ACCESS_TOKEN, user));
        willThrow(new InvalidCredentialsException(INVALID_CREDENTIALS_MESSAGE))
                .given(userService).mfaVerify(any());

        doLoginAndVerifyFails(INVALID_CREDENTIALS_CODE, INVALID_CREDENTIALS_MESSAGE);
    }

    @Test
    @DisplayName("login fails - using reCaptcha with invalid token")
    void loginFailsUsingInvalidRecaptchaToken() throws Exception {
        user.setUsingMfa(false);
        loginRequest.setReCaptchaV3Token("invalid-recaptcha-token");

        given(userService.login(any(LoginRequest.class)))
                .willReturn(new JwtAuthenticationResponse(JWT_ACCESS_TOKEN, user));
        willThrow(new ReCaptchaInvalidException(RECAPTCHA_INVALID_MESSAGE))
                .given(captchaService).processCaptchaV3Token(any(), any());

        doLoginAndVerifyFails(RECAPTCHA_INVALID_CODE, RECAPTCHA_INVALID_MESSAGE);
    }

    private void doLoginAndVerifyFails(String errorCode, String message) throws Exception {
        mockMvc.perform(post(BASE_PATH + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest))
                        .accept(MediaType.APPLICATION_JSON))

                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code", containsString(errorCode)))
                .andExpect(jsonPath("$.message", containsString(message)));
    }

    @Test
    @DisplayName("logout succeeds")
    void logoutSucceeds() throws Exception {
        mockMvc.perform(post(BASE_PATH + "/logout"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("multi-factor authentication setup succeeds")
    void mfaSetupSucceeds() throws Exception {
        EncodedQrImage encodedQrImage = new EncodedQrImage("qr-code-image");
        given(userService.mfaSetup()).willReturn(encodedQrImage);

        mockMvc.perform(post(BASE_PATH + "/mfa-setup"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.base64Encoding", containsString("qr-code-image")));
    }

    @Test
    @DisplayName("multi-factor authentication setup fails with service exception")
    void mfaSetupFailsWithServiceException() throws Exception {
        given(userService.mfaSetup())
                .willThrow(new ServiceException("qr_error", QR_CODE_GEN_EXCEPTION_MESSAGE));

        mockMvc.perform(post(BASE_PATH + "/mfa-setup"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", containsString(QR_CODE_GEN_ERROR_CODE)))
                .andExpect(jsonPath("$.message", containsString(QR_CODE_GEN_EXCEPTION_MESSAGE)));
    }

}
