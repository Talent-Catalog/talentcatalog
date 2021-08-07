/*
 * Copyright (c) 2021 Talent Beyond Boundaries.
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

package org.tbbtalent.server.service.db.impl;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.tbbtalent.server.configuration.SlackConfig;
import org.tbbtalent.server.exception.SlackException;
import org.tbbtalent.server.model.db.User;
import org.tbbtalent.server.request.opportunity.PostJobToSlackRequest;
import org.tbbtalent.server.security.AuthService;
import org.tbbtalent.server.service.db.SlackService;

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
public class SlackServiceImpl implements SlackService {
  private static final Logger log = LoggerFactory.getLogger(SlackServiceImpl.class);

  private final AuthService authService;
  private final SlackConfig slackConfig;

  public SlackServiceImpl(AuthService authService, SlackConfig slackConfig) {
    this.authService = authService;
    this.slackConfig = slackConfig;
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
            .text("• Talent Catalog List: " + request.getListlink())
            .build())
        .build());
    
    String folderDescription =
        "• Google Folders:" 
            + " <" + request.getFolderlink() + "|Root> folder,"
            + " <" + request.getFoldercvlink() + "|CVs> folder (share with employers),"
            + " <" + request.getFolderjdlink() + "|Job Description> folder";
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
        + "• Copy job description documents into Job Description folder (if they are not already there)\n" 
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
