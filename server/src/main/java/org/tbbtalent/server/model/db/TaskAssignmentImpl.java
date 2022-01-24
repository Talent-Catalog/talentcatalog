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

import java.time.OffsetDateTime;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.tbbtalent.server.model.db.task.TaskAssignment;

/**
 * Default implementation of {@link TaskAssignment}
 *
 * @author John Cameron
 */
@Entity(name="TaskAssignment")
@Table(name = "task_assignment")
@SequenceGenerator(name = "seq_gen", sequenceName = "task_assignment_id_seq", allocationSize = 1)
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
    OffsetDateTime dueDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_list_id")
    SavedList relatedList;

    @Enumerated(EnumType.STRING)
    Status status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id")
    TaskImpl task;

}
