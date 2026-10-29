/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.support.MapJobRegistry;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.support.TaskExecutorJobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

/**
 * Batch configuration class for setting up the candidate migration job, including its steps,
 * readers, processors, and writers.
 * </p>
 * This class defines the Spring Batch beans required for the candidate migration process:
 * - A job to encapsulate the overall batch process
 * - A job to migrate candidates to AuroraDB
 * - A job to migrate candidates to MongoDB
 * - A step to read, process, and write candidates to AuroraDB
 * - A step to read, process, and write candidates to MongoDB
 * - Components for reading from the TC service or JPA repository, processing candidates into
 *   anonymised documents, and writing documents to MongoDB
 * </p>
 * Additional logging listeners are configured to monitor and log the batch progress at various
 * stages.
 *
 * @author sadatmalik
 */
@Configuration
@RequiredArgsConstructor
public class BatchConfig {

    /**
     * Spring Batch 6 no longer provides a JobRegistry bean, so we define one. As in Spring
     * Batch 5, it is automatically populated with any Job beans.
     */
    @Bean
    public JobRegistry jobRegistry() {
        return new MapJobRegistry();
    }

    /**
     * In Spring Batch 6 the JobOperator also launches jobs (it replaces JobLauncher).
     * Using an async executor so that jobOperator.start() returns immediately.
     * See: https://docs.spring.io/spring-batch/reference/job/configuring-launcher.html
     */
    @Bean
    @Qualifier("asyncJobOperator")
    public JobOperator asyncJobOperator(
        JobRepository jobRepository, JobRegistry jobRegistry) throws Exception {

        TaskExecutorJobOperator jobOperator = new TaskExecutorJobOperator();
        jobOperator.setJobRepository(jobRepository);
        jobOperator.setJobRegistry(jobRegistry);
        jobOperator.setTaskExecutor(new SimpleAsyncTaskExecutor());
        jobOperator.afterPropertiesSet();

        return jobOperator;
    }

}
