/*
 * Copyright (c) 2026 Talent Catalog.
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

package org.tctalent.server.util.text;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Returns tidied text if available, otherwise returns original text.
 *
 * @author John Cameron
 */
@Service
public class TextPartsTidiedTextService {

    public String getTidiedText(String text) {

        if (text == null || text.isBlank()) {
            return text;
        }

        TextParts parts = TextPartsCodec.read(text);

        if (StringUtils.hasText(parts.getTidied())) {
            return parts.getTidied();
        }

        return parts.getOriginal();
    }
}
