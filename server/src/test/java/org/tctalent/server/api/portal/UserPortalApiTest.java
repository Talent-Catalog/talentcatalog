package org.tctalent.server.api.portal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.tctalent.server.exception.ExpiredTokenException;
import org.tctalent.server.exception.InvalidPasswordFormatException;
import org.tctalent.server.exception.InvalidPasswordTokenException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.exception.ReCaptchaInvalidException;
import org.tctalent.server.model.db.User;
import org.tctalent.server.request.user.CheckPasswordResetTokenRequest;
import org.tctalent.server.request.user.ResetPasswordRequest;
import org.tctalent.server.request.user.SendResetPasswordEmailRequest;
import org.tctalent.server.request.user.UpdateUserPasswordRequest;
import org.tctalent.server.service.db.CaptchaService;
import org.tctalent.server.service.db.UserService;

class UserPortalApiTest {

  @Mock
  private CaptchaService captchaService;

  @Mock
  private UserService userService;

  @InjectMocks
  private UserPortalApi userPortalApi;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    User loggedInUser = createSampleUser();
    when(userService.getLoggedInUser()).thenReturn(loggedInUser);
  }

  @Test
  void testGetMyUser_Success() {
    Map<String, Object> result = userPortalApi.getMyUser();

    assertNotNull(result);
    assertEquals(1L, result.get("id"));
    assertEquals("test@example.com", result.get("username"));
    assertEquals("test@example.com", result.get("email"));
    assertEquals("John", result.get("firstName"));
    assertEquals("Doe", result.get("lastName"));
    assertTrue((Boolean) result.get("emailVerified"));
    verify(userService).getLoggedInUser();
  }

  @Test
  void testGetMyUser_NoLoggedInUser() {
    when(userService.getLoggedInUser()).thenReturn(null);

    Map<String, Object> result = userPortalApi.getMyUser();

    assertNotNull(result);
    assertTrue(result.isEmpty());
    verify(userService).getLoggedInUser();
  }

  @Test
  void testUpdatePassword_Success() {
    UpdateUserPasswordRequest request = new UpdateUserPasswordRequest();
    request.setPassword("newPassword");
    request.setPasswordConfirmation("newPassword");

    userPortalApi.updatePassword(request);

    verify(userService).updatePassword(request);
  }

  @Test
  void testSendResetPasswordEmail_Success()
      throws NoSuchObjectException, ReCaptchaInvalidException {
    SendResetPasswordEmailRequest request = new SendResetPasswordEmailRequest();
    request.setReCaptchaV3Token("validToken");
    request.setEmail("test@example.com");

    userPortalApi.sendResetPasswordEmail(request);

    verify(captchaService).processCaptchaV3Token("validToken", "resetPassword");
    verify(userService).generateResetPasswordToken(request);
  }

  @Test
  void testSendResetPasswordEmail_InvalidCaptcha() {
    SendResetPasswordEmailRequest request = new SendResetPasswordEmailRequest();
    request.setReCaptchaV3Token("invalidToken");
    doThrow(new ReCaptchaInvalidException("Invalid captcha"))
        .when(captchaService).processCaptchaV3Token("invalidToken", "resetPassword");

    ReCaptchaInvalidException exception = assertThrows(
        ReCaptchaInvalidException.class,
        () -> userPortalApi.sendResetPasswordEmail(request)
    );

    assertEquals("Invalid captcha", exception.getMessage());
    verify(captchaService).processCaptchaV3Token("invalidToken", "resetPassword");
    verifyNoInteractions(userService);
  }

  @Test
  void testSendResetPasswordEmail_NoSuchUser() throws ReCaptchaInvalidException {
    SendResetPasswordEmailRequest request = new SendResetPasswordEmailRequest();
    request.setReCaptchaV3Token("validToken");
    doThrow(new NoSuchObjectException("User not found"))
        .when(userService).generateResetPasswordToken(request);

    NoSuchObjectException exception = assertThrows(
        NoSuchObjectException.class,
        () -> userPortalApi.sendResetPasswordEmail(request)
    );

    assertEquals("User not found", exception.getMessage());
    verify(captchaService).processCaptchaV3Token("validToken", "resetPassword");
    verify(userService).generateResetPasswordToken(request);
  }

  @Test
  void testCheckResetTokenValidity_Success()
      throws ExpiredTokenException, InvalidPasswordTokenException {
    CheckPasswordResetTokenRequest request = new CheckPasswordResetTokenRequest();
    request.setToken("validToken");

    userPortalApi.checkResetTokenValidity(request);

    verify(userService).checkResetToken(request);
  }

  @Test
  void testCheckResetTokenValidity_ExpiredToken() {
    CheckPasswordResetTokenRequest request = new CheckPasswordResetTokenRequest();
    request.setToken("expiredToken");
    doThrow(new ExpiredTokenException("Token expired"))
        .when(userService).checkResetToken(request);

    ExpiredTokenException exception = assertThrows(
        ExpiredTokenException.class,
        () -> userPortalApi.checkResetTokenValidity(request)
    );

    assertEquals("Token expired", exception.getMessage());
    verify(userService).checkResetToken(request);
  }

  @Test
  void testCheckResetTokenValidity_InvalidToken() {
    CheckPasswordResetTokenRequest request = new CheckPasswordResetTokenRequest();
    request.setToken("invalidToken");
    doThrow(new InvalidPasswordTokenException())
        .when(userService).checkResetToken(request);

    InvalidPasswordTokenException exception = assertThrows(
        InvalidPasswordTokenException.class,
        () -> userPortalApi.checkResetTokenValidity(request)
    );

    assertEquals("The reset password token is not valid", exception.getMessage());
    verify(userService).checkResetToken(request);
  }

  @Test
  void testResetPassword_Success() throws InvalidPasswordFormatException {
    ResetPasswordRequest request = new ResetPasswordRequest();
    request.setToken("validToken");
    request.setPassword("newPassword");

    userPortalApi.resetPassword(request);

    verify(userService).resetPassword(request);
  }

  @Test
  void testResetPassword_InvalidPasswordFormat() {
    ResetPasswordRequest request = new ResetPasswordRequest();
    request.setToken("validToken");
    request.setPassword("weak");
    doThrow(new InvalidPasswordFormatException("Password too weak"))
        .when(userService).resetPassword(request);

    InvalidPasswordFormatException exception = assertThrows(
        InvalidPasswordFormatException.class,
        () -> userPortalApi.resetPassword(request)
    );

    assertEquals("Password too weak", exception.getMessage());
    verify(userService).resetPassword(request);
  }

  private User createSampleUser() {
    User user = new User();
    user.setId(1L);
    user.setUsername("test@example.com");
    user.setEmail("test@example.com");
    user.setFirstName("John");
    user.setLastName("Doe");
    user.setEmailVerified(true);
    return user;
  }
}