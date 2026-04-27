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

package org.tctalent.server.service.db.impl;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.CandidateExam;
import org.tctalent.server.model.db.DuolingoExtraFields;
import org.tctalent.server.repository.db.CandidateExamRepository;
import org.tctalent.server.repository.db.DuolingoExtraFieldsRepository;
import org.tctalent.server.request.duolingo.DuolingoExtraFieldsRequest;
import org.tctalent.server.service.db.DuolingoExtraFieldsService;

@Service
@RequiredArgsConstructor
public class DuolingoExtraFieldsServiceImpl implements DuolingoExtraFieldsService {

  private final DuolingoExtraFieldsRepository duolingoExtraFieldsRepository;
  private final CandidateExamRepository candidateExamRepository;

  @Override
  @Transactional
  public void createOrUpdateDuolingoExtraFields(
      DuolingoExtraFieldsRequest request) throws NoSuchObjectException {
    DuolingoExtraFields duolingoExtraFields = new DuolingoExtraFields();

    // Set the fields from request to entity
    duolingoExtraFields.setCertificateUrl(request.getCertificateUrl());
    duolingoExtraFields.setInterviewUrl(request.getInterviewUrl());
    duolingoExtraFields.setVerificationDate(request.getVerificationDate());
    duolingoExtraFields.setPercentScore(request.getPercentScore());
    duolingoExtraFields.setScale(request.getScale());
    duolingoExtraFields.setLiteracySubscore(request.getLiteracySubscore());
    duolingoExtraFields.setConversationSubscore(request.getConversationSubscore());
    duolingoExtraFields.setComprehensionSubscore(request.getComprehensionSubscore());
    duolingoExtraFields.setProductionSubscore(request.getProductionSubscore());
    // Find the candidate exam by ID and set it
    CandidateExam candidateExam = candidateExamRepository.findById(request.getCandidateExamId())
        .orElseThrow(() -> new NoSuchObjectException("CandidateExam not found with ID: " + request.getCandidateExamId()));
    duolingoExtraFields.setCandidateExam(candidateExam);

    // Save the entity
    duolingoExtraFieldsRepository.save(duolingoExtraFields);
  }
}

