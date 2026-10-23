/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.lang.Nullable;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.AbstractSubscribableChannel;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.messaging.context.SecurityContextChannelInterceptor;
import org.springframework.stereotype.Controller;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.tctalent.server.security.JwtTokenProvider;
import org.tctalent.server.security.TcUserDetails;
import org.tctalent.server.security.TcUserDetailsService;

/**
 * Tests the security applied to STOMP messages arriving on the client inbound channel of our
 * chat web socket.
 * <p/>
 * The real web socket configuration classes are loaded (without the rest of the application)
 * and STOMP messages are sent directly to the client inbound channel, as the STOMP handler does
 * for frames received from the browser.
 * <p/>
 * These tests only check behaviour (not how the security is wired), so that they were run
 * against the legacy AbstractSecurityWebSocketMessageBrokerConfigurer based configuration
 * before it was replaced, to confirm that the replacement behaves the same.
 */
@SpringJUnitWebConfig(WebSocketSecurityConfigTest.TestConfig.class)
class WebSocketSecurityConfigTest {

  private static final String VALID_TOKEN = "valid-token";
  private static final String INVALID_TOKEN = "invalid-token";

  @Configuration
  @Import({WebSocketConfig.class, WebSocketConfig2.class, WebSocketSecurityConfig.class,
      TestChatController.class})
  static class TestConfig {
  }

  /**
   * Stands in for our real @MessageMapping handlers, recording what the handler sees about the
   * user who sent the message.
   */
  @Controller
  static class TestChatController {
    final BlockingQueue<Received> received = new LinkedBlockingQueue<>();

    @MessageMapping("/test")
    void handle(@AuthenticationPrincipal UserDetails principal) {
      received.add(new Received(principal,
          SecurityContextHolder.getContext().getAuthentication()));
    }
  }

  record Received(UserDetails principal, Authentication contextAuthentication) {}

  @MockitoBean
  private JwtTokenProvider tokenProvider;

  @MockitoBean
  private TcUserDetailsService userDetailsService;

  @Autowired
  @Qualifier("clientInboundChannel")
  private AbstractSubscribableChannel clientInboundChannel;

  @Autowired
  private TestChatController chatController;

  private TcUserDetails userDetails;

