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

package org.tctalent.server.util.text;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateJobExperience;

@ExtendWith(MockitoExtension.class)
class CandidateTidiedTextViewFactoryTest {

  @Mock
  private TextPartsTidiedTextService textPartsTidiedTextService;

  private CandidateTidiedTextViewFactory factory;

  @BeforeEach
  void setUp() {
    factory = new CandidateTidiedTextViewFactory(
        textPartsTidiedTextService
    );
  }

  @Test
  void createReturnsCandidateProxyWithTidiedExperienceDescriptions() {
    CandidateJobExperience firstExperience =
        new CandidateJobExperience();
    firstExperience.setCompanyName("First company");
    firstExperience.setRole("Developer");
    firstExperience.setDescription("encoded-description-one");

    CandidateJobExperience secondExperience =
        new CandidateJobExperience();
    secondExperience.setCompanyName("Second company");
    secondExperience.setRole("Designer");
    secondExperience.setDescription("encoded-description-two");

    Candidate candidate = new Candidate();
    candidate.setCandidateNumber("123");
    candidate.setCandidateJobExperiences(
        List.of(firstExperience, secondExperience)
    );

    when(textPartsTidiedTextService.getTidiedText(
        "encoded-description-one"
    )).thenReturn("Tidied first description");

    when(textPartsTidiedTextService.getTidiedText(
        "encoded-description-two"
    )).thenReturn("Tidied second description");

    Candidate result = factory.create(candidate);

    List<CandidateJobExperience> experiences =
        result.getCandidateJobExperiences();

    assertAll(
        () -> assertNotSame(candidate, result),
        () -> assertEquals("123", result.getCandidateNumber()),
        () -> assertEquals(2, experiences.size()),
        () -> assertNotSame(
            firstExperience,
            experiences.get(0)
        ),
        () -> assertNotSame(
            secondExperience,
            experiences.get(1)
        ),
        () -> assertEquals(
            "Tidied first description",
            experiences.get(0).getDescription()
        ),
        () -> assertEquals(
            "Tidied second description",
            experiences.get(1).getDescription()
        )
    );

    verify(textPartsTidiedTextService)
        .getTidiedText("encoded-description-one");
    verify(textPartsTidiedTextService)
        .getTidiedText("encoded-description-two");
  }

  @Test
  void createReturnsNullWhenCandidateExperiencesAreNull() {
    Candidate candidate = new Candidate();
    candidate.setCandidateJobExperiences(null);

    Candidate result = factory.create(candidate);

    assertNull(result.getCandidateJobExperiences());
    verifyNoInteractions(textPartsTidiedTextService);
  }

  @Test
  void createReturnsEmptyListWhenCandidateExperiencesAreEmpty() {
    Candidate candidate = new Candidate();
    candidate.setCandidateJobExperiences(List.of());

    Candidate result = factory.create(candidate);

    List<CandidateJobExperience> experiences =
        result.getCandidateJobExperiences();

    assertTrue(experiences.isEmpty());
    verifyNoInteractions(textPartsTidiedTextService);
  }

  @Test
  void candidateMethodsOtherThanExperienceGetterPassThrough() {
    Candidate candidate = new Candidate();
    candidate.setCandidateNumber("456");
    candidate.setPhone("0700000000");
    candidate.setCandidateJobExperiences(List.of());

    Candidate result = factory.create(candidate);

    assertAll(
        () -> assertEquals("456", result.getCandidateNumber()),
        () -> assertEquals("0700000000", result.getPhone())
    );

    verifyNoInteractions(textPartsTidiedTextService);
  }

  @Test
  void wrappedExperienceMethodsOtherThanDescriptionPassThrough() {
    CandidateJobExperience experience =
        new CandidateJobExperience();
    experience.setCompanyName("Example Company");
    experience.setRole("Software Engineer");
    experience.setDescription("encoded-description");

    Candidate candidate = new Candidate();
    candidate.setCandidateJobExperiences(List.of(experience));

    when(textPartsTidiedTextService.getTidiedText(
        "encoded-description"
    )).thenReturn("Tidied description");

    Candidate result = factory.create(candidate);

    CandidateJobExperience wrappedExperience =
        result.getCandidateJobExperiences().get(0);

    assertAll(
        () -> assertEquals(
            "Example Company",
            wrappedExperience.getCompanyName()
        ),
        () -> assertEquals(
            "Software Engineer",
            wrappedExperience.getRole()
        ),
        () -> assertEquals(
            "Tidied description",
            wrappedExperience.getDescription()
        )
    );

    verify(textPartsTidiedTextService)
        .getTidiedText("encoded-description");
  }

  @Test
  void wrappedExperiencePassesNullDescriptionToTidiedTextService() {
    CandidateJobExperience experience =
        new CandidateJobExperience();
    experience.setDescription(null);

    Candidate candidate = new Candidate();
    candidate.setCandidateJobExperiences(List.of(experience));

    when(textPartsTidiedTextService.getTidiedText(null))
        .thenReturn(null);

    Candidate result = factory.create(candidate);

    CandidateJobExperience wrappedExperience =
        result.getCandidateJobExperiences().get(0);

    assertNull(wrappedExperience.getDescription());

    verify(textPartsTidiedTextService).getTidiedText(null);
  }

  @Test
  void accessingNonDescriptionFieldDoesNotInvokeTidiedTextService() {
    CandidateJobExperience experience =
        new CandidateJobExperience();
    experience.setCompanyName("Example Company");
    experience.setDescription("encoded-description");

    Candidate candidate = new Candidate();
    candidate.setCandidateJobExperiences(List.of(experience));

    Candidate result = factory.create(candidate);

    CandidateJobExperience wrappedExperience =
        result.getCandidateJobExperiences().get(0);

    assertSame(
        "Example Company",
        wrappedExperience.getCompanyName()
    );

    verify(
        textPartsTidiedTextService,
        never()
    ).getTidiedText("encoded-description");
  }
}