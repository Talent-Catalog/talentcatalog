package org.tctalent.server.api.portal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import javax.security.auth.login.AccountLockedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.tctalent.server.configuration.TranslationConfig;
import org.tctalent.server.exception.*;
import org.tctalent.server.request.AuthenticateInContextTranslationRequest;
import org.tctalent.server.request.LoginRequest;
import org.tctalent.server.request.candidate.SelfRegistrationRequest;
import org.tctalent.server.response.JwtAuthenticationResponse;
import org.tctalent.server.service.db.CandidateService;
import org.tctalent.server.service.db.UserService;

class AuthPortalApiTest {

  @Mock
  private UserService userService;

  @Mock
  private CandidateService candidateService;

  @Mock
  private TranslationConfig translationConfig;

  @Mock
  private HttpServletRequest httpRequest;

  @InjectMocks
  private AuthPortalApi authPortalApi;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void testAuthorizeInContextTranslation_Success() {
    AuthenticateInContextTranslationRequest request = new AuthenticateInContextTranslationRequest();
    request.setPassword("correctPassword");
    when(translationConfig.getPassword()).thenReturn("correctPassword");

    assertDoesNotThrow(() -> authPortalApi.authorizeInContextTranslation(request));
    verify(translationConfig).getPassword();
  }

  @Test
  void testAuthorizeInContextTranslation_InvalidCredentials() {
    AuthenticateInContextTranslationRequest request = new AuthenticateInContextTranslationRequest();
    request.setPassword("wrongPassword");
    when(translationConfig.getPassword()).thenReturn("correctPassword");

    InvalidCredentialsException exception = assertThrows(
        InvalidCredentialsException.class,
        () -> authPortalApi.authorizeInContextTranslation(request)
    );
    assertEquals("Not authorized", exception.getMessage());
    verify(translationConfig).getPassword();
  }

  @Test
  void testAuthorizeInContextTranslation_NullPassword() {
    AuthenticateInContextTranslationRequest request = new AuthenticateInContextTranslationRequest();
    request.setPassword("anyPassword");
    when(translationConfig.getPassword()).thenReturn(null);

    InvalidCredentialsException exception = assertThrows(
        InvalidCredentialsException.class,
        () -> authPortalApi.authorizeInContextTranslation(request)
    );
    assertEquals("Not authorized", exception.getMessage());
    verify(translationConfig).getPassword();
  }

  @Test
  void testLogin_Success() throws Exception {
    LoginRequest request = new LoginRequest();
    request.setUsername("user@example.com");
    request.setPassword("password");
    JwtAuthenticationResponse jwtResponse = createSampleJwtResponse();
    when(userService.login(request)).thenReturn(jwtResponse);

    Map<String, Object> result = authPortalApi.login(request);

    assertNotNull(result);
    assertEquals("access_token", result.get("accessToken"));
    assertEquals("Bearer", result.get("tokenType"));
    Map<?, ?> userDto = (Map<?, ?>) result.get("user");
    assertEquals(1L, userDto.get("id"));
    assertEquals("user@example.com", userDto.get("username"));
    assertEquals("user@example.com", userDto.get("email"));
    verify(userService).login(request);
  }

  @Test
  void testLogin_AccountLockedException() throws Exception {
    LoginRequest request = new LoginRequest();
    when(userService.login(any(LoginRequest.class))).thenThrow(
        new AccountLockedException("Account locked"));

    AccountLockedException exception = assertThrows(
        AccountLockedException.class,
        () -> authPortalApi.login(request)
    );
    assertEquals("Account locked", exception.getMessage());
    verify(userService).login(request);
  }

  @Test
  void testLogin_PasswordExpiredException() throws Exception {
    LoginRequest request = new LoginRequest();
    when(userService.login(any(LoginRequest.class))).thenThrow(
        new PasswordExpiredException("Password expired"));

    PasswordExpiredException exception = assertThrows(
        PasswordExpiredException.class,
        () -> authPortalApi.login(request)
    );
    assertEquals("Password expired", exception.getMessage());
    verify(userService).login(request);
  }

  @Test
  void testLogin_InvalidCredentialsException() throws Exception {
    LoginRequest request = new LoginRequest();
    when(userService.login(any(LoginRequest.class))).thenThrow(
        new InvalidCredentialsException("Invalid credentials"));

    InvalidCredentialsException exception = assertThrows(
        InvalidCredentialsException.class,
        () -> authPortalApi.login(request)
    );
    assertEquals("Invalid credentials", exception.getMessage());
    verify(userService).login(request);
  }

