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

package org.tctalent.server.repository.db;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.tctalent.server.model.db.PartnerImpl;
import org.tctalent.server.model.db.Status;
import org.tctalent.server.request.partner.SearchPartnerRequest;

@ExtendWith(MockitoExtension.class)
class PartnerSpecificationTest {

  @Mock private Root<PartnerImpl> partner;
  @Mock private CriteriaQuery<?> query;
  @Mock private CriteriaBuilder cb;

  @Mock private Predicate conjunction;
  @Mock private Predicate combinedPredicate;

  @Mock private Path<String> namePath;
  @Mock private Path<String> abbreviationPath;
  @Mock private Expression<String> lowerNamePath;
  @Mock private Expression<String> lowerAbbreviationPath;
  @Mock private Predicate nameLikePredicate;
  @Mock private Predicate abbreviationLikePredicate;
  @Mock private Predicate keywordPredicate;

  @Mock private Path<Status> statusPath;
  @Mock private Predicate statusPredicate;

  @Mock private Path<Boolean> jobCreatorPath;
  @Mock private Predicate jobCreatorPredicate;

  @Mock private Path<Boolean> sourcePartnerPath;
  @Mock private Predicate sourcePartnerPredicate;

  @Test
  @DisplayName("should cover default constructor")
  void constructor_shouldCreateInstance() {
    assertNotNull(new PartnerSpecification());
  }

  @Test
  @DisplayName("should throw when criteria query is null")
  void buildSearchQuery_shouldThrow_whenCriteriaQueryIsNull() {
    SearchPartnerRequest request = new SearchPartnerRequest();

    assertThrows(IllegalArgumentException.class,
        () -> PartnerSpecification.buildSearchQuery(request)
            .toPredicate(partner, null, cb));
  }

  @Test
  @DisplayName("should build query with no optional filters")
  void buildSearchQuery_shouldBuildQueryWithNoOptionalFilters() {
    SearchPartnerRequest request = new SearchPartnerRequest();

    when(cb.conjunction()).thenReturn(conjunction);

    Predicate result = PartnerSpecification.buildSearchQuery(request)
        .toPredicate(partner, query, cb);

    assertEquals(conjunction, result);

    verify(query).distinct(true);
    verify(cb).conjunction();
  }

  @Test
  @DisplayName("should apply keyword, status, job creator and source partner filters")
  void buildSearchQuery_shouldApplyAllFilters() {
    SearchPartnerRequest request = new SearchPartnerRequest();
    request.setKeyword("Talent Partner");
    request.setStatus(Status.active);
    request.setJobCreator(true);
    request.setSourcePartner(true);

    stubBaseAnd();

    when(partner.<String>get("name")).thenReturn(namePath);
    when(partner.<String>get("abbreviation")).thenReturn(abbreviationPath);
    when(cb.lower(namePath)).thenReturn(lowerNamePath);
    when(cb.lower(abbreviationPath)).thenReturn(lowerAbbreviationPath);
    when(cb.like(lowerNamePath, "%talent partner%")).thenReturn(nameLikePredicate);
    when(cb.like(lowerAbbreviationPath, "%talent partner%"))
        .thenReturn(abbreviationLikePredicate);
    when(cb.or(nameLikePredicate, abbreviationLikePredicate)).thenReturn(keywordPredicate);

    when(partner.<Status>get("status")).thenReturn(statusPath);
    when(cb.equal(statusPath, Status.active)).thenReturn(statusPredicate);

    when(partner.<Boolean>get("jobCreator")).thenReturn(jobCreatorPath);
    when(cb.equal(jobCreatorPath, true)).thenReturn(jobCreatorPredicate);

    when(partner.<Boolean>get("sourcePartner")).thenReturn(sourcePartnerPath);
    when(cb.equal(sourcePartnerPath, true)).thenReturn(sourcePartnerPredicate);

    Predicate result = PartnerSpecification.buildSearchQuery(request)
        .toPredicate(partner, query, cb);

    assertNotNull(result);

    verify(query).distinct(true);
    verify(cb).like(lowerNamePath, "%talent partner%");
    verify(cb).like(lowerAbbreviationPath, "%talent partner%");
    verify(cb).equal(statusPath, Status.active);
    verify(cb).equal(jobCreatorPath, true);
    verify(cb).equal(sourcePartnerPath, true);
  }

  @Test
  @DisplayName("should skip blank keyword and apply false boolean filters")
  void buildSearchQuery_shouldSkipBlankKeywordAndApplyFalseBooleanFilters() {
    SearchPartnerRequest request = new SearchPartnerRequest();
    request.setKeyword("   ");
    request.setJobCreator(false);
    request.setSourcePartner(false);

    stubBaseAnd();

    when(partner.<Boolean>get("jobCreator")).thenReturn(jobCreatorPath);
    when(cb.equal(jobCreatorPath, false)).thenReturn(jobCreatorPredicate);

    when(partner.<Boolean>get("sourcePartner")).thenReturn(sourcePartnerPath);
    when(cb.equal(sourcePartnerPath, false)).thenReturn(sourcePartnerPredicate);

    Predicate result = PartnerSpecification.buildSearchQuery(request)
        .toPredicate(partner, query, cb);

    assertNotNull(result);

    verify(query).distinct(true);
    verify(cb).equal(jobCreatorPath, false);
    verify(cb).equal(sourcePartnerPath, false);
  }

  @Test
  @DisplayName("should apply status filter only")
  void buildSearchQuery_shouldApplyStatusFilterOnly() {
    SearchPartnerRequest request = new SearchPartnerRequest();
    request.setStatus(Status.active);

    stubBaseAnd();

    when(partner.<Status>get("status")).thenReturn(statusPath);
    when(cb.equal(statusPath, Status.active)).thenReturn(statusPredicate);

    Predicate result = PartnerSpecification.buildSearchQuery(request)
        .toPredicate(partner, query, cb);

    assertNotNull(result);

    verify(query).distinct(true);
    verify(cb).equal(statusPath, Status.active);
  }

  private void stubBaseAnd() {
    when(cb.conjunction()).thenReturn(conjunction);
    when(cb.and(any(Predicate.class), any(Predicate.class))).thenReturn(combinedPredicate);
  }
}