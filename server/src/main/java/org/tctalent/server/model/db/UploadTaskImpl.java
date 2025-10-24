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

package org.tctalent.server.model.db;

import jakarta.persistence.Convert;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.tctalent.server.model.db.task.TaskType;
import org.tctalent.server.model.db.task.UploadTask;
import org.tctalent.server.model.db.task.UploadType;

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
    @Convert(converter = CommaDelimitedStringsConverter.class)
    private List<String> uploadableFileTypes;

    /*
      Note that this should not be necessary because the interface provides a default implementation
      but PropertyUtils does not find this taskType property if it is just provided by the default
      interface implementations. Looks like some kind of bug.
      - John Cameron
     */
    @Override
    public TaskType getTaskType() {
        return UploadTask.super.getTaskType();
    }
}
