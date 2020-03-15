package org.tbbtalent.server.repository;

import javax.persistence.criteria.Predicate;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;
import org.tbbtalent.server.model.SavedSearch;
import org.tbbtalent.server.model.SavedSearchSubtype;
import org.tbbtalent.server.model.SavedSearchType;
import org.tbbtalent.server.model.Status;
import org.tbbtalent.server.request.search.SearchSavedSearchRequest;

public class SavedSearchSpecification {

    public static Specification<SavedSearch> buildSearchQuery(final SearchSavedSearchRequest request) {
        return (savedSearch, query, builder) -> {
            Predicate conjunction = builder.conjunction();
            query.distinct(true);

            // KEYWORD SEARCH
            if (!StringUtils.isBlank(request.getKeyword())){
                String lowerCaseMatchTerm = request.getKeyword().toLowerCase();
                String likeMatchTerm = "%" + lowerCaseMatchTerm + "%";
                conjunction.getExpressions().add(
                        builder.or(
                                builder.like(builder.lower(savedSearch.get("name")), likeMatchTerm)
                        ));
            }
            SavedSearchType savedSearchType = request.getSavedSearchType();
            if (savedSearchType != null) {
                // Search for specified type
                SavedSearchSubtype savedSearchSubtype = request.getSavedSearchSubtype();
                String type = SavedSearch.makeStringSavedSearchType(
                        savedSearchType, savedSearchSubtype);
                conjunction.getExpressions().add(builder.equal(savedSearch.get("type"), type));
            }
            // ONLY SHOW ACTIVE SAVED SEARCHES
            conjunction.getExpressions().add(builder.equal(savedSearch.get("status"), Status.active));

            //If fixed is specified, only supply matching saved searches
            if (request.getFixed() != null) {
                conjunction.getExpressions().add(
                        builder.equal(savedSearch.get("fixed"), request.getFixed()));
            }

            return conjunction;
        };
    }

}
