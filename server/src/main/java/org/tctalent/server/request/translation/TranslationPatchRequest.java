/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.request.translation;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents a request to patch translations. This class encapsulates the version of the translation,
 * an optional description, a list of languages to be updated, and a list of translation entries
 * that contain the keys and their corresponding translations.
 *
 * @author sadatmalik
 */
@Setter
@Getter
public class TranslationPatchRequest {

    @NotNull
    private Integer version;

    private String description;

    private List<String> languages;

    @NotNull
    @Valid
    private List<TranslationPatchEntry> entries;

}
