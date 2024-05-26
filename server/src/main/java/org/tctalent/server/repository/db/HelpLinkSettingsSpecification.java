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
import org.tctalent.server.model.db.HelpLink;
import org.tctalent.server.request.helplink.SearchHelpLinkRequest;

/**
 * This is used in the standard HelpLinkAPI search methods for managing the TC Settings HelpLinks
 * page.
 * <p/>
 * It only processes the keyword and countryId fields of a {@link SearchHelpLinkRequest}.
 * Other fields are ignored.
 * @see HelpLinkFetchSpecification
 */
public class HelpLinkSettingsSpecification {

    public static Specification<HelpLink> buildSearchQuery(final SearchHelpLinkRequest request) {
        return (helpLink, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            query.distinct(true);

            // KEYWORD SEARCH
            if (!StringUtils.isBlank(request.getKeyword())){
                String lowerCaseMatchTerm = request.getKeyword().toLowerCase();
                String likeMatchTerm = "%" + lowerCaseMatchTerm + "%";
                predicates.add(
                        builder.or(
                                builder.like(builder.lower(helpLink.get("label")), likeMatchTerm),
                                builder.like(builder.lower(helpLink.get("link")), likeMatchTerm)
                        ));
            }

            if (request.getCountryId() != null){
                predicates.add(builder.equal(helpLink.get("country").get("id"), request.getCountryId()));
            }

            return builder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
