package org.tbbtalent.server.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.tbbtalent.server.model.Candidate;
import org.tbbtalent.server.model.Country;

public interface CandidateRepository extends JpaRepository<Candidate, Long>, JpaSpecificationExecutor<Candidate> {

    /**
     * CANDIDATE PORTAL METHODS: Used to display candidate in registration/profile.
     */

    /* Used for candidate registration to check for existing accounts with different username options */
//    Candidate findByEmailIgnoreCase(String email);

    @Query("select distinct c from Candidate c "
            + " where (lower(c.phone) = lower(:phone) )"
            + " and c.status <> 'deleted'")
    Candidate findByPhoneIgnoreCase(@Param("phone") String phone);

    @Query("select distinct c from Candidate c "
            + " where (lower(c.whatsapp) = lower(:whatsapp) )"
            + " and c.status <> 'deleted'")
    Candidate findByWhatsappIgnoreCase(@Param("whatsapp") String whatsapp);

    @Query(" select distinct c from Candidate c "
            + " left join c.candidateOccupations p "
            + " where c.id = :id ")
    Candidate findByIdLoadCandidateOccupations(@Param("id") Long id);

    @Query(" select distinct c from Candidate c "
            + " left join c.candidateEducations e "
            + " left join e.educationMajor m "
            + " where c.id = :id ")
    Candidate findByIdLoadEducations(@Param("id") Long id);

    @Query(" select distinct c from Candidate c "
            + " left join c.candidateJobExperiences e "
            + " left join e.candidateOccupation o "
            + " left join e.country co "
            + " where c.id = :id ")
    Candidate findByIdLoadJobExperiences(@Param("id") Long id);

    @Query(" select distinct c from Candidate c "
            + " left join fetch c.candidateCertifications cert "
            + " where c.id = :id ")
    Candidate findByIdLoadCertifications(@Param("id") Long id);

    @Query(" select distinct c from Candidate c "
            + " left join fetch c.candidateLanguages lang "
            + " where c.id = :id ")
    Candidate findByIdLoadCandidateLanguages(@Param("id") Long id);

    @Query(" select distinct c from Candidate c "
            + " left join fetch c.savedLists "
            + " where c.id = :id ")
    Candidate findByIdLoadSavedLists(@Param("id") Long id);

    @Query(" select c from Candidate c "
            + " where c.user.id = :id ")
    Candidate findByUserId(@Param("id") Long userId);

    @Query(" select distinct c from Candidate c "
            + " left join c.candidateOccupations occ "
            + " left join c.candidateJobExperiences exp "
            + " left join exp.candidateOccupation expOcc "
            + " left join exp.country co "
            + " left join c.candidateEducations edu "
            + " left join edu.educationMajor maj "
            + " left join c.candidateCertifications cert "
            + " left join c.candidateLanguages clang "
            + " left join clang.language lang "
            + " where c.user.id = :id ")
    Candidate findByUserIdLoadProfile(@Param("id") Long userId);

    /**
     * ADMIN PORTAL DISPLAY CANDIDATE METHODS: includes source country restrictions.
     */
    String sourceCountryRestriction = " and c.country in (:userSourceCountries)";

    @Query(" select distinct c from Candidate c "
            + " where lower(c.candidateNumber) like lower(:number)"
            + sourceCountryRestriction)
    Optional<Candidate> findByCandidateNumberRestricted(@Param("number") String number,
                                              @Param("userSourceCountries") Set<Country> userSourceCountries);

    @Query(" select distinct c from Candidate c left join c.user u "
            + " where lower(u.email) like lower(:candidateEmail) "
            + sourceCountryRestriction)
    Page<Candidate> searchCandidateEmail(@Param("candidateEmail") String candidateEmail,
                                         @Param("userSourceCountries") Set<Country> userSourceCountries,
                                         Pageable pageable);

    @Query(" select distinct c from Candidate c "
            + " where lower(c.candidateNumber) like lower(:candidateNumber) "
            + sourceCountryRestriction)
    Page<Candidate> searchCandidateNumber(@Param("candidateNumber") String candidateNumber,
                                          @Param("userSourceCountries") Set<Country> userSourceCountries,
                                          Pageable pageable);

    @Query(" select distinct c from Candidate c "
            + " where lower(c.phone) like lower(:candidatePhone) "
            + sourceCountryRestriction)
    Page<Candidate> searchCandidatePhone(@Param("candidatePhone") String candidatePhone,
                                         @Param("userSourceCountries") Set<Country> userSourceCountries,
                                         Pageable pageable);

    @Query(" select distinct c from Candidate c left join c.user u "
            + " where lower(concat(u.firstName, ' ', u.lastName)) like lower(:candidateName)"
            + sourceCountryRestriction)
    Page<Candidate> searchCandidateName(@Param("candidateName") String candidateName,
                                        @Param("userSourceCountries") Set<Country> userSourceCountries,
                                        Pageable pageable);

    @Query(" select c from Candidate c "
            + " join c.user u "
            + " where c.id = :id "
            + sourceCountryRestriction)
    Optional<Candidate> findByIdLoadUser(@Param("id") Long id,
                                         @Param("userSourceCountries") Set<Country> userSourceCountries);


    /**
     * ADMIN PORTAL SETTINGS METHODS: no source country restrictions.
     */

    @Query(" select c from Candidate c "
            + " where c.nationality.id = :nationalityId ")
    List<Candidate> findByNationalityId(@Param("nationalityId") Long nationalityId);

    @Query(" select c from Candidate c "
            + " where c.country.id = :countryId ")
    List<Candidate> findByCountryId(@Param("countryId") Long countryId);


    /**
     * ADMIN PORTAL INFOGRAPHICS METHODS: includes source country restrictions.
     */
    
    String countingStandardFilter = "u.status = 'active' and c.status != 'draft'";
    String dateFilter = "u.created_date >= (:dateFrom) and u.created_date <= (:dateTo)";
    
    //Note that I have been forced to go to native queries for these more 
    //complex queries. The non native queries seem a bit buggy.
    //Anyway - I couldn't get them working. Simpler to use normal SQL. JC.
    @Query(value = " select gender, count(distinct c) as PeopleCount" +
            " from candidate c left join users u on c.user_id = u.id" +
            " where c.country_id in (:sourceCountryIds)" +
            " and " + countingStandardFilter +
            " and " + dateFilter +
            " group by gender order by PeopleCount desc",
            nativeQuery = true)
    List<Object[]> countByGenderOrderByCount(@Param("sourceCountryIds") List<Long> sourceCountryIds,
                                             @Param("dateFrom") LocalDate dateFrom,
                                             @Param("dateTo") LocalDate dateTo);

    @Query(value = "select cast(extract(year from dob) as bigint) as year, " +
            " count(distinct c) as PeopleCount" +
            " from candidate c left join users u on c.user_id = u.id" +
            " where c.country_id in (:sourceCountryIds)" +
            " and " + countingStandardFilter +
            " and " + dateFilter +
            " and gender like :gender" +
            " and dob is not null and extract(year from dob) > 1940 " +
            " group by year order by year asc",
    nativeQuery = true)
    List<Object[]> countByBirthYearOrderByYear(@Param("gender") String gender,
                                               @Param("sourceCountryIds") List<Long> sourceCountryIds,
                                               @Param("dateFrom") LocalDate dateFrom,
                                               @Param("dateTo") LocalDate dateTo);

    @Query(value = "select n.name, count(distinct c) as PeopleCount" +
            " from candidate c left join users u on c.user_id = u.id" +
            " left join nationality n on c.nationality_id = n.id " +
            " left join country on c.country_id = country.id " +
            " where c.country_id in (:sourceCountryIds) " +
            " and " + countingStandardFilter +
            " and " + dateFilter +
            " and gender like :gender" +
            " and lower(country.name) like :country" +
            " group by n.name order by PeopleCount desc",
    nativeQuery = true)
    List<Object[]> countByNationalityOrderByCount(@Param("gender") String gender,
                                                  @Param("country") String country,
                                                  @Param("sourceCountryIds") List<Long> sourceCountryIds,
                                                  @Param("dateFrom") LocalDate dateFrom,
                                                  @Param("dateTo") LocalDate dateTo);

    @Query(value = "select s.name, count(distinct c) as PeopleCount" +
            " from candidate c left join users u on c.user_id = u.id" +
            " left join survey_type s on c.survey_type_id = s.id " +
            " left join country on c.country_id = country.id " +
            " where c.country_id in (:sourceCountryIds)" +
            " and " + countingStandardFilter +
            " and " + dateFilter +
            " and gender like :gender" +
            " and lower(country.name) like :country" +
            " group by s.name order by PeopleCount desc",
    nativeQuery = true)
    List<Object[]> countBySurveyOrderByCount(@Param("gender") String gender,
                                             @Param("country") String country,
                                             @Param("sourceCountryIds") List<Long> sourceCountryIds,
                                             @Param("dateFrom") LocalDate dateFrom,
                                             @Param("dateTo") LocalDate dateTo);

    @Query( value="select case when max_education_level_id is null then 'Unknown' " +
            "else el.name end as EducationLevel, " +
            "       count(distinct user_id) as PeopleCount " +
            "from candidate c left join users u on c.user_id = u.id " +
            "left join education_level el on c.max_education_level_id = el.id " +
            " where c.country_id in (:sourceCountryIds)" +
            " and " + countingStandardFilter +
            " and " + dateFilter +
            " and gender like :gender " +
            "group by EducationLevel " +
            "order by PeopleCount desc;",
    nativeQuery = true)
    List<Object[]> countByMaxEducationLevelOrderByCount(@Param("gender") String gender,
                                                        @Param("sourceCountryIds") List<Long> sourceCountryIds,
                                                        @Param("dateFrom") LocalDate dateFrom,
                                                        @Param("dateTo") LocalDate dateTo);

