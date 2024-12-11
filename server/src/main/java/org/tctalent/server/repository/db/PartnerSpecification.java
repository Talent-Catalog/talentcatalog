/*
 * Copyright (c) 2024 Talent Catalog.
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

import jakarta.persistence.criteria.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;
import org.tctalent.server.model.db.PartnerImpl;
import org.tctalent.server.request.partner.SearchPartnerRequest;

public class PartnerSpecification {

    public static Specification<PartnerImpl> buildSearchQuery(final SearchPartnerRequest request) {
        return (partner, query, cb) -> {
            if (query == null) {
                throw new IllegalArgumentException("PartnerSpecification.CriteriaQuery should not be null");
            }
            query.distinct(true);

            Predicate conjunction = cb.conjunction();

            // KEYWORD SEARCH
            if (!StringUtils.isBlank(request.getKeyword())){
                String lowerCaseMatchTerm = request.getKeyword().toLowerCase();
                String likeMatchTerm = "%" + lowerCaseMatchTerm + "%";
                conjunction = cb.and(conjunction,
                        cb.or(
                                cb.like(cb.lower(partner.get("name")), likeMatchTerm),
                                cb.like(cb.lower(partner.get("abbreviation")), likeMatchTerm)
                        ));
            }

            // STATUS
            if (request.getStatus() != null){
                conjunction = cb.and(conjunction,
                    cb.equal(partner.get("status"), request.getStatus()));
            }

            // Job Creators
            if (request.getJobCreator() != null){
                conjunction = cb.and(conjunction,
                    cb.equal(partner.get("jobCreator"), request.getJobCreator()));
            }

            // Source Partners
            if (request.getSourcePartner() != null){
                conjunction = cb.and(conjunction,
                    cb.equal(partner.get("sourcePartner"), request.getSourcePartner()));
            }

            return conjunction;
        };
    }
}
