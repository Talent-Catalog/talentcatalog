package org.tctalent.server.api.portal;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.tctalent.server.configuration.TranslationConfig;
import org.tctalent.server.exception.InvalidCredentialsException;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.TcInstanceType;
import org.tctalent.server.model.db.User;
import org.tctalent.server.request.AuthenticateInContextTranslationRequest;
import org.tctalent.server.request.candidate.CompleteOauthAuthenticationRequest;
import org.tctalent.server.response.AuthenticationResponse;
import org.tctalent.server.security.AuthProfile;
import org.tctalent.server.security.OAuth2UserService;
import org.tctalent.server.service.db.CandidateService;
import org.tctalent.server.service.db.UserService;
import org.tctalent.server.service.db.impl.TcInstanceService;

class AuthPortalApiTest {

  @Mock
  private UserService userService;

  @Mock
  private CandidateService candidateService;

  @Mock
  private OAuth2UserService oAuth2UserService;

  @Mock
  private TcInstanceService tcInstanceService;

  @Mock
  private TranslationConfig translationConfig;

  @InjectMocks
  private AuthPortalApi authPortalApi;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);

    //Assume that the instance type is GRN
    when(tcInstanceService.getInstanceType()).thenReturn(TcInstanceType.GRN);
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
  void testLogin_Success() {
    User user = createSampleUser();
    AuthProfile authProfile = new AuthProfile();
    when(userService.login(authProfile)).thenReturn(user);
    AuthenticationResponse response = createSampleAuthenticationResponse(user);
    when(userService.createAuthenticationResponse(user)).thenReturn(response);

    Map<String, Object> result = authPortalApi.login(authProfile);

    assertNotNull(result);
    assertEquals(false, result.get("canViewChats"));
    assertEquals(TcInstanceType.GRN, result.get("tcInstanceType"));
    Map<?, ?> userDto = (Map<?, ?>) result.get("user");
    assertEquals(1L, userDto.get("id"));
    assertEquals("user@example.com", userDto.get("email"));
    verify(userService).login(authProfile);
    verify(userService).createAuthenticationResponse(user);
  }

  //Test that any exception thrown by the user service is propagated to the login method
  @Test
  void testLogin_Exception() {
    AuthProfile authProfile = new AuthProfile();
    when(userService.login(any(AuthProfile.class))).thenThrow(
        new RuntimeException("Some exception"));

    RuntimeException exception = assertThrows(
        RuntimeException.class,
        () -> authPortalApi.login(authProfile)
    );
    assertEquals("Some exception", exception.getMessage());
    verify(userService).login(authProfile);
  }

  @Test
  void testLogout_Success() {
    ResponseEntity<Void> response = authPortalApi.logout();

    assertEquals(HttpStatusCode.valueOf(200), response.getStatusCode());
    assertNull(response.getBody());
    verify(userService).logout();
  }

  @Test
  void testRegister_Success() {
    User user = createSampleUser();
    Candidate candidate = createSampleCandidate(user);
    CompleteOauthAuthenticationRequest registrationRequest = new CompleteOauthAuthenticationRequest();
    HttpServletRequest httpRequest = new MockHttpServletRequest();
    when(candidateService.register(registrationRequest, httpRequest)).thenReturn(candidate);
    AuthenticationResponse response = createSampleAuthenticationResponse(user);
    when(userService.createAuthenticationResponse(user)).thenReturn(response);

    Map<String, Object> result = authPortalApi.register(registrationRequest, httpRequest);

    assertNotNull(result);
    assertEquals(false, result.get("canViewChats"));
    assertEquals(TcInstanceType.GRN, result.get("tcInstanceType"));
    Map<?, ?> userDto = (Map<?, ?>) result.get("user");
    assertEquals(1L, userDto.get("id"));
    assertEquals("user@example.com", userDto.get("email"));
    verify(candidateService).register(registrationRequest, httpRequest);
  }

  @Test
  void testRegister_Exception() {
    CompleteOauthAuthenticationRequest registrationRequest = new CompleteOauthAuthenticationRequest();
    HttpServletRequest httpRequest = new MockHttpServletRequest();
    when(candidateService.register(any(CompleteOauthAuthenticationRequest.class),
        any(HttpServletRequest.class)))
        .thenThrow(new RuntimeException("Some Exception"));

    RuntimeException exception = assertThrows(
        RuntimeException.class,
        () -> authPortalApi.register(registrationRequest, httpRequest)
    );
    assertEquals("Some Exception", exception.getMessage());
    verify(candidateService).register(registrationRequest, httpRequest);
  }

  private AuthenticationResponse createSampleAuthenticationResponse(User user) {
    AuthenticationResponse response = new AuthenticationResponse(user);
    response.setCanViewChats(false);
    response.setTcInstanceType(TcInstanceType.GRN);
    return response;
  }

  private Candidate createSampleCandidate(User user) {
    Candidate candidate = new Candidate();
    candidate.setUser(user);
    return candidate;
  }

  private org.tctalent.server.model.db.User createSampleUser() {
    org.tctalent.server.model.db.User user = new org.tctalent.server.model.db.User();
    user.setId(1L);
    user.setUsername("user@example.com");
    user.setEmail("user@example.com");
    return user;
  }
}
