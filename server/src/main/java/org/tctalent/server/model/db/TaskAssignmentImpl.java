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

import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.NonNull;
import org.tctalent.server.model.db.task.TaskAssignment;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import org.tctalent.server.model.db.task.TaskType;

/**
 * Default implementation of {@link TaskAssignment}
 *
 * @author John Cameron
 */
@Entity(name="TaskAssignment")
@Table(name = "task_assignment")
@SequenceGenerator(name = "seq_gen", sequenceName = "task_assignment_id_seq", allocationSize = 1)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "task_type")
@DiscriminatorValue("Task")
@Getter
@Setter
public class TaskAssignmentImpl extends AbstractDomainObject<Long> implements TaskAssignment {
    OffsetDateTime abandonedDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activated_by")
    User activatedBy;

    OffsetDateTime activatedDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id")
    Candidate candidate;

    String candidateNotes;
    OffsetDateTime completedDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deactivated_by")
    User deactivatedBy;

    OffsetDateTime deactivatedDate;

    // Comment CC - Couldn't this be a LocalDate, we don't need the time as we only as for a date.
    // Also we don't neccessarily want an Offset date, if I want something due on the 25th that would be the same date across timezones.
    LocalDate dueDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_list_id")
    SavedList relatedList;

    @Enumerated(EnumType.STRING)
    Status status;

    //We need an EAGER fetch for task, otherwise we don't always get the correct class returne
    //We just get a Hibernate proxy which allows us to access all the TaskImpl attributes
    //but not attributes of subclasses such as UploadTaskImpl - and there is no elegant way
    //of casting to UploadImpl. We need an EAGER fetch to always return the full class. JC
    @NonNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "task_id")
    TaskImpl task;

    /*
      Note that this should not be necessary because the interface provides a default implementation
      but PropertyUtils does not find this taskType property if it is just provided by the default
      interface implementations. Looks like some kind of bug.
      - John Cameron
     */
    @Override
    public TaskType getTaskType() {
        return TaskAssignment.super.getTaskType();
    }
}
