package org.tbbtalent.server.repository;

import java.util.Collection;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Root;

import org.springframework.data.jpa.domain.Specification;
import org.tbbtalent.server.model.Candidate;
import org.tbbtalent.server.model.SavedList;
import org.tbbtalent.server.request.candidate.SavedListGetRequest;

public class CandidateListSpecification {

    public static Specification<Candidate> buildSearchQuery(final SavedListGetRequest request) {
        return (candidate, query, cb) -> {
            Long savedListId = request.getSavedListId();

            query.distinct(true);

            Root<SavedList> savedList = query.from(SavedList.class);
            Expression<Collection<Candidate>> savedListCandidates = 
                    savedList.get("candidates");
            
            
            //TODO JC Take account of sort fields - see Search code
    
            return cb.and(cb.equal(savedList.get("id"), savedListId),
                    cb.isMember(candidate, savedListCandidates));
        };
    }

}
