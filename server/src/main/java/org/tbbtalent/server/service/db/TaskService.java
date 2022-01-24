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

package org.tbbtalent.server.service.db;

import java.util.List;
import org.tbbtalent.server.model.db.TaskImpl;
import org.tbbtalent.server.model.db.UploadTaskImpl;
import org.tbbtalent.server.request.CreateTaskRequest;
import org.tbbtalent.server.request.task.CreateUploadTaskRequest;

/**
 * TODO JC Doc
 *
 * @author John Cameron
 */
public interface TaskService {
    TaskImpl createTask(CreateTaskRequest request);

    UploadTaskImpl createUploadTask(CreateUploadTaskRequest request);

    List<TaskImpl> listTasks();
}
