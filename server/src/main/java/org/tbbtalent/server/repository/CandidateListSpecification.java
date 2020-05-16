package org.tbbtalent.server.repository;

import java.util.Collection;
import java.util.List;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Fetch;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.springframework.data.jpa.domain.Specification;
import org.tbbtalent.server.model.Candidate;
import org.tbbtalent.server.model.SavedList;
import org.tbbtalent.server.request.candidate.SavedListGetRequest;

import static org.tbbtalent.server.repository.CandidateSpecificationUtil.getOrderByOrders;

public class CandidateListSpecification {

    public static Specification<Candidate> buildSearchQuery(final SavedListGetRequest request) {
        return (candidate, query, cb) -> {
            Long savedListId = request.getSavedListId();
            
            //Start by adding fetches and Order by
            boolean isCountQuery = query.getResultType().equals(Long.class);
            if (!isCountQuery) {
                //Fetch to populate the key linked entities
                Fetch<Object, Object> userFetch = candidate.fetch("user", JoinType.LEFT);
                Fetch<Object, Object> nationalityFetch = candidate.fetch("nationality", JoinType.LEFT);
                Fetch<Object, Object> countryFetch = candidate.fetch("country", JoinType.LEFT);

                //Do sorting by passing in the equivalent joins
                List<Order> orders = getOrderByOrders(request, candidate, cb,
                        (Join<Object, Object>) userFetch,
                        (Join<Object, Object>) nationalityFetch,
                        (Join<Object, Object>) countryFetch);
                query.orderBy(orders);
            } 

            //Now construct the actual query
            //Guided by https://stackoverflow.com/questions/31841471/spring-data-jpa-specification-for-a-manytomany-unidirectional-relationship
            /*
            select candidate from candidate 
            where exists 
                (select savedList from savedList 
                    where savedList.id = savedListID
                    and
                    candidate in savedList.candidates)  
             */
            Subquery<SavedList> savedListSubquery = query.subquery(SavedList.class);
            Root<SavedList> savedList = savedListSubquery.from(SavedList.class);
            Expression<Collection<Candidate>> savedListCandidates = 
                    savedList.get("candidates");
            savedListSubquery.select(savedList);
            savedListSubquery.where(
                    cb.equal(savedList.get("id"), savedListId),
                    cb.isMember(candidate, savedListCandidates)
            );
            return cb.exists(savedListSubquery);
        };
    }
}
