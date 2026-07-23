/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 *  the terms of the GNU General Public License as published by the Free
 *  Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.batchjob.candidate;

import static org.mockito.Mockito.verify;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.item.Chunk;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.repository.db.CandidateRepository;

@ExtendWith(MockitoExtension.class)
class CandidateWriterTest {

  @Mock
  private CandidateRepository candidateRepository;

  @Mock
  private Candidate firstCandidate;

  @Mock
  private Candidate secondCandidate;

  @Test
  void writeSavesAllCandidatesFromChunk() throws Exception {
    CandidateWriter writer = new CandidateWriter(candidateRepository);
    Chunk<Candidate> chunk = new Chunk<>(List.of(firstCandidate, secondCandidate));

    writer.write(chunk);

    verify(candidateRepository).saveAll(chunk.getItems());
  }
}