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


package org.tctalent.server.service.db.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.tctalent.server.exception.CvGenerationException;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.service.db.impl.TcInstanceService;
import org.tctalent.server.util.text.CandidateTidiedTextViewFactory;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@ExtendWith(MockitoExtension.class)
class CvTemplateHelperTest {

  @Mock
  private TemplateEngine cvTemplateEngine;

  @Mock
  private TcInstanceService tcInstanceService;

  @Mock
  private CandidateTidiedTextViewFactory candidateTidiedTextViewFactory;

  @Mock
  private CvExportDataPreparer cvExportDataPreparer;

  private CvTemplateHelper helper;

  @BeforeEach
  void setUp() {
    helper = new CvTemplateHelper(cvTemplateEngine, tcInstanceService,
        candidateTidiedTextViewFactory, cvExportDataPreparer);
  }

  @Test
  void renderCvXhtmlPreparesCandidateSetsTemplateVariablesAndRemovesNullBytes() {
    Candidate originalCandidate = new Candidate();
    Candidate preparedCandidate = new Candidate();
    Candidate candidateView = new Candidate();

    when(cvExportDataPreparer.prepare(originalCandidate, true)).thenReturn(preparedCandidate);
    when(candidateTidiedTextViewFactory.create(preparedCandidate)).thenReturn(candidateView);
    when(tcInstanceService.getLogoFile()).thenReturn("tbblogo.png");

    when(cvTemplateEngine.process(eq("cvTemplate"), any(Context.class))).thenReturn(
        "<html><body><p>Hello CV\u0000</p></body></html>");

    String result = helper.renderCvXhtml(originalCandidate, true, true);

    assertNotNull(result);
    assertFalse(result.contains("\u0000"));
    assertTrueContains(result, "Hello CV");

    verify(cvExportDataPreparer).prepare(originalCandidate, true);
    verify(candidateTidiedTextViewFactory).create(preparedCandidate);
    verify(tcInstanceService).getLogoFile();

    ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);

    verify(cvTemplateEngine).process(eq("cvTemplate"), contextCaptor.capture());

    Context context = contextCaptor.getValue();

    assertSame(candidateView, context.getVariable("candidate"));
    assertEquals(true, context.getVariable("showName"));
    assertEquals(true, context.getVariable("showContact"));
    assertEquals("tbblogo.png", context.getVariable("logoFile"));
  }

  @Test
  void renderCvXhtmlPassesFalseShowContactToPreparerAndTemplateContext() {
    Candidate originalCandidate = new Candidate();
    Candidate preparedCandidate = new Candidate();
    Candidate candidateView = new Candidate();

    when(cvExportDataPreparer.prepare(originalCandidate, false)).thenReturn(preparedCandidate);
    when(candidateTidiedTextViewFactory.create(preparedCandidate)).thenReturn(candidateView);
    when(tcInstanceService.getLogoFile()).thenReturn("grnlogo.png");

    when(cvTemplateEngine.process(eq("cvTemplate"), any(Context.class))).thenReturn(
        "<html><body><p>No contact CV</p></body></html>");

    String result = helper.renderCvXhtml(originalCandidate, false, false);

    assertTrueContains(result, "No contact CV");

    verify(cvExportDataPreparer).prepare(originalCandidate, false);

    ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);

    verify(cvTemplateEngine).process(eq("cvTemplate"), contextCaptor.capture());

    Context context = contextCaptor.getValue();

    assertSame(candidateView, context.getVariable("candidate"));
    assertEquals(false, context.getVariable("showName"));
    assertEquals(false, context.getVariable("showContact"));
    assertEquals("grnlogo.png", context.getVariable("logoFile"));
  }

  @Test
  void renderCvXhtmlWrapsPreparationFailureInCvGenerationException() {
    Candidate candidate = new Candidate();

    when(cvExportDataPreparer.prepare(candidate, true)).thenThrow(
        new RuntimeException("prepare failed"));

    CvGenerationException exception = assertThrows(CvGenerationException.class,
        () -> helper.renderCvXhtml(candidate, true, true));

    assertEquals("prepare failed", exception.getMessage());
  }

  @Test
  void renderCvXhtmlWrapsTemplateFailureInCvGenerationException() {
    Candidate candidate = new Candidate();
    Candidate preparedCandidate = new Candidate();
    Candidate candidateView = new Candidate();

    when(cvExportDataPreparer.prepare(candidate, true)).thenReturn(preparedCandidate);
    when(candidateTidiedTextViewFactory.create(preparedCandidate)).thenReturn(candidateView);
    when(tcInstanceService.getLogoFile()).thenReturn("tbblogo.png");

    when(cvTemplateEngine.process(eq("cvTemplate"), any(Context.class))).thenThrow(
        new RuntimeException("template failed"));

    CvGenerationException exception = assertThrows(CvGenerationException.class,
        () -> helper.renderCvXhtml(candidate, true, true));

    assertEquals("template failed", exception.getMessage());
  }

  private void assertTrueContains(String actual, String expectedPart) {
    org.junit.jupiter.api.Assertions.assertTrue(actual.contains(expectedPart),
        "Expected text to contain: " + expectedPart + "\nActual text:\n" + actual);
  }
}