package org.tbbtalent.server.repository;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;
import org.tbbtalent.server.model.Occupation;
import org.tbbtalent.server.request.occupation.SearchOccupationRequest;

import javax.persistence.criteria.Predicate;

public class OccupationSpecification {

    public static Specification<Occupation> buildSearchQuery(final SearchOccupationRequest request) {
        return (occupation, query, builder) -> {
            Predicate conjunction = builder.conjunction();
            query.distinct(true);

            // KEYWORD SEARCH
            if (!StringUtils.isBlank(request.getKeyword())){
                String lowerCaseMatchTerm = request.getKeyword().toLowerCase();
                String likeMatchTerm = "%" + lowerCaseMatchTerm + "%";
                conjunction.getExpressions().add(
                        builder.or(
                                builder.like(builder.lower(occupation.get("name")), likeMatchTerm)
                        ));
            }

            if (request.getStatus() != null){
                conjunction.getExpressions().add(builder.equal(occupation.get("status"), request.getStatus()));
            }

            return conjunction;
        };
    }

}
