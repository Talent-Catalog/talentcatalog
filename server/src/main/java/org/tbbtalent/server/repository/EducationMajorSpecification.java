package org.tbbtalent.server.repository;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;
import org.tbbtalent.server.model.EducationMajor;
import org.tbbtalent.server.request.education.major.SearchEducationMajorRequest;

import javax.persistence.criteria.Predicate;

public class EducationMajorSpecification {

    public static Specification<EducationMajor> buildSearchQuery(final SearchEducationMajorRequest request) {
        return (educationMajor, query, builder) -> {
            Predicate conjunction = builder.conjunction();
            query.distinct(true);

            // KEYWORD SEARCH
            if (!StringUtils.isBlank(request.getKeyword())){
                String lowerCaseMatchTerm = request.getKeyword().toLowerCase();
                String likeMatchTerm = "%" + lowerCaseMatchTerm + "%";
                conjunction.getExpressions().add(
                        builder.or(
                                builder.like(builder.lower(educationMajor.get("name")), likeMatchTerm)
                        ));
            }

            if (request.getStatus() != null){
                conjunction.getExpressions().add(builder.equal(educationMajor.get("status"), request.getStatus()));
            }

            return conjunction;
        };
    }
}