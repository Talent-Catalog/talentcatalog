package org.tbbtalent.server.repository;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;
import org.tbbtalent.server.model.EducationLevel;
import org.tbbtalent.server.request.education.level.SearchEducationLevelRequest;

import javax.persistence.criteria.Predicate;

public class EducationLevelSpecification {

    public static Specification<EducationLevel> buildSearchQuery(final SearchEducationLevelRequest request) {
        return (educationLevel, query, builder) -> {
            Predicate conjunction = builder.conjunction();
            query.distinct(true);

            // KEYWORD SEARCH
            if (!StringUtils.isBlank(request.getKeyword())){
                String lowerCaseMatchTerm = request.getKeyword().toLowerCase();
                String likeMatchTerm = "%" + lowerCaseMatchTerm + "%";
                conjunction.getExpressions().add(
                        builder.or(
                                builder.like(builder.lower(educationLevel.get("level")), likeMatchTerm)
                        ));
            }

            if (request.getStatus() != null){
                conjunction.getExpressions().add(builder.equal(educationLevel.get("status"), request.getStatus()));
            }

            return conjunction;
        };
    }
}