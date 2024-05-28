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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

public class PredicateUtilTest {

  @Test
  public void testCreateOrPredicate() {
    CriteriaBuilder builder = mock(CriteriaBuilder.class);
    Predicate predicate1 = mock(Predicate.class);
    Predicate predicate2 = mock(Predicate.class);
    List<Predicate> predicates = Arrays.asList(predicate1, predicate2);
    when(builder.or(predicates.toArray(new Predicate[0]))).thenReturn(mock(Predicate.class));

    Predicate orPredicate = PredicateUtil.createOrPredicate(builder, predicates);

    assertNotNull(orPredicate);
    verify(builder).or(predicates.toArray(new Predicate[0]));
  }

  @Test
  public void testCreateAndPredicate() {
    CriteriaBuilder builder = mock(CriteriaBuilder.class);
    Predicate predicate1 = mock(Predicate.class);
    Predicate predicate2 = mock(Predicate.class);
    List<Predicate> predicates = List.of(predicate1, predicate2);
    when(builder.and(predicates.toArray(new Predicate[0]))).thenReturn(mock(Predicate.class));

    Predicate andPredicate = PredicateUtil.createAndPredicate(builder, predicates);

    assertNotNull(andPredicate);
    verify(builder).and(predicates.toArray(new Predicate[0]));
  }

  @Test
  public void testAddOrPredicates() {
    CriteriaBuilder builder = mock(CriteriaBuilder.class);
    Predicate predicate1 = mock(Predicate.class);
    Predicate predicate2 = mock(Predicate.class);
    List<Predicate> predicates = List.of(predicate1, predicate2);
    List<Predicate> orPredicates = new ArrayList<>();
    orPredicates.add(mock(Predicate.class));
    orPredicates.add(mock(Predicate.class));
    when(builder.or(predicates.toArray(new Predicate[0]))).thenReturn(mock(Predicate.class));

    List<Predicate> result = PredicateUtil.addOrPredicates(builder, predicates, orPredicates);

    assertEquals(2, result.size());
  }

  @Test
  public void testAddOrPredicatesWithNullOrPredicates() {
    CriteriaBuilder builder = mock(CriteriaBuilder.class);
    List<Predicate> predicates = new ArrayList<>();

    List<Predicate> result = PredicateUtil.addOrPredicates(builder, predicates, null);

    assertEquals(0, result.size());
  }

  @Test
  public void testAddOrPredicatesWithEmptyPredicates() {
    CriteriaBuilder builder = mock(CriteriaBuilder.class);
    List<Predicate> predicates = new ArrayList<>();
    List<Predicate> orPredicates = new ArrayList<>();

    List<Predicate> result = PredicateUtil.addOrPredicates(builder, predicates, orPredicates);

    assertEquals(0, result.size());
  }

  @Test
  void testCreateOrPredicate_withNullPredicates() {
    CriteriaBuilder builder = mock(CriteriaBuilder.class);

    Predicate result = PredicateUtil.createOrPredicate(builder, null);

    assertNull(result);
  }

  @Test
  void testCreateOrPredicate_withEmptyPredicates() {
    CriteriaBuilder builder = mock(CriteriaBuilder.class);

    Predicate result = PredicateUtil.createOrPredicate(builder, new ArrayList<>());

    assertNull(result);
  }

  @Test
  void testCreateOrPredicate_withSinglePredicate() {
    CriteriaBuilder builder = mock(CriteriaBuilder.class);
    Predicate predicate = mock(Predicate.class);
    List<Predicate> predicates = List.of(predicate);

    when(builder.or(predicates.toArray(new Predicate[0]))).thenReturn(predicate);

    Predicate result = PredicateUtil.createOrPredicate(builder, predicates);

    assertNotNull(result);
    assertEquals(predicate, result);
    verify(builder).or(predicate);
  }

  @Test
  void testCreateOrPredicate_withMultiplePredicates() {
    CriteriaBuilder builder = mock(CriteriaBuilder.class);
    Predicate predicate = mock(Predicate.class);
    Predicate predicate1 = mock(Predicate.class);
    Predicate predicate2 = mock(Predicate.class);
    List<Predicate> predicates = List.of(predicate1, predicate2);

    when(builder.or(predicates.toArray(new Predicate[0]))).thenReturn(predicate);

    Predicate result = PredicateUtil.createOrPredicate(builder, predicates);

    assertNotNull(result);
    verify(builder).or(predicates.toArray(new Predicate[0]));
  }

  @Test
  void testCreateAndPredicate_withNullPredicates() {
    CriteriaBuilder builder = mock(CriteriaBuilder.class);

    Predicate result = PredicateUtil.createAndPredicate(builder, null);

    assertNull(result);
  }

  @Test
  void testCreateAndPredicate_withEmptyPredicates() {
    CriteriaBuilder builder = mock(CriteriaBuilder.class);

    Predicate result = PredicateUtil.createAndPredicate(builder, new ArrayList<>());

    assertNull(result);
  }

  @Test
  void testCreateAndPredicate_withSinglePredicate() {
    CriteriaBuilder builder = mock(CriteriaBuilder.class);
    Predicate predicate = mock(Predicate.class);
    List<Predicate> predicates = Collections.singletonList(predicate);

    when(builder.and(predicates.toArray(new Predicate[0]))).thenReturn(predicate);
    Predicate result = PredicateUtil.createAndPredicate(builder, predicates);

    assertNotNull(result);
    assertEquals(predicate, result);
    verify(builder).and(predicates.toArray(new Predicate[0]));
  }

  @Test
  void testCreateAndPredicate_withMultiplePredicates() {
    CriteriaBuilder builder = mock(CriteriaBuilder.class);
    Predicate predicate = mock(Predicate.class);
    Predicate predicate1 = mock(Predicate.class);
    Predicate predicate2 = mock(Predicate.class);
    List<Predicate> predicates = Arrays.asList(predicate1, predicate2);

    when(builder.and(predicates.toArray(new Predicate[0]))).thenReturn(predicate);

    Predicate result = PredicateUtil.createAndPredicate(builder, predicates);

    assertNotNull(result);
    verify(builder).and(predicates.toArray(new Predicate[0]));
  }

  @Test
  void testAddOrPredicates_withNullOrPredicate() {
    List<Predicate> predicates = Collections.singletonList(mock(Predicate.class));

    List<Predicate> result = PredicateUtil.addOrPredicates(predicates, null);

    assertEquals(predicates, result);
  }

  @Test
  void testAddOrPredicates_withEmptyPredicates() {
    List<Predicate> predicates = new ArrayList<>();
    Predicate orPredicate = mock(Predicate.class);

    List<Predicate> result = PredicateUtil.addOrPredicates(predicates, orPredicate);

    assertEquals(Collections.emptyList(), result);
  }

  @Test
  void testAddOrPredicates_withNonEmptyPredicates() {
    Predicate predicate = mock(Predicate.class);
    Predicate orPredicate = mock(Predicate.class);
    List<Predicate> predicates = Collections.singletonList(predicate);

    List<Predicate> result = PredicateUtil.addOrPredicates(predicates, orPredicate);

    assertEquals(Arrays.asList(predicate, orPredicate), result);
  }

  @Test
  void testAddOrPredicates_withNullListAndPredicate() {
    List<Predicate> result = PredicateUtil.addOrPredicates(null, null);

    assertEquals(Collections.emptyList(), result);
  }

  @Test
  void testAddOrPredicates_withNullList() {
    Predicate orPredicate = mock(Predicate.class);

    List<Predicate> result = PredicateUtil.addOrPredicates(null, orPredicate);

    assertEquals(Collections.emptyList(), result);
  }
}