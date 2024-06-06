/*
 * Copyright (c) 2023 Talent Beyond Boundaries.
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
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;
import org.tctalent.server.model.db.PartnerImpl;
import org.tctalent.server.request.partner.SearchPartnerRequest;

import jakarta.persistence.criteria.Predicate;

public class PartnerSpecification {

    public static Specification<PartnerImpl> buildSearchQuery(final SearchPartnerRequest request) {
        return (Root<PartnerImpl> partner, CriteriaQuery<?> query, CriteriaBuilder builder) -> {
            // List to hold the predicates
            List<Predicate> predicates = new ArrayList<>();
            query.distinct(true);

            // KEYWORD SEARCH
            if (request.getKeyword() != null && !request.getKeyword().isEmpty()){
                String lowerCaseMatchTerm = request.getKeyword().toLowerCase();
                String likeMatchTerm = "%" + lowerCaseMatchTerm + "%";
                predicates.add(
                    builder.or(
                        builder.like(builder.lower(partner.get("name")), likeMatchTerm),
                        builder.like(builder.lower(partner.get("abbreviation")), likeMatchTerm)
                    )
                );
            }

            // STATUS
            if (request.getStatus() != null){
                predicates.add(builder.equal(partner.get("status"), request.getStatus()));
            }

            // Job Creators
            if (request.getJobCreator() != null){
                predicates.add(builder.equal(partner.get("jobCreator"), request.getJobCreator()));
            }

            // Source Partners
            if (request.getSourcePartner() != null){
                predicates.add(builder.equal(partner.get("sourcePartner"), request.getSourcePartner()));
            }

            // Combine all predicates into a single conjunction predicate
            return builder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
