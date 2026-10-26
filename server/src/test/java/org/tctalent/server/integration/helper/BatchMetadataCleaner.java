/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.integration.helper;

import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Deletes the Spring Batch metadata of jobs run by integration tests.
 * <p>
 * Tests which run batch jobs commit their metadata. The shared test database is reloaded from
 * dump.sql before each test class, which resets the batch sequences - so metadata left behind
 * would clash with the ids of later tests' job executions.
 * </p>
 */
public final class BatchMetadataCleaner {

    private BatchMetadataCleaner() {
    }

    /**
     * Deletes all metadata of jobs whose names match the given SQL "like" pattern.
     */
    public static void deleteJobs(JdbcTemplate jdbc, String jobNamePattern) {
        String instances = "select job_instance_id from batch_job_instance where job_name like ?";
        String executions = "select job_execution_id from batch_job_execution"
            + " where job_instance_id in (" + instances + ")";
        String steps = "select step_execution_id from batch_step_execution"
            + " where job_execution_id in (" + executions + ")";
        jdbc.update("delete from batch_step_execution_context where step_execution_id in ("
            + steps + ")", jobNamePattern);
        jdbc.update("delete from batch_step_execution where job_execution_id in ("
            + executions + ")", jobNamePattern);
        jdbc.update("delete from batch_job_execution_context where job_execution_id in ("
            + executions + ")", jobNamePattern);
        jdbc.update("delete from batch_job_execution_params where job_execution_id in ("
            + executions + ")", jobNamePattern);
        jdbc.update("delete from batch_job_execution where job_instance_id in ("
            + instances + ")", jobNamePattern);
        jdbc.update("delete from batch_job_instance where job_name like ?", jobNamePattern);
    }
}