  @Test
  void testLogin_InvalidPasswordFormatException() throws Exception {
    LoginRequest request = new LoginRequest();
    when(userService.login(any(LoginRequest.class))).thenThrow(
        new InvalidPasswordFormatException("Invalid password format"));

    InvalidPasswordFormatException exception = assertThrows(
        InvalidPasswordFormatException.class,
        () -> authPortalApi.login(request)
    );
    assertEquals("Invalid password format", exception.getMessage());
    verify(userService).login(request);
  }

  @Test
  void testLogin_UserDeactivatedException() throws Exception {
    LoginRequest request = new LoginRequest();
    when(userService.login(any(LoginRequest.class))).thenThrow(
        new UserDeactivatedException("User deactivated"));

    UserDeactivatedException exception = assertThrows(
        UserDeactivatedException.class,
        () -> authPortalApi.login(request)
    );
    assertEquals("User deactivated", exception.getMessage());
    verify(userService).login(request);
  }

  @Test
  void testLogin_ReCaptchaInvalidException() throws Exception {
    LoginRequest request = new LoginRequest();
    when(userService.login(any(LoginRequest.class))).thenThrow(
        new ReCaptchaInvalidException("Invalid reCAPTCHA"));

    ReCaptchaInvalidException exception = assertThrows(
        ReCaptchaInvalidException.class,
        () -> authPortalApi.login(request)
    );
    assertEquals("Invalid reCAPTCHA", exception.getMessage());
    verify(userService).login(request);
  }

  @Test
  void testLogout_Success() {
    ResponseEntity<Void> response = authPortalApi.logout();

    assertEquals(200, response.getStatusCodeValue());
    assertNull(response.getBody());
    verify(userService).logout();
  }

  @Test
  void testRegister_Success() throws Exception {
    SelfRegistrationRequest request = new SelfRegistrationRequest();
    request.setEmail("user@example.com");
    LoginRequest loginRequest = new LoginRequest();
    loginRequest.setUsername("user@example.com");
    JwtAuthenticationResponse jwtResponse = createSampleJwtResponse();
    when(candidateService.register(request, httpRequest)).thenReturn(loginRequest);
    when(userService.login(loginRequest)).thenReturn(jwtResponse);

    Map<String, Object> result = authPortalApi.register(httpRequest, request);

    assertNotNull(result);
    assertEquals("access_token", result.get("accessToken"));
    assertEquals("Bearer", result.get("tokenType"));
    Map<?, ?> userDto = (Map<?, ?>) result.get("user");
    assertEquals(1L, userDto.get("id"));
    assertEquals("user@example.com", userDto.get("username"));
    assertEquals("user@example.com", userDto.get("email"));
    verify(candidateService).register(request, httpRequest);
    verify(userService).login(loginRequest);
  }

  @Test
  void testRegister_ReCaptchaInvalidException() throws Exception {
    SelfRegistrationRequest request = new SelfRegistrationRequest();
    when(candidateService.register(any(SelfRegistrationRequest.class),
        any(HttpServletRequest.class)))
        .thenThrow(new ReCaptchaInvalidException("Invalid reCAPTCHA"));

    ReCaptchaInvalidException exception = assertThrows(
        ReCaptchaInvalidException.class,
        () -> authPortalApi.register(httpRequest, request)
    );
    assertEquals("Invalid reCAPTCHA", exception.getMessage());
    verify(candidateService).register(request, httpRequest);
    verify(userService, never()).login(any(LoginRequest.class));
  }

  @Test
  void testRegister_AccountLockedException() throws Exception {
    SelfRegistrationRequest request = new SelfRegistrationRequest();
    LoginRequest loginRequest = new LoginRequest();
    when(candidateService.register(any(SelfRegistrationRequest.class),
        any(HttpServletRequest.class)))
        .thenReturn(loginRequest);
    when(userService.login(loginRequest)).thenThrow(new AccountLockedException("Account locked"));

    AccountLockedException exception = assertThrows(
        AccountLockedException.class,
        () -> authPortalApi.register(httpRequest, request)
    );
    assertEquals("Account locked", exception.getMessage());
    verify(candidateService).register(request, httpRequest);
    verify(userService).login(loginRequest);
  }

  private JwtAuthenticationResponse createSampleJwtResponse() {
    JwtAuthenticationResponse response = new JwtAuthenticationResponse("access_token",
        createSampleUser());
    response.setTokenType("Bearer");
    return response;
  }

  private org.tctalent.server.model.db.User createSampleUser() {
    org.tctalent.server.model.db.User user = new org.tctalent.server.model.db.User();
    user.setId(1L);
    user.setUsername("user@example.com");
    user.setEmail("user@example.com");
    return user;
  }
}