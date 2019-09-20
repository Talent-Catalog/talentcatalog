package org.tbbtalent.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.tbbtalent.server.model.Candidate;

public interface CandidateRepository extends JpaRepository<Candidate, Long>, JpaSpecificationExecutor<Candidate> {

    /* Used for candidate authentication */
    @Query(" select distinct c from Candidate c "
            + " where lower(c.email) = lower(:username) "
            + " or lower(c.phone) = lower(:username) "
            + " or lower(c.whatsapp) = lower(:username) ")
    Candidate findByAnyUserIdentityIgnoreCase(@Param("username") String username);

    /* Used for JWT token parsing */
    Candidate findByCandidateNumber(String number);

    /* Used for candidate registration to check for existing accounts with different username options */
    Candidate findByEmailIgnoreCase(String email);
    Candidate findByPhoneIgnoreCase(String phone);
    Candidate findByWhatsappIgnoreCase(String whatsapp);

    @Query(" select distinct c from Candidate c "
            + " left join c.professions p"
            + " where c.id = :id ")
    Candidate findByIdLoadProfessions(@Param("id") Long id);

    @Query(" select distinct c from Candidate c "
            + " left join c.educations e"
            + " where c.id = :id ")
    Candidate findByIdLoadEducations(@Param("id") Long id);

    @Query(" select distinct c from Candidate c "
            + " left join c.workExperiences e"
            + " where c.id = :id ")
    Candidate findByIdLoadWorkExperiences(@Param("id") Long id);

    @Query(" select distinct c from Candidate c "
            + " left join c.certifications cert"
            + " where c.id = :id ")
    Candidate findByIdLoadCertifications(@Param("id") Long id);

    @Query(" select distinct c from Candidate c "
            + " left join c.candidateLanguages lang"
            + " where c.id = :id ")
    Candidate findByIdLoadCandidateLanguages(@Param("id") Long id);


}
