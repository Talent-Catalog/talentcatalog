/*
 * Copyright (c) 2022 Talent Beyond Boundaries.
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

package org.tbbtalent.server.model.db.task;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/**
 * Provides information about different types of upload.
 *
 * @author John Cameron
 */
@Getter
@Setter
public class UploadInfo {

    /**
     * Type of file being uploaded
     */
    @NonNull
    private UploadType type;

    /**
     * Optional subfolder name to upload into. If null, no subfolder is used.
     */
    @Nullable
    private String subFolderName;

    /**
     * Allowable file types (eg pdf, doc, jpg etc). If null, any file type is acceptable.
     */
    @Nullable
    //todo This really needs to be known by Angular - maybe lose this. Or keep and throw Exception for bad suffixes
    private List<String> fileTypes;
}
