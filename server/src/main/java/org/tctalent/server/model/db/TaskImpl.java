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


import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.util.List;
import javax.annotation.Nullable;
import lombok.Getter;
import lombok.Setter;
import org.tctalent.server.model.db.task.Task;
import org.tctalent.server.model.db.task.TaskType;
import org.tctalent.server.response.MetadataFieldResponse;

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
    private String docLink;
    private String displayName;
    private String name;
    private boolean optional;
    @Nullable
    @Convert(converter = MetadataFieldConverter.class)
    @Column(name = "required_metadata", columnDefinition = "jsonb")
    private List<MetadataFieldResponse> requiredMetadata;
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
