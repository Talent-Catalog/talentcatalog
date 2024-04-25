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

package org.tctalent.server.elastic

import co.elastic.clients.elasticsearch._types.query_dsl.*
import org.springframework.data.elasticsearch.core.query.StringQuery
import org.tctalent.server.exception.CircularReferencedException
import org.tctalent.server.model.db.Candidate
import org.tctalent.server.request.candidate.SearchCandidateRequest
import org.tctalent.server.request.candidate.SearchJoinRequest


/*
       Constructing a filtered simple query that looks like this:

       GET /candidates/_search
        {
          "query": {
            "bool": {
              "must": [
                { "simple_query_string": {"query":"the +jet+ engine"}}
              ],
              "filter": [
                { "term":  { "status": "pending" }},
                { "range":  { "minEnglishSpokenLevel": {"gte": 2}}}
              ]
            }
          }
       }
*/
/**
 * Extracted/moved from SavedSearchServiceImpl
 */
fun computeElasticQuery(
    request: SearchCandidateRequest, simpleQueryString: String?,
    excludedCandidates: Collection<Candidate>?
): Query {
    // with some design this may not be required.
//    val user: User = userService.getLoggedInUser()
    val x : StringQuery = getSimpleStringAsQuery(simpleQueryString)

    return Query.Builder().build()
}

fun addElasticQuery(searchJoinRequest: SearchJoinRequest, savedSearchIds: List<Long>): Query {

    // We don't want searches built on themselves - this is also guarded against in frontend
    if (savedSearchIds.contains(searchJoinRequest.savedSearchId)) {
        throw CircularReferencedException(searchJoinRequest.savedSearchId)
    }
    val request: SearchCandidateRequest = impl.loadSavedSearch(searchJoinRequest.savedSearchId)
    // Get the keyword search term, if any
    val simpleStringQuery = request.simpleQueryString

    // Compute the candidates that should be excluded from search
    val excludeCandidates: Set<Candidate> =
        impl.computeCandidatesExcludedFromSearchCandidateRequest(request)

    return Query.Builder().build()
}

//fun getRangeFilter(field: String, min: String?, max: String?): Query {
//    return RangeQuery.of { r -> r.field(field).from(min).to(max) }._toQuery()
//}

// TODO (fix the values being a string)...
//fun getTermFilter(searchType: SearchType?, field: String, values: List<String>): Query? {
//    if (values.isEmpty()) return null
//
//    val valuesToUse = TermsQueryField.Builder()
//        .value(values.stream().map(FieldValue::of).toList())
//        .build()
//    val builder = TermsQuery.of { t -> t
//        .field(field)
//        .terms(valuesToUse) }._toQuery()
//
//    val boolQry = QueryBuilders.bool()
//    when (searchType) {
//        SearchType.not -> boolQry.mustNot(builder)
//        else -> boolQry.filter(listOf(builder))
//    }
//    return boolQry.build()._toQuery()
//}

//fun getSimpleStringAsQuery(simpleQueryString: String?): StringQuery {
//    if (simpleQueryString.isNullOrEmpty()) {
//        return StringQuery("")
//    }
//    val q = StringQuery(simpleQueryString)
//    return q
//}