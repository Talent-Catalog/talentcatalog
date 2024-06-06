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

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.lang.Nullable;

/**
 * Simple class to wrap some utility functions for creating/managing predicates, extracting common
 * checking and adding.
 */
public class PredicateUtil {

  /**
   * Use the provided builder to create an OR predicate from the list of predicates provided.
   *
   * @param builder    the builder
   * @param predicates list of predicates
   * @return an OR predicate or null if there is no list passed in.
   */
  public static @Nullable Predicate createOrPredicate(CriteriaBuilder builder,
      List<Predicate> predicates) {
    if (predicates == null || predicates.isEmpty()) {
      return null;
    }

    return builder.or(predicates.toArray(new Predicate[0]));
  }

  /**
   * Use the provided builder to create an AND predicate from the list of predicates provided.
   *
   * @param builder    the builder
   * @param predicates list of predicates
   * @return an AND predicate or null if there is no list passed in.
   */
  public static @Nullable Predicate createAndPredicate(CriteriaBuilder builder,
      List<Predicate> predicates) {
    if (predicates == null || predicates.isEmpty()) {
      return null;
    }
    return builder.and(predicates.toArray(new Predicate[0]));
  }

  public static List<Predicate> addOrPredicates(CriteriaBuilder builder, List<Predicate> predicates,
      @Nullable List<Predicate> orPredicates) {
    if (orPredicates == null) {
      return predicates;
    }

    return addOrPredicates(predicates, createOrPredicate(builder, orPredicates));
  }

  /**
   * Simple utility that will add the ORs predicate to the list. Deals with nulls to move it out of
   * logic.
   *
   * @param predicates  the list of predicates to add to
   * @param orPredicate the OR predicate to add
   * @return the resulting predicates or an empty list if predicates provided were null.
   */
  public static List<Predicate> addOrPredicates(List<Predicate> predicates,
      @Nullable Predicate orPredicate) {
    if (predicates == null) {
      return Collections.emptyList();
    }
    if (orPredicate == null || predicates.isEmpty()) {
      return predicates;
    }

    List<Predicate> newPredicates = new ArrayList<>(predicates.size() + 1);
    newPredicates.addAll(predicates);
    newPredicates.add(orPredicate);
    return newPredicates;
  }
}
