package org.tbbtalent.server.repository;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;
import org.tbbtalent.server.model.Country;
import org.tbbtalent.server.request.country.SearchCountryRequest;

import javax.persistence.criteria.Predicate;

public class CountrySpecification {

    public static Specification<Country> buildSearchQuery(final SearchCountryRequest request) {
        return (country, query, builder) -> {
            Predicate conjunction = builder.conjunction();
            query.distinct(true);

            // KEYWORD SEARCH
            if (!StringUtils.isBlank(request.getKeyword())){
                String lowerCaseMatchTerm = request.getKeyword().toLowerCase();
                String likeMatchTerm = "%" + lowerCaseMatchTerm + "%";
                conjunction.getExpressions().add(
                        builder.or(
                                builder.like(builder.lower(country.get("name")), likeMatchTerm)
                        ));
            }

            if (request.getStatus() != null){
                conjunction.getExpressions().add(builder.equal(country.get("status"), request.getStatus()));
            }

            return conjunction;
        };
    }
}