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

package org.tctalent.server.service.db.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tctalent.server.exception.EntityExistsException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.Occupation;
import org.tctalent.server.model.db.TaskImpl;
import org.tctalent.server.model.db.task.AllowedQuestionTaskAnswer;
import org.tctalent.server.model.db.task.QuestionTask;
import org.tctalent.server.model.db.task.Task;
import org.tctalent.server.repository.db.TaskRepository;
import org.tctalent.server.repository.db.TaskSpecification;
import org.tctalent.server.request.task.SearchTaskRequest;
import org.tctalent.server.request.task.UpdateTaskRequest;
import org.tctalent.server.service.db.TaskService;
import org.tctalent.server.util.BeanHelper;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.List;


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

    @Override
    @Transactional
    public TaskImpl update(long id, UpdateTaskRequest request) throws EntityExistsException, NoSuchObjectException {
        TaskImpl task = this.taskRepository.findById(id)
                .orElseThrow(() -> new NoSuchObjectException(Occupation.class, id));
        checkDuplicates(id, request.getDisplayName());

        task.setDisplayName(request.getDisplayName());
        task.setDescription(request.getDescription());
        task.setDaysToComplete(request.getDaysToComplete());
        if (StringUtils.isNotBlank(request.getHelpLink())) {
            task.setHelpLink(request.getHelpLink());
        } else {
            task.setHelpLink(null);
        }
        task.setOptional(request.isOptional());
        return taskRepository.save(task);
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

        //TODO JC This is a bit of a hack - we need a better way of processing things like candidateExams
        if (field != null && !field.startsWith("candidateExams.")) {
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
        } else if (field == null && qt.getExplicitAllowedAnswers() != null){
            // There is no answer field associated with question (it should be stored as a property) but there are explicit allowed answers provided.
            // We need to then set the allowed answers for to these explicit answers, so that they can be displayed in the front end.
            List<AllowedQuestionTaskAnswer> allowedAnswers = new ArrayList<>();
            for (String answer : qt.getExplicitAllowedAnswers()) {
                allowedAnswers.add(new AllowedQuestionTaskAnswer(
                        answer, answer));
            }
            qt.setAllowedAnswers(allowedAnswers);
        }
    }

    private void checkDuplicates(Long id, String displayName) {
        TaskImpl existing = taskRepository.findByLowerDisplayName(displayName);
        if (existing != null && !existing.getId().equals(id)){
            throw new EntityExistsException("task");
        }
    }
}
