/*
 * Copyright (c) 2024 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.repository.db;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.transaction.annotation.Transactional;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateStatus;
import org.tctalent.server.model.db.Country;
import org.tctalent.server.model.db.ReviewStatus;

/**
 * See notes on "join fetch" in the doc for {@link #findByIdLoadCandidateOccupations}
 */
public interface CandidateRepository extends CacheEvictingRepository<Candidate, Long>, JpaSpecificationExecutor<Candidate> {

    /**
     * This method overrides the default save behavior in CacheEvictingRepository. Only the
     * cache entry corresponding to the saved candidate's username will be removed from the cache.
     *
     * @param candidate the candidate entity to save; must not be null
     */
    @NonNull
    @Override
    @CacheEvict(value = "users", key = "#p0?.user?.username")
    <S extends Candidate> S save(@NonNull S candidate);

    /**
     * This method overrides the default saveAndFlush behavior in CacheEvictingRepository. Only the
     * cache entry corresponding to the saved candidate's username will be removed from the cache.
     *
     * @param candidate the candidate entity to save; must not be null
     */
    @NonNull
    @Override
    @CacheEvict(value = "users", key = "#p0?.user?.username")
    <S extends Candidate> S saveAndFlush(@NonNull S candidate);

    /**
     * This method overrides the default delete behavior in CacheEvictingRepository. Only the
     * cache entry corresponding to the deleted candidate's username will be removed from the cache.
     *
     * @param candidate the candidate entity to delete; must not be null
     */
    @Override
    @CacheEvict(value = "users", key = "#p0?.user?.username")
    void delete(@NonNull Candidate candidate);

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

    /**
     * Uses "join fetch" to load the candidate as well as its associated
     * occupations. These are configured on the Candidate entity to be
     * "lazy" loaded which means that they will not be loaded without
     * this join fetch.
     * <p/>
     * Note that Hibernate - the standard JPA provider - generally doesn't like
     * "join fetch"ing for more than one association at a time.
     * There are ways around that - eg by using Set's instead of List's - but
     * then you run into cartesian product issues.
     * See https://vladmihalcea.com/hibernate-multiplebagfetchexception/
     * @param id ID of candidate to be loaded from the database.
     * @return Candidate with loaded occupations.
     */
    @Query(" select c from Candidate c "
            + " left join fetch c.candidateOccupations p "
            + " where c.id = :id ")
    Candidate findByIdLoadCandidateOccupations(@Param("id") Long id);

    @Query(" select c from Candidate c "
        + " left join fetch c.candidateExams p "
        + " where c.id = :id ")
    Candidate findByIdLoadCandidateExams(@Param("id") Long id);

    @Query(" select c from Candidate c "
            + " left join fetch c.candidateCertifications cert "
            + " where c.id = :id ")
    Candidate findByIdLoadCertifications(@Param("id") Long id);

    @Query(" select c from Candidate c "
            + " left join fetch c.candidateDestinations dest "
            + " where c.id = :id ")
    Candidate findByIdLoadDestinations(@Param("id") Long id);

    @Query(" select c from Candidate c "
            + " left join fetch c.candidateLanguages lang "
            + " where c.id = :id ")
    Candidate findByIdLoadCandidateLanguages(@Param("id") Long id);

    @Query(" select c from Candidate c "
            + " left join fetch c.candidateSavedLists "
            + " where c.id = :id ")
    Candidate findByIdLoadSavedLists(@Param("id") Long id);

    @Query(" select c from Candidate c "
            + " where c.user.id = :id ")
    Candidate findByUserId(@Param("id") Long userId);

    @Query(" select c from Candidate c "
            + " where c.id in (:ids) ")
    List<Candidate> findByIds(@Param("ids") Iterable<Long> ids);

    @Query(" select c from Candidate c "
            + " where c.status <> 'deleted'"
    )
    Page<Candidate> findCandidatesWhereStatusNotDeleted(Pageable pageable);

    @Query(" select c from Candidate c "
            + " where c.status in (:statuses)")
    List<Candidate> findByStatuses(@Param("statuses") List<CandidateStatus> statuses);

    /**
     * Gets candidates for the nightly TC-SF candidate sync: we only really need active candidates
     * on SF — but adding the sfLink condition ensures that active-inactive status changes are
     * reflected on SF while still bypassing candidates in draft stage or who were inactive at time
     * of implementation. SF reports will need to filter for active/desired stages.
     * @param statuses provided by the CandidateStatus enum — 'active' defined here as incl active,
     *                pending, incomplete
     * @return candidate list
     */
    @Query(" select c from Candidate c "
            + " where c.status in (:statuses)"
            + " or c.sflink is not null")
    Page<Candidate> findByStatusesOrSfLinkIsNotNull(
        @Param("statuses") List<CandidateStatus> statuses, Pageable pageable);


    @Query("select c from Candidate c "
        + "join CandidateReviewStatusItem cri "
        + "on c.id = cri.candidate.id "
        + "where cri.savedSearch.id = :savedSearchId "
        + "and cri.reviewStatus not in (:statuses)")
    Page<Candidate> findReviewedCandidatesBySavedSearchId(@Param("savedSearchId") Long savedSearchId,
        @Param("statuses") List<ReviewStatus> statuses, Pageable pageable);

    @Transactional
    @Modifying
    @Query("update Candidate c set c.textSearchId = null")
    @CacheEvict(value = "users", allEntries = true)
    void clearAllCandidateTextSearchIds();

