/*
 * Copyright (c) 2023 Talent Beyond Boundaries.
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


import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.tctalent.server.model.db.task.Task;
import org.tctalent.server.model.db.task.TaskType;

/**
 * Base implementation of all tasks.
 */
@Entity(name="Task")
@Table(name = "task")
@SequenceGenerator(name = "seq_gen", sequenceName = "task_id_seq", allocationSize = 1)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "task_type")
@DiscriminatorValue("Task")
@Getter
@Setter
public class TaskImpl extends AbstractAuditableDomainObject<Long> implements Task {
    private boolean admin;
    private Integer daysToComplete;
    private String description;
    private String helpLink;
    private String displayName;
    private String name;
    private boolean optional;

    /*
      Note that this should not be necessary because the interface provides a default implementation
      but PropertyUtils does not find this taskType property if it is just provided by the default
      interface implementations. Looks like some kind of bug.
      - John Cameron
     */
    @Override
    public TaskType getTaskType() {
        return Task.super.getTaskType();
    }
}
