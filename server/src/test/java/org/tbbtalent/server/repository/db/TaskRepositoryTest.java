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

package org.tbbtalent.server.repository.db;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.tbbtalent.server.model.db.TaskImpl;
import org.tbbtalent.server.model.db.UploadTaskImpl;
import org.tbbtalent.server.model.db.User;
import org.tbbtalent.server.model.db.task.TaskType;
import org.tbbtalent.server.model.db.task.UploadType;
import org.tbbtalent.server.service.db.UserService;

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
        utask.setUploadType(UploadType.Cv);
        utask.setUploadSubfolderName("CVsGoHere");

        taskRepository.save(utask);

    }

    @Transactional
    @Test
    void fetchTask() {
        List<TaskImpl> tasks;
        tasks = taskRepository.findByName("Sample Upload Task");
        assertNotNull(tasks);
        if (tasks.size() > 0) {
            task = tasks.get(0);
            String name = task.getName();
            assertEquals(TaskType.Upload, task.getTaskType());
            if (task instanceof UploadTaskImpl) {
                UploadTaskImpl uploadTask = (UploadTaskImpl) task;
                assertEquals(UploadType.Cv, uploadTask.getUploadType());
                assertEquals("CVsGoHere", uploadTask.getUploadSubfolderName());
            }
        }

        tasks = taskRepository.findByName("Sample Simple Task");
        if (tasks.size() > 0) {
            assertEquals(TaskType.Simple, tasks.get(0).getTaskType());
        }
    }
}
