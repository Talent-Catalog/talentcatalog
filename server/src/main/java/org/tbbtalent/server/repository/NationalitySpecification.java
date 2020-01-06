package org.tbbtalent.server.repository;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;
import org.tbbtalent.server.model.Nationality;
import org.tbbtalent.server.request.nationality.SearchNationalityRequest;

import javax.persistence.criteria.Predicate;

public class NationalitySpecification {

    public static Specification<Nationality> buildSearchQuery(final SearchNationalityRequest request) {
        return (nationality, query, builder) -> {
            Predicate conjunction = builder.conjunction();
            query.distinct(true);

            // KEYWORD SEARCH
            if (!StringUtils.isBlank(request.getKeyword())){
                String lowerCaseMatchTerm = request.getKeyword().toLowerCase();
                String likeMatchTerm = "%" + lowerCaseMatchTerm + "%";
                conjunction.getExpressions().add(
                        builder.or(
                                builder.like(builder.lower(nationality.get("name")), likeMatchTerm)
                        ));
            }

            if (request.getStatus() != null){
                conjunction.getExpressions().add(builder.equal(nationality.get("status"), request.getStatus()));
            }

            return conjunction;
        };
    }

}
