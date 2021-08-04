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
import org.tbbtalent.server.request.opportunity.PostJobToSlackRequest;
import org.tbbtalent.server.service.db.SlackService;

/**
 * TODO JC Doc
 * See https://medium.com/cloud-native-the-gathering/how-to-send-formatted-slack-message-blocks-via-the-java-slack-sdk-client-api-3fc9edb1aa98
 * There is an app registered on https://api.slack.com/apps for TBB's Slack workspace 
 * (Jobs Marketplace for Refugees) called TalentCatalog.
 *
 * @author John Cameron
 */
@Service
public class SlackServiceImpl implements SlackService {
  private static final Logger log = LoggerFactory.getLogger(SlackServiceImpl.class);

  @Override
  public void postJob(PostJobToSlackRequest request) {
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
            .text("• List: " + request.getListlink())
            .build())
        .build());
    message.add(SectionBlock
        .builder()
        .text(MarkdownTextObject
            .builder()
            .text("• Google Folder: " + request.getFolderlink())
            .build())
        .build());
    message.add(SectionBlock
        .builder()
        .text(MarkdownTextObject
            .builder()
            .text("• Salesforce: " + request.getSfJoblink())
            .build())
        .build());
    message.add(DividerBlock
        .builder()
        .build());
    
    String finalInstructions = 
        "Now please...\n" 
        + "• Copy job description documents into Job Description folder (if they are not already there)\n" 
            + "• Share this post to the channel associated with this employer.";
    message.add(SectionBlock
        .builder()
        .text(PlainTextObject
            .builder()
            .text(finalInstructions)
            .build())
        .build());

    //todo Need config for prod and test Slack channels
    ChatPostMessageRequest req = ChatPostMessageRequest.builder()
//        .channel("C029WMY6H1U")
        .channel("C029XH683GW") //test
        .text("New job")
        .blocks(message)
        .build();
    
    Slack slack = Slack.getInstance();
    MethodsClient methodsClient = slack.methods("xoxb-130076373828-1988494714609-qfarB4l7JJvo1fGlLFjO3RsJ");
    try {
      // Get a response as a Java object
//      ChatPostMessageResponse response = methodsClient.chatPostMessage(reqx -> 
//          reqx.channel("CU1SM56CD").text(":wave: Hi from a bot written in Java!"));
      ChatPostMessageResponse response = methodsClient.chatPostMessage(req);
      response.getChannel();
    } catch (Exception ex) {
      log.info("Slack failed", ex);
    }
    
  }
}
