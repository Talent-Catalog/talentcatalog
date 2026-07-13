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
import org.tctalent.server.model.db.OfferToAssist;
import org.tctalent.server.request.KeywordPagedSearchRequest;

@ExtendWith(MockitoExtension.class)
class OfferToAssistSpecificationTest {

  @Mock private Root<OfferToAssist> ota;
  @Mock private CriteriaQuery<?> query;
  @Mock private CriteriaBuilder cb;

  @Mock private Predicate conjunction;
  @Mock private Predicate combinedPredicate;

  @Mock private Path<Object> partnerPath;
  @Mock private Path<String> partnerNamePath;
  @Mock private Path<String> publicIdPath;
  @Mock private Path<String> reasonPath;

  @Mock private Expression<String> lowerPartnerNamePath;
  @Mock private Expression<String> lowerPublicIdPath;
  @Mock private Expression<String> lowerReasonPath;

  @Mock private Predicate partnerNameLikePredicate;
  @Mock private Predicate publicIdLikePredicate;
  @Mock private Predicate reasonLikePredicate;
  @Mock private Predicate keywordPredicate;

  @Test
  @DisplayName("should cover default constructor")
  void constructor_shouldCreateInstance() {
    assertNotNull(new OfferToAssistSpecification());
  }

  @Test
  @DisplayName("should throw when criteria query is null")
  void buildSearchQuery_shouldThrow_whenCriteriaQueryIsNull() {
    KeywordPagedSearchRequest request = new KeywordPagedSearchRequest();

    assertThrows(IllegalArgumentException.class,
        () -> OfferToAssistSpecification.buildSearchQuery(request)
            .toPredicate(ota, null, cb));
  }

  @Test
  @DisplayName("should build base query when keyword is null")
  void buildSearchQuery_shouldBuildBaseQuery_whenKeywordIsNull() {
    KeywordPagedSearchRequest request = new KeywordPagedSearchRequest();

    when(cb.conjunction()).thenReturn(conjunction);

    Predicate result = OfferToAssistSpecification.buildSearchQuery(request)
        .toPredicate(ota, query, cb);

    assertEquals(conjunction, result);

    verify(query).distinct(true);
    verify(cb).conjunction();
  }

  @Test
  @DisplayName("should build base query when keyword is blank")
  void buildSearchQuery_shouldBuildBaseQuery_whenKeywordIsBlank() {
    KeywordPagedSearchRequest request = new KeywordPagedSearchRequest();
    request.setKeyword("   ");

    when(cb.conjunction()).thenReturn(conjunction);

    Predicate result = OfferToAssistSpecification.buildSearchQuery(request)
        .toPredicate(ota, query, cb);

    assertEquals(conjunction, result);

    verify(query).distinct(true);
    verify(cb).conjunction();
  }

  @Test
  @DisplayName("should apply keyword filter")
  void buildSearchQuery_shouldApplyKeywordFilter() {
    KeywordPagedSearchRequest request = new KeywordPagedSearchRequest();
    request.setKeyword("Partner Reason");

    when(cb.conjunction()).thenReturn(conjunction);
    when(cb.and(any(Predicate.class), any(Predicate.class))).thenReturn(combinedPredicate);

    when(ota.<Object>get("partner")).thenReturn(partnerPath);
    when(partnerPath.<String>get("name")).thenReturn(partnerNamePath);
    when(ota.<String>get("publicId")).thenReturn(publicIdPath);
    when(ota.<String>get("reason")).thenReturn(reasonPath);

    when(cb.lower(partnerNamePath)).thenReturn(lowerPartnerNamePath);
    when(cb.lower(publicIdPath)).thenReturn(lowerPublicIdPath);
    when(cb.lower(reasonPath)).thenReturn(lowerReasonPath);

    when(cb.like(lowerPartnerNamePath, "%partner reason%"))
        .thenReturn(partnerNameLikePredicate);
    when(cb.like(lowerPublicIdPath, "%partner reason%"))
        .thenReturn(publicIdLikePredicate);
    when(cb.like(lowerReasonPath, "%partner reason%"))
        .thenReturn(reasonLikePredicate);

    when(cb.or(partnerNameLikePredicate, publicIdLikePredicate, reasonLikePredicate))
        .thenReturn(keywordPredicate);

    Predicate result = OfferToAssistSpecification.buildSearchQuery(request)
        .toPredicate(ota, query, cb);

    assertEquals(combinedPredicate, result);

    verify(query).distinct(true);
    verify(cb).like(lowerPartnerNamePath, "%partner reason%");
    verify(cb).like(lowerPublicIdPath, "%partner reason%");
    verify(cb).like(lowerReasonPath, "%partner reason%");
  }
}