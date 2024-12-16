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

package org.tctalent.server.repository.db;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.tctalent.server.model.db.TaskImpl;
import org.tctalent.server.model.db.UploadTaskImpl;
import org.tctalent.server.model.db.User;
import org.tctalent.server.model.db.task.TaskType;
import org.tctalent.server.model.db.task.UploadType;
import org.tctalent.server.service.db.UserService;

@Tag("skip-test-in-gradle-build")
@SpringBootTest
class TaskRepositoryTest {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserService userService;

    private TaskImpl task;
    private User owningUser;


    @BeforeEach
    void setUp() {
        assertNotNull(taskRepository);

        owningUser = userService.getSystemAdminUser();
    }

    @Test
    void createTask() {

        task = new TaskImpl();

        task.setName("Sample Simple Task");
        task.setCreatedBy(owningUser);
        task.setCreatedDate(OffsetDateTime.now());

        taskRepository.save(task);

        UploadTaskImpl utask = new UploadTaskImpl();

        utask.setName("Sample Upload Task");
        utask.setCreatedBy(owningUser);
        utask.setCreatedDate(OffsetDateTime.now());
        utask.setUploadType(UploadType.cv);
        utask.setUploadSubfolderName("CVsGoHere");

        taskRepository.save(utask);

    }

    @Test
    void fetchTask() {
        List<TaskImpl> tasks;
        tasks = taskRepository.findByName("Sample Upload Task");
        assertNotNull(tasks);
        if (tasks.size() > 0) {
            task = tasks.get(0);
            String name = task.getName();
            Assertions.assertEquals(TaskType.Upload, task.getTaskType());
            if (task instanceof UploadTaskImpl) {
                UploadTaskImpl uploadTask = (UploadTaskImpl) task;
                Assertions.assertEquals(UploadType.cv, uploadTask.getUploadType());
                assertEquals("CVsGoHere", uploadTask.getUploadSubfolderName());
            }
        }

        tasks = taskRepository.findByName("Sample Simple Task");
        if (tasks.size() > 0) {
            Assertions.assertEquals(TaskType.Simple, tasks.get(0).getTaskType());
        }
    }
}
