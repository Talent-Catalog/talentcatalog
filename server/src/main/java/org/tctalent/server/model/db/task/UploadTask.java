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

package org.tctalent.server.model.db.task;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.List;

/**
 * Task where candidate is required to upload a file.
 *
 * Specifies information including where the upload will be made to, how it is named,
 * and which file types are allowed.
 *
 * @author John Cameron
 */
public interface UploadTask extends Task {
    /**
     * Type of file being uploaded
     */
    @NonNull
    UploadType getUploadType();

    /**
     * Optional subfolder name to upload into. If null, no subfolder is used.
     */
    @Nullable
    String getUploadSubfolderName();

    /**
     * Allowable file types (eg pdf, doc, jpg etc). If null, any file type is acceptable.
     */
    @Nullable
    //todo This really needs to be known by Angular - maybe lose this. Or keep and throw Exception for bad suffixes
    List<String> getUploadableFileTypes();


    default TaskType getTaskType() {
        return TaskType.Upload;
    }

}
