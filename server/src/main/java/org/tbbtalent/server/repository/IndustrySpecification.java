package org.tbbtalent.server.repository;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;
import org.tbbtalent.server.model.Industry;
import org.tbbtalent.server.request.industry.SearchIndustryRequest;

import javax.persistence.criteria.Predicate;

public class IndustrySpecification {

    public static Specification<Industry> buildSearchQuery(final SearchIndustryRequest request) {
        return (industry, query, builder) -> {
            Predicate conjunction = builder.conjunction();
            query.distinct(true);

            // KEYWORD SEARCH
            if (!StringUtils.isBlank(request.getKeyword())){
                String lowerCaseMatchTerm = request.getKeyword().toLowerCase();
                String likeMatchTerm = "%" + lowerCaseMatchTerm + "%";
                conjunction.getExpressions().add(
                        builder.or(
                                builder.like(builder.lower(industry.get("name")), likeMatchTerm)
                        ));
            }

            if (request.getStatus() != null){
                conjunction.getExpressions().add(builder.equal(industry.get("status"), request.getStatus()));
            }

            return conjunction;
        };
    }

}
