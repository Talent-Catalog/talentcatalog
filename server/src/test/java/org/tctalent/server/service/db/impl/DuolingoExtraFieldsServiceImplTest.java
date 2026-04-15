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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.CandidateExam;
import org.tctalent.server.model.db.DuolingoExtraFields;
import org.tctalent.server.repository.db.CandidateExamRepository;
import org.tctalent.server.repository.db.DuolingoExtraFieldsRepository;
import org.tctalent.server.request.duolingo.DuolingoExtraFieldsRequest;

@ExtendWith(MockitoExtension.class)
class DuolingoExtraFieldsServiceImplTest {

  private DuolingoExtraFieldsRequest request;
  private CandidateExam candidateExam;

  private static final Long CANDIDATE_EXAM_ID = 123L;
  private static final String CERTIFICATE_URL = "https://example.com/certificate";
  private static final String INTERVIEW_URL = "https://example.com/interview";
  private static final String VERIFICATION_DATE = "2024-06-01";
  private static final int PERCENT_SCORE = 85;
  private static final int SCALE = 100;
  private static final int LITERACY_SUBSCORE = 80;
  private static final int CONVERSATION_SUBSCORE = 90;
  private static final int COMPREHENSION_SUBSCORE = 85;
  private static final int PRODUCTION_SUBSCORE = 88;

  @Mock private DuolingoExtraFieldsRepository duolingoExtraFieldsRepository;
  @Mock private CandidateExamRepository candidateExamRepository;

  @InjectMocks
  DuolingoExtraFieldsServiceImpl duolingoExtraFieldsService;

  @BeforeEach
  void setUp() {
    candidateExam = new CandidateExam();
    candidateExam.setId(CANDIDATE_EXAM_ID);

    request = new DuolingoExtraFieldsRequest(
        CERTIFICATE_URL,
        INTERVIEW_URL,
        VERIFICATION_DATE,
        PERCENT_SCORE,
        SCALE,
        LITERACY_SUBSCORE,
        CONVERSATION_SUBSCORE,
        COMPREHENSION_SUBSCORE,
        PRODUCTION_SUBSCORE,
        CANDIDATE_EXAM_ID
    );
  }

  @Test
  @DisplayName("should throw when candidate exam not found")
  void createOrUpdateDuolingoExtraFields_shouldThrow_whenCandidateExamNotFound() {
    given(candidateExamRepository.findById(CANDIDATE_EXAM_ID))
        .willReturn(Optional.empty());

    assertThrows(NoSuchObjectException.class,
        () -> duolingoExtraFieldsService.createOrUpdateDuolingoExtraFields(request));
  }

  @Test
  @DisplayName("should create and save duolingo extra fields with all fields properly mapped")
  void createOrUpdateDuolingoExtraFields_shouldCreateAndSaveDuolingoExtraFields() {
    given(candidateExamRepository.findById(CANDIDATE_EXAM_ID))
        .willReturn(Optional.of(candidateExam));
    given(duolingoExtraFieldsRepository.save(any(DuolingoExtraFields.class)))
        .will(returnsFirstArg());

    duolingoExtraFieldsService.createOrUpdateDuolingoExtraFields(request);

    verify(candidateExamRepository).findById(CANDIDATE_EXAM_ID);
    
    ArgumentCaptor<DuolingoExtraFields> captor = ArgumentCaptor.forClass(DuolingoExtraFields.class);
    verify(duolingoExtraFieldsRepository).save(captor.capture());

    DuolingoExtraFields savedFields = captor.getValue();
    verifyDuolingoExtraFields(savedFields);
  }

  @Test
  @DisplayName("should verify repository save operation is called appropriately")
  void createOrUpdateDuolingoExtraFields_shouldCallRepositorySave() {
    given(candidateExamRepository.findById(CANDIDATE_EXAM_ID))
        .willReturn(Optional.of(candidateExam));

    duolingoExtraFieldsService.createOrUpdateDuolingoExtraFields(request);

    verify(duolingoExtraFieldsRepository).save(any(DuolingoExtraFields.class));
  }

  private void verifyDuolingoExtraFields(DuolingoExtraFields duolingoExtraFields) {
    assertEquals(CERTIFICATE_URL, duolingoExtraFields.getCertificateUrl());
    assertEquals(INTERVIEW_URL, duolingoExtraFields.getInterviewUrl());
    assertEquals(VERIFICATION_DATE, duolingoExtraFields.getVerificationDate());
    assertEquals(PERCENT_SCORE, duolingoExtraFields.getPercentScore());
    assertEquals(SCALE, duolingoExtraFields.getScale());
    assertEquals(LITERACY_SUBSCORE, duolingoExtraFields.getLiteracySubscore());
    assertEquals(CONVERSATION_SUBSCORE, duolingoExtraFields.getConversationSubscore());
    assertEquals(COMPREHENSION_SUBSCORE, duolingoExtraFields.getComprehensionSubscore());
    assertEquals(PRODUCTION_SUBSCORE, duolingoExtraFields.getProductionSubscore());
    assertEquals(candidateExam, duolingoExtraFields.getCandidateExam());
  }
}
