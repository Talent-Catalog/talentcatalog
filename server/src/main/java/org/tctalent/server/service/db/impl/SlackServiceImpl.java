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

package org.tctalent.server.service.db.impl;

import com.slack.api.Slack;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import com.slack.api.model.block.DividerBlock;
import com.slack.api.model.block.HeaderBlock;
import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.block.SectionBlock;
import com.slack.api.model.block.composition.MarkdownTextObject;
import com.slack.api.model.block.composition.PlainTextObject;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.tctalent.server.configuration.SlackConfig;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.exception.SlackException;
import org.tctalent.server.model.db.User;
import org.tctalent.server.request.job.JobInfoForSlackPost;
import org.tctalent.server.request.opportunity.PostJobToSlackRequest;
import org.tctalent.server.security.AuthService;
import org.tctalent.server.service.db.SlackService;

/**
 * Interface to TBB's Slack workspace
 * <p/>
 * See https://medium.com/cloud-native-the-gathering/how-to-send-formatted-slack-message-blocks-via-the-java-slack-sdk-client-api-3fc9edb1aa98
 * <p/>
 * There is an app registered on https://api.slack.com/apps for TBB's Slack workspace
 * (Jobs Marketplace for Refugees) called TalentCatalog.
 *
 * @author John Cameron
 */
@Service
@Slf4j
public class SlackServiceImpl implements SlackService {

  private final AuthService authService;
  private final SlackConfig slackConfig;

  public SlackServiceImpl(AuthService authService, SlackConfig slackConfig) {
    this.authService = authService;
    this.slackConfig = slackConfig;
  }

  @Override
  public String postJob(JobInfoForSlackPost jobInfo) throws NoSuchObjectException {
    User user = jobInfo.getContact();
    String contact = user == null ? "?" :
        "<mailto:" + user.getEmail() + "|" + user.getDisplayName() + ">";

    List<LayoutBlock> message = new ArrayList<>();
    message.add(HeaderBlock
        .builder()
        .text(PlainTextObject
            .builder()
            .text(jobInfo.getJobName())
            .build())
        .build());
    message.add(DividerBlock
        .builder()
        .build());

    message.add(SectionBlock
        .builder()
        .text(PlainTextObject
            .builder()
            .text(jobInfo.getJobSummary())
            .build())
        .build());

    message.add(SectionBlock
        .builder()
        .text(MarkdownTextObject
            .builder()
            .text("• Contact person for job " + contact)
            .build())
        .build());

    message.add(SectionBlock
        .builder()
        .text(MarkdownTextObject
            .builder()
            .text("• Link to job on Talent Catalog: " + jobInfo.getTcJobLink())
            .build())
        .build());

    String sfLine =
        "• Salesforce: <" + jobInfo.getSfJobLink() + "|Employer job opportunity>";
    message.add(SectionBlock
        .builder()
        .text(MarkdownTextObject
            .builder()
            .text(sfLine)
            .build())
        .build());
    message.add(DividerBlock
        .builder()
        .build());

    String finalInstructions =
        "Next...\n\n"
            + "• Share this post to the channel associated with this employer, and pin post to that channel";
    message.add(SectionBlock
        .builder()
        .text(PlainTextObject
            .builder()
            .text(finalInstructions)
            .build())
        .build());

    ChatPostMessageRequest req = ChatPostMessageRequest.builder()
        .channel(slackConfig.getChannelId())
        .text("New job")
        .blocks(message)
        .build();

    Slack slack = Slack.getInstance();
    MethodsClient methodsClient = slack.methods(slackConfig.getToken());
    try {
      // Get a response as a Java object
      ChatPostMessageResponse response = methodsClient.chatPostMessage(req);
      response.getChannel();
    } catch (Exception ex) {
      throw new SlackException(
          "Post to Slack channel " + slackConfig.getChannelUrl() + " failed: " + ex);
    }

    return slackConfig.getChannelUrl();
  }

  @Override
  public String postJob(PostJobToSlackRequest request) {
    User user = authService.getLoggedInUser().orElse(null);
    String registeredBy = user == null ? "?" :
        "<mailto:" + user.getEmail() + "|" + user.getDisplayName() + ">";

    List<LayoutBlock> message = new ArrayList<>();
    message.add(HeaderBlock
        .builder()
        .text(PlainTextObject
            .builder()
            .text(request.getJobName())
            .build())
        .build());
    message.add(DividerBlock
        .builder()
        .build());

    message.add(SectionBlock
        .builder()
        .text(MarkdownTextObject
            .builder()
            .text("• Job registered by " + registeredBy)
            .build())
        .build());

    message.add(SectionBlock
        .builder()
        .text(MarkdownTextObject
            .builder()
            .text("• Talent Catalog List: " + request.getListlink() +
                ". Put candidates in this list and publish the list to a sheet you can share with employer.")
            .build())
        .build());

    String folderDescription =
        "• Google Folders:"
            + " <" + request.getFolderlink() + "|Root> folder (contains published list sheets),"
            + " <" + request.getFolderjdlink() + "|Job Description> sub folder";
    message.add(SectionBlock
        .builder()
        .text(MarkdownTextObject
            .builder()
            .text(folderDescription)
            .build())
        .build());

    String sfLine =
        "• Salesforce: <" + request.getSfJoblink() + "|Employer job opportunity>";
    message.add(SectionBlock
        .builder()
        .text(MarkdownTextObject
            .builder()
            .text(sfLine)
            .build())
        .build());
    message.add(DividerBlock
        .builder()
        .build());

    String finalInstructions =
        "Next...\n\n"
        + "• Copy job description documents into the above Job Description folder (if they are not already there)\n"
            + "• Share this post to the channel associated with this employer.";
    message.add(SectionBlock
        .builder()
        .text(PlainTextObject
            .builder()
            .text(finalInstructions)
            .build())
        .build());

    ChatPostMessageRequest req = ChatPostMessageRequest.builder()
        .channel(slackConfig.getChannelId())
        .text("New job")
        .blocks(message)
        .build();

    Slack slack = Slack.getInstance();
    MethodsClient methodsClient = slack.methods(slackConfig.getToken());
    try {
      // Get a response as a Java object
      ChatPostMessageResponse response = methodsClient.chatPostMessage(req);
      response.getChannel();
    } catch (Exception ex) {
      throw new SlackException(
          "Post to Slack channel " + slackConfig.getChannelUrl() + " failed: " + ex);
    }

    return slackConfig.getChannelUrl();
  }
}
