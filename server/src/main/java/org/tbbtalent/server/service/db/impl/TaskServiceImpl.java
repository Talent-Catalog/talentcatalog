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

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.model.db.Candidate;
import org.tbbtalent.server.model.db.TaskImpl;
import org.tbbtalent.server.model.db.task.AllowedQuestionTaskAnswer;
import org.tbbtalent.server.model.db.task.QuestionTask;
import org.tbbtalent.server.model.db.task.Task;
import org.tbbtalent.server.repository.db.TaskRepository;
import org.tbbtalent.server.repository.db.TaskSpecification;
import org.tbbtalent.server.request.task.SearchTaskRequest;
import org.tbbtalent.server.service.db.TaskService;
import org.tbbtalent.server.util.BeanHelper;


@Service
public class TaskServiceImpl implements TaskService {
    private final TaskRepository taskRepository;

    public TaskServiceImpl(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @NonNull
    @Override
    public TaskImpl get(long taskId) throws NoSuchObjectException {
        final TaskImpl task = taskRepository.findById(taskId)
            .orElseThrow(() -> new NoSuchObjectException(Task.class, taskId));

        populateTransientFields(task);

        return task;
    }

    @NonNull
    @Override
    public TaskImpl getByName(String name) throws NoSuchObjectException {
        final TaskImpl task = taskRepository.findByLowerName(name)
            .orElseThrow(() -> new NoSuchObjectException(Task.class, name));

        populateTransientFields(task);

        return task;
    }

    @Override
    public List<TaskImpl> listTasks() {
        final List<TaskImpl> tasks = taskRepository.findAll(Sort.by(Direction.ASC, "name"));

        populateTransientFields(tasks);

        return tasks;
    }

    @Override
    public Page<TaskImpl> searchTasks(SearchTaskRequest request) {
        Page<TaskImpl> tasks = taskRepository.findAll(
                TaskSpecification.buildSearchQuery(request), request.getPageRequest());

        populateTransientFields(tasks.getContent());

        return tasks;
    }

    private void populateTransientFields(List<TaskImpl> tasks) {
        for (TaskImpl task : tasks) {
            populateTransientFields(task);
        }
    }

    public void populateTransientFields(Task task) {
        if (task instanceof QuestionTask) {
            populateTransientQuestionFields((QuestionTask) task);
        }
    }

    private void populateTransientQuestionFields(QuestionTask qt) {
        final String field = qt.getCandidateAnswerField();
        if (field != null) {
            final PropertyDescriptor descriptor;
            try {
                descriptor = BeanHelper.getPropertyDescriptor(Candidate.class, field);
            } catch (IntrospectionException e) {
                throw new NoSuchObjectException("Could not access Candidate field: " + field);
            }
            if (descriptor == null) {
                throw new NoSuchObjectException("No such Candidate field: " + field);
            }

            //Get type of Candidate field
            final Class<?> type = descriptor.getPropertyType();
            final Enum<?>[] enumConstants = (Enum<?>[]) type.getEnumConstants();
            if (enumConstants != null) {
                //Field is an enum type - use its values to populate allowed answers
                List<AllowedQuestionTaskAnswer> allowedAnswers = new ArrayList<>();
                for (Enum<?> enumConstant : enumConstants) {
                    //Note that displayName is populated by toString. For a simple enum
                    //toString will be the same as the enum name.
                    //But if you want to have more descriptive display names, you can
                    //override an enum's toString as described here:
                    //https://www.baeldung.com/java-enum-values#1-overriding-tostring
                    //See enum ResidenceStatus for an example of this.
                    final String enumName = enumConstant.name();
                    //Filter out "NoResponse" values - eg see values for enum YesNo
                    if (!enumName.equals("NoResponse")) {
                        allowedAnswers.add(new AllowedQuestionTaskAnswer(
                            enumName, enumConstant.toString()));
                    }
                }
                qt.setAllowedAnswers(allowedAnswers);
            }
        }
    }
}
