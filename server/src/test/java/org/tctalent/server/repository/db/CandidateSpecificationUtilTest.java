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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.request.PagedSearchRequest;

@ExtendWith(MockitoExtension.class)
class CandidateSpecificationUtilTest {

  @Mock private Root<Candidate> candidate;
  @Mock private CriteriaBuilder cb;

  @Mock private Join<Object, Object> user;
  @Mock private Join<Object, Object> partner;
  @Mock private Join<Object, Object> nationality;
  @Mock private Join<Object, Object> country;
  @Mock private Join<Object, Object> educationLevel;

  @Mock private Path<Object> candidateIdPath;
  @Mock private Path<Object> candidateNamePath;
  @Mock private Path<Object> partnerNamePath;
  @Mock private Path<Object> userFirstNamePath;
  @Mock private Path<Object> nationalityNamePath;
  @Mock private Path<Object> countryNamePath;
  @Mock private Path<Object> educationLevelNamePath;

  @Mock private Order candidateIdDescOrder;
  @Mock private Order candidateNameAscOrder;
  @Mock private Order partnerNameAscOrder;
  @Mock private Order userFirstNameAscOrder;
  @Mock private Order nationalityNameAscOrder;
  @Mock private Order countryNameAscOrder;
  @Mock private Order educationLevelNameAscOrder;
  @Mock private Order candidateNameDescOrder;

  @Test
  @DisplayName("should cover default constructor")
  void constructor_shouldCreateInstance() {
    assertNotNull(new CandidateSpecificationUtil());
  }

  @Test
  @DisplayName("should add stable id sort when sort fields are null")
  void getOrderByOrders_shouldAddStableIdSort_whenSortFieldsAreNull() {
    PagedSearchRequest request = new PagedSearchRequest();

    when(candidate.<Object>get("id")).thenReturn(candidateIdPath);
    when(cb.desc(candidateIdPath)).thenReturn(candidateIdDescOrder);

    List<Order> result = CandidateSpecificationUtil.getOrderByOrders(
        request,
        candidate,
        cb,
        user,
        partner,
        nationality,
        country,
        educationLevel
    );

    assertEquals(List.of(candidateIdDescOrder), result);

    verify(candidate).get("id");
    verify(cb).desc(candidateIdPath);
  }

  @Test
  @DisplayName("should sort candidate direct field ascending and add stable id sort")
  void getOrderByOrders_shouldSortCandidateDirectFieldAscendingAndAddStableIdSort() {
    PagedSearchRequest request = new PagedSearchRequest(
        Sort.Direction.ASC,
        new String[] {"name"}
    );

    when(candidate.<Object>get("name")).thenReturn(candidateNamePath);
    when(cb.asc(candidateNamePath)).thenReturn(candidateNameAscOrder);

    when(candidate.<Object>get("id")).thenReturn(candidateIdPath);
    when(cb.desc(candidateIdPath)).thenReturn(candidateIdDescOrder);

    List<Order> result = CandidateSpecificationUtil.getOrderByOrders(
        request,
        candidate,
        cb,
        user,
        partner,
        nationality,
        country,
        educationLevel
    );

    assertEquals(List.of(candidateNameAscOrder, candidateIdDescOrder), result);

    verify(candidate).get("name");
    verify(cb).asc(candidateNamePath);
    verify(candidate).get("id");
    verify(cb).desc(candidateIdPath);
  }

  @Test
  @DisplayName("should sort candidate direct field descending and add stable id sort")
  void getOrderByOrders_shouldSortCandidateDirectFieldDescendingAndAddStableIdSort() {
    PagedSearchRequest request = new PagedSearchRequest(
        Sort.Direction.DESC,
        new String[] {"name"}
    );

    when(candidate.<Object>get("name")).thenReturn(candidateNamePath);
    when(cb.desc(candidateNamePath)).thenReturn(candidateNameDescOrder);

    when(candidate.<Object>get("id")).thenReturn(candidateIdPath);
    when(cb.desc(candidateIdPath)).thenReturn(candidateIdDescOrder);

    List<Order> result = CandidateSpecificationUtil.getOrderByOrders(
        request,
        candidate,
        cb,
        user,
        partner,
        nationality,
        country,
        educationLevel
    );

    assertEquals(List.of(candidateNameDescOrder, candidateIdDescOrder), result);

    verify(candidate).get("name");
    verify(cb).desc(candidateNamePath);
    verify(candidate).get("id");
    verify(cb).desc(candidateIdPath);
  }

