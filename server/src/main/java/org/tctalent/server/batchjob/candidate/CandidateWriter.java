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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.repository.db.CandidateRepository;

/**
 * Standard CandidateWriter for CandidateJob batches as created by CandidateJobFactory.
 *
 * @author John Cameron
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Qualifier("candidateWriter")
public class CandidateWriter implements ItemWriter<Candidate> {
    private final CandidateRepository candidateRepository;

    @Override
    public void write(@NonNull Chunk<? extends Candidate> chunk) throws Exception {
        candidateRepository.saveAll(chunk.getItems());
    }
}