    /**
     * ADMIN PORTAL DISPLAY CANDIDATE METHODS: includes source country restrictions.
     */
    String sourceCountryRestriction = " and c.country in (:userSourceCountries)";
    String excludeDeleted = " and c.status <> 'deleted'";

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

    @Query("""
      select distinct c 
      from Candidate c 
      left join c.user u 
      where lower(u.firstName) like lower(:candidateName)
         or lower(u.lastName) like lower(:candidateName) 
         or lower(concat(u.firstName, ' ', u.lastName)) like lower(:candidateName)
      """ + excludeDeleted + sourceCountryRestriction)
    Page<Candidate> searchCandidateName(@Param("candidateName") String candidateName,
        @Param("userSourceCountries") Set<Country> userSourceCountries,
        Pageable pageable);

    /**
     * Note that we need to pass in a CandidateStatus.deleted constant in the
     * "exclude" parameter because I haven't been able to just put
     * CanadidateStatus.deleted constant directly into the query.
     * Theoretically the fully qualified name should work eg
     * " and c.status <> db.model.org.tctalent.server.CandidateStatus.deleted"
     * See https://stackoverflow.com/questions/8217144/problems-with-making-a-query-when-using-enum-in-entity
     * but Hibernate rejects that at run time with the following error:
     * org.hibernate.hql.internal.ast.QuerySyntaxException: Invalid path
     * - JC
     */
    @Query(" select distinct c from Candidate c "
        + " where c.candidateNumber like :candidateNumber "
        + excludeDeleted
        + sourceCountryRestriction)
    Page<Candidate> searchCandidateNumber(@Param("candidateNumber") String candidateNumber,
        @Param("userSourceCountries") Set<Country> userSourceCountries,
        Pageable pageable);

    @Query(" select distinct c from Candidate c left join c.user u "
        + " where (lower(c.phone) like lower(:emailPhoneOrWhatsapp) "
        + " or lower(c.whatsapp) like lower(:emailPhoneOrWhatsapp) "
        + " or lower(u.email) like lower(:emailPhoneOrWhatsapp)) "
        + excludeDeleted
        + sourceCountryRestriction)
    Page<Candidate> searchCandidateEmailPhoneOrWhatsapp(@Param("emailPhoneOrWhatsapp") String emailPhoneOrWhatsapp,
                                                        @Param("userSourceCountries") Set<Country> userSourceCountries,
                                                        Pageable pageable);