  @Test
  @DisplayName("should sort by id without adding extra stable id sort")
  void getOrderByOrders_shouldSortByIdWithoutAddingExtraStableIdSort() {
    PagedSearchRequest request = new PagedSearchRequest(
        Sort.Direction.DESC,
        new String[] {"id"}
    );

    when(candidate.<Object>get("id")).thenReturn(candidateIdPath);
    when(cb.desc(candidateIdPath)).thenReturn(candidateIdDescOrder);

    List<Order> result = CandidateSpecificationUtil.getOrderByOrders(
        request,
        candidate,
        cb,
        user,
        partner,
        nationality,
        country,
        educationLevel
    );

    assertEquals(List.of(candidateIdDescOrder), result);

    verify(candidate).get("id");
    verify(cb).desc(candidateIdPath);
  }

  @Test
  @DisplayName("should sort using partner join for user partner field")
  void getOrderByOrders_shouldSortUsingPartnerJoin() {
    PagedSearchRequest request = new PagedSearchRequest(
        Sort.Direction.ASC,
        new String[] {"user.partner.name"}
    );

    when(partner.get("name")).thenReturn(partnerNamePath);
    when(cb.asc(partnerNamePath)).thenReturn(partnerNameAscOrder);

    when(candidate.<Object>get("id")).thenReturn(candidateIdPath);
    when(cb.desc(candidateIdPath)).thenReturn(candidateIdDescOrder);

    List<Order> result = CandidateSpecificationUtil.getOrderByOrders(
        request,
        candidate,
        cb,
        user,
        partner,
        nationality,
        country,
        educationLevel
    );

    assertEquals(List.of(partnerNameAscOrder, candidateIdDescOrder), result);

    verify(partner).get("name");
    verify(cb).asc(partnerNamePath);
  }

  @Test
  @DisplayName("should sort using user join for user field")
  void getOrderByOrders_shouldSortUsingUserJoin() {
    PagedSearchRequest request = new PagedSearchRequest(
        Sort.Direction.ASC,
        new String[] {"user.firstName"}
    );

    when(user.get("firstName")).thenReturn(userFirstNamePath);
    when(cb.asc(userFirstNamePath)).thenReturn(userFirstNameAscOrder);

    when(candidate.<Object>get("id")).thenReturn(candidateIdPath);
    when(cb.desc(candidateIdPath)).thenReturn(candidateIdDescOrder);

    List<Order> result = CandidateSpecificationUtil.getOrderByOrders(
        request,
        candidate,
        cb,
        user,
        partner,
        nationality,
        country,
        educationLevel
    );

    assertEquals(List.of(userFirstNameAscOrder, candidateIdDescOrder), result);

    verify(user).get("firstName");
    verify(cb).asc(userFirstNamePath);
  }

  @Test
  @DisplayName("should sort using nationality join")
  void getOrderByOrders_shouldSortUsingNationalityJoin() {
    PagedSearchRequest request = new PagedSearchRequest(
        Sort.Direction.ASC,
        new String[] {"nationality.name"}
    );

    when(nationality.get("name")).thenReturn(nationalityNamePath);
    when(cb.asc(nationalityNamePath)).thenReturn(nationalityNameAscOrder);

    when(candidate.<Object>get("id")).thenReturn(candidateIdPath);
    when(cb.desc(candidateIdPath)).thenReturn(candidateIdDescOrder);

    List<Order> result = CandidateSpecificationUtil.getOrderByOrders(
        request,
        candidate,
        cb,
        user,
        partner,
        nationality,
        country,
        educationLevel
    );

    assertEquals(List.of(nationalityNameAscOrder, candidateIdDescOrder), result);

    verify(nationality).get("name");
    verify(cb).asc(nationalityNamePath);
  }

