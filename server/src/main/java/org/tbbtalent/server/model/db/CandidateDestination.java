package org.tbbtalent.server.model.db;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.springframework.lang.NonNull;
import org.tbbtalent.server.request.candidate.CandidateIntakeDataUpdate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "candidate_destination")
@SequenceGenerator(name = "seq_gen", sequenceName = "candidate_destination_id_seq", allocationSize = 1)
public class CandidateDestination extends AbstractDomainObject<Long> 
        implements Comparable<CandidateDestination>{

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

    @Override
    public int compareTo(CandidateDestination o) {
        if (country == null) {
            return o.country == null ? 0 : -1;
        }
        return country.compareTo(o.country);
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
