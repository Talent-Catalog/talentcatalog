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

package org.tbbtalent.server.model.db;

import java.util.Set;
import javax.persistence.Convert;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.tbbtalent.server.model.db.task.UploadTask;
import org.tbbtalent.server.model.db.task.UploadType;

/**
 * Default Implementation
 *
 * @author John Cameron
 */
@Entity(name="UploadTask")
@DiscriminatorValue("UploadTask")
@Getter
@Setter
public class UploadTaskImpl extends TaskImpl implements UploadTask {

    /**
     * Type of file being uploaded
     */
    @Enumerated(EnumType.STRING)
    @NonNull
    private UploadType uploadType;

    /**
     * Optional subfolder name to upload into. If null, no subfolder is used.
     */
    @Nullable
    private String uploadSubfolderName;

    /**
     * Allowable file types (eg pdf, doc, jpg etc). If null, any file type is acceptable.
     */
    @Nullable
    @Convert(converter = DelimitedStringsConverter.class)
    private Set<String> uploadableFileTypes;

}
