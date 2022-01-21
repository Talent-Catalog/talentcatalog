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

import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.NonNull;
import org.tbbtalent.server.model.db.task.TaskType;
import org.tbbtalent.server.model.db.task.UploadInfo;
import org.tbbtalent.server.model.db.task.UploadTask;

/**
 * TODO JC Doc
 *
 * @author John Cameron
 */
@Getter
@Setter
public class UploadTaskImpl extends TaskImpl implements UploadTask {

    private UploadInfo uploadInfo;

    @NonNull
    @Override
    public TaskType getType() {
        return TaskType.Upload;
    }
}
