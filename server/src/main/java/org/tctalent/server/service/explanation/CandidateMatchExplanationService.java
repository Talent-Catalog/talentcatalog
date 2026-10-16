package org.tctalent.server.service.explanation;

import org.springframework.lang.NonNull;
import org.tctalent.server.exception.InvalidRequestException;
import org.tctalent.server.exception.MatchExplanationException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.response.CandidateMatchExplanation;

/**
 * On-demand LLM explanations of how a candidate's existing job experience relates to the
 * description of a {@link org.tctalent.server.model.db.Matching}.
 * <p>Explanations are generated one candidate at a time, and only operate on a Matching: they
 * don't depend on whether the Matching is used by a job, a search or a list. The explanation
 * compares the Matching's description with the candidate's job experience text. It does not
 * perform candidate matching or rerun any Best-N matching algorithm.</p>
 * <p>Generated explanations are cached - at most one per (candidate, Matching) - and reused
 * while the Matching's description is unchanged, ie while the Matching's updatedDate still
 * equals its updatedDate when the explanation's generation started. Changes to the candidate's
 * job experience are not tracked, so {@link #regenerateExplanation} is provided to refresh an
 * explanation explicitly.</p>
 * <p>An explanation generated from a description which changed during generation is never
 * saved, so it can never replace an explanation of the current description.</p>
 */
public interface CandidateMatchExplanationService {

    /**
     * Returns the cached explanation of the candidate's match against the Matching's
     * description, if it is current. Otherwise, generates a new explanation from the Matching's
     * current description, replacing any cached one.
     * <p>The Matching's description is obtained by the server - see
     * {@link org.tctalent.server.service.db.MatchingService#resolveDescription}, which also
     * initialises a job's blank Matching from the job's text.</p>
     *
     * @param candidateId ID of the candidate to explain
     * @param matchingId ID of the Matching whose description the candidate is compared against
     * @return current match explanation
     * @throws NoSuchObjectException if there is no such candidate or Matching
     * @throws InvalidRequestException if the Matching has no description
     * @throws MatchExplanationException if the explanation could not be generated, for example
     * because the Python match explanation service returned no result, an item-level error or a
     * mismatched candidate ID, could not be reached, or the Matching's description kept changing
     * during generation
     */
    @NonNull
    CandidateMatchExplanation getOrGenerateExplanation(long candidateId, long matchingId)
        throws NoSuchObjectException, InvalidRequestException, MatchExplanationException;

    /**
     * Same as {@link #getOrGenerateExplanation}, except that any cached explanation is ignored:
     * a new explanation is always generated, replacing the cached one. Used when the cached
     * explanation may be out of date for reasons which are not tracked, such as changes to the
     * candidate's job experience.
     *
     * @param candidateId ID of the candidate to explain
     * @param matchingId ID of the Matching whose description the candidate is compared against
     * @return newly generated match explanation - or, if the Matching's description changed
     * during generation, the current explanation of the new description
     * @throws NoSuchObjectException if there is no such candidate or Matching
     * @throws InvalidRequestException if the Matching has no description
     * @throws MatchExplanationException if the explanation could not be generated - see
     * {@link #getOrGenerateExplanation}
     */
    @NonNull
    CandidateMatchExplanation regenerateExplanation(long candidateId, long matchingId)
        throws NoSuchObjectException, InvalidRequestException, MatchExplanationException;
}
