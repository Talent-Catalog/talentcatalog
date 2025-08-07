/*
 * Copyright (c) 2025 Talent Catalog.
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

package org.tctalent.server.batchjob.candidate;

import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.tctalent.server.repository.db.CandidateRepository;
import org.tctalent.server.service.db.SavedSearchService;

/**
 * Creates and configures a CandidateJobFactory.
 *
 * @author John Cameron
 */
@Configuration
public class CandidateJobFactoryConfig {

    @Bean CandidateJobFactory candidateJobFactory(
        PlatformTransactionManager transactionManager,
        JobRepository jobRepository,
        CandidateRepository candidateRepository,
        SavedSearchService savedSearchService) {
        return new CandidateJobFactory(
            transactionManager,
            jobRepository,
            candidateRepository,
            savedSearchService);
    }

}
