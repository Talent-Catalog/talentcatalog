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

package org.tbbtalent.server.service.db.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.tbbtalent.server.model.db.QuestionTask;
import org.tbbtalent.server.model.db.TaskImpl;
import org.tbbtalent.server.model.db.task.UploadTask;
import org.tbbtalent.server.repository.db.TaskRepository;
import org.tbbtalent.server.request.CreateTaskRequest;
import org.tbbtalent.server.request.task.CreateQuestionTaskRequest;
import org.tbbtalent.server.request.task.CreateUploadTaskRequest;


// TODO Note for Caroline: The problem with SpringBootTest is that it starts up the whole of Spring
//  which is very slow. Where possible tests should be fast and not require Spring.
// You only want SpringBootTests when you want to test the integration with Spring.
@SpringBootTest
class TaskServiceImplTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskServiceImpl taskService;

    /**
     * GIVEN: A task is to be created with a create task request.
     * WHEN: Saved into the repository by the service's create method.
     * THEN: Returns created/saved task object.
     */
    @Test
    void createTask(){
        CreateTaskRequest request = new CreateTaskRequest();
        request.setName("Test Task");
        request.setDescription("This is a test description.");
        request.setTimeframe("2 Weeks");
        request.setAdminOnly(false);
        request.setList(false);

        // Do I need a repository for each task class (e.g. question/upload/other, even if they extend from the one interface)
        // https://stackoverflow.com/a/63658452 - looks like can use the single repo
        when(taskRepository.save(any(org.tbbtalent.server.model.db.task.Task.class))).then(returnsFirstArg());
        TaskImpl task = taskService.createTask(request);

        assertNotNull(task);
        assertThat(task.getName()).isEqualTo("Test Task");
        assertThat(task.getDescription()).isEqualTo("This is a test description.");
    }

    /**
     * GIVEN: A question task is to be created with a create question task request.
     * WHEN: Saved into the repository by the service's create method.
     * THEN: Returns created/saved question task object.
     */
    @Test
    void createQuestionTask(){
        CreateQuestionTaskRequest request = new CreateQuestionTaskRequest();
        request.setName("Test Question Task");
        request.setDescription("This is a test description.");
        request.setTimeframe("2 Weeks");
        request.setAdminOnly(false);
        request.setQuestion("Do you have a passport?");
        request.setAnswer("Yes");

        // Do I need a repository for each task class (e.g. question/upload/other, even if they extend from the one interface)
        // https://stackoverflow.com/a/63658452 - looks like can use the single repo
        when(taskRepository.save(any(org.tbbtalent.server.model.db.task.Task.class))).then(returnsFirstArg());
        QuestionTask qTask = taskService.createQuestionTask(request);

        assertNotNull(qTask);
        assertThat(qTask.getName()).isEqualTo("Test Question Task");
        assertThat(qTask.getDescription()).isEqualTo("This is a test description.");
        assertThat(qTask.getTimeframe()).isEqualTo("2 Weeks");
        assertThat(qTask.isAdminOnly()).isFalse();
        assertThat(qTask.getQuestion()).isEqualTo("Do you have a passport?");
        assertThat(qTask.getAnswer()).isEqualTo("Yes");
    }

    /**
     * GIVEN: An upload task is to be created with a create upload task request.
     * WHEN: Saved into the repository by the service's create method.
     * THEN: Returns created/saved upload task object.
     */
    @Test
    void createUploadTask(){
        CreateUploadTaskRequest request = new CreateUploadTaskRequest();
        request.setName("Test Upload Task");
        request.setDescription("This is a test description.");
        request.setDaysToComplete(14);
        request.setAdmin(false);

        // Do I need a repository for each task class (e.g. question/upload/other, even if they extend from the one interface)
        // https://stackoverflow.com/a/63658452 - looks like can use the single repo
        when(taskRepository.save(any(org.tbbtalent.server.model.db.task.Task.class))).then(returnsFirstArg());
        UploadTask uTask = taskService.createUploadTask(request);

        assertNotNull(uTask);
        assertEquals("Test Upload Task", uTask.getName());
        assertEquals("This is a test description.", uTask.getDescription());
        assertEquals(14, uTask.getDaysToComplete());
        assertThat(uTask.isAdmin()).isFalse();
    }

    /**
     * GIVEN: A task list is to be created with a create task request.
     * WHEN: Saved into the repository by the service's create method.
     * THEN: Returns created/saved upload task object.
     */
    @Test
    void createTaskList(){
        CreateTaskRequest request = new CreateTaskRequest();
        request.setName("Test Task List");
        request.setDescription("This is a test description.");
        request.setTimeframe("2 Weeks");
        request.setAdminOnly(false);
        request.setList(true);

        // Do I need a repository for each task class (e.g. question/upload/other, even if they extend from the one interface)
        // https://stackoverflow.com/a/63658452 - looks like can use the single repo
        when(taskRepository.save(any(org.tbbtalent.server.model.db.task.Task.class))).then(returnsFirstArg());
        TaskImpl task = taskService.createTask(request);

        assertNotNull(task);
        assertThat(task.getName()).isEqualTo("Test Task List");
        assertThat(task.getDescription()).isEqualTo("This is a test description.");
    }
}
