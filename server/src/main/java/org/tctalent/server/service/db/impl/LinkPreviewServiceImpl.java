/*
 * Copyright (c) 2024 Talent Beyond Boundaries.
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

import org.springframework.stereotype.Service;
import org.tctalent.server.exception.EntityReferencedException;
import org.tctalent.server.exception.InvalidRequestException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.ChatPost;
import org.tctalent.server.model.db.LinkPreview;
import org.tctalent.server.model.db.LinkPreviewRepository;
import org.tctalent.server.repository.db.ChatPostRepository;
import org.tctalent.server.request.chat.link_preview.CreateLinkPreviewRequest;
import org.tctalent.server.service.db.LinkPreviewService;

@Service
public class LinkPreviewServiceImpl implements LinkPreviewService {

  private final ChatPostRepository chatPostRepository;
  private final LinkPreviewRepository linkPreviewRepository;

  public LinkPreviewServiceImpl(ChatPostRepository chatPostRepository,
      LinkPreviewRepository linkPreviewRepository) {
    this.chatPostRepository = chatPostRepository;
    this.linkPreviewRepository = linkPreviewRepository;
  }

  @Override
  public LinkPreview createLinkPreview(long chatPostId, CreateLinkPreviewRequest request)
      throws NoSuchObjectException {

    ChatPost chatPost = chatPostRepository.findById(chatPostId)
        .orElseThrow(() -> new NoSuchObjectException(ChatPost.class, chatPostId));

    LinkPreview linkPreview = new LinkPreview();

    linkPreview.setChatPost(chatPost);
    linkPreview.setTitle(request.getTitle());
    linkPreview.setUrl(request.getUrl());
    linkPreview.setDescription(request.getDescription());
    linkPreview.setImageUrl(request.getImageUrl());

    return linkPreviewRepository.save(linkPreview);
  }

  @Override
  public boolean deleteLinkPreview(long linkPreviewId)
      throws EntityReferencedException, InvalidRequestException {
    linkPreviewRepository.deleteById(linkPreviewId);
    return true;
  }

  @Override
  public LinkPreview buildLinkPreview(String url) {
    LinkPreview linkPreview = new LinkPreview();
    linkPreview.setUrl("www.nonsense.com");
    linkPreview.setTitle("A made-up webpage about nothing");
    linkPreview.setDescription("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Pellentesque mollis pretium quis.");
    linkPreview.setImageUrl("https://ipsumfactory.com/wp-content/uploads/2023/04/ipsum-factory-150x150.png");
    return linkPreview;
  }

  private String getTitle(Document document) {
    // implement JSoup scraping w JC link as code guidance
    return "";
  }

  private String getDescription(Document document) {
    // implement JSoup scraping w JC link as code guidance
    return "";
  }

  private String getImageUrl(Document document) {
    // implement JSoup scraping w JC link as code guidance
    return "";
  }

}
