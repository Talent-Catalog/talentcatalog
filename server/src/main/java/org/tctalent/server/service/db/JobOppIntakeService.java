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

package org.tctalent.server.service.db;

import org.springframework.lang.NonNull;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.JobOppIntake;
import org.tctalent.server.request.job.IJobIntakeData;

/**
 * Service for managing {@link JobOppIntake}
 *
 * @author John Cameron
 */
public interface JobOppIntakeService {

    /**
     * Get the Job Opp Intake with the given id.
     * @param id Id of intake to get
     * @return Job Opp Intake
     * @throws NoSuchObjectException if there is no Job with this id.
     */
    @NonNull
    JobOppIntake get(long id) throws NoSuchObjectException;

    /**
     * Creates the intake data.
     * @param data Partially populated JobIntakeData record. Null data
     *             fields are ignored. Only non-null fields are added to the new entity.
     */
    JobOppIntake create(IJobIntakeData data);

    /**
     * Updates the intake data.
     * @param id ID of job opp intake
     * @param data Partially populated JobIntakeData record. Null data
     *             fields are ignored. Only non-null fields are updated.
     * @throws NoSuchObjectException if no job is found with that id
     */
    void update(long id, IJobIntakeData data) throws NoSuchObjectException;

}
