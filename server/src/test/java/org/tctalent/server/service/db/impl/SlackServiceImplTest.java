/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.service.db.impl;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.slack.api.Slack;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.tctalent.server.configuration.SlackConfig;
import org.tctalent.server.exception.SlackException;
import org.tctalent.server.model.db.User;
import org.tctalent.server.request.job.JobInfoForSlackPost;
import org.tctalent.server.request.opportunity.PostJobToSlackRequest;
import org.tctalent.server.security.AuthService;

@ExtendWith(MockitoExtension.class)
class SlackServiceImplTest {

  private static final String CHANNEL_ID = "C123456";
  private static final String TOKEN = "xoxb-test-token";
  private static final String WORKSPACE = "https://refugeejobsmarket.slack.com";
  private static final String CHANNEL_URL = WORKSPACE + "/archives/" + CHANNEL_ID;

  @Mock private AuthService authService;
  @Mock private Slack slack;
  @Mock private MethodsClient methodsClient;
  @Captor private ArgumentCaptor<ChatPostMessageRequest> requestCaptor;

  private SlackServiceImpl slackService;

  @BeforeEach
  void setUp() {
    SlackConfig slackConfig = new SlackConfig();
    slackConfig.setChannelId(CHANNEL_ID);
    slackConfig.setToken(TOKEN);
    slackConfig.setWorkspace(WORKSPACE);

    slackService = new SlackServiceImpl(authService, slackConfig);
  }

  @Test
  void postJobWithJobInfoAndContactPostsMessageAndReturnsChannelUrl() throws Exception {
    JobInfoForSlackPost jobInfo = jobInfo();
    jobInfo.setContact(user());

    try (SlackMocks ignored = mockSuccessfulSlackPost()) {
      String result = slackService.postJob(jobInfo);

      ChatPostMessageRequest request = captureRequest();
      String blocks = request.getBlocks().toString();

      assertAll(
          () -> assertEquals(CHANNEL_URL, result),
          () -> assertEquals(CHANNEL_ID, request.getChannel()),
          () -> assertEquals("New job", request.getText()),
          () -> assertEquals(8, request.getBlocks().size()),
          () -> assertTrue(blocks.contains("Software Engineer")),
          () -> assertTrue(blocks.contains("Build useful software")),
          () -> assertTrue(blocks.contains("john.doe@example.com")),
          () -> assertTrue(blocks.contains("John Doe")),
          () -> assertTrue(blocks.contains("https://tctalent.org/admin-portal/job/123")),
          () -> assertTrue(blocks.contains("https://salesforce.example/job/123"))
      );
    }
  }

  @Test
  void postJobWithJobInfoAndNoContactUsesQuestionMarkForContact() throws Exception {
    JobInfoForSlackPost jobInfo = jobInfo();
    jobInfo.setContact(null);

    try (SlackMocks ignored = mockSuccessfulSlackPost()) {
      String result = slackService.postJob(jobInfo);

      ChatPostMessageRequest request = captureRequest();
      String blocks = request.getBlocks().toString();

      assertAll(
          () -> assertEquals(CHANNEL_URL, result),
          () -> assertTrue(blocks.contains("Contact person for job ?"))
      );
    }
  }

  @Test
  void postJobWithJobInfoWrapsSlackClientFailure() throws Exception {
    JobInfoForSlackPost jobInfo = jobInfo();
    jobInfo.setContact(user());

    try (SlackMocks ignored = mockFailingSlackPost()) {
      SlackException exception = assertThrows(
          SlackException.class,
          () -> slackService.postJob(jobInfo)
      );

      assertAll(
          () -> assertTrue(exception.getMessage().contains(CHANNEL_URL)),
          () -> assertTrue(exception.getMessage().contains("Slack down"))
      );
    }
  }

  @Test
  void postJobWithRequestAndLoggedInUserPostsMessageAndReturnsChannelUrl() throws Exception {
    when(authService.getLoggedInUser()).thenReturn(Optional.of(user()));

    PostJobToSlackRequest request = postJobToSlackRequest();

    try (SlackMocks ignored = mockSuccessfulSlackPost()) {
      String result = slackService.postJob(request);

      ChatPostMessageRequest slackRequest = captureRequest();
      String blocks = slackRequest.getBlocks().toString();

      assertAll(
          () -> assertEquals(CHANNEL_URL, result),
          () -> assertEquals(CHANNEL_ID, slackRequest.getChannel()),
          () -> assertEquals("New job", slackRequest.getText()),
          () -> assertEquals(8, slackRequest.getBlocks().size()),
          () -> assertTrue(blocks.contains("Software Engineer")),
          () -> assertTrue(blocks.contains("Job registered by")),
          () -> assertTrue(blocks.contains("john.doe@example.com")),
          () -> assertTrue(blocks.contains("John Doe")),
          () -> assertTrue(blocks.contains("https://tctalent.org/admin-portal/list/77")),
          () -> assertTrue(blocks.contains("https://drive.example/root-folder")),
          () -> assertTrue(blocks.contains("https://drive.example/job-description-folder")),
          () -> assertTrue(blocks.contains("https://salesforce.example/job/123"))
      );
    }
  }

