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

import jakarta.persistence.criteria.Predicate;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;
import org.tctalent.server.model.db.Country;
import org.tctalent.server.request.country.SearchCountryRequest;

public class CountrySpecification {

    public static Specification<Country> buildSearchQuery(final SearchCountryRequest request) {
        return (country, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            query.distinct(true);

            // KEYWORD SEARCH
            if (!StringUtils.isBlank(request.getKeyword())){
                String lowerCaseMatchTerm = request.getKeyword().toLowerCase();
                String likeMatchTerm = "%" + lowerCaseMatchTerm + "%";
                predicates.add(
                        builder.or(
                                builder.like(builder.lower(country.get("name")), likeMatchTerm)
                        ));
            }

            if (request.getStatus() != null){
                predicates.add(builder.equal(country.get("status"), request.getStatus()));
            }

            return builder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
