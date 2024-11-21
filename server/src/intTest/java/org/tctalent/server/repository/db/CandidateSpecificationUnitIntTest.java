/*
 * Copyright (c) 2024 Talent Beyond Boundaries.
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

package org.tctalent.server.repository.db;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import org.junit.Before;
import org.junit.Test;
import org.springframework.data.domain.Sort;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.request.PagedSearchRequest;

public class CandidateSpecificationUnitIntTest {

  private Root<Candidate> candidate;
  private PagedSearchRequest request;
  private CriteriaBuilder builder;
  private Join<Object, Object> user;
  private Join<Object, Object> partner;
  private Join<Object, Object> nationality;
  private Join<Object, Object> country;
  private Join<Object, Object> educationLevel;

  @SuppressWarnings("unchecked")
  @Before
  public void setup() {
    request = mock(PagedSearchRequest.class);
    candidate = mock(Root.class);
    builder = mock(CriteriaBuilder.class);
    user = mock(Join.class);
    partner = mock(Join.class);
    nationality = mock(Join.class);
    country = mock(Join.class);
    educationLevel = mock(Join.class);
  }

  @Test
  public void sortsByUserPartnerInAscendingOrder() {
    Path<String> pathAsc = mock(Path.class);
    Path<String> pathId = mock(Path.class);
    Order ascOrder = mock(Order.class);
    Order descOrder = mock(Order.class);

    when(request.getSortFields()).thenReturn(new String[]{"user.partner.name"});
    when(request.getSortDirection()).thenReturn(Sort.Direction.ASC);
    when(partner.get("name")).thenReturn((Path) pathAsc);
    when(builder.asc(pathAsc)).thenReturn(ascOrder);
    when(candidate.get("id")).thenReturn((Path) pathId);
    when(builder.asc(any(Path.class))).thenReturn(ascOrder);
    when(builder.desc(any(Path.class))).thenReturn(descOrder);

    List<Order> orders = CandidateSpecificationUtil.getOrderByOrders(
        request,
        candidate,
        builder,
        user,
        partner,
        nationality,
        country,
        educationLevel
    );

    verify(builder).asc(any());
    verify(partner).get("name");
    verify(request).getSortFields();
    verify(request).getSortDirection();
    verify(candidate).get("id");

    assertEquals(2, orders.size());
  }

  @Test
  public void sortsByUserEmailOrderDesc() {
    Path<String> pathAsc = mock(Path.class);
    Path<String> pathId = mock(Path.class);
    Order ascOrder = mock(Order.class);
    Order descOrder = mock(Order.class);

    when(request.getSortFields()).thenReturn(new String[]{"user.email"});
    when(request.getSortDirection()).thenReturn(Sort.Direction.DESC);
    when(user.get("email")).thenReturn((Path) pathAsc);
    when(builder.asc(pathAsc)).thenReturn(ascOrder);
    when(candidate.get("id")).thenReturn((Path) pathId);
    when(builder.asc(any())).thenReturn(ascOrder);
    when(builder.desc(any())).thenReturn(descOrder);

    List<Order> orders = CandidateSpecificationUtil.getOrderByOrders(
        request,
        candidate,
        builder,
        user,
        partner,
        nationality,
        country,
        educationLevel
    );

    verify(user).get("email");
    verify(request).getSortFields();
    verify(request).getSortDirection();
    verify(candidate).get("id");

    assertEquals(2, orders.size());
  }

  @Test
  public void sortsByUserInAscendingOrder() {
    Path<String> pathAsc = mock(Path.class);
    Path<String> pathId = mock(Path.class);
    Order ascOrder = mock(Order.class);
    Order descOrder = mock(Order.class);

    when(request.getSortFields()).thenReturn(new String[]{"user.name"});
    when(request.getSortDirection()).thenReturn(Sort.Direction.ASC);
    when(user.get("name")).thenReturn((Path) pathAsc);
    when(builder.asc(pathAsc)).thenReturn(ascOrder);
    when(candidate.get("id")).thenReturn((Path) pathId);
    when(builder.asc(any())).thenReturn(ascOrder);
    when(builder.desc(any())).thenReturn(descOrder);

    List<Order> orders = CandidateSpecificationUtil.getOrderByOrders(
        request,
        candidate,
        builder,
        user,
        partner,
        nationality,
        country,
        educationLevel
    );

    verify(builder).asc(any());
    verify(user).get("name");
    verify(request).getSortFields();
    verify(request).getSortDirection();
    verify(candidate).get("id");

    assertEquals(2, orders.size());
  }

  @Test
  public void sortsByUserNationalityPropertyInAscendingOrder() {
    Path<String> pathAsc = mock(Path.class);
    Path<String> pathId = mock(Path.class);
    Order ascOrder = mock(Order.class);
    Order descOrder = mock(Order.class);

    when(request.getSortFields()).thenReturn(new String[]{"nationality.name"});
    when(request.getSortDirection()).thenReturn(Sort.Direction.ASC);
    when(nationality.get("name")).thenReturn((Path) pathAsc);
    when(builder.asc(pathAsc)).thenReturn(ascOrder);
    when(candidate.get("id")).thenReturn((Path) pathId);
    when(builder.asc(any())).thenReturn(ascOrder);
    when(builder.desc(any())).thenReturn(descOrder);

    List<Order> orders = CandidateSpecificationUtil.getOrderByOrders(
        request,
        candidate,
        builder,
        user,
        partner,
        nationality,
        country,
        educationLevel
    );

    verify(builder).asc(any());
    verify(nationality).get("name");
    verify(request).getSortFields();
    verify(request).getSortDirection();
    verify(candidate).get("id");

    assertEquals(2, orders.size());
  }

  @Test
  public void sortsByCountryInAscendingOrder() {
    Path<String> pathAsc = mock(Path.class);
    Path<String> pathId = mock(Path.class);
    Order ascOrder = mock(Order.class);
    Order descOrder = mock(Order.class);

    when(request.getSortFields()).thenReturn(new String[]{"country.name"});
    when(request.getSortDirection()).thenReturn(Sort.Direction.ASC);
    when(country.get("name")).thenReturn((Path) pathAsc);
    when(builder.asc(pathAsc)).thenReturn(ascOrder);
    when(candidate.get("id")).thenReturn((Path) pathId);
    when(builder.asc(any())).thenReturn(ascOrder);
    when(builder.desc(any())).thenReturn(descOrder);

    List<Order> orders = CandidateSpecificationUtil.getOrderByOrders(
        request,
        candidate,
        builder,
        user,
        partner,
        nationality,
        country,
        educationLevel
    );

    verify(builder).asc(any());
    verify(country).get("name");
    verify(request).getSortFields();
    verify(request).getSortDirection();
    verify(candidate).get("id");

    assertEquals(2, orders.size());
  }

  @Test
  public void sortsByUserEducationLevelInAscendingOrder() {
    Path<String> pathAsc = mock(Path.class);
    Path<String> pathId = mock(Path.class);
    Order ascOrder = mock(Order.class);
    Order descOrder = mock(Order.class);

    when(request.getSortFields()).thenReturn(new String[]{"maxEducationLevel.name"});
    when(request.getSortDirection()).thenReturn(Sort.Direction.ASC);
    when(educationLevel.get("name")).thenReturn((Path) pathAsc);
    when(builder.asc(pathAsc)).thenReturn(ascOrder);
    when(candidate.get("id")).thenReturn((Path) pathId);
    when(builder.asc(any())).thenReturn(ascOrder);
    when(builder.desc(any())).thenReturn(descOrder);

    List<Order> orders = CandidateSpecificationUtil.getOrderByOrders(
        request,
        candidate,
        builder,
        user,
        partner,
        nationality,
        country,
        educationLevel
    );

    verify(builder).asc(any());
    verify(educationLevel).get("name");
    verify(request).getSortFields();
    verify(request).getSortDirection();
    verify(candidate).get("id");

    assertEquals(2, orders.size());
  }

  @Test
  public void sortsByUserIdInAscendingOrder() {
    Path<String> pathId = mock(Path.class);
    Order ascOrder = mock(Order.class);

    when(request.getSortFields()).thenReturn(new String[]{"id"});
    when(request.getSortDirection()).thenReturn(Sort.Direction.ASC);

    when(candidate.get("id")).thenReturn((Path) pathId);
    when(builder.asc(any())).thenReturn(ascOrder);

    List<Order> orders = CandidateSpecificationUtil.getOrderByOrders(
        request,
        candidate,
        builder,
        user,
        partner,
        nationality,
        country,
        educationLevel
    );

    verify(builder).asc(any());
    verify(request).getSortFields();
    verify(request).getSortDirection();
    verify(candidate).get("id");

    assertEquals(1, orders.size());
  }
}