  @Test
  void postJobWithRequestAndNoLoggedInUserUsesQuestionMarkForRegisteredBy() throws Exception {
    when(authService.getLoggedInUser()).thenReturn(Optional.empty());

    PostJobToSlackRequest request = postJobToSlackRequest();

    try (SlackMocks ignored = mockSuccessfulSlackPost()) {
      String result = slackService.postJob(request);

      ChatPostMessageRequest slackRequest = captureRequest();
      String blocks = slackRequest.getBlocks().toString();

      assertAll(
          () -> assertEquals(CHANNEL_URL, result),
          () -> assertTrue(blocks.contains("Job registered by ?"))
      );
    }
  }

  @Test
  void postJobWithRequestWrapsSlackClientFailure() throws Exception {
    when(authService.getLoggedInUser()).thenReturn(Optional.of(user()));

    PostJobToSlackRequest request = postJobToSlackRequest();

    try (SlackMocks ignored = mockFailingSlackPost()) {
      SlackException exception = assertThrows(
          SlackException.class,
          () -> slackService.postJob(request)
      );

      assertAll(
          () -> assertTrue(exception.getMessage().contains(CHANNEL_URL)),
          () -> assertTrue(exception.getMessage().contains("Slack down"))
      );
    }
  }

  private JobInfoForSlackPost jobInfo() {
    JobInfoForSlackPost jobInfo = new JobInfoForSlackPost();
    jobInfo.setJobName("Software Engineer");
    jobInfo.setJobSummary("Build useful software");
    jobInfo.setTcJobLink("https://tctalent.org/admin-portal/job/123");
    jobInfo.setSfJobLink("https://salesforce.example/job/123");
    return jobInfo;
  }

  private PostJobToSlackRequest postJobToSlackRequest() {
    PostJobToSlackRequest request = new PostJobToSlackRequest();
    request.setJobName("Software Engineer");
    request.setListlink("https://tctalent.org/admin-portal/list/77");
    request.setFolderlink("https://drive.example/root-folder");
    request.setFolderjdlink("https://drive.example/job-description-folder");
    request.setSfJoblink("https://salesforce.example/job/123");
    return request;
  }

  private User user() {
    User user = new User();
    user.setFirstName("John");
    user.setLastName("Doe");
    user.setEmail("john.doe@example.com");
    return user;
  }

  private SlackMocks mockSuccessfulSlackPost() throws Exception {
    ChatPostMessageResponse response = new ChatPostMessageResponse();
    response.setChannel(CHANNEL_ID);

    return mockSlackPost(response, null);
  }

  private SlackMocks mockFailingSlackPost() throws Exception {
    return mockSlackPost(null, new RuntimeException("Slack down"));
  }

  private SlackMocks mockSlackPost(
      ChatPostMessageResponse response,
      RuntimeException exception
  ) throws Exception {
    MockedStatic<Slack> slackStatic = mockStatic(Slack.class);
    slackStatic.when(Slack::getInstance).thenReturn(slack);

    when(slack.methods(TOKEN)).thenReturn(methodsClient);

    if (exception == null) {
      when(methodsClient.chatPostMessage(any(ChatPostMessageRequest.class)))
          .thenReturn(response);
    } else {
      when(methodsClient.chatPostMessage(any(ChatPostMessageRequest.class)))
          .thenThrow(exception);
    }

    return new SlackMocks(slackStatic);
  }

  private ChatPostMessageRequest captureRequest() throws Exception {
    verify(slack).methods(TOKEN);
    verify(methodsClient).chatPostMessage(requestCaptor.capture());
    return requestCaptor.getValue();
  }

  private static class SlackMocks implements AutoCloseable {
    private final MockedStatic<Slack> slackStatic;

    private SlackMocks(MockedStatic<Slack> slackStatic) {
      this.slackStatic = slackStatic;
    }

    @Override
    public void close() {
      slackStatic.close();
    }
  }
}
