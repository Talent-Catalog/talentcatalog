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
import org.tctalent.server.model.db.HelpLink;
import org.tctalent.server.request.helplink.SearchHelpLinkRequest;

@ExtendWith(MockitoExtension.class)
class HelpLinkSettingsSpecificationTest {

  @Mock private Root<HelpLink> helpLink;
  @Mock private CriteriaQuery<?> query;
  @Mock private CriteriaBuilder cb;

  @Mock private Predicate conjunction;
  @Mock private Predicate combinedPredicate;

  @Mock private Path<String> labelPath;
  @Mock private Path<String> linkPath;
  @Mock private Expression<String> lowerLabelPath;
  @Mock private Expression<String> lowerLinkPath;
  @Mock private Predicate labelLikePredicate;
  @Mock private Predicate linkLikePredicate;
  @Mock private Predicate keywordPredicate;

  @Mock private Path<Object> countryPath;
  @Mock private Path<Long> countryIdPath;
  @Mock private Predicate countryPredicate;

  @Test
  @DisplayName("should cover default constructor")
  void constructor_shouldCreateInstance() {
    assertNotNull(new HelpLinkSettingsSpecification());
  }

  @Test
  @DisplayName("should throw when criteria query is null")
  void buildSearchQuery_shouldThrow_whenCriteriaQueryIsNull() {
    SearchHelpLinkRequest request = new SearchHelpLinkRequest();

    assertThrows(IllegalArgumentException.class,
        () -> HelpLinkSettingsSpecification.buildSearchQuery(request)
            .toPredicate(helpLink, null, cb));
  }

  @Test
  @DisplayName("should build base query when keyword and country are null")
  void buildSearchQuery_shouldBuildBaseQuery_whenKeywordAndCountryAreNull() {
    SearchHelpLinkRequest request = new SearchHelpLinkRequest();

    when(cb.conjunction()).thenReturn(conjunction);

    Predicate result = HelpLinkSettingsSpecification.buildSearchQuery(request)
        .toPredicate(helpLink, query, cb);

    assertEquals(conjunction, result);

    verify(query).distinct(true);
    verify(cb).conjunction();
  }

  @Test
  @DisplayName("should build base query when keyword is blank")
  void buildSearchQuery_shouldBuildBaseQuery_whenKeywordIsBlank() {
    SearchHelpLinkRequest request = new SearchHelpLinkRequest();
    request.setKeyword("   ");

    when(cb.conjunction()).thenReturn(conjunction);

    Predicate result = HelpLinkSettingsSpecification.buildSearchQuery(request)
        .toPredicate(helpLink, query, cb);

    assertEquals(conjunction, result);

    verify(query).distinct(true);
    verify(cb).conjunction();
  }

  @Test
  @DisplayName("should apply keyword filter")
  void buildSearchQuery_shouldApplyKeywordFilter() {
    SearchHelpLinkRequest request = new SearchHelpLinkRequest();
    request.setKeyword("Visa Help");

    stubBaseAnd();
    stubKeyword("visa help");

    Predicate result = HelpLinkSettingsSpecification.buildSearchQuery(request)
        .toPredicate(helpLink, query, cb);

    assertEquals(combinedPredicate, result);

    verify(query).distinct(true);
    verify(cb).like(lowerLabelPath, "%visa help%");
    verify(cb).like(lowerLinkPath, "%visa help%");
  }

  @Test
  @DisplayName("should apply country filter")
  void buildSearchQuery_shouldApplyCountryFilter() {
    SearchHelpLinkRequest request = new SearchHelpLinkRequest();
    request.setCountryId(123L);

    stubBaseAnd();
    stubCountry(123L);

    Predicate result = HelpLinkSettingsSpecification.buildSearchQuery(request)
        .toPredicate(helpLink, query, cb);

    assertEquals(combinedPredicate, result);

    verify(query).distinct(true);
    verify(cb).equal(countryIdPath, 123L);
  }

  @Test
  @DisplayName("should apply keyword and country filters")
  void buildSearchQuery_shouldApplyKeywordAndCountryFilters() {
    SearchHelpLinkRequest request = new SearchHelpLinkRequest();
    request.setKeyword("Next Step");
    request.setCountryId(456L);

    stubBaseAnd();
    stubKeyword("next step");
    stubCountry(456L);

    Predicate result = HelpLinkSettingsSpecification.buildSearchQuery(request)
        .toPredicate(helpLink, query, cb);

    assertEquals(combinedPredicate, result);

    verify(query).distinct(true);
    verify(cb).like(lowerLabelPath, "%next step%");
    verify(cb).like(lowerLinkPath, "%next step%");
    verify(cb).equal(countryIdPath, 456L);
  }

  private void stubBaseAnd() {
    when(cb.conjunction()).thenReturn(conjunction);
    when(cb.and(any(Predicate.class), any(Predicate.class))).thenReturn(combinedPredicate);
  }

  private void stubKeyword(String lowerCaseKeyword) {
    when(helpLink.<String>get("label")).thenReturn(labelPath);
    when(helpLink.<String>get("link")).thenReturn(linkPath);

    when(cb.lower(labelPath)).thenReturn(lowerLabelPath);
    when(cb.lower(linkPath)).thenReturn(lowerLinkPath);

    when(cb.like(lowerLabelPath, "%" + lowerCaseKeyword + "%"))
        .thenReturn(labelLikePredicate);
    when(cb.like(lowerLinkPath, "%" + lowerCaseKeyword + "%"))
        .thenReturn(linkLikePredicate);

    when(cb.or(labelLikePredicate, linkLikePredicate)).thenReturn(keywordPredicate);
  }

  private void stubCountry(Long countryId) {
    when(helpLink.<Object>get("country")).thenReturn(countryPath);
    when(countryPath.<Long>get("id")).thenReturn(countryIdPath);
    when(cb.equal(countryIdPath, countryId)).thenReturn(countryPredicate);
  }
}