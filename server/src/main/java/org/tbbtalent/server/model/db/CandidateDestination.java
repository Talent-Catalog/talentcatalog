package org.tbbtalent.server.model.db;

import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.NonNull;
import org.tbbtalent.server.request.candidate.CandidateIntakeDataUpdate;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "candidate_destination")
@SequenceGenerator(name = "seq_gen", sequenceName = "candidate_destination_id_seq", allocationSize = 1)
public class CandidateDestination extends AbstractDomainObject<Long> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id")
    private Candidate candidate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id")
    private Country country;

    @Enumerated(EnumType.STRING)
    private YesNoUnsure interest;

    @Enumerated(EnumType.STRING)
    private FamilyRelations family;

    private String location;

    private String notes;

    public CandidateDestination() {
    }

    public void populateIntakeData(
            @NonNull Candidate candidate, @NonNull Country country,
            CandidateIntakeDataUpdate data) {
        setCandidate(candidate);
        setCountry(country);
        if (data.getDestinationInterest() != null) {
            setInterest(data.getDestinationInterest());
        }
        if (data.getDestinationFamily() != null) {
            setFamily(data.getDestinationFamily());
        }
        if (data.getDestinationLocation() != null) {
            setLocation(data.getDestinationLocation());
        }
        if (data.getDestinationNotes() != null) {
            setNotes(data.getDestinationNotes());
        }
    }
}