  @Test
  @DisplayName("should sort using country join")
  void getOrderByOrders_shouldSortUsingCountryJoin() {
    PagedSearchRequest request = new PagedSearchRequest(
        Sort.Direction.ASC,
        new String[] {"country.name"}
    );

    when(country.get("name")).thenReturn(countryNamePath);
    when(cb.asc(countryNamePath)).thenReturn(countryNameAscOrder);

    when(candidate.<Object>get("id")).thenReturn(candidateIdPath);
    when(cb.desc(candidateIdPath)).thenReturn(candidateIdDescOrder);

    List<Order> result = CandidateSpecificationUtil.getOrderByOrders(
        request,
        candidate,
        cb,
        user,
        partner,
        nationality,
        country,
        educationLevel
    );

    assertEquals(List.of(countryNameAscOrder, candidateIdDescOrder), result);

    verify(country).get("name");
    verify(cb).asc(countryNamePath);
  }

  @Test
  @DisplayName("should sort using max education level join")
  void getOrderByOrders_shouldSortUsingMaxEducationLevelJoin() {
    PagedSearchRequest request = new PagedSearchRequest(
        Sort.Direction.ASC,
        new String[] {"maxEducationLevel.name"}
    );

    when(educationLevel.get("name")).thenReturn(educationLevelNamePath);
    when(cb.asc(educationLevelNamePath)).thenReturn(educationLevelNameAscOrder);

    when(candidate.<Object>get("id")).thenReturn(candidateIdPath);
    when(cb.desc(candidateIdPath)).thenReturn(candidateIdDescOrder);

    List<Order> result = CandidateSpecificationUtil.getOrderByOrders(
        request,
        candidate,
        cb,
        user,
        partner,
        nationality,
        country,
        educationLevel
    );

    assertEquals(List.of(educationLevelNameAscOrder, candidateIdDescOrder), result);

    verify(educationLevel).get("name");
    verify(cb).asc(educationLevelNamePath);
  }

  @Test
  @DisplayName("should sort multiple fields and add stable id sort once")
  void getOrderByOrders_shouldSortMultipleFieldsAndAddStableIdSortOnce() {
    PagedSearchRequest request = new PagedSearchRequest(
        Sort.Direction.ASC,
        new String[] {
            "user.partner.name",
            "user.firstName",
            "nationality.name",
            "country.name",
            "maxEducationLevel.name",
            "name"
        }
    );

    when(partner.get("name")).thenReturn(partnerNamePath);
    when(cb.asc(partnerNamePath)).thenReturn(partnerNameAscOrder);

    when(user.get("firstName")).thenReturn(userFirstNamePath);
    when(cb.asc(userFirstNamePath)).thenReturn(userFirstNameAscOrder);

    when(nationality.get("name")).thenReturn(nationalityNamePath);
    when(cb.asc(nationalityNamePath)).thenReturn(nationalityNameAscOrder);

    when(country.get("name")).thenReturn(countryNamePath);
    when(cb.asc(countryNamePath)).thenReturn(countryNameAscOrder);

    when(educationLevel.get("name")).thenReturn(educationLevelNamePath);
    when(cb.asc(educationLevelNamePath)).thenReturn(educationLevelNameAscOrder);

    when(candidate.<Object>get("name")).thenReturn(candidateNamePath);
    when(cb.asc(candidateNamePath)).thenReturn(candidateNameAscOrder);

    when(candidate.<Object>get("id")).thenReturn(candidateIdPath);
    when(cb.desc(candidateIdPath)).thenReturn(candidateIdDescOrder);

    List<Order> result = CandidateSpecificationUtil.getOrderByOrders(
        request,
        candidate,
        cb,
        user,
        partner,
        nationality,
        country,
        educationLevel
    );

    assertEquals(
        List.of(
            partnerNameAscOrder,
            userFirstNameAscOrder,
            nationalityNameAscOrder,
            countryNameAscOrder,
            educationLevelNameAscOrder,
            candidateNameAscOrder,
            candidateIdDescOrder
        ),
        result
    );
  }
}