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

package org.tctalent.server.util.html;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Service;

/**
 * JSoup https://jsoup.org/ implementation of Text Extraction from HTML
 *
 * @author Tim Hill
 */
@Service
public class JsoupTextExtracterImpl implements TextExtracter {
    /**
     * Given an HTML string extract just the text
     *
     * @param html an HTML string
     * @return the text from the HTML. Tags replaced with space
     */
    @Override
    public String ExtractText(String html) {
        return Jsoup.clean(html, Safelist.none());
    }
}