  @BeforeEach
  void setUp() {
    org.tctalent.server.model.db.User user = new org.tctalent.server.model.db.User();
    user.setUsername("alice");
    userDetails = new TcUserDetails(user);
    userDetails.setAuthorities(List.of());

    given(tokenProvider.validateToken(VALID_TOKEN)).willReturn(true);
    given(tokenProvider.validateToken(INVALID_TOKEN)).willReturn(false);
    given(tokenProvider.getUsernameFromJwt(VALID_TOKEN)).willReturn("alice");
    given(userDetailsService.loadUserByUsername("alice")).willReturn(userDetails);

    chatController.received.clear();
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  @DisplayName("JWT authentication interceptor runs before the security interceptors")
  void jwtInterceptorRunsBeforeSecurityInterceptors() {
    List<ChannelInterceptor> interceptors = clientInboundChannel.getInterceptors();

    int jwtIndex = indexOf(interceptors, i -> i.getClass().getName()
        .startsWith(WebSocketConfig2.class.getName()));
    int securityContextIndex = indexOf(interceptors,
        i -> i instanceof SecurityContextChannelInterceptor);

    assertThat(jwtIndex).isGreaterThanOrEqualTo(0);
    assertThat(securityContextIndex).isGreaterThan(jwtIndex);
  }

  @Test
  @DisplayName("no CSRF interceptor is applied to STOMP messages (CSRF was disabled by sameOriginDisabled)")
  void noCsrfInterceptor() {
    //Checked by package rather than class because the CSRF interceptor classes differ between
    //Spring Security versions.
    assertThat(clientInboundChannel.getInterceptors())
        .noneMatch(i -> i.getClass().getPackageName()
            .equals("org.springframework.security.messaging.web.csrf"));
  }

  @Test
  @DisplayName("CONNECT with a valid JWT and no CSRF token is accepted and authenticates the user")
  void connectWithValidJwtAuthenticatesUser() {
    StompHeaderAccessor accessor = accessor(StompCommand.CONNECT, null, null);
    accessor.addNativeHeader("Authorization", "Bearer " + VALID_TOKEN);

    send(accessor);

    assertThat(accessor.getUser()).isInstanceOf(UsernamePasswordAuthenticationToken.class);
    assertThat(((Authentication) accessor.getUser()).getPrincipal()).isSameAs(userDetails);
  }

  @Test
  @DisplayName("CONNECT with no JWT is accepted (permitted) but leaves the user unauthenticated")
  void connectWithoutJwtIsPermitted() {
    StompHeaderAccessor accessor = accessor(StompCommand.CONNECT, null, null);

    assertThatCode(() -> send(accessor)).doesNotThrowAnyException();
    assertThat(accessor.getUser()).isNull();
  }

  @Test
  @DisplayName("CONNECT with an invalid JWT is rejected with the expired or invalid token message")
  void connectWithInvalidJwtIsRejected() {
    StompHeaderAccessor accessor = accessor(StompCommand.CONNECT, null, null);
    accessor.addNativeHeader("Authorization", "Bearer " + INVALID_TOKEN);

    //The frontend relies on this message to stop reconnecting
    assertThatThrownBy(() -> send(accessor))
        .isInstanceOf(MessageDeliveryException.class)
        .hasMessage(JwtTokenProvider.EXPIRED_OR_INVALID_TOKEN_MSG);
  }

  @ParameterizedTest(name = "{0} is permitted without authentication")
  @MethodSource("permittedWithoutAuthentication")
  void permittedWithoutAuthentication(StompCommand command) {
    assertThatCode(() -> send(accessor(command, null, null))).doesNotThrowAnyException();
  }

  static Stream<StompCommand> permittedWithoutAuthentication() {
    return Stream.of(StompCommand.CONNECT, StompCommand.UNSUBSCRIBE, StompCommand.DISCONNECT);
  }

  @ParameterizedTest(name = "{0} {1} is denied without authentication")
  @MethodSource("deniedWithoutAuthentication")
  void deniedWithoutAuthentication(StompCommand command, @Nullable String destination) {
    StompHeaderAccessor accessor = accessor(command, destination, null);

    assertThatThrownBy(() -> send(accessor))
        .isInstanceOf(MessageDeliveryException.class)
        .hasRootCauseInstanceOf(AccessDeniedException.class);
  }

  static Stream<Arguments> deniedWithoutAuthentication() {
    return Stream.of(
        //Matched by the "/app/**" rule
        Arguments.of(StompCommand.SEND, "/app/test"),
        //Matched by the "/topic/**" subscribe rule
        Arguments.of(StompCommand.SUBSCRIBE, "/topic/chat/1"),
        //Matched by the "any message" rule
        Arguments.of(StompCommand.SEND, "/topic/chat/1"),
        Arguments.of(StompCommand.SUBSCRIBE, "/user/queue/errors")
    );
  }

  @Test
  @DisplayName("heartbeat is denied without authentication (matched by the any message rule)")
  void heartbeatDeniedWithoutAuthentication() {
    StompHeaderAccessor accessor = StompHeaderAccessor.createForHeartbeat();
    accessor.setSessionId("session-1");
    accessor.setLeaveMutable(true);

    assertThatThrownBy(() -> send(accessor))
        .isInstanceOf(MessageDeliveryException.class)
        .hasRootCauseInstanceOf(AccessDeniedException.class);
  }

  @Test
  @DisplayName("authenticated SUBSCRIBE to a chat topic is permitted")
  void authenticatedSubscribeIsPermitted() {
    StompHeaderAccessor accessor =
        accessor(StompCommand.SUBSCRIBE, "/topic/chat/1", authentication());

    assertThatCode(() -> send(accessor)).doesNotThrowAnyException();
  }

  @Test
  @DisplayName("authenticated SEND to /app is handled with the user in the security context")
  void authenticatedSendReachesHandlerWithSecurityContext() throws InterruptedException {
    Authentication authentication = authentication();

    send(accessor(StompCommand.SEND, "/app/test", authentication));

    //The handler runs on the inbound channel's executor thread
    Received received = chatController.received.poll(5, TimeUnit.SECONDS);
    assertThat(received).isNotNull();
    assertThat(received.principal()).isSameAs(userDetails);
    assertThat(received.contextAuthentication()).isSameAs(authentication);
  }

  private Authentication authentication() {
    return new UsernamePasswordAuthenticationToken(userDetails, null, List.of());
  }

  /**
   * Creates the headers of a STOMP message as they would be after being received from a client
   * whose session is associated with the given user (which is set from the CONNECT message).
   * The headers are left mutable, as the STOMP handler does, so that interceptors can update
   * them.
   */
  private static StompHeaderAccessor accessor(
      StompCommand command, @Nullable String destination, @Nullable Authentication user) {
    StompHeaderAccessor accessor = StompHeaderAccessor.create(command);
    accessor.setSessionId("session-1");
    accessor.setSessionAttributes(new HashMap<>());
    if (destination != null) {
      accessor.setDestination(destination);
    }
    if (command == StompCommand.SUBSCRIBE || command == StompCommand.UNSUBSCRIBE) {
      accessor.setSubscriptionId("sub-1");
    }
    if (user != null) {
      accessor.setUser(user);
    }
    accessor.setLeaveMutable(true);
    return accessor;
  }

  private void send(StompHeaderAccessor accessor) {
    Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    clientInboundChannel.send(message);
  }

  private static int indexOf(
      List<ChannelInterceptor> interceptors, java.util.function.Predicate<ChannelInterceptor> test) {
    for (int i = 0; i < interceptors.size(); i++) {
      if (test.test(interceptors.get(i))) {
        return i;
      }
    }
    return -1;
  }
}
