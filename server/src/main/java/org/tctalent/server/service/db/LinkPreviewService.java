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

package org.tctalent.server.service.db;

import java.util.List;
import org.tctalent.server.exception.EntityReferencedException;
import org.tctalent.server.exception.InvalidRequestException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.ChatPost;
import org.tctalent.server.model.db.LinkPreview;

public interface LinkPreviewService {

  /**
   * TODO doc
   * @param url
   * @return
   */
  LinkPreview buildLinkPreview(String url);

  /**
   * TODO doc
   * @param
   * @param
   * @throws NoSuchObjectException
   */
  void attach(ChatPost chatPost, List<LinkPreview> linkPreviews)
      throws NoSuchObjectException;

  /**
   * TODO doc
   * @param linkPreviewId
   * @return
   * @throws EntityReferencedException
   * @throws InvalidRequestException
   */
  boolean deleteLinkPreview(long linkPreviewId)
    throws EntityReferencedException, InvalidRequestException;

}