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

import java.io.IOException;
import org.tctalent.server.exception.EntityReferencedException;
import org.tctalent.server.exception.InvalidRequestException;
import org.tctalent.server.model.db.LinkPreview;

/**
 * Implements Jsoup web-scraping and HTML parsing to return link previews.
 * Uses <a href="https://andrejgajdos.com/how-to-create-a-link-preview/">...</a> as a starting point
 * for selecting content from the many divergent options available across different websites.
 *
 * @author samschlicht
 */

public interface LinkPreviewService {

  /**
   * Receives a URL in String form and returns a LinkPreview for display in Job Chats if successful.
   * @param url URL String of link to be previewed
   * @return Map of LinkPreview or null if unsuccessful
   * @throws IOException if Jsoup web scraping is unsuccessful
   */
  LinkPreview buildLinkPreview(String url) throws IOException;

  /**
   * @param linkPreviewId ID of record to be deleted
   * @return boolean confirming successful deletion
   * @throws EntityReferencedException if the object cannot be deleted because
   * it is referenced by another object.
   * @throws InvalidRequestException if not authorized to delete this object.
   */
  boolean deleteLinkPreview(long linkPreviewId)
    throws EntityReferencedException, InvalidRequestException;

}