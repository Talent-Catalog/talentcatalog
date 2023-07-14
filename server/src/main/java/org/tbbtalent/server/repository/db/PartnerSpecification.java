/*
 * Copyright (c) 2022 Talent Beyond Boundaries.
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

package org.tbbtalent.server.repository.db;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;
import org.tbbtalent.server.model.db.PartnerImpl;
import org.tbbtalent.server.request.partner.SearchPartnerRequest;

import javax.persistence.criteria.Predicate;

public class PartnerSpecification {

    public static Specification<PartnerImpl> buildSearchQuery(final SearchPartnerRequest request) {
        return (partner, query, builder) -> {
            Predicate conjunction = builder.conjunction();
            query.distinct(true);

            // KEYWORD SEARCH
            if (!StringUtils.isBlank(request.getKeyword())){
                String lowerCaseMatchTerm = request.getKeyword().toLowerCase();
                String likeMatchTerm = "%" + lowerCaseMatchTerm + "%";
                conjunction.getExpressions().add(
                        builder.or(
                                builder.like(builder.lower(partner.get("name")), likeMatchTerm),
                                builder.like(builder.lower(partner.get("abbreviation")), likeMatchTerm)
                        ));
            }

            // STATUS
            if (request.getStatus() != null){
                conjunction.getExpressions().add(builder.equal(partner.get("status"), request.getStatus()));
            }

            // Job Creators
            if (request.getJobCreator() != null){
                conjunction.getExpressions().add(builder.equal(partner.get("jobCreator"), request.getJobCreator()));
            }

            // Source Partners
            if (request.getSourcePartner() != null){
                conjunction.getExpressions().add(builder.equal(partner.get("sourcePartner"), request.getSourcePartner()));
            }

            return conjunction;
        };
    }
}