    @Query(value = "select l.name, count(distinct c) as PeopleCount" +
            " from candidate c left join users u on c.user_id = u.id" +
            " left join candidate_language cl on c.id = cl.candidate_id" +
            " left join language l on cl.language_id = l.id" +
            " where c.country_id in (:sourceCountryIds)" +
            " and " + countingStandardFilter +
            " and " + dateFilter +
            " and gender like :gender" +
            " group by l.name order by PeopleCount desc",
            nativeQuery = true)
    List<Object[]> countByLanguageOrderByCount(@Param("gender") String gender,
                                               @Param("sourceCountryIds") List<Long> sourceCountryIds,
                                               @Param("dateFrom") LocalDate dateFrom,
                                               @Param("dateTo") LocalDate dateTo);

    @Query(value = "select ll.name, count(distinct c) as PeopleCount" +
            " from candidate c left join users u on c.user_id = u.id" +
            " left join candidate_language cl on c.id = cl.candidate_id" +
            " left join language l on cl.language_id = l.id" +
            " left join language_level ll on cl.spoken_level_id = ll.id" +
            " where c.country_id in (:sourceCountryIds)" +
            " and " + countingStandardFilter +
            " and " + dateFilter +
            " and gender like :gender" +
            " and lower(l.name) = lower(:language)" +
            " group by ll.name order by PeopleCount desc",
            nativeQuery = true)
    List<Object[]> countBySpokenLanguageLevelByCount(@Param("gender") String gender,
                                                     @Param("language") String language,
                                                     @Param("sourceCountryIds") List<Long> sourceCountryIds,
                                                     @Param("dateFrom") LocalDate dateFrom,
                                                     @Param("dateTo") LocalDate dateTo);

    @Query( value="select o.name, " +
            "       count(distinct c) as PeopleCount " +
            "from candidate c left join users u on c.user_id = u.id " +
            "left join candidate_occupation co on c.id = co.candidate_id " +
            "left join occupation o on co.occupation_id = o.id " +
            " where c.country_id in (:sourceCountryIds)" +
            " and " + countingStandardFilter +
            " and " + dateFilter +
            " and gender like :gender " +
            "group by o.name " +
            "order by PeopleCount desc;",
            nativeQuery = true)
    List<Object[]> countByOccupationOrderByCount(@Param("gender") String gender,
                                                 @Param("sourceCountryIds") List<Long> sourceCountryIds,
                                                 @Param("dateFrom") LocalDate dateFrom,
                                                 @Param("dateTo") LocalDate dateTo);
    /**
     * This is the same as countByOccupationOrderByCount except that it excludes
     * undefined or unknown occupations (which unfortunately are common)
     * @param gender Gender filter or % if all genders
     * @return List of occupation name and count
     */
    @Query( value="select o.name, " +
            "       count(distinct c) as PeopleCount " +
            "from candidate c left join users u on c.user_id = u.id " +
            "left join candidate_occupation co on c.id = co.candidate_id " +
            "left join occupation o on co.occupation_id = o.id " +
            " where c.country_id in (:sourceCountryIds)" +
            " and " + countingStandardFilter +
            " and " + dateFilter +
            " and gender like :gender " +
            "and not lower(o.name) in ('undefined', 'unknown')" +
            "group by o.name " +
            "order by PeopleCount desc;",
            nativeQuery = true)
    List<Object[]> countByMostCommonOccupationOrderByCount(@Param("gender") String gender,
                                                           @Param("sourceCountryIds") List<Long> sourceCountryIds,
                                                           @Param("dateFrom") LocalDate dateFrom,
                                                           @Param("dateTo") LocalDate dateTo);

    @Query( value="select DATE(u.created_date), count(distinct u.id) as PeopleCount from users u " +
            "left join candidate c on u.id = c.user_id " +
            "where c.country_id in (:sourceCountryIds) " +
            "and " + dateFilter +
            "group by DATE(u.created_date) " +
            "order by DATE(u.created_date) asc;",
            nativeQuery = true)
    List<Object[]> countByCreatedDateOrderByCount(@Param("sourceCountryIds") List<Long> sourceCountryIds,
                                                  @Param("dateFrom") LocalDate dateFrom,
                                                  @Param("dateTo") LocalDate dateTo);

    @Query( value="select o.name, " +
            "       count(distinct c) as PeopleCount " +
            "from candidate c left join users u on c.user_id = u.id " +
            "left join candidate_occupation co on c.id = co.candidate_id " +
            "left join occupation o on co.occupation_id = o.id " +
            " where c.country_id in (:sourceCountryIds)" +
            " and " + countingStandardFilter +
            " and " + dateFilter +
            " group by o.name " +
            "order by PeopleCount desc;",
            nativeQuery = true)
    List<Object[]> countByOccupationOrderByCount(@Param("sourceCountryIds") List<Long> sourceCountryIds,
                                                 @Param("dateFrom") LocalDate dateFrom,
                                                 @Param("dateTo") LocalDate dateTo);

}
