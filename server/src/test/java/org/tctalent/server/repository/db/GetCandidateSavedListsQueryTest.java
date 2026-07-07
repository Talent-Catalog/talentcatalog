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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.tctalent.server.model.db.CandidateSavedList;
import org.tctalent.server.model.db.SavedList;

@ExtendWith(MockitoExtension.class)
class GetCandidateSavedListsQueryTest {

  @Mock private Root<SavedList> savedList;
  @Mock private CriteriaQuery<?> query;
  @Mock private CriteriaBuilder cb;

  @Mock private Subquery<SavedList> subquery;
  @Mock private Root<CandidateSavedList> candidateSavedList;

  @Mock private Path<SavedList> savedListPath;
  @Mock private Path<Object> candidatePath;
  @Mock private Path<Long> candidateIdPath;

  @Mock private Predicate candidateIdPredicate;
  @Mock private CriteriaBuilder.In<SavedList> inPredicate;

  @Test
  @DisplayName("should build subquery for saved lists belonging to candidate")
  void toPredicate_shouldBuildSubqueryForCandidateSavedLists() {
    long candidateId = 123L;

    when(query.subquery(SavedList.class)).thenReturn(subquery);
    when(subquery.from(CandidateSavedList.class)).thenReturn(candidateSavedList);

    when(candidateSavedList.<SavedList>get("savedList")).thenReturn(savedListPath);
    when(candidateSavedList.<Object>get("candidate")).thenReturn(candidatePath);
    when(candidatePath.<Long>get("id")).thenReturn(candidateIdPath);

    when(cb.equal(candidateIdPath, candidateId)).thenReturn(candidateIdPredicate);
    when(subquery.select(savedListPath)).thenReturn(subquery);
    when(subquery.where(candidateIdPredicate)).thenReturn(subquery);

    when(cb.in(savedList)).thenReturn(inPredicate);
    when(inPredicate.value(subquery)).thenReturn(inPredicate);

    Predicate result = new GetCandidateSavedListsQuery(candidateId)
        .toPredicate(savedList, query, cb);

    assertEquals(inPredicate, result);

    verify(query).subquery(SavedList.class);
    verify(subquery).from(CandidateSavedList.class);
    verify(subquery).select(savedListPath);
    verify(subquery).where(candidateIdPredicate);
    verify(cb).equal(candidateIdPath, candidateId);
    verify(cb).in(savedList);
    verify(inPredicate).value(subquery);
  }
}