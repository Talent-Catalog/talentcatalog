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

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;


/**
 * Represents the parts of some other text, including the "original" text,
 * a "tidied" up version of the text, and keywords related to the text.
 *
 * @author John Cameron
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TextParts {
    /**
     * The original "source" text.
     * For example, this could be text entered by a candidate
     * (possibly not in their first language, so it may need some "tidying up").
     */
    private String original;

    /**
     * Optional tidied-up version of the original text.
     * This could be used for constructing a CV.
     */
    @Nullable
    private String tidied;

    /**
     * Keywords associated with the original text.
     */
    private List<String> keywords = new ArrayList<>();

    public TextParts(String original) {
        this.original = original;
        this.keywords = new ArrayList<>();
    }
}
