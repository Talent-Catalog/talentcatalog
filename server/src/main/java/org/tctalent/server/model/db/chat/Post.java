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

package org.tctalent.server.model.db.chat;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.tctalent.server.model.db.LinkPreview;
import org.tctalent.server.util.html.HtmlSanitizer;

/**
 * A post received from a user.
 *
 * @author John Cameron
 */
@Getter
@Setter
public class Post {
    private String content;

    private List<LinkPreview> linkPreviews = new ArrayList<>();

    public void setContent(String content) {
        this.content = HtmlSanitizer.sanitizeWithLinksNewTab(content);
    }
}
