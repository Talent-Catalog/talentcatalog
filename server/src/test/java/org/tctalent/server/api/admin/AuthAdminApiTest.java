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

package org.tctalent.server.api.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.stream.Stream;
import javax.security.auth.login.AccountLockedException;
import org.hamcrest.Matchers;
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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.tctalent.server.exception.InvalidCredentialsException;
import org.tctalent.server.exception.PasswordExpiredException;
import org.tctalent.server.exception.ServiceException;
import org.tctalent.server.exception.UserDeactivatedException;
import org.tctalent.server.request.LoginRequest;
import org.tctalent.server.response.JwtAuthenticationResponse;
import org.tctalent.server.service.db.UserService;
import org.tctalent.server.util.qr.EncodedQrImage;

/**
 * Unit tests for Auth Admin Api - the login and mfa handler for the admin-portal.
 *
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

    private static final String INVALID_CREDENTIALS_CODE = "invalid_credentials";
    private static final String ACCOUNT_LOCKED_EXCEPTION = "account_locked";
    private static final String USER_DEACTIVATED_CODE = "user_deactivated";
    private static final String PASSWORD_EXPIRED_CODE = "password_expired";
    private static final String QR_CODE_GEN_ERROR_CODE = "qr_error";

    private LoginRequest loginRequest;

    @Autowired AuthAdminApi controller;
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean UserService userService;

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
    }

    private void doLoginAndVerifyResponse() throws Exception {
        mockMvc.perform(post(BASE_PATH + "/login")
                .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest))
                        .accept(MediaType.APPLICATION_JSON))

                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accessToken", is(JWT_ACCESS_TOKEN)))
                .andExpect(jsonPath("$.tokenType", is(JWT_TOKEN_TYPE)))

                .andExpect(MockMvcResultMatchers.jsonPath("$.user.username", Matchers.is(user.getUsername())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.user.email", Matchers.is(user.getEmail())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.user.role", Matchers.is(user.getRole().toString())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.user.firstName", Matchers.is(user.getFirstName())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.user.lastName", Matchers.is(user.getLastName())));
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

    private void doLoginAndVerifyFails(String errorCode, String message) throws Exception {
        mockMvc.perform(post(BASE_PATH + "/login")
                .with(csrf())
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
        mockMvc.perform(post(BASE_PATH + "/logout")
                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("multi-factor authentication setup succeeds")
    void mfaSetupSucceeds() throws Exception {
        EncodedQrImage encodedQrImage = new EncodedQrImage("qr-code-image");
        given(userService.mfaSetup()).willReturn(encodedQrImage);

        mockMvc.perform(post(BASE_PATH + "/mfa-setup")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.base64Encoding", containsString("qr-code-image")));
    }

    @Test
    @DisplayName("multi-factor authentication setup fails with service exception")
    void mfaSetupFailsWithServiceException() throws Exception {
        given(userService.mfaSetup())
                .willThrow(new ServiceException("qr_error", QR_CODE_GEN_EXCEPTION_MESSAGE));

        mockMvc.perform(post(BASE_PATH + "/mfa-setup")
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", containsString(QR_CODE_GEN_ERROR_CODE)))
                .andExpect(jsonPath("$.message", containsString(QR_CODE_GEN_EXCEPTION_MESSAGE)));
    }

}
