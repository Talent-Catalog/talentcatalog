package org.tbbtalent.server.repository;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;
import org.tbbtalent.server.model.Language;
import org.tbbtalent.server.request.language.SearchLanguageRequest;

import javax.persistence.criteria.Predicate;

public class LanguageSpecification {

    public static Specification<Language> buildSearchQuery(final SearchLanguageRequest request) {
        return (language, query, builder) -> {
            Predicate conjunction = builder.conjunction();
            query.distinct(true);

            // KEYWORD SEARCH
            if (!StringUtils.isBlank(request.getKeyword())){
                String lowerCaseMatchTerm = request.getKeyword().toLowerCase();
                String likeMatchTerm = "%" + lowerCaseMatchTerm + "%";
                conjunction.getExpressions().add(
                        builder.or(
                                builder.like(builder.lower(language.get("name")), likeMatchTerm)
                        ));
            }

            if (request.getStatus() != null){
                conjunction.getExpressions().add(builder.equal(language.get("status"), request.getStatus()));
            }

            return conjunction;
        };
    }
}