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

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import org.tctalent.server.exception.EntityReferencedException;
import org.tctalent.server.exception.InvalidRequestException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.ChatPost;
import org.tctalent.server.model.db.LinkPreview;
import org.tctalent.server.repository.db.LinkPreviewRepository;
import org.tctalent.server.repository.db.ChatPostRepository;
import org.tctalent.server.service.db.LinkPreviewService;

@RequiredArgsConstructor
@Service
public class LinkPreviewServiceImpl implements LinkPreviewService {

  private final LinkPreviewRepository linkPreviewRepository;

  @Override
  public void attach(ChatPost chatPost, List<LinkPreview> linkPreviews)
      throws NoSuchObjectException {

    linkPreviews.forEach(linkPreview -> {
      linkPreview.setChatPost(chatPost);
      linkPreviewRepository.save(linkPreview);
    });
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
    linkPreview.setUrl(url);
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
