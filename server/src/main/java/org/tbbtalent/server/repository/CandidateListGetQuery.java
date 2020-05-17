package org.tbbtalent.server.repository;

import java.util.Collection;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Fetch;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.tbbtalent.server.model.Candidate;
import org.tbbtalent.server.model.SavedList;
import org.tbbtalent.server.request.candidate.SavedListGetRequest;

import lombok.RequiredArgsConstructor;
import static org.tbbtalent.server.repository.CandidateSpecificationUtil.getOrderByOrders;

/**
 * "Specification" which defines the database query to retrieve all candidates
 * in a Saved List based on a {@link SavedListGetRequest}.
 * <p>
 *   To me, this is a more comprehensible way of using the {@link Specification}
 *   interface.
 * </p>
 * <p>
 *     Instead of calling a static buildQuery method, you just pass in an
 *     instance of this to the JPA {@link CandidateRepository#findAll} method.
 *     The instance is created by passing the {@link SavedListGetRequest} to
 *     the constructor.
 * </p>
 * <p>
 *     Note that this Specification query handles the sorting internally
 *     so the {@link PageRequest} passed in should not provide any sorts.
 *     You can get this by calling 
 *     {@link SavedListGetRequest#getPageRequestWithoutSort()} 
 * </p>
 *     eg:
 *     <code>
 *     PageRequest pageRequest = request.getPageRequestWithoutSort();
 *     Page<Candidate> candidatesPage = candidateRepository.findAll(
 *                 new CandidateListGetQuery(request), pageRequest);         
 *     </code>
 */
@RequiredArgsConstructor
public class CandidateListGetQuery implements Specification<Candidate> {
    private final SavedListGetRequest request;

    @Override
    public Predicate toPredicate(Root<Candidate> candidate, 
                                 CriteriaQuery<?> query, CriteriaBuilder cb) {
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
    }
}