    @Query(" select distinct c from Candidate c "
        + " where lower(c.externalId) like lower(:externalId) "
        + sourceCountryRestriction)
    Page<Candidate> searchCandidateExternalId(@Param("externalId") String externalId,
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


    Candidate findByCandidateNumber(String candidateNumber);

    Optional<Candidate> findByPublicId(String publicId);

    /**
     * ADMIN PORTAL INFOGRAPHICS METHODS: includes source country restrictions.
     */

    String candidatesCondition = " and c.id in (:candidateIds)";
    String notTestCandidateCondition =
        " and c.id NOT IN (select candidate_id from candidate_saved_list" +
        " where saved_list_id = (select id from saved_list where name = 'TestCandidates' and global = true))";
    String countingStandardFilter =
        " u.status = 'active' and c.status != 'draft'" + notTestCandidateCondition;
    String dateConditionFilter = " and u.created_date >= (:dateFrom) and u.created_date <= (:dateTo)";

    //Stats that are not based on predefined candidate ids, should exclude ineligible.
    //(With candidate ids, it is up to the associated list or search to decide whether or not to
    // exclude ineligible)
    String excludeIneligible = " and c.status != 'ineligible'";

    //Note that I have been forced to go to native queries for these more
    //complex queries. The non native queries seem a bit buggy.
    //Anyway - I couldn't get them working. Simpler to use normal SQL. JC.

    /***************************************************************************
     Count By Birth Year
     **************************************************************************/
    String countByBirthYearSelectSQL =
            "select cast(extract(year from dob) as bigint) as year, " +
                    " count(distinct c) as PeopleCount" +
                    " from candidate c left join users u on c.user_id = u.id" +
                    " where c.country_id in (:sourceCountryIds)" +
                    " and " + countingStandardFilter + dateConditionFilter +
                    " and gender like :gender" +
                    " and dob is not null and extract(year from dob) > 1940 ";
    String countByBirthYearGroupBySQL = " group by year order by year asc";
    @Query(value = countByBirthYearSelectSQL + excludeIneligible +
            countByBirthYearGroupBySQL, nativeQuery = true)
    List<Object[]> countByBirthYearOrderByYear(@Param("gender") String gender,
                                               @Param("sourceCountryIds") List<Long> sourceCountryIds,
                                               @Param("dateFrom") LocalDate dateFrom,
                                               @Param("dateTo") LocalDate dateTo);

    @Query(value = countByBirthYearSelectSQL + candidatesCondition +
            countByBirthYearGroupBySQL, nativeQuery = true)
    List<Object[]> countByBirthYearOrderByYear(@Param("gender") String gender,
                                               @Param("sourceCountryIds") List<Long> sourceCountryIds,
                                               @Param("dateFrom") LocalDate dateFrom,
                                               @Param("dateTo") LocalDate dateTo,
                                               @Param("candidateIds") Set<Long> candidateIds);


    /***************************************************************************
     Count By Created Date
     **************************************************************************/
    String countByCreatedDateSelectSQL = "select DATE(u.created_date), count(distinct u.id) as PeopleCount from users u " +
            "left join candidate c on u.id = c.user_id " +
            "where c.country_id in (:sourceCountryIds) " + dateConditionFilter;
    String countByCreatedDateGroupBySQL = "group by DATE(u.created_date) " +
            "order by DATE(u.created_date) asc;";
    @Query(value = countByCreatedDateSelectSQL + excludeIneligible +
            countByCreatedDateGroupBySQL, nativeQuery = true)
    List<Object[]> countByCreatedDateOrderByCount(@Param("sourceCountryIds") List<Long> sourceCountryIds,
                                                  @Param("dateFrom") LocalDate dateFrom,
                                                  @Param("dateTo") LocalDate dateTo);

    @Query(value = countByCreatedDateSelectSQL + candidatesCondition +
            countByCreatedDateGroupBySQL, nativeQuery = true)
    List<Object[]> countByCreatedDateOrderByCount(@Param("sourceCountryIds") List<Long> sourceCountryIds,
        @Param("dateFrom") LocalDate dateFrom,
        @Param("dateTo") LocalDate dateTo,
                                                  @Param("candidateIds") Set<Long> candidateIds);

    String countLinkedInByCreatedDateSelectSQL = countByCreatedDateSelectSQL +
    " and linked_in_link is not null";
    @Query(value = countLinkedInByCreatedDateSelectSQL + excludeIneligible +
            countByCreatedDateGroupBySQL, nativeQuery = true)
    List<Object[]> countLinkedInByCreatedDateOrderByCount(@Param("sourceCountryIds") List<Long> sourceCountryIds,
                                                  @Param("dateFrom") LocalDate dateFrom,
                                                  @Param("dateTo") LocalDate dateTo);

    @Query(value = countLinkedInByCreatedDateSelectSQL + candidatesCondition +
            countByCreatedDateGroupBySQL, nativeQuery = true)
    List<Object[]> countLinkedInByCreatedDateOrderByCount(@Param("sourceCountryIds") List<Long> sourceCountryIds,
        @Param("dateFrom") LocalDate dateFrom,
        @Param("dateTo") LocalDate dateTo,
                                                  @Param("candidateIds") Set<Long> candidateIds);


    /***************************************************************************
        Count By Gender
     **************************************************************************/
    String countByGenderSelectSQL =
            "select gender, count(distinct c) as PeopleCount" +
            " from candidate c left join users u on c.user_id = u.id" +
            " where c.country_id in (:sourceCountryIds)" +
                    " and " + countingStandardFilter + dateConditionFilter;
    String countByGenderGroupBySQL =
                    " group by gender order by PeopleCount desc";

    @Query(value = countByGenderSelectSQL + excludeIneligible +
            countByGenderGroupBySQL, nativeQuery = true)
    List<Object[]> countByGenderOrderByCount(
            @Param("sourceCountryIds") List<Long> sourceCountryIds,
            @Param("dateFrom") LocalDate dateFrom,
            @Param("dateTo") LocalDate dateTo);

    @Query(value = countByGenderSelectSQL + candidatesCondition +
            countByGenderGroupBySQL, nativeQuery = true)
    List<Object[]> countByGenderOrderByCount(
            @Param("sourceCountryIds") List<Long> sourceCountryIds,
        @Param("dateFrom") LocalDate dateFrom,
        @Param("dateTo") LocalDate dateTo,
            @Param("candidateIds") Set<Long> candidateIds);

    /***************************************************************************
        Count By Unhcr Registered
     **************************************************************************/
    String countByUnhcrRegisteredSelectSQL =
            "select case" +
                    " when unhcr_status = 'NotRegistered' then 'No'" +
                    " when unhcr_status = 'RegisteredAsylum' then 'Yes'" +
                    " when unhcr_status = 'MandateRefugee' then 'Yes'" +
                    " when unhcr_status = 'RegisteredStateless' then 'Yes'" +
                    " when unhcr_status = 'RegisteredStatusUnknown' then 'Yes'" +
                    " when unhcr_status = 'Unsure' then 'Unsure'" +
                    " when unhcr_status = 'NoResponse' then 'NoResponse'" +
                    " else 'NoResponse' end as UNHCRRegistered," +
            " count(distinct c) as PeopleCount" +
            " from candidate c left join users u on c.user_id = u.id" +
            " where c.country_id in (:sourceCountryIds)" +
                    " and " + countingStandardFilter + dateConditionFilter;
    String countByUnhcrRegisteredGroupBySQL =
                    " group by UNHCRRegistered order by PeopleCount desc";

    @Query(value = countByUnhcrRegisteredSelectSQL + excludeIneligible +
            countByUnhcrRegisteredGroupBySQL, nativeQuery = true)
    List<Object[]> countByUnhcrRegisteredOrderByCount(
            @Param("sourceCountryIds") List<Long> sourceCountryIds,
            @Param("dateFrom") LocalDate dateFrom,
            @Param("dateTo") LocalDate dateTo);

    @Query(value = countByUnhcrRegisteredSelectSQL + candidatesCondition +
            countByUnhcrRegisteredGroupBySQL, nativeQuery = true)
    List<Object[]> countByUnhcrRegisteredOrderByCount(
            @Param("sourceCountryIds") List<Long> sourceCountryIds,
        @Param("dateFrom") LocalDate dateFrom,
        @Param("dateTo") LocalDate dateTo,
            @Param("candidateIds") Set<Long> candidateIds);

    /***************************************************************************
        Count By Unhcr Status
     **************************************************************************/
    String countByUnhcrStatusSelectSQL =
            "select unhcr_status, count(distinct c) as PeopleCount" +
            " from candidate c left join users u on c.user_id = u.id" +
            " where c.country_id in (:sourceCountryIds)" +
                    " and " + countingStandardFilter + dateConditionFilter +
                " and unhcr_status is not null";
    String countByUnhcrStatusGroupBySQL =
                    " group by unhcr_status order by PeopleCount desc";

    @Query(value = countByUnhcrStatusSelectSQL + excludeIneligible +
            countByUnhcrStatusGroupBySQL, nativeQuery = true)
    List<Object[]> countByUnhcrStatusOrderByCount(
            @Param("sourceCountryIds") List<Long> sourceCountryIds,
            @Param("dateFrom") LocalDate dateFrom,
            @Param("dateTo") LocalDate dateTo);

    @Query(value = countByUnhcrStatusSelectSQL + candidatesCondition +
            countByUnhcrStatusGroupBySQL, nativeQuery = true)
    List<Object[]> countByUnhcrStatusOrderByCount(
            @Param("sourceCountryIds") List<Long> sourceCountryIds,
        @Param("dateFrom") LocalDate dateFrom,
        @Param("dateTo") LocalDate dateTo,
            @Param("candidateIds") Set<Long> candidateIds);

    /***************************************************************************
        Count By LinkedIn
     **************************************************************************/
    String countByLinkedInSelectSQL =
            "select case when " +
            " linked_in_link is not null then 'Has link' else 'No link' end as haslink," +
            " count(distinct c) as PeopleCount" +
            " from candidate c left join users u on c.user_id = u.id" +
            " where c.country_id in (:sourceCountryIds)" +
                    " and " + countingStandardFilter + dateConditionFilter;
    String countByLinkedInGroupBySQL =
                    " group by haslink order by PeopleCount desc";

    @Query(value = countByLinkedInSelectSQL + excludeIneligible +
            countByLinkedInGroupBySQL, nativeQuery = true)
    List<Object[]> countByLinkedInExistsOrderByCount(
            @Param("sourceCountryIds") List<Long> sourceCountryIds,
            @Param("dateFrom") LocalDate dateFrom,
            @Param("dateTo") LocalDate dateTo);

    @Query(value = countByLinkedInSelectSQL + candidatesCondition +
            countByLinkedInGroupBySQL, nativeQuery = true)
    List<Object[]> countByLinkedInExistsOrderByCount(
            @Param("sourceCountryIds") List<Long> sourceCountryIds,
        @Param("dateFrom") LocalDate dateFrom,
        @Param("dateTo") LocalDate dateTo,
            @Param("candidateIds") Set<Long> candidateIds);


    /***************************************************************************
     Count By Language
     **************************************************************************/
    String countByLanguageSelectSQL = "select l.name, count(distinct c) as PeopleCount" +
            " from candidate c left join users u on c.user_id = u.id" +
            " left join candidate_language cl on c.id = cl.candidate_id" +
            " left join language l on cl.language_id = l.id" +
            " where c.country_id in (:sourceCountryIds)" +
            " and " + countingStandardFilter + dateConditionFilter +
            " and gender like :gender";
    String countByLanguageGroupBySQL = " group by l.name order by PeopleCount desc";
    @Query(value = countByLanguageSelectSQL + excludeIneligible +
            countByLanguageGroupBySQL, nativeQuery = true)
    List<Object[]> countByLanguageOrderByCount(@Param("gender") String gender,
                                               @Param("sourceCountryIds") List<Long> sourceCountryIds,
                                               @Param("dateFrom") LocalDate dateFrom,
                                               @Param("dateTo") LocalDate dateTo);
    @Query(value = countByLanguageSelectSQL + candidatesCondition +
            countByLanguageGroupBySQL, nativeQuery = true)
    List<Object[]> countByLanguageOrderByCount(@Param("gender") String gender,
                                               @Param("sourceCountryIds") List<Long> sourceCountryIds,
        @Param("dateFrom") LocalDate dateFrom,
        @Param("dateTo") LocalDate dateTo,
                                               @Param("candidateIds") Set<Long> candidateIds);


    /***************************************************************************
     Count By Max Education
     **************************************************************************/
    String countByMaxEducationSelectSQL = "select case when max_education_level_id is null then 'Unknown' " +
            "else el.name end as EducationLevel, " +
            "       count(distinct user_id) as PeopleCount " +
            "from candidate c left join users u on c.user_id = u.id " +
            "left join education_level el on c.max_education_level_id = el.id " +
            " where c.country_id in (:sourceCountryIds)" +
            " and " + countingStandardFilter + dateConditionFilter +
            " and gender like :gender ";
    String countByMaxEducationGroupBySQL = "group by EducationLevel " +
            "order by PeopleCount desc;";
    @Query(value = countByMaxEducationSelectSQL + excludeIneligible +
            countByMaxEducationGroupBySQL, nativeQuery = true)
    List<Object[]> countByMaxEducationLevelOrderByCount(@Param("gender") String gender,
                                                        @Param("sourceCountryIds") List<Long> sourceCountryIds,
                                                        @Param("dateFrom") LocalDate dateFrom,
                                                        @Param("dateTo") LocalDate dateTo);
    @Query(value = countByMaxEducationSelectSQL + candidatesCondition +
            countByMaxEducationGroupBySQL, nativeQuery = true)
    List<Object[]> countByMaxEducationLevelOrderByCount(@Param("gender") String gender,
                                                        @Param("sourceCountryIds") List<Long> sourceCountryIds,
        @Param("dateFrom") LocalDate dateFrom,
        @Param("dateTo") LocalDate dateTo,
                                                        @Param("candidateIds") Set<Long> candidateIds);

    /***************************************************************************
     Count By Most Common Occupation
     **************************************************************************/
    String countByMostCommonOccupationSelectSQL = "select o.name, " +
            "       count(distinct c) as PeopleCount " +
            "from candidate c left join users u on c.user_id = u.id " +
            "left join candidate_occupation co on c.id = co.candidate_id " +
            "left join occupation o on co.occupation_id = o.id " +
            " where c.country_id in (:sourceCountryIds)" +
            " and " + countingStandardFilter + dateConditionFilter +
            " and gender like :gender " +
            "and not lower(o.name) in ('undefined', 'unknown')";
    String countByMostCommonOccupationGroupBySQL = "group by o.name " +
            "order by PeopleCount desc;";
    /**
     * This is the same as countByOccupationOrderByCount except that it excludes
     * undefined or unknown occupations (which unfortunately are common)
     * @param gender Gender filter or % if all genders
     * @return List of occupation name and count
     */
    @Query(value = countByMostCommonOccupationSelectSQL + excludeIneligible +
            countByMostCommonOccupationGroupBySQL, nativeQuery = true)
    List<Object[]> countByMostCommonOccupationOrderByCount(@Param("gender") String gender,
                                                           @Param("sourceCountryIds") List<Long> sourceCountryIds,
                                                           @Param("dateFrom") LocalDate dateFrom,
                                                           @Param("dateTo") LocalDate dateTo);
    @Query(value = countByMostCommonOccupationSelectSQL + candidatesCondition +
            countByMostCommonOccupationGroupBySQL, nativeQuery = true)
    List<Object[]> countByMostCommonOccupationOrderByCount(@Param("gender") String gender,
                                                           @Param("sourceCountryIds") List<Long> sourceCountryIds,
        @Param("dateFrom") LocalDate dateFrom,
        @Param("dateTo") LocalDate dateTo,
                                                           @Param("candidateIds") Set<Long> candidateIds);

    /***************************************************************************
     Count By Nationality
     **************************************************************************/
    String countByNationalitySelectSQL = "select n.name, count(distinct c) as PeopleCount" +
            " from candidate c left join users u on c.user_id = u.id" +
            " left join country n on c.nationality_id = n.id " +
            " left join country on c.country_id = country.id " +
            " where c.country_id in (:sourceCountryIds) " +
            " and " + countingStandardFilter + dateConditionFilter +
            " and gender like :gender" +
            " and lower(country.name) like :country";
    String countByNationalityGroupBySQL = " group by n.name order by PeopleCount desc";
    @Query(value = countByNationalitySelectSQL + excludeIneligible +
            countByNationalityGroupBySQL, nativeQuery = true)
    List<Object[]> countByNationalityOrderByCount(@Param("gender") String gender,
                                                  @Param("country") String country,
                                                  @Param("sourceCountryIds") List<Long> sourceCountryIds,
                                                  @Param("dateFrom") LocalDate dateFrom,
                                                  @Param("dateTo") LocalDate dateTo);
    @Query(value = countByNationalitySelectSQL + candidatesCondition +
            countByNationalityGroupBySQL, nativeQuery = true)
    List<Object[]> countByNationalityOrderByCount(@Param("gender") String gender,
                                                  @Param("country") String country,
                                                  @Param("sourceCountryIds") List<Long> sourceCountryIds,
        @Param("dateFrom") LocalDate dateFrom,
        @Param("dateTo") LocalDate dateTo,
                                                  @Param("candidateIds") Set<Long> candidateIds);

    /***************************************************************************
     Count By Source Country
     **************************************************************************/
    String countBySourceCountrySelectSQL = "select sc.name, count(distinct c) as PeopleCount" +
            " from candidate c left join users u on c.user_id = u.id" +
            " left join country sc on c.country_id = sc.id " +
            " left join country on c.country_id = country.id " +
            " where c.country_id in (:sourceCountryIds) " +
            " and " + countingStandardFilter + dateConditionFilter +
            " and gender like :gender";
    String countBySourceCountryGroupBySQL = " group by sc.name order by PeopleCount desc";
    @Query(value = countBySourceCountrySelectSQL + excludeIneligible +
            countBySourceCountryGroupBySQL, nativeQuery = true)
    List<Object[]> countBySourceCountryOrderByCount(@Param("gender") String gender,
                                                    @Param("sourceCountryIds") List<Long> sourceCountryIds,
                                                    @Param("dateFrom") LocalDate dateFrom,
                                                    @Param("dateTo") LocalDate dateTo);
    @Query(value = countBySourceCountrySelectSQL + candidatesCondition +
            countBySourceCountryGroupBySQL, nativeQuery = true)
    List<Object[]> countBySourceCountryOrderByCount(@Param("gender") String gender,
                                                    @Param("sourceCountryIds") List<Long> sourceCountryIds,
                                                    @Param("dateFrom") LocalDate dateFrom,
                                                    @Param("dateTo") LocalDate dateTo,
                                                    @Param("candidateIds") Set<Long> candidateIds);



    /***************************************************************************
     Count By Occupation/Gender
     **************************************************************************/
    String countByOccupationGenderSelectSQL = "select o.name, " +
            "       count(distinct c) as PeopleCount " +
            "from candidate c left join users u on c.user_id = u.id " +
            "left join candidate_occupation co on c.id = co.candidate_id " +
            "left join occupation o on co.occupation_id = o.id " +
            " where c.country_id in (:sourceCountryIds)" +
            " and " + countingStandardFilter + dateConditionFilter +
            " and gender like :gender ";
    String countByOccupationGenderGroupBySQL = "group by o.name " +
            "order by PeopleCount desc;";
    @Query(value = countByOccupationGenderSelectSQL + excludeIneligible +
            countByOccupationGenderGroupBySQL, nativeQuery = true)
    List<Object[]> countByOccupationOrderByCount(@Param("gender") String gender,
                                                 @Param("sourceCountryIds") List<Long> sourceCountryIds,
                                                 @Param("dateFrom") LocalDate dateFrom,
                                                 @Param("dateTo") LocalDate dateTo);
    @Query(value = countByOccupationGenderSelectSQL + candidatesCondition +
            countByOccupationGenderGroupBySQL, nativeQuery = true)
    List<Object[]> countByOccupationOrderByCount(@Param("gender") String gender,
                                                 @Param("sourceCountryIds") List<Long> sourceCountryIds,
        @Param("dateFrom") LocalDate dateFrom,
        @Param("dateTo") LocalDate dateTo,
                                                 @Param("candidateIds") Set<Long> candidateIds);



    /***************************************************************************
     Count By Occupation
     **************************************************************************/
    String countByOccupationSelectSQL = "select o.name, " +
            "       count(distinct c) as PeopleCount " +
            "from candidate c left join users u on c.user_id = u.id " +
            "left join candidate_occupation co on c.id = co.candidate_id " +
            "left join occupation o on co.occupation_id = o.id " +
            " where c.country_id in (:sourceCountryIds)" +
            " and " + countingStandardFilter + dateConditionFilter;
    String countByOccupationGroupBySQL = " group by o.name " +
            "order by PeopleCount desc;";
    @Query(value = countByOccupationSelectSQL + excludeIneligible +
            countByOccupationGroupBySQL, nativeQuery = true)
    List<Object[]> countByOccupationOrderByCount(@Param("sourceCountryIds") List<Long> sourceCountryIds,
                                                 @Param("dateFrom") LocalDate dateFrom,
                                                 @Param("dateTo") LocalDate dateTo);
    @Query(value = countByOccupationSelectSQL + candidatesCondition +
            countByOccupationGroupBySQL, nativeQuery = true)
    List<Object[]> countByOccupationOrderByCount(@Param("sourceCountryIds") List<Long> sourceCountryIds,
        @Param("dateFrom") LocalDate dateFrom,
        @Param("dateTo") LocalDate dateTo,
                                                 @Param("candidateIds") Set<Long> candidateIds);


    /***************************************************************************
     Count By Spoken Language
     **************************************************************************/
    String countBySpokenLanguageSelectSQL = "select ll.name, count(distinct c) as PeopleCount" +
            " from candidate c left join users u on c.user_id = u.id" +
            " left join candidate_language cl on c.id = cl.candidate_id" +
            " left join language l on cl.language_id = l.id" +
            " left join language_level ll on cl.spoken_level_id = ll.id" +
            " where c.country_id in (:sourceCountryIds)" +
            " and " + countingStandardFilter + dateConditionFilter +
            " and gender like :gender" +
            " and lower(l.name) = lower(:language)";
    String countBySpokenLanguageGroupBySQL = " group by ll.name order by PeopleCount desc";
    @Query(value = countBySpokenLanguageSelectSQL + excludeIneligible +
            countBySpokenLanguageGroupBySQL, nativeQuery = true)
    List<Object[]> countBySpokenLanguageLevelByCount(@Param("gender") String gender,
                                                     @Param("language") String language,
                                                     @Param("sourceCountryIds") List<Long> sourceCountryIds,
                                                     @Param("dateFrom") LocalDate dateFrom,
                                                     @Param("dateTo") LocalDate dateTo);
    @Query(value = countBySpokenLanguageSelectSQL + candidatesCondition +
            countBySpokenLanguageGroupBySQL, nativeQuery = true)
    List<Object[]> countBySpokenLanguageLevelByCount(@Param("gender") String gender,
                                                     @Param("language") String language,
                                                     @Param("sourceCountryIds") List<Long> sourceCountryIds,
        @Param("dateFrom") LocalDate dateFrom,
        @Param("dateTo") LocalDate dateTo,
                                                     @Param("candidateIds") Set<Long> candidateIds);


    /***************************************************************************
     Count By Survey
     **************************************************************************/
    String countBySurveySelectSQL = "select s.name, count(distinct c) as PeopleCount" +
            " from candidate c left join users u on c.user_id = u.id" +
            " left join survey_type s on c.survey_type_id = s.id " +
            " left join country on c.country_id = country.id " +
            " where c.country_id in (:sourceCountryIds)" +
            " and " + countingStandardFilter + dateConditionFilter +
            " and gender like :gender" +
            " and lower(country.name) like :country";
    String countBySurveyGroupBySQL = " group by s.name order by PeopleCount desc";
    @Query(value = countBySurveySelectSQL + excludeIneligible +
            countBySurveyGroupBySQL, nativeQuery = true)
    List<Object[]> countBySurveyOrderByCount(@Param("gender") String gender,
                                             @Param("country") String country,
                                             @Param("sourceCountryIds") List<Long> sourceCountryIds,
                                             @Param("dateFrom") LocalDate dateFrom,
                                             @Param("dateTo") LocalDate dateTo);
    @Query(value = countBySurveySelectSQL + candidatesCondition +
            countBySurveyGroupBySQL, nativeQuery = true)
    List<Object[]> countBySurveyOrderByCount(@Param("gender") String gender,
                                             @Param("country") String country,
                                             @Param("sourceCountryIds") List<Long> sourceCountryIds,
        @Param("dateFrom") LocalDate dateFrom,
        @Param("dateTo") LocalDate dateTo,
                                             @Param("candidateIds") Set<Long> candidateIds);

    /***************************************************************************
     Count By Status
     **************************************************************************/
    String countByStatusSelectSQL = "select c.status, count(distinct c) as PeopleCount" +
            " from candidate c left join users u on c.user_id = u.id" +
            " left join country on c.country_id = country.id " +
            " where c.country_id in (:sourceCountryIds)" +
            " and " + countingStandardFilter + dateConditionFilter +
            " and gender like :gender" +
            " and lower(country.name) like :country";
    String countByStatusGroupBySQL = " group by c.status order by PeopleCount desc";
    @Query(value = countByStatusSelectSQL + excludeIneligible +
            countByStatusGroupBySQL, nativeQuery = true)
    List<Object[]> countByStatusOrderByCount(@Param("gender") String gender,
                                             @Param("country") String country,
                                             @Param("sourceCountryIds") List<Long> sourceCountryIds,
                                             @Param("dateFrom") LocalDate dateFrom,
                                             @Param("dateTo") LocalDate dateTo);
    @Query(value = countByStatusSelectSQL + candidatesCondition +
            countByStatusGroupBySQL, nativeQuery = true)
    List<Object[]> countByStatusOrderByCount(@Param("gender") String gender,
                                             @Param("country") String country,
                                             @Param("sourceCountryIds") List<Long> sourceCountryIds,
                                             @Param("dateFrom") LocalDate dateFrom,
                                             @Param("dateTo") LocalDate dateTo,
                                             @Param("candidateIds") Set<Long> candidateIds);

    /***************************************************************************
     Count By Referrer
     **************************************************************************/
    String countByReferrerSelectSQL = "select c.rego_referrer_param, count(distinct c) as PeopleCount" +
            " from candidate c left join users u on c.user_id = u.id" +
            " left join country on c.country_id = country.id " +
            " where c.country_id in (:sourceCountryIds)" +
            " and " + countingStandardFilter + dateConditionFilter +
            " and rego_referrer_param is not null" +
            " and gender like :gender" +
            " and lower(country.name) like :country";
    String countByReferrerGroupBySQL = " group by c.rego_referrer_param order by PeopleCount desc";
    @Query(value = countByReferrerSelectSQL + excludeIneligible +
            countByReferrerGroupBySQL, nativeQuery = true)
    List<Object[]> countByReferrerOrderByCount(@Param("gender") String gender,
                                             @Param("country") String country,
                                             @Param("sourceCountryIds") List<Long> sourceCountryIds,
                                             @Param("dateFrom") LocalDate dateFrom,
                                             @Param("dateTo") LocalDate dateTo);
    @Query(value = countByReferrerSelectSQL + candidatesCondition +
            countByReferrerGroupBySQL, nativeQuery = true)
    List<Object[]> countByReferrerOrderByCount(@Param("gender") String gender,
                                             @Param("country") String country,
                                             @Param("sourceCountryIds") List<Long> sourceCountryIds,
                                             @Param("dateFrom") LocalDate dateFrom,
                                             @Param("dateTo") LocalDate dateTo,
                                             @Param("candidateIds") Set<Long> candidateIds);

    // CANDIDATE CHAT
    // These methods are used to find candidates and check read statuses for the
    // [Partner] Candidate Chats tab.

    /**
     * Returns IDs of Job Chats of type 'CandidateProspect' for candidates managed by the logged-in
     * user's partner organisation, if they contain posts unread by same user.
     * @param partnerId logged-in user's partner org's ID
     * @param userId logged in user's ID
     * @return list of IDs of Job Chats matching the criteria
     */
    @Query(
        value =
        """
        SELECT chats.id
        FROM (
            SELECT job_chat.id
            FROM candidate
            JOIN job_chat ON candidate.id = job_chat.candidate_id
                AND job_chat.type = 'CandidateProspect'
            WHERE candidate.id IN (
                SELECT candidate.id
                FROM candidate
                JOIN users ON candidate.user_id = users.id
                WHERE users.partner_id = :partnerId
            )
        ) AS chats
        WHERE (
            (SELECT last_read_post_id
             FROM job_chat_user
             WHERE job_chat_id = chats.id
               AND user_id = :userId
            ) < (
                SELECT MAX(id)
                FROM chat_post
                WHERE job_chat_id = chats.id
            )
        )
        OR (
            (SELECT last_read_post_id
             FROM job_chat_user
             WHERE job_chat_id = chats.id
               AND user_id = :userId
            ) IS NULL
            AND (
                SELECT COUNT(*)
                FROM chat_post
                WHERE job_chat_id = chats.id
            ) > 0
        )
        """, nativeQuery = true
    )
    List<Long> findUnreadChatsInCandidates(
        @Param("partnerId") long partnerId,
        @Param("userId") long userId
    );

    /**
     * Paged search finding candidates managed by the logged-in user's partner organisation, if
     * they have a Job Chat of type 'CandidateProspect' containing at least one post.
     * @param partnerId logged-in user's partner org's ID
     * @param keyword pre-processed keyword entered in search filter, may represent candidate
     *                number, first name or last name of candidate
     * @param pageable details of requested pagination
     * @return paged search results of matching candidates
     */
    @Query(
        value =
            """
            SELECT c FROM Candidate c
             JOIN c.user u
             JOIN JobChat jc ON c.id = jc.candidate.id
             JOIN ChatPost cp ON jc.id = cp.jobChat.id
             WHERE u.partner.id = :partnerId
             AND jc.type = 'CandidateProspect'
             AND (:keyword IS NULL
                 OR LOWER(u.firstName) LIKE :keyword
                 OR LOWER(u.lastName) LIKE :keyword
                 OR c.candidateNumber LIKE :keyword)
             GROUP BY c.id
            """
    )
    Page<Candidate> findCandidatesWithActiveChat(
        @Param("partnerId") long partnerId,
        @Param("keyword") String keyword,
        Pageable pageable
    );

    /**
     * Search finding IDs of candidates managed by the logged-in user's partner organisation, if they
     * have a Job Chat of type 'CandidateProspect' containing at least one post that is unread by
     * the logged-in user.
     * Being a complex query, this was not suitable for JPQL so is instead written in regular SQL,
     * requiring return of IDs rather than the whole candidate object.
     * @param partnerId logged-in user's partner org's ID
     * @param userId logged-in user's ID
     * @param keyword pre-processed keyword entered in search filter, may represent candidate
     *                number, first name or last name of candidate
     * @return list of IDs of candidates who match the criteria
     */
    @Query(
        value =
            """
            SELECT DISTINCT candidate.id
            FROM candidate
            JOIN users ON candidate.user_id = users.id
            JOIN job_chat jc ON jc.candidate_id = candidate.id AND jc.type = 'CandidateProspect'
            LEFT JOIN job_chat_user jcu ON jcu.job_chat_id = jc.id AND jcu.user_id = :userId
            JOIN chat_post cp ON cp.job_chat_id = jc.id
            WHERE users.partner_id = :partnerId
            AND (
                :keyword IS NULL
                OR LOWER(users.first_name) LIKE :keyword
                OR LOWER(users.last_name) LIKE :keyword
                OR candidate.candidate_number LIKE :keyword
            )
            AND (
                -- Case 1: If last_read_post_id is less than the latest chat_post id
                (jcu.last_read_post_id < (SELECT max(cp2.id) FROM chat_post cp2 WHERE cp2.job_chat_id = jc.id))
                OR
                -- Case 2: If last_read_post_id is NULL and there are chat_posts in the chat
                (jcu.last_read_post_id IS NULL AND (SELECT count(*) FROM chat_post cp2 WHERE cp2.job_chat_id = jc.id) > 0)
            )
            """, nativeQuery = true
    )
    List<Long> findIdsOfCandidatesWithActiveAndUnreadChat(
        @Param("partnerId") long partnerId,
        @Param("userId") long userId,
        @Param("keyword") String keyword
    );

    /**
     * Takes a collection of candidate IDs and returns their corresponding candidates.
     * @param candidateIds any Collection of candidate IDs
     * @param pageable details of requested pagination
     * @return paged search result of candidates who match the criteria
     */
    Page<Candidate> findByIdIn(Collection<Long> candidateIds, Pageable pageable);

    //TODO JC Doc
    @Query(value = "select * from candidate where id in :idsSql", nativeQuery = true)
    Page<Candidate> findByIdIn(@Param("idsSql") String idsSql, Pageable pageable);

    @Query(
        value =
            """
            WITH Duplicates AS (
            SELECT
                c.id AS candidate_id,
                u.first_name,
                u.last_name,
                c.dob,
                COUNT(*) OVER (PARTITION BY u.first_name, u.last_name, c.dob) AS dup_count
            FROM candidate c
                INNER JOIN users u ON c.user_id = u.id
            WHERE c.status IN ('active', 'unreachable', 'incomplete', 'pending')
                AND u.first_name IS NOT NULL
                AND u.last_name IS NOT NULL
                AND c.dob IS NOT NULL
                AND (
                    :potentialDuplicate IS NULL
                    OR c.potential_duplicate = :potentialDuplicate
                    )
            )
            SELECT
                candidate_id
            FROM Duplicates
            WHERE dup_count > 1
            ORDER BY first_name, last_name, dob;
            """, nativeQuery = true
    )
    List<Long> findIdsOfPotentialDuplicateCandidates(
        @Nullable @Param("potentialDuplicate") Boolean potentialDuplicate
    );

    @Query("SELECT c.id FROM Candidate c WHERE c.potentialDuplicate = true")
    List<Long> findIdsOfCandidatesMarkedPotentialDuplicates();

    @Query(
        value =
            """
            SELECT c FROM Candidate c
              JOIN c.user u
            WHERE c.status IN :statuses
              AND LOWER(u.lastName) LIKE :lastName
              AND LOWER(u.firstName) LIKE :firstName
              AND c.dob = :dob
              AND c.id != :id
            """)
    List<Candidate> findPotentialDuplicatesOfGivenCandidate(
        @Param("statuses") List<CandidateStatus> statuses,
        @Param("dob") LocalDate dob,
        @Param("lastName") String lastName,
        @Param("firstName") String firstName,
        @Param("id") long id
    );

}
