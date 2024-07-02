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

import java.io.IOException;
import org.springframework.stereotype.Service;
import org.tctalent.server.service.db.LinkPreviewService;

@Service
public class LinkPreviewServiceImpl implements LinkPreviewService {

  @Override
  public String buildLinkPreview(String url) throws IOException {
    // TODO: may need to utilise cross-site scripting safety measures in HtmlSanitizer before doing any parsing - read up on this
    return "";
  }
}
