/*
 * Copyright (c) 2021 Talent Beyond Boundaries.
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

package org.tbbtalent.server.service.db.impl;

import com.opencsv.CSVWriter;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClientException;
import org.tbbtalent.server.configuration.GoogleDriveConfig;
import org.tbbtalent.server.exception.CountryRestrictionException;
import org.tbbtalent.server.exception.EntityExistsException;
import org.tbbtalent.server.exception.EntityReferencedException;
import org.tbbtalent.server.exception.ExportFailedException;
import org.tbbtalent.server.exception.InvalidRequestException;
import org.tbbtalent.server.exception.InvalidSessionException;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.exception.PasswordMatchException;
import org.tbbtalent.server.exception.SalesforceException;
import org.tbbtalent.server.exception.UsernameTakenException;
import org.tbbtalent.server.model.db.Candidate;
import org.tbbtalent.server.model.db.CandidateDestination;
import org.tbbtalent.server.model.db.CandidateEducation;
import org.tbbtalent.server.model.db.CandidateExam;
import org.tbbtalent.server.model.db.CandidateLanguage;
import org.tbbtalent.server.model.db.CandidateOccupation;
import org.tbbtalent.server.model.db.CandidateProperty;
import org.tbbtalent.server.model.db.CandidateStatus;
import org.tbbtalent.server.model.db.CandidateSubfolderType;
import org.tbbtalent.server.model.db.Country;
import org.tbbtalent.server.model.db.DataRow;
import org.tbbtalent.server.model.db.DependantRelations;
import org.tbbtalent.server.model.db.EducationLevel;
import org.tbbtalent.server.model.db.Exam;
import org.tbbtalent.server.model.db.Gender;
import org.tbbtalent.server.model.db.HasTcQueryParameters;
import org.tbbtalent.server.model.db.LanguageLevel;
import org.tbbtalent.server.model.db.Occupation;
import org.tbbtalent.server.model.db.PartnerImpl;
import org.tbbtalent.server.model.db.QuestionTaskAssignmentImpl;
import org.tbbtalent.server.model.db.Role;
import org.tbbtalent.server.model.db.RootRequest;
import org.tbbtalent.server.model.db.SavedList;
import org.tbbtalent.server.model.db.SavedSearch;
import org.tbbtalent.server.model.db.SearchJoin;
import org.tbbtalent.server.model.db.Status;
import org.tbbtalent.server.model.db.SurveyType;
import org.tbbtalent.server.model.db.TaskAssignmentImpl;
import org.tbbtalent.server.model.db.UnhcrStatus;
import org.tbbtalent.server.model.db.UploadTaskImpl;
import org.tbbtalent.server.model.db.User;
import org.tbbtalent.server.model.db.YesNoUnsure;
import org.tbbtalent.server.model.db.partner.Partner;
import org.tbbtalent.server.model.db.partner.SourcePartner;
import org.tbbtalent.server.model.db.task.QuestionTask;
import org.tbbtalent.server.model.db.task.QuestionTaskAssignment;
import org.tbbtalent.server.model.db.task.Task;
import org.tbbtalent.server.model.db.task.TaskAssignment;
import org.tbbtalent.server.model.es.CandidateEs;
import org.tbbtalent.server.model.sf.Contact;
import org.tbbtalent.server.repository.db.CandidateExamRepository;
import org.tbbtalent.server.repository.db.CandidateRepository;
import org.tbbtalent.server.repository.db.CountryRepository;
import org.tbbtalent.server.repository.db.EducationLevelRepository;
import org.tbbtalent.server.repository.db.GetSavedListCandidatesQuery;
import org.tbbtalent.server.repository.db.LanguageLevelRepository;
import org.tbbtalent.server.repository.db.OccupationRepository;
import org.tbbtalent.server.repository.db.SurveyTypeRepository;
import org.tbbtalent.server.repository.db.TaskAssignmentRepository;
import org.tbbtalent.server.repository.db.UserRepository;
import org.tbbtalent.server.repository.es.CandidateEsRepository;
import org.tbbtalent.server.request.LoginRequest;
import org.tbbtalent.server.request.candidate.BaseCandidateContactRequest;
import org.tbbtalent.server.request.candidate.CandidateEmailOrPhoneSearchRequest;
import org.tbbtalent.server.request.candidate.CandidateEmailSearchRequest;
import org.tbbtalent.server.request.candidate.CandidateExternalIdSearchRequest;
import org.tbbtalent.server.request.candidate.CandidateIntakeDataUpdate;
import org.tbbtalent.server.request.candidate.CandidateNumberOrNameSearchRequest;
import org.tbbtalent.server.request.candidate.CreateCandidateRequest;
import org.tbbtalent.server.request.candidate.RegisterCandidateRequest;
import org.tbbtalent.server.request.candidate.ResolveTaskAssignmentsRequest;
import org.tbbtalent.server.request.candidate.SavedListGetRequest;
import org.tbbtalent.server.request.candidate.SearchCandidateRequest;
import org.tbbtalent.server.request.candidate.SearchJoinRequest;
import org.tbbtalent.server.request.candidate.UpdateCandidateAdditionalInfoRequest;
import org.tbbtalent.server.request.candidate.UpdateCandidateContactRequest;
import org.tbbtalent.server.request.candidate.UpdateCandidateEducationRequest;
import org.tbbtalent.server.request.candidate.UpdateCandidateLinksRequest;
import org.tbbtalent.server.request.candidate.UpdateCandidateMediaRequest;
import org.tbbtalent.server.request.candidate.UpdateCandidatePersonalRequest;
import org.tbbtalent.server.request.candidate.UpdateCandidateRegistrationRequest;
import org.tbbtalent.server.request.candidate.UpdateCandidateRequest;
import org.tbbtalent.server.request.candidate.UpdateCandidateShareableNotesRequest;
import org.tbbtalent.server.request.candidate.UpdateCandidateStatusInfo;
import org.tbbtalent.server.request.candidate.UpdateCandidateStatusRequest;
import org.tbbtalent.server.request.candidate.UpdateCandidateSurveyRequest;
import org.tbbtalent.server.request.note.CreateCandidateNoteRequest;
import org.tbbtalent.server.security.AuthService;
import org.tbbtalent.server.security.PasswordHelper;
import org.tbbtalent.server.service.db.CandidateCitizenshipService;
import org.tbbtalent.server.service.db.CandidateDependantService;
import org.tbbtalent.server.service.db.CandidateDestinationService;
import org.tbbtalent.server.service.db.CandidateNoteService;
import org.tbbtalent.server.service.db.CandidatePropertyService;
import org.tbbtalent.server.service.db.CandidateSavedListService;
import org.tbbtalent.server.service.db.CandidateService;
import org.tbbtalent.server.service.db.CandidateVisaJobCheckService;
import org.tbbtalent.server.service.db.CandidateVisaService;
import org.tbbtalent.server.service.db.CountryService;
import org.tbbtalent.server.service.db.FileSystemService;
import org.tbbtalent.server.service.db.PartnerService;
import org.tbbtalent.server.service.db.RootRequestService;
import org.tbbtalent.server.service.db.SalesforceService;
import org.tbbtalent.server.service.db.SavedListService;
import org.tbbtalent.server.service.db.SavedSearchService;
import org.tbbtalent.server.service.db.TaskService;
import org.tbbtalent.server.service.db.UserService;
import org.tbbtalent.server.service.db.email.EmailHelper;
import org.tbbtalent.server.service.db.util.PdfHelper;
import org.tbbtalent.server.util.BeanHelper;
import org.tbbtalent.server.util.filesystem.GoogleFileSystemDrive;
import org.tbbtalent.server.util.filesystem.GoogleFileSystemFolder;
import org.tbbtalent.server.util.html.TextExtracter;


/**
 * This is the lowest level service relating to managing candidates.
 * The "higher level" services are:
 * <ul>
 *     <li>{@link SavedListService}</li>
 *     <li>{@link CandidateSavedListService}</li>
 *     <li>{@link SavedSearchService}</li>
 * </ul>
 * To avoid Spring Boot not starting up due to circular dependencies between services, it is
 * important that this service implementation does not add any dependence on the above
 * services.
 * <p/>
 * Currently the dependencies look like this:
 * <p/>
 * SavedSearchService depends on CandidateSavedListService, SavedListService and CandidateService
 * <p/>
 * CandidateSavedListService depends on SavedListService and CandidateService
 * <p/>
 * SavedListService depends on CandidateService
 */
@Service
public class CandidateServiceImpl implements CandidateService {

    private static final int afghanistanCountryId = 6180;
    private static final int ukraineCountryId = 6406;

    private static final Logger log = LoggerFactory.getLogger(CandidateServiceImpl.class);

    private static final Map<CandidateSubfolderType, String> candidateSubfolderNames;
    private static final String NOT_AUTHORIZED = "Hidden";

    static {
        candidateSubfolderNames = new HashMap<>();
        candidateSubfolderNames.put(CandidateSubfolderType.address, "Address");
        candidateSubfolderNames.put(CandidateSubfolderType.character, "Character");
        candidateSubfolderNames.put(CandidateSubfolderType.employer, "Employer");
        candidateSubfolderNames.put(CandidateSubfolderType.engagement, "Engagement");
        candidateSubfolderNames.put(CandidateSubfolderType.experience, "Experience");
        candidateSubfolderNames.put(CandidateSubfolderType.family, "Family");
        candidateSubfolderNames.put(CandidateSubfolderType.identity, "Identity");
        candidateSubfolderNames.put(CandidateSubfolderType.immigration, "Immigration");
        candidateSubfolderNames.put(CandidateSubfolderType.language, "Language");
        candidateSubfolderNames.put(CandidateSubfolderType.medical, "Medical");
        candidateSubfolderNames.put(CandidateSubfolderType.qualification, "Qualification");
        candidateSubfolderNames.put(CandidateSubfolderType.registration, "Registration");
    }

    private final UserRepository userRepository;
    private final UserService userService;
    private final CandidateRepository candidateRepository;
    private final CandidateEsRepository candidateEsRepository;
    private final FileSystemService fileSystemService;
    private final GoogleDriveConfig googleDriveConfig;
    private final SalesforceService salesforceService;
    private final CountryRepository countryRepository;
    private final CountryService countryService;
    private final EducationLevelRepository educationLevelRepository;
    private final PasswordHelper passwordHelper;
    private final AuthService authService;
    private final CandidateNoteService candidateNoteService;
    private final CandidateCitizenshipService candidateCitizenshipService;
    private final CandidateVisaService candidateVisaService;
    private final CandidateVisaJobCheckService candidateVisaJobCheckService;
    private final CandidateDependantService candidateDependantService;
    private final CandidateDestinationService candidateDestinationService;
    private final CandidatePropertyService candidatePropertyService;
    private final SurveyTypeRepository surveyTypeRepository;
    private final OccupationRepository occupationRepository;
    private final PartnerService partnerService;
    private final LanguageLevelRepository languageLevelRepository;
    private final CandidateExamRepository candidateExamRepository;

    private final RootRequestService rootRequestService;
    private final TaskAssignmentRepository taskAssignmentRepository;
    private final TaskService taskService;
    private final EmailHelper emailHelper;
    private final PdfHelper pdfHelper;
    private final TextExtracter textExtracter;

    @Autowired
    public CandidateServiceImpl(UserRepository userRepository,
        UserService userService,
        CandidateRepository candidateRepository,
        CandidateEsRepository candidateEsRepository,
        FileSystemService fileSystemService,
        GoogleDriveConfig googleDriveConfig,
        SalesforceService salesforceService,
        CountryRepository countryRepository,
        CountryService countryService,
        EducationLevelRepository educationLevelRepository,
        PasswordHelper passwordHelper,
        AuthService authService,
        CandidateNoteService candidateNoteService,
        CandidateCitizenshipService candidateCitizenshipService,
        CandidateDependantService candidateDependantService,
        CandidateDestinationService candidateDestinationService,
        CandidateVisaService candidateVisaService,
        CandidateVisaJobCheckService candidateVisaJobCheckService,
        CandidatePropertyService candidatePropertyService,
        SurveyTypeRepository surveyTypeRepository,
        OccupationRepository occupationRepository,
        PartnerService partnerService,
        LanguageLevelRepository languageLevelRepository,
        CandidateExamRepository candidateExamRepository,
        RootRequestService rootRequestService, TaskAssignmentRepository taskAssignmentRepository,
        TaskService taskService, EmailHelper emailHelper,
        PdfHelper pdfHelper, TextExtracter textExtracter) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.candidateRepository = candidateRepository;
        this.candidateEsRepository = candidateEsRepository;
        this.googleDriveConfig = googleDriveConfig;
        this.countryRepository = countryRepository;
        this.countryService = countryService;
        this.educationLevelRepository = educationLevelRepository;
        this.passwordHelper = passwordHelper;
        this.authService = authService;
        this.candidateNoteService = candidateNoteService;
        this.candidateCitizenshipService = candidateCitizenshipService;
        this.candidateDependantService = candidateDependantService;
        this.candidateDestinationService = candidateDestinationService;
        this.candidateVisaService = candidateVisaService;
        this.candidateVisaJobCheckService = candidateVisaJobCheckService;
        this.candidatePropertyService = candidatePropertyService;
        this.surveyTypeRepository = surveyTypeRepository;
        this.occupationRepository = occupationRepository;
        this.partnerService = partnerService;
        this.languageLevelRepository = languageLevelRepository;
        this.candidateExamRepository = candidateExamRepository;
        this.rootRequestService = rootRequestService;
        this.taskAssignmentRepository = taskAssignmentRepository;
        this.taskService = taskService;
        this.emailHelper = emailHelper;
        this.pdfHelper = pdfHelper;
        this.fileSystemService = fileSystemService;
        this.salesforceService = salesforceService;
        this.textExtracter = textExtracter;
    }

    @Transactional
    @Override
    public int populateElasticCandidates(
            Pageable pageable, boolean logTotal, boolean createElastic) {
        Page<Candidate> candidates = candidateRepository.findCandidatesWhereStatusNotDeleted(pageable);
        if (logTotal) {
            log.info(candidates.getTotalElements() + " candidates to be processed.");
        }

        int count = 0;
        for (Candidate candidate : candidates) {
            try {
                if (createElastic) {
                    CandidateEs ces = new CandidateEs();
                    ces.copy(candidate, textExtracter);
                    ces = candidateEsRepository.save(ces);

                    //Update textSearchId on candidate.
                    String textSearchId = ces.getId();
                    candidate.setTextSearchId(textSearchId);
                    save(candidate, false);
                } else {
                    //This also handles all the awkward cases - such as
                    //links to non existent proxies - creating them as needed.
                    updateElasticProxy(candidate);
                }

                count++;
            } catch (Exception ex) {
                log.warn("Could not load candidate " + candidate.getId(), ex);
            }
        }

        return count;
    }

    @Transactional
    @Override
    public int populateCandidatesFromElastic(Pageable pageable) {
        Page<Candidate> candidates = candidateRepository.findCandidatesWhereStatusNotDeleted(pageable);

        int count = 0;
        for (Candidate candidate : candidates) {
            try {
                CandidateEs twin;
                String textSearchId = candidate.getTextSearchId();
                if (textSearchId != null) {
                    twin = candidateEsRepository.findById(textSearchId)
                            .orElse(null);
                    if (twin != null) {
                        //Get the desired field from twin and save to candidate
                        if (twin.getUnhcrStatus() == UnhcrStatus.NotRegistered) {
                            candidate.setUnhcrRegistered(YesNoUnsure.No);
                            candidate.setUnhcrStatus(twin.getUnhcrStatus());
                            save(candidate, false);
                            log.warn("Updated candidate " + candidate.getId() + " with Not Registered status to Not Registered and UnhcrRegistered is No!");
                        } else if (twin.getUnhcrStatus() == UnhcrStatus.RegisteredAsylum) {
                            candidate.setUnhcrRegistered(YesNoUnsure.Yes);
                            save(candidate, false);
                            log.warn("Updated candidate " + candidate.getId() + " with Registered Asylum status to UnhcrRegistered is Yes!");
                        }
                    } else {
                        log.warn("Could not find twin in database");
                    }
                }
                count++;
            } catch (Exception ex) {
                log.warn("Could not load candidate " + candidate.getId(), ex);
            }
        }

        return count;
    }

    @Override
    public Page<Candidate> getSavedListCandidates(long savedListId, SavedListGetRequest request) {
        Page<Candidate> candidatesPage = candidateRepository.findAll(
                new GetSavedListCandidatesQuery(savedListId, request), request.getPageRequestWithoutSort());
        log.info("Found " + candidatesPage.getTotalElements() + " candidates in list");
        return candidatesPage;
    }

    /**
     * Update audit fields and use repository to save the Candidate
     * @param candidate Entity to save
     */
    public void saveIt(Candidate candidate) {
        candidate.setAuditFields(authService.getLoggedInUser().orElse(null));
        save(candidate, true);
    }

    //todo this is horrible cloned code duplicated from SavedSearchServiceImpl.convertToSearchCandidateRequest - factor it out.
    private SearchCandidateRequest convertToSearchCandidateRequest(SavedSearch savedSearch) {
        SearchCandidateRequest searchCandidateRequest = new SearchCandidateRequest();
        searchCandidateRequest.setSavedSearchId(savedSearch.getId());
        searchCandidateRequest.setSimpleQueryString(savedSearch.getSimpleQueryString());
        searchCandidateRequest.setKeyword(savedSearch.getKeyword());
        searchCandidateRequest.setStatuses(getStatusListFromString(savedSearch.getStatuses()));
        searchCandidateRequest.setGender(savedSearch.getGender());
        searchCandidateRequest.setOccupationIds(getIdsFromString(savedSearch.getOccupationIds()));
        searchCandidateRequest.setMinYrs(savedSearch.getMinYrs());
        searchCandidateRequest.setMaxYrs(savedSearch.getMaxYrs());
        searchCandidateRequest.setVerifiedOccupationIds(getIdsFromString(savedSearch.getVerifiedOccupationIds()));
        searchCandidateRequest.setVerifiedOccupationSearchType(savedSearch.getVerifiedOccupationSearchType());
        searchCandidateRequest.setPartnerIds(getIdsFromString(savedSearch.getPartnerIds()));
        searchCandidateRequest.setNationalityIds(getIdsFromString(savedSearch.getNationalityIds()));
        searchCandidateRequest.setNationalitySearchType(savedSearch.getNationalitySearchType());
        searchCandidateRequest.setCountryIds(getIdsFromString(savedSearch.getCountryIds()));
        searchCandidateRequest.setCountrySearchType(savedSearch.getCountrySearchType());
        searchCandidateRequest.setSurveyTypeIds(getIdsFromString(savedSearch.getSurveyTypeIds()));
        searchCandidateRequest.setEnglishMinSpokenLevel(savedSearch.getEnglishMinSpokenLevel());
        searchCandidateRequest.setEnglishMinWrittenLevel(savedSearch.getEnglishMinWrittenLevel());
        searchCandidateRequest.setOtherLanguageId(savedSearch.getOtherLanguage() != null ? savedSearch.getOtherLanguage().getId() : null);
        searchCandidateRequest.setOtherMinSpokenLevel(savedSearch.getOtherMinSpokenLevel());
        searchCandidateRequest.setOtherMinWrittenLevel(savedSearch.getOtherMinWrittenLevel());
        searchCandidateRequest.setLastModifiedFrom(savedSearch.getLastModifiedFrom());
        searchCandidateRequest.setLastModifiedTo(savedSearch.getLastModifiedTo());
//        searchCandidateRequest.setRegisteredFrom(request.getCreatedFrom());
//        searchCandidateRequest.setRegisteredTo(request.getCreatedTo());
        searchCandidateRequest.setMinAge(savedSearch.getMinAge());
        searchCandidateRequest.setMaxAge(savedSearch.getMaxAge());
        searchCandidateRequest.setMinEducationLevel(savedSearch.getMinEducationLevel());
        searchCandidateRequest.setEducationMajorIds(getIdsFromString(savedSearch.getEducationMajorIds()));

        List<SearchJoinRequest> searchJoinRequests = new ArrayList<>();
        for (SearchJoin searchJoin : savedSearch.getSearchJoins()) {
            searchJoinRequests.add(new SearchJoinRequest(searchJoin.getChildSavedSearch().getId(), searchJoin.getChildSavedSearch().getName(), searchJoin.getSearchType()));
        }
        searchCandidateRequest.setSearchJoinRequests(searchJoinRequests);

        return searchCandidateRequest;

    }


    String getListAsString(List<Long> ids){
        return !org.springframework.util.CollectionUtils.isEmpty(ids) ? ids.stream().map(String::valueOf)
                .collect(Collectors.joining(",")) : null;
    }

    List<Long> getIdsFromString(String listIds){
        return listIds != null ? Stream.of(listIds.split(","))
                .map(Long::parseLong)
                .collect(Collectors.toList()) : null;
    }

    String getStatusListAsString(List<CandidateStatus> statuses){
        return !org.springframework.util.CollectionUtils.isEmpty(statuses) ? statuses.stream().map(String::valueOf)
                .collect(Collectors.joining(",")) : null;
    }

    List<CandidateStatus> getStatusListFromString(String statusList){
        return statusList != null ? Stream.of(statusList.split(","))
                .map(s -> CandidateStatus.valueOf(s))
                .collect(Collectors.toList()) : null;
    }

    @Override
    public Page<Candidate> searchCandidates(CandidateEmailSearchRequest request) {
        String s = request.getCandidateEmail();
        User loggedInUser = authService.getLoggedInUser()
                .orElseThrow(() -> new InvalidSessionException("Not logged in"));
        if (authService.hasAdminPrivileges(loggedInUser.getRole())) {
            Set<Country> sourceCountries = userService.getDefaultSourceCountries(loggedInUser);
            Page<Candidate> candidates;

            candidates = candidateRepository.searchCandidateEmail(
                    '%' + s +'%', sourceCountries, request.getPageRequestWithoutSort());

            log.info("Found " + candidates.getTotalElements() + " candidates in search");
            return candidates;
        } else {
            return null;
        }
    }

    @Override
    public Page<Candidate> searchCandidates(CandidateEmailOrPhoneSearchRequest request) {
        String s = request.getCandidateEmailOrPhone();
        User loggedInUser = authService.getLoggedInUser()
                .orElseThrow(() -> new InvalidSessionException("Not logged in"));

        boolean searchForNumber = s.length() > 0 && Character.isDigit(s.charAt(0));
        Set<Country> sourceCountries = userService.getDefaultSourceCountries(loggedInUser);
        Page<Candidate> candidates = candidateRepository.searchCandidateEmailOrPhone(
                '%' + s + '%', sourceCountries,
                        request.getPageRequestWithoutSort());

//        if (searchForNumber) {
//            candidates = candidateRepository.searchCandidateNumber(
//                    s +'%', sourceCountries,
//                    request.getPageRequestWithoutSort());
//        } else {
//            if (loggedInUser.getRole() == Role.admin || loggedInUser.getRole() == Role.sourcepartneradmin) {
//                candidates = candidateRepository.searchCandidateName(
//                        '%' + s + '%', sourceCountries,
//                        request.getPageRequestWithoutSort());
//            } else {
//                return null;
//            }
//        }

        log.info("Found " + candidates.getTotalElements() + " candidates in search");
        return candidates;
    }

    @Override
    public Page<Candidate> searchCandidates(CandidateNumberOrNameSearchRequest request) {
        String s = request.getCandidateNumberOrName();
        User loggedInUser = authService.getLoggedInUser()
                .orElseThrow(() -> new InvalidSessionException("Not logged in"));

        boolean searchForNumber = s.length() > 0 && Character.isDigit(s.charAt(0));
        Set<Country> sourceCountries = userService.getDefaultSourceCountries(loggedInUser);
        Page<Candidate> candidates;

        if (searchForNumber) {
            candidates = candidateRepository.searchCandidateNumber(
                        s +'%', sourceCountries,
                    request.getPageRequestWithoutSort());
        } else {
            if (authService.hasAdminPrivileges(loggedInUser.getRole())) {
                candidates = candidateRepository.searchCandidateName(
                        '%' + s + '%', sourceCountries,
                        request.getPageRequestWithoutSort());
            } else {
                return null;
            }
        }

        log.info("Found " + candidates.getTotalElements() + " candidates in search");
        return candidates;
    }

    @Override
    public Page<Candidate> searchCandidates(CandidateExternalIdSearchRequest request) {
        String s = request.getExternalId();
        User loggedInUser = authService.getLoggedInUser()
                .orElseThrow(() -> new InvalidSessionException("Not logged in"));

        if (authService.hasAdminPrivileges(loggedInUser.getRole())) {
            Set<Country> sourceCountries = userService.getDefaultSourceCountries(loggedInUser);
            Page<Candidate> candidates;

            candidates = candidateRepository.searchCandidateExternalId(
                    s +'%', sourceCountries, request.getPageRequestWithoutSort());

            log.info("Found " + candidates.getTotalElements() + " candidates in search");
            return candidates;
        } else {
            return null;
        }
    }

    @Override
    public Candidate addMissingDestinations(Candidate candidate) {
        //Check candidate's preferred destinations
        List<CandidateDestination> destinations = candidate.getCandidateDestinations();
        //Construct hashset of country ids for quick checking
        Set<Long> candidateDestinationCountryIds = new HashSet<>();
        for (CandidateDestination destination : destinations) {
            candidateDestinationCountryIds.add(destination.getCountry().getId());
        }

        //Check that all TBB destinations are present for candidate, adding
        //missing ones if necessary
        boolean addedDestinations = false;
        for (Country country : countryService.getTBBDestinations()) {
            //Does candidate have this destination preference?
            if (!candidateDestinationCountryIds.contains(country.getId())) {
                //If not, add in a new one
                CandidateDestination cd = new CandidateDestination();
                cd.setCountry(country);
                cd.setCandidate(candidate);
                destinations.add(cd);
                addedDestinations = true;
            }
        }

        if (addedDestinations) {
            candidate = save(candidate, false);
        }
        return candidate;
    }

    @Override
    public @NonNull Candidate getCandidate(long id) throws NoSuchObjectException {
        final Candidate candidate = candidateRepository.findById(id)
            .orElseThrow(() -> new NoSuchObjectException(Candidate.class, id));

        populateTransientTaskAssignmentFields(candidate.getTaskAssignments());

        return candidate;
    }

    /**
     * Sets transient fields on the given task assignments.
     * @param taskAssignments Task assignments
     */
    private void populateTransientTaskAssignmentFields(List<TaskAssignmentImpl> taskAssignments) {
        for (TaskAssignmentImpl taskAssignment : taskAssignments) {
            populateTransientTaskAssignmentFields(taskAssignment);
        }
    }

    private void populateTransientTaskAssignmentFields(TaskAssignment taskAssignment) {

        taskService.populateTransientFields(taskAssignment.getTask());

        //If task is completed, see if there is any transient data to be populated - eg the
        //answer on a question task
        if (taskAssignment.getCompletedDate() != null) {
            if (taskAssignment instanceof QuestionTaskAssignmentImpl) {
                QuestionTaskAssignment qta = (QuestionTaskAssignmentImpl) taskAssignment;
                String answer = fetchCandidateTaskAnswer(qta);
                qta.setAnswer(answer);
            }
        }

    }

    /**
     * Retrieves the answer, if any, of the give question task assignment.
     * @param questionTaskAssignment Question task assignment
     * @return Candidate's answer to the question
     * @throws NoSuchObjectException if the answer could not be retrieved because the answer has
     * been specified as being located in a non existent candidate field or property.
     * @see #storeCandidateTaskAnswer
     */
    @Nullable
    private String fetchCandidateTaskAnswer(QuestionTaskAssignment questionTaskAssignment)
        throws NoSuchObjectException {
        String answer;
        Task task = questionTaskAssignment.getTask();
        if (task instanceof QuestionTask) {
            String answerField = ((QuestionTask) task).getCandidateAnswerField();
            Candidate candidate = questionTaskAssignment.getCandidate();
            if (answerField == null) {
                //Get answer from candidate property
                String propertyName = task.getName();
                final CandidateProperty property =
                    candidatePropertyService.findProperty(candidate, propertyName);
                answer = property != null ? property.getValue() : null;
            } else {
                //Get answer from candidate field

                //TODO JC This is a bit of a hack - we need a better way of processing things like candidateExams
                String candidateExamsFieldName = "candidateExams";
                if (answerField.startsWith(candidateExamsFieldName + ".")) {
                    //Special code for candidate exams
                    //Extract the exam name from the answerField
                    String examName = answerField.substring(candidateExamsFieldName.length() + 1);
                    Exam examType = Exam.valueOf(examName);

                    //See if we already have an entry for this exam
                    Optional<CandidateExam> examO = Optional.empty();
                    final List<CandidateExam> candidateExams = candidate.getCandidateExams();
                    if (candidateExams != null) {
                        examO = candidateExams.stream().filter(
                            e -> e.getExam().equals(examType)).findFirst();
                    }

                    //Fetch the answer
                    CandidateExam exam;
                    if (examO.isPresent()) {
                        exam = examO.get();
                        answer = exam.getScore();
                    } else {
                        answer = null;
                    }
                } else {
                    try {
                        Object value = PropertyUtils.getProperty(candidate, answerField);
                        answer = value != null ? value.toString() : null;
                    } catch (IllegalAccessException e) {
                        throw new InvalidRequestException("Unable to access '" + answerField
                            + "' field of candidate");
                    } catch (InvocationTargetException e) {
                        throw new InvalidRequestException("Error while accessing '" + answerField
                            + "' field of candidate");
                    } catch (NoSuchMethodException e) {
                        throw new NoSuchObjectException(
                            "Answer not found to " + task.getDisplayName()
                                + ". No such candidate field: " + answerField);
                    }
                }
            }
        } else {
            throw new InvalidRequestException("Task is not a QuestionTask: " + task.getName());
        }

        return answer;
    }

    @Override
    public Candidate getTestCandidate() {
        //TODO JC Hack for the moment
        long id = 32156L;
        return candidateRepository.findById(id)
            .orElseThrow(() -> new NoSuchObjectException(Candidate.class, id));
    }

    private Candidate createCandidate(CreateCandidateRequest request, SourcePartner partner, String ipAddress,
        HasTcQueryParameters queryParameters, String passwordEncrypted)
        throws UsernameTakenException {
        User user = new User(
                StringUtils.isNotBlank(request.getUsername()) ? request.getUsername() : request.getEmail(),
                request.getFirstName(),
                request.getLastName(),
                request.getEmail(),
                Role.user);

        User existing = userRepository.findByUsernameAndRole(user.getUsername(), Role.user);
        if (existing != null) {
            throw new UsernameTakenException("A user already exists with username: " + existing.getUsername());
        }

        //Add partner
        user.setPartner((PartnerImpl) partner);

        /* Set the password */
        user.setPasswordEnc(passwordEncrypted);

        //Save the user
        user = userRepository.save(user);

        Candidate candidate = new Candidate(user, request.getPhone(), request.getWhatsapp(), user);
        candidate.setCandidateNumber("TEMP%04d" + RandomStringUtils.random(6));

        candidate.setRegoIp(ipAddress);
        if (queryParameters != null) {
            candidate.setRegoPartnerParam(queryParameters.getPartnerAbbreviation());
            candidate.setRegoReferrerParam(queryParameters.getReferrerParam());
            candidate.setRegoUtmCampaign(queryParameters.getUtmCampaign());
            candidate.setRegoUtmContent(queryParameters.getUtmContent());
            candidate.setRegoUtmMedium(queryParameters.getUtmMedium());
            candidate.setRegoUtmSource(queryParameters.getUtmSource());
            candidate.setRegoUtmTerm(queryParameters.getUtmTerm());
        }

        //set some fields to unknown on create as required for search
        //see CandidateSpecification. It works better if these attributes are not null, but instead
        //point to an "Unknown" value.
        candidate.setCountry(countryRepository.getOne(0L));
        candidate.setNationality(countryRepository.getOne(0L));
        candidate.setMaxEducationLevel(educationLevelRepository.getOne(0L));

        //Save candidate to get id (but don't update Elasticsearch yet)
        candidate = save(candidate, false);

        //Use id to generate candidate number
        String candidateNumber = String.format("%04d", candidate.getId());
        candidate.setCandidateNumber(candidateNumber);

        //Set partner ref to candidate number if that is what partner uses to identify candidates
        if (partner != null && partner.isDefaultPartnerRef()) {
            candidate.setPartnerRef(candidateNumber);
        }

        //Now save again with candidateNumber, updating Elasticsearch
        candidate = save(candidate, true);

        return candidate;
    }

    @Override
    @Transactional
    public void updateCandidateStatus(UpdateCandidateStatusRequest request) {
        User loggedInUser = authService.getLoggedInUser()
                .orElseThrow(() -> new InvalidSessionException("Not logged in"));

        Set<Country> sourceCountries = userService.getDefaultSourceCountries(loggedInUser);

        UpdateCandidateStatusInfo info = request.getInfo();

        //Update status for all given ids
        Collection<Long> ids = request.getCandidateIds();
        for (Long id : ids) {
            Candidate candidate = this.candidateRepository.findByIdLoadUser(id, sourceCountries)
                .orElse(null);
            if (candidate == null) {
                log.error("updateCandidateStatus: No candidate exists for id " + id);
            } else {
                updateCandidateStatus(candidate, info);
            }
        }
    }

    @Override
    public Candidate updateCandidateSalesforceLink(Candidate candidate, String sfLink) {
        candidate.setSflink(sfLink);
        return save(candidate, false);
    }

    /**
     * Updates a candidate's status, creates note and sends registration or incomplete application email.
     * @param candidate The candidate that needs the status update
     * @param info Information regarding the candidate's status change, such as the new status, any message to candidate and comments for the note.
     * @return Updated Candidate object
     */
    public Candidate updateCandidateStatus(Candidate candidate, UpdateCandidateStatusInfo info) {
        CandidateStatus originalStatus = candidate.getStatus();
        candidate.setStatus(info.getStatus());
        candidate.setCandidateMessage(info.getCandidateMessage());
        candidate = save(candidate, true);
        if (!info.getStatus().equals(originalStatus)) {
            candidateNoteService.createCandidateNote(new CreateCandidateNoteRequest(candidate.getId(),
                    "Status change from " + originalStatus + " to " + info.getStatus(),
                    info.getComment()));
            if (originalStatus.equals(CandidateStatus.draft) && !info.getStatus()
                    .equals(CandidateStatus.deleted)) {
                emailHelper.sendRegistrationEmail(candidate.getUser());
                log.info("Registration email sent to " + candidate.getUser().getEmail());
            }
            if (info.getStatus().equals(CandidateStatus.incomplete)) {
                emailHelper.sendIncompleteApplication(candidate.getUser(),
                        info.getCandidateMessage());
                log.info("Incomplete email sent to " + candidate.getUser().getEmail());
            }
        }

        //Keep user status in sync with candidate status.
        //When candidate status is deleted, user status should also be deleted.
        //When candidate status is not deleted, user status cannot be. Set it to active.
        User user = candidate.getUser();
        if (candidate.getStatus().equals(CandidateStatus.deleted)) {
            user.setStatus(Status.deleted);
            userRepository.save(user);
        } else {
            //Candidate status is not deleted - make sure that user status is also not deleted.
            if (Status.deleted.equals(user.getStatus())) {
                user.setStatus(Status.active);
                userRepository.save(user);
            }
        }



        return candidate;
    }

    @Override
    public void updateCandidateStatus(SavedList savedList, UpdateCandidateStatusInfo info) {
        UpdateCandidateStatusRequest ucsr = new UpdateCandidateStatusRequest();
        List<Long> candidateIds =
            savedList.getCandidates().stream().map(Candidate::getId).collect(Collectors.toList());
        ucsr.setCandidateIds(candidateIds);
        ucsr.setInfo(info);

        updateCandidateStatus(ucsr);
    }

    @Override
    public Candidate updateCandidateLinks(long id, UpdateCandidateLinksRequest request) {
        User loggedInUser = authService.getLoggedInUser()
                .orElseThrow(() -> new InvalidSessionException("Not logged in"));

        Set<Country> sourceCountries = userService.getDefaultSourceCountries(loggedInUser);
        Candidate candidate = this.candidateRepository.findByIdLoadUser(id, sourceCountries)
                .orElseThrow(() -> new NoSuchObjectException(Candidate.class, id));
        candidate.setSflink(request.getSflink());
        candidate.setFolderlink(request.getFolderlink());
        candidate.setVideolink(request.getVideolink());
        candidate.setLinkedInLink(request.getLinkedInLink());
        candidate = save(candidate, true);
        return candidate;
    }

    @Override
    public Candidate updateCandidate(long id, UpdateCandidateRequest request) {
        User loggedInUser = authService.getLoggedInUser()
                .orElseThrow(() -> new InvalidSessionException("Not logged in"));

        Set<Country> sourceCountries = userService.getDefaultSourceCountries(loggedInUser);
        Candidate candidate = this.candidateRepository.findByIdLoadUser(id, sourceCountries)
                .orElseThrow(() -> new NoSuchObjectException(Candidate.class, id));
        // Check update request for a duplicate email or phone number
        request.setId(id);
        validateContactRequest(candidate.getUser(), request);

        // Load the country from the database - throw an exception if not found
        Country country = countryRepository.findById(request.getCountryId())
                .orElseThrow(() -> new NoSuchObjectException(Country.class, request.getCountryId()));

        // Load the country from the database - throw an exception if not found
        Country nationality = countryRepository.findById(request.getNationalityId())
                .orElseThrow(() -> new NoSuchObjectException(Country.class, request.getNationalityId()));

        User user = candidate.getUser();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        userRepository.save(user);

        candidate.setUser(user);
        candidate.setDob(request.getDob());
        candidate.setGender(request.getGender());
        candidate.setPhone(request.getPhone());
        candidate.setWhatsapp(request.getWhatsapp());
        candidate.setAddress1(request.getAddress1());
        candidate.setCity(request.getCity());
        candidate.setState(request.getState());

        candidate.setCountry(country);
        checkForChangedPartner(candidate, country);

        candidate.setYearOfArrival(request.getYearOfArrival());
        candidate.setNationality(nationality);
        return save(candidate, true);
    }

    @Override
    public Candidate updateCandidateAdditionalInfo(long id, UpdateCandidateAdditionalInfoRequest request) {
        User loggedInUser = authService.getLoggedInUser()
                .orElseThrow(() -> new InvalidSessionException("Not logged in"));

        Set<Country> sourceCountries = userService.getDefaultSourceCountries(loggedInUser);
        Candidate candidate = this.candidateRepository.findByIdLoadUser(id, sourceCountries)
                .orElseThrow(() -> new NoSuchObjectException(Candidate.class, id));

        candidate.setAdditionalInfo(request.getAdditionalInfo());
        return save(candidate, true);
    }

    @Override
    public Candidate updateShareableNotes(long id, UpdateCandidateShareableNotesRequest request) {
        User loggedInUser = authService.getLoggedInUser()
            .orElseThrow(() -> new InvalidSessionException("Not logged in"));

        Set<Country> sourceCountries = userService.getDefaultSourceCountries(loggedInUser);
        Candidate candidate = this.candidateRepository.findByIdLoadUser(id, sourceCountries)
            .orElseThrow(() -> new NoSuchObjectException(Candidate.class, id));

        candidate.setShareableNotes(request.getShareableNotes());
        return save(candidate, true);
    }

    @Override
    public Candidate updateCandidateSurvey(long id, UpdateCandidateSurveyRequest request) {
        User loggedInUser = authService.getLoggedInUser()
                .orElseThrow(() -> new InvalidSessionException("Not logged in"));

        Set<Country> sourceCountries = userService.getDefaultSourceCountries(loggedInUser);
        Candidate candidate = this.candidateRepository.findByIdLoadUser(id, sourceCountries)
                .orElseThrow(() -> new NoSuchObjectException(Candidate.class, id));

        SurveyType surveyType = null;
        if (request.getSurveyTypeId() != null) {
            // Load the education level from the database - throw an exception if not found
            surveyType = surveyTypeRepository.findById(request.getSurveyTypeId())
                    .orElseThrow(() -> new NoSuchObjectException(EducationLevel.class, request.getSurveyTypeId()));
        }
        candidate.setSurveyType(surveyType);
        candidate.setSurveyComment(request.getSurveyComment());
        return save(candidate, true);
    }

    @Override
    public Candidate updateCandidateMedia(long id, UpdateCandidateMediaRequest request) {
        User loggedInUser = authService.getLoggedInUser()
                .orElseThrow(() -> new InvalidSessionException("Not logged in"));

        Set<Country> sourceCountries = userService.getDefaultSourceCountries(loggedInUser);
        Candidate candidate = this.candidateRepository.findByIdLoadUser(id, sourceCountries)
                .orElseThrow(() -> new NoSuchObjectException(Candidate.class, id));

        candidate.setMediaWillingness(request.getMediaWillingness());
        return save(candidate, true);
    }

    @Override
    public Candidate updateCandidateRegistration(long id, UpdateCandidateRegistrationRequest request) {
        User loggedInUser = authService.getLoggedInUser()
                .orElseThrow(() -> new InvalidSessionException("Not logged in"));

        Set<Country> sourceCountries = userService.getDefaultSourceCountries(loggedInUser);
        Candidate candidate = this.candidateRepository.findByIdLoadUser(id, sourceCountries)
                .orElseThrow(() -> new NoSuchObjectException(Candidate.class, id));

        candidate.setExternalId(request.getExternalId());
        candidate.setExternalIdSource(request.getExternalIdSource());
        candidate.setPartnerRef(request.getPartnerRef());
        candidate.setUnhcrStatus(request.getUnhcrStatus());
        candidate.setUnhcrConsent(request.getUnhcrConsent());
        candidate.setUnhcrNumber(request.getUnhcrNumber());
        candidate.setUnrwaRegistered(request.getUnrwaRegistered());
        candidate.setUnrwaNumber(request.getUnrwaNumber());
        return save(candidate, true);
    }

    @Override
    @Transactional
    public boolean deleteCandidate(long id) {
        Candidate candidate = candidateRepository.findById(id).orElse(null);
        if (candidate != null) {
            String textSearchId = candidate.getTextSearchId();
            candidateRepository.delete(candidate);
            if (textSearchId != null) {
                candidateEsRepository.deleteById(textSearchId);
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginRequest register(RegisterCandidateRequest request, HttpServletRequest httpRequest) {
        if (!request.getPassword().equals(request.getPasswordConfirmation())) {
            throw new PasswordMatchException();
        }

        // Check update request for a duplicate email or phone number
        validateContactRequest(null, request);

        /* Check for existing account with the username fields */
        if (StringUtils.isNotBlank(request.getUsername())) {
            User exists = userRepository.findByUsernameAndRole(request.getUsername(), Role.user);
            if (exists != null) {
                throw new UsernameTakenException("username");
            }
        }

        if (StringUtils.isBlank(request.getEmail())
                && StringUtils.isBlank(request.getPhone())
                && StringUtils.isBlank(request.getWhatsapp())) {
            throw new InvalidRequestException("Must specify at least one method of contact");
        }

        /* Validate the password before account creation */
        String passwordEncrypted = passwordHelper.validateAndEncodePassword(request.getPassword());

        //Fetch any recent Root Request by this candidate from their ip address.
        //Candidate may have been referred to our website by a partner with query parameters
        //identifying the partner as well as how the candidate was referred (in UTM parameters).
        //These parameters will have been stored under the candidate's ip address in RootRequest.
        //See RootRouteAdminApi.
        //(Note that we could have used the browser session id instead for looking up RootRequests
        //but it is useful to store the ip address anyway, so may as well use that rather than
        //adding session id as well to RootRequest and the database).
        String ipAddress = httpRequest.getRemoteAddr();
        //Ignore anything that is fairly old (eg 36 hours old) - ip addresses change over time.
        RootRequest rootRequest = rootRequestService.getMostRecentRootRequest(ipAddress, 36);

        //Compute and assign partner.
        //A non null partner abbreviation can define partner
        String partnerAbbreviation = request.getPartnerAbbreviation();
        if (partnerAbbreviation == null) {
            //See if partner info is available on RootRequest.
            if (rootRequest != null) {
                partnerAbbreviation = rootRequest.getPartnerAbbreviation();
            }
        }
        if (partnerAbbreviation != null) {
            log.info("Registration with partner abbreviation: " + partnerAbbreviation);
        }

        //Pick up query parameters from request if they are passed in
        HasTcQueryParameters queryParameters;
        if (areQueryParametersPresent(request)) {
            queryParameters = request;
        } else {
            queryParameters = rootRequest;
        }

        //Assign partner based on the partner abbreviation, if any
        SourcePartner sourcePartner = (SourcePartner) partnerService.getPartnerFromAbbreviation(partnerAbbreviation);
        if (sourcePartner == null) {
            //No source partner found based on partner query param.

            //HACK - Start - Hack for Alight, using referral query parameter if necessary to assign source partner
            //Gets around problem that registration link was shared with alight as referral query
            // param instead of partner query param.
            if (queryParameters != null) {
                String referral = queryParameters.getReferrerParam();
                if (referral != null) {
                    if ("alight".equalsIgnoreCase(referral)) {
                        //Referral was to alight - Set source partner to Alight
                        sourcePartner = (SourcePartner) partnerService.getPartnerFromAbbreviation(referral);
                    }
                }
            }
            //HACK - End

            if (sourcePartner == null) {
                //Use default partner.
                sourcePartner = partnerService.getDefaultSourcePartner();
            }
        }

        /* Create the candidate */
        CreateCandidateRequest createCandidateRequest = new CreateCandidateRequest();
        createCandidateRequest.setUsername(request.getUsername());
        createCandidateRequest.setEmail(request.getEmail());
        createCandidateRequest.setPhone(request.getPhone());
        createCandidateRequest.setWhatsapp(request.getWhatsapp());

        Candidate candidate = createCandidate(createCandidateRequest, sourcePartner, ipAddress,
            queryParameters, passwordEncrypted);

        /* Log the candidate in */
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername( candidate.getUser().getUsername());
        loginRequest.setPassword(request.getPassword());
        return loginRequest;
    }

    /**
     * Return true if any of the TC query parameters are set (not null)
     * @param queryParameters Query parameters
     * @return True if any query parameter is set
     */
    private boolean areQueryParametersPresent(HasTcQueryParameters queryParameters) {
        return queryParameters.getPartnerAbbreviation() != null ||
            queryParameters.getReferrerParam() != null ||
            queryParameters.getUtmCampaign() != null ||
            queryParameters.getUtmContent() != null ||
            queryParameters.getUtmMedium() != null ||
            queryParameters.getUtmSource() != null ||
            queryParameters.getUtmTerm() != null;
    }

    @Override
    public Candidate updateContact(UpdateCandidateContactRequest request) {
        User user = authService.getLoggedInUser()
                .orElseThrow(() -> new InvalidSessionException("Not logged in"));

        // Check update request for a duplicate email or phone number
        validateContactRequest(user, request);

        user.setEmail(request.getEmail());
        user = userRepository.save(user);
        Candidate candidate = user.getCandidate();
        candidate.setPhone(request.getPhone());
        candidate.setWhatsapp(request.getWhatsapp());
        candidate.setAuditFields(user);
        candidate.setUser(user);
        candidate = save(candidate, true);
        return candidate;
    }

    @Override
    public Candidate updatePersonal(UpdateCandidatePersonalRequest request) {
        // Load the country from the database - throw an exception if not found
        Country country = countryRepository.findById(request.getCountryId())
                .orElseThrow(() -> new NoSuchObjectException(Country.class, request.getCountryId()));

        // Load the nationality from the database - throw an exception if not found
        Country nationality = countryRepository.findById(request.getNationalityId())
                .orElseThrow(() -> new NoSuchObjectException(Country.class, request.getNationalityId()));

        User user = authService.getLoggedInUser()
                .orElseThrow(() -> new InvalidSessionException("Not logged in"));

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user = userRepository.save(user);
        Candidate candidate = candidateRepository.findByUserId(user.getId());

        String newStatus = checkStatusValidity(request.getCountryId(), request.getNationalityId(), candidate);

        if (candidate != null) {
            candidate.setGender(request.getGender());
            candidate.setDob(request.getDob());

            candidate.setCountry(country);
            checkForChangedPartner(candidate, country);

            candidate.setCity(request.getCity());
            candidate.setState(request.getState());
            candidate.setYearOfArrival(request.getYearOfArrival());
            candidate.setNationality(nationality);
            candidate.setExternalId(request.getExternalId());
            candidate.setExternalIdSource(request.getExternalIdSource());
            candidate.setUnhcrRegistered(request.getUnhcrRegistered());
            candidate.setUnhcrNumber(request.getUnhcrNumber());
            candidate.setUnhcrConsent(request.getUnhcrConsent());

            candidate.setAuditFields(user);
        }

        candidate = save(candidate, true);

        // Change status if required, and create note.
        if (newStatus.equals("ineligible")) {
            UpdateCandidateStatusInfo info = new UpdateCandidateStatusInfo();
            info.setStatus(CandidateStatus.ineligible);
            info.setComment("TC criteria not met: Country located is same as country of nationality.");
            candidate = updateCandidateStatus(candidate, info);
        } else if (newStatus.equals("pending")) {
            UpdateCandidateStatusInfo info = new UpdateCandidateStatusInfo();
            info.setStatus(CandidateStatus.pending);
            info.setComment("TC criteria met: Country located different to country of nationality.");
            candidate = updateCandidateStatus(candidate, info);
        }

        return candidate;
    }

    private void checkForChangedPartner(Candidate candidate, Country country) {
        //Do we have an auto assignable partner in this country
        Partner partner = partnerService.getAutoAssignablePartnerByCountry(country);
        User user = candidate.getUser();
        if (partner != null && !partner.equals(user.getPartner())) {
            //Partner of candidate needs to change
            user.setPartner((PartnerImpl) partner);
            userRepository.save(user);
        }
    }

    @Override
    public Candidate updateEducation(UpdateCandidateEducationRequest request) {
        Candidate candidate = getLoggedInCandidate()
                .orElseThrow(() -> new InvalidSessionException("Not logged in"));

        EducationLevel educationLevel = null;
        if (request.getMaxEducationLevelId() != null) {
            // Load the education level from the database - throw an exception if not found
            educationLevel = educationLevelRepository.findById(request.getMaxEducationLevelId())
                    .orElseThrow(() -> new NoSuchObjectException(EducationLevel.class, request.getMaxEducationLevelId()));
        }

        candidate.setMaxEducationLevel(educationLevel);
        candidate.setAuditFields(candidate.getUser());
        return save(candidate, true);
    }

    @Override
    public Candidate updateCandidateSurvey(UpdateCandidateSurveyRequest request) {
        Candidate candidate = getLoggedInCandidate()
                .orElseThrow(() -> new InvalidSessionException("Not logged in"));

        SurveyType surveyType = null;
        if (request.getSurveyTypeId() != null) {
            // Load the education level from the database - throw an exception if not found
            surveyType = surveyTypeRepository.findById(request.getSurveyTypeId())
                    .orElseThrow(() -> new NoSuchObjectException(EducationLevel.class, request.getSurveyTypeId()));
        }
        candidate.setSurveyType(surveyType);
        candidate.setSurveyComment(request.getSurveyComment());

        candidate.setAuditFields(candidate.getUser());
        return save(candidate, true);
    }

    @Override
    public Candidate updateAdditionalInfo(UpdateCandidateAdditionalInfoRequest request) {
        Candidate candidate = getLoggedInCandidate()
                .orElseThrow(() -> new InvalidSessionException("Not logged in"));
        candidate.setAdditionalInfo(request.getAdditionalInfo());
        if (request.getLinkedInLink() != null && !request.getLinkedInLink().isEmpty()) {
            String linkedInRegex = "^http[s]?:/\\/www\\.linkedin\\.com\\/in\\/[A-z0-9_-]+\\/?$";
            Pattern p = Pattern.compile(linkedInRegex);
            Matcher m = p.matcher(request.getLinkedInLink());
            if (m.find()) {
                candidate.setLinkedInLink(request.getLinkedInLink());
            } else {
                throw new InvalidRequestException("This is not a valid LinkedIn link.");
            }
        } else {
            candidate.setLinkedInLink(null);
        }
        candidate.setAuditFields(candidate.getUser());
        return save(candidate, true);
    }

    public void storeCandidateTaskAnswer(
        QuestionTaskAssignment questionTaskAssignment, String answer)
        throws InvalidRequestException {
        Task task = questionTaskAssignment.getTask();
        if (task instanceof QuestionTask) {
            String answerField = ((QuestionTask) task).getCandidateAnswerField();
            Candidate candidate = questionTaskAssignment.getCandidate();
            if (answerField == null) {
                //Store answer in a candidate property
                String propertyName = task.getName();
                candidatePropertyService.createOrUpdateProperty(
                    candidate, propertyName, answer, questionTaskAssignment);
            } else {
                //Store answer in the candidate field

                //TODO JC This is a bit of a hack - we need a better way of processing things like candidateExams
                String candidateExamsFieldName = "candidateExams";
                if (answerField.startsWith(candidateExamsFieldName + ".")) {
                    //Special code for storing to candidate exams
                    //Extract the exam name from the answerField
                    String examName = answerField.substring(candidateExamsFieldName.length() + 1);
                    Exam examType = Exam.valueOf(examName);

                    //See if we already have an entry for this exam
                    Optional<CandidateExam> examO = Optional.empty();
                    final List<CandidateExam> candidateExams = candidate.getCandidateExams();
                    if (candidateExams != null) {
                        examO = candidateExams.stream().filter(
                            e -> e.getExam().equals(examType)).findFirst();
                    }

                    //Get or create the CandidateExam object
                    CandidateExam exam;
                    if (examO.isPresent()) {
                        exam = examO.get();
                    } else {
                        //Create new exam
                        exam = new CandidateExam();
                        exam.setCandidate(candidate);
                        exam.setExam(examType);
                        exam.setYear((long) OffsetDateTime.now().getYear());
                    }

                    //Update existing exam result
                    exam.setScore(answer);

                    //Save the exam
                    candidateExamRepository.save(exam);
                } else {
                    Object answerValue = convertAnswerToCorrectType(answerField, answer);
                    try {
                        PropertyUtils.setProperty(candidate, answerField, answerValue);
                    } catch (IllegalAccessException e) {
                        throw new InvalidRequestException("Unable to access '" + answerField
                            + "' field of candidate");
                    } catch (InvocationTargetException e) {
                        throw new InvalidRequestException("Error while accessing '" + answerField
                            + "' field of candidate");
                    } catch (NoSuchMethodException e) {
                        throw new InvalidRequestException(
                            "Candidate field does not exist: '" + answerField
                                + "'");
                    }

                    save(candidate, true);
                }
            }
        } else {
            throw new InvalidRequestException("Task is not a QuestionTask: " + task.getName());
        }

        //Fetch answer back to capture any reformatting (eg answers stored as Enums, return their
        //display name) in the transient answer attached to the task assignment.
        answer = fetchCandidateTaskAnswer(questionTaskAssignment);

        questionTaskAssignment.setAnswer(answer);
    }

    /**
     * Converts the given String answer to the type expected by the given Candidate field.
     * This may be a String, in which case the given String answer is returned.
     * However, if it is an enum, it will be converted to the matching enum value.
     * @param answerField Field of Candidate object
     * @param answer String answer to be converted if needed
     * @return Object containing type version of String answer
     * @throws InvalidRequestException If we can't find info on the given field on the Candidate class.
     */
    private Object convertAnswerToCorrectType(String answerField, String answer)
        throws InvalidRequestException {
        PropertyDescriptor pd;
        try {
            pd = BeanHelper.getPropertyDescriptor(Candidate.class, answerField);
        } catch (IntrospectionException e) {
            throw new InvalidRequestException("Could not extract candidate field: " + answerField);
        }
        Object val;
        if (pd == null) {
            val = answer;
        } else {
            Class clz = pd.getPropertyType();
            // Need to cater for the different classes of the objects that are stored in the database and convert from
            // the string sent from the front end (e.g. Big Decimals, Integers, LocalDates). There may be some missing
            // classes, which will need to be added. - CC
            if (clz.isEnum()) {
                val = Enum.valueOf(clz, answer);
            } else if (clz.equals(BigDecimal.class)) {
                try {
                    val = new BigDecimal(answer);
                } catch (Exception e) {
                    throw new InvalidRequestException("Incorrect format, please enter in a numeric eg. 9 instead of nine");
                }
            } else if(clz.equals(Integer.class)) {
                try {
                    val = Integer.parseInt(answer);
                } catch (Exception e) {
                    throw new InvalidRequestException("Incorrect format, please enter in a numeric eg. 9 instead of nine");
                }
            } else if (clz.equals(LocalDate.class)) {
                try {
                    val = LocalDate.parse(answer);
                } catch (Exception e) {
                    throw new InvalidRequestException("Incorrect format, please enter in yyyy-mm-dd. eg. 2022-04-29");
                }
            } else if (clz.equals(String.class)) {
                val = answer;
            } else {
                // If we don't have the class type to convert the string to, it will throw this error.
                // A new class type may need to be added (as other else if's).
                throw new InvalidRequestException("Could not convert answer to candidate field: " + answerField);
            }
        }
        return val;
    }

    @Override
    public Candidate submitRegistration() {
        Candidate candidate = getLoggedInCandidate()
                .orElseThrow(() -> new InvalidSessionException("Not logged in"));
        // Don't update status to pending if status is already pending
        final CandidateStatus candidateStatus = candidate.getStatus();
        if (!candidateStatus.equals(CandidateStatus.pending)) {
            if (candidate.getNationality() != candidate.getCountry() ||
                    candidate.getCountry().getId() == afghanistanCountryId ||
                    candidate.getCountry().getId() == ukraineCountryId
            ) {
                UpdateCandidateStatusInfo info = new UpdateCandidateStatusInfo();

                //Only set status to pending if current status is draft. This addresses the case
                //where a candidate's status has been changed from draft (the normal status during
                //registration) by an admin while the candidate is still in the process of registering.
                //In that case we don't want to override the admin's status with pending once
                //the submission (at the end of registration) finally happens.
                if (candidateStatus.equals(CandidateStatus.draft)) {
                    info.setStatus(CandidateStatus.pending);
                } else {
                    info.setStatus(candidateStatus);
                }
                info.setComment("Candidate submitted");
                candidate = updateCandidateStatus(candidate, info);
            } else {
                UpdateCandidateStatusInfo info = new UpdateCandidateStatusInfo();
                info.setStatus(CandidateStatus.ineligible);
                info.setComment("TC criteria not met: Country located is same as country of nationality.");
                candidate = updateCandidateStatus(candidate, info);
            }
        }
        candidate.setAuditFields(candidate.getUser());
        return save(candidate, true);
    }

    @Override
    public Optional<Candidate> getLoggedInCandidateLoadCandidateOccupations() {
        Long candidateId = authService.getLoggedInCandidateId();
        if (candidateId == null) {
            return Optional.empty();
        } else {
            Candidate candidate = candidateRepository
                    .findByIdLoadCandidateOccupations(candidateId);
            return candidate == null ? Optional.empty() : Optional.of(candidate);
        }
    }

    @Override
    public Optional<Candidate> getLoggedInCandidateLoadCertifications() {
        Long candidateId = authService.getLoggedInCandidateId();
        if (candidateId == null) {
            return Optional.empty();
        } else {
            Candidate candidate = candidateRepository
                    .findByIdLoadCertifications(candidateId);
            return candidate == null ? Optional.empty() : Optional.of(candidate);
        }
    }

    @Override
    public Optional<Candidate> getLoggedInCandidateLoadCandidateLanguages() {
        Long candidateId = authService.getLoggedInCandidateId();
        if (candidateId == null) {
            return Optional.empty();
        } else {
            Candidate candidate = candidateRepository
                    .findByIdLoadCandidateLanguages(candidateId);
            return candidate == null ? Optional.empty() : Optional.of(candidate);
        }
    }

    @Override
    public Optional<Candidate> getLoggedInCandidate() {
        User user = authService.getLoggedInUser().orElse(null);
        if (user == null) {
            return Optional.empty();
        }
        Candidate candidate = candidateRepository.findByUserId(user.getId());

        populateTransientTaskAssignmentFields(candidate.getTaskAssignments());

        return Optional.of(candidate);
    }

    @Override
    public Candidate findByCandidateNumber(String candidateNumber) {
        return candidateRepository.findByCandidateNumber(candidateNumber);
    }

    @Override
    public List<Candidate> findByIds(@NonNull Collection<Long> ids) {
        return candidateRepository.findByIds(ids);
    }

    @Override
    public Candidate findByCandidateNumberRestricted(String candidateNumber) {
        User loggedInUser = authService.getLoggedInUser()
                .orElseThrow(() -> new InvalidSessionException("Not logged in"));

        Set<Country> sourceCountries = userService.getDefaultSourceCountries(loggedInUser);
        return candidateRepository.findByCandidateNumberRestricted(candidateNumber, sourceCountries)
                .orElseThrow(() -> new CountryRestrictionException("You don't have access to this candidate."));
    }

    @Override
    public Candidate findByIdLoadSavedLists(long candidateId) {
        return candidateRepository.findByIdLoadSavedLists(candidateId);
    }

    @Override
    public Candidate findByIdLoadUser(long id, Set<Country> sourceCountries) {
        return candidateRepository.findByIdLoadUser(id, sourceCountries).orElse(null);
    }

    @Transactional(readOnly = true)
    void validateContactRequest(User user, BaseCandidateContactRequest request) {
        // Check email not already taken
        if (!StringUtils.isBlank(request.getEmail())) {
            try {
                User exists = userRepository.findByEmailIgnoreCase(request.getEmail());
                if (user == null && exists != null || exists != null && !exists.getId().equals(user.getId())) {
                    throw new UsernameTakenException("email");
                }
            } catch (IncorrectResultSizeDataAccessException e) {
                throw new UsernameTakenException("email");
            }
        }
    }

    private static String countryStr(String country) {
        return country == null ? "%" : country;
    }

    private static String genderStr(Gender gender) {
        return gender == null ? "%" : gender.toString();
    }

    private static List<DataRow> toRows(List<Object[]> objects) {
        List<DataRow> dataRows = new ArrayList<>(objects.size());
        for (Object[] row: objects) {
            String label = row[0] == null ? "undefined" : row[0].toString();
            DataRow dataRow = new DataRow(label, (BigInteger)row[1]);
            dataRows.add(dataRow);
        }
        return dataRows;
    }

    @Override
    public List<DataRow> computeBirthYearStats(Gender gender, LocalDate dateFrom, LocalDate dateTo, List<Long> sourceCountryIds) {
        return toRows(candidateRepository.
                countByBirthYearOrderByYear(
                        genderStr(gender), sourceCountryIds, dateFrom, dateTo));
    }

    @Override
    public List<DataRow> computeBirthYearStats(Gender gender, LocalDate dateFrom, LocalDate dateTo, Set<Long> candidateIds, List<Long> sourceCountryIds) {
        return toRows(candidateRepository.
                countByBirthYearOrderByYear(
                        genderStr(gender), sourceCountryIds, dateFrom, dateTo, candidateIds));
    }

    @Override
    public List<DataRow> computeGenderStats(LocalDate dateFrom, LocalDate dateTo, List<Long> sourceCountryIds) {
        return toRows(candidateRepository.countByGenderOrderByCount(
                sourceCountryIds, dateFrom, dateTo));
    }

    @Override
    public List<DataRow> computeGenderStats(LocalDate dateFrom, LocalDate dateTo, Set<Long> candidateIds, List<Long> sourceCountryIds) {
        return toRows(candidateRepository.countByGenderOrderByCount(
                sourceCountryIds, dateFrom, dateTo, candidateIds));
    }

    /**
     * Computes and sets the given candidate's IeltsScore
     * @param candidate Candidate whose score needs to be computed
     */
    private void computeIeltsScore(Candidate candidate) {
        CandidateExam ieltsGen = candidate.getCandidateExams().stream()
            .filter(ce -> Objects.nonNull(ce.getExam()) && ce.getExam().equals(Exam.IELTSGen))
            .findAny().orElse(null);

        CandidateExam ieltsAca = candidate.getCandidateExams().stream()
            .filter(ce -> Objects.nonNull(ce.getExam()) && ce.getExam().equals(Exam.IELTSAca))
            .findAny().orElse(null);

        BigDecimal score;
        // Setting Ielts Score in order of Ielts General, Ielts Academic, Ielts Estimated.
        if (ieltsGen != null && ieltsGen.getScore() != null) {
            score = new BigDecimal(ieltsGen.getScore());
        } else if (ieltsAca != null && ieltsAca.getScore() != null) {
            score = new BigDecimal(ieltsAca.getScore());
        } else if (candidate.getLangAssessmentScore() != null) {
            score = new BigDecimal(candidate.getLangAssessmentScore());
        } else {
            score = null;
        }
        candidate.setIeltsScore(score);
    }

    @Override
    public List<DataRow> computeUnhcrRegisteredStats(LocalDate dateFrom, LocalDate dateTo,
        List<Long> sourceCountryIds) {
        return toRows(candidateRepository.countByUnhcrRegisteredOrderByCount(
            sourceCountryIds, dateFrom, dateTo));
    }

    @Override
    public List<DataRow> computeUnhcrRegisteredStats(LocalDate dateFrom, LocalDate dateTo,
        Set<Long> candidateIds, List<Long> sourceCountryIds) {
        return toRows(candidateRepository.countByUnhcrRegisteredOrderByCount(
            sourceCountryIds, dateFrom, dateTo, candidateIds));
    }

    @Override
    public List<DataRow> computeUnhcrStatusStats(LocalDate dateFrom, LocalDate dateTo, List<Long> sourceCountryIds) {
        return toRows(candidateRepository.countByUnhcrStatusOrderByCount(
            sourceCountryIds, dateFrom, dateTo));
    }

    @Override
    public List<DataRow> computeUnhcrStatusStats(LocalDate dateFrom, LocalDate dateTo,
        Set<Long> candidateIds, List<Long> sourceCountryIds) {
        return toRows(candidateRepository.countByUnhcrStatusOrderByCount(
            sourceCountryIds, dateFrom, dateTo, candidateIds));
    }

    @Override
    public List<DataRow> computeLanguageStats(Gender gender, LocalDate dateFrom, LocalDate dateTo, List<Long> sourceCountryIds) {
        List<DataRow> rows = toRows(candidateRepository.
                countByLanguageOrderByCount(
                        genderStr(gender), sourceCountryIds, dateFrom, dateTo));
        return limitRows(rows, 15);
    }

    @Override
    public List<DataRow> computeLanguageStats(Gender gender, LocalDate dateFrom, LocalDate dateTo, Set<Long> candidateIds, List<Long> sourceCountryIds) {
        List<DataRow> rows = toRows(candidateRepository.
                countByLanguageOrderByCount(
                        genderStr(gender), sourceCountryIds, dateFrom, dateTo, candidateIds));
        return limitRows(rows, 15);
    }

    @Override
    public List<DataRow> computeLinkedInStats(LocalDate dateFrom, LocalDate dateTo, List<Long> sourceCountryIds) {
        return toRows(candidateRepository.countLinkedInByCreatedDateOrderByCount(
            sourceCountryIds, dateFrom, dateTo));
    }

    @Override
    public List<DataRow> computeLinkedInStats(LocalDate dateFrom, LocalDate dateTo, Set<Long> candidateIds, List<Long> sourceCountryIds) {
        return toRows(candidateRepository.countLinkedInByCreatedDateOrderByCount(
            sourceCountryIds, dateFrom, dateTo, candidateIds));
    }

    @Override
    public List<DataRow> computeLinkedInExistsStats(LocalDate dateFrom, LocalDate dateTo, List<Long> sourceCountryIds) {
        return toRows(candidateRepository.countByLinkedInExistsOrderByCount(
            sourceCountryIds, dateFrom, dateTo));
    }

    @Override
    public List<DataRow> computeLinkedInExistsStats(LocalDate dateFrom, LocalDate dateTo, Set<Long> candidateIds, List<Long> sourceCountryIds) {
        return toRows(candidateRepository.countByLinkedInExistsOrderByCount(
            sourceCountryIds, dateFrom, dateTo, candidateIds));
    }

    @Override
    public List<DataRow> computeMaxEducationStats(Gender gender, LocalDate dateFrom, LocalDate dateTo, List<Long> sourceCountryIds) {
        return toRows(candidateRepository.
                countByMaxEducationLevelOrderByCount(
                        genderStr(gender), sourceCountryIds, dateFrom, dateTo));
    }

    @Override
    public List<DataRow> computeMaxEducationStats(Gender gender, LocalDate dateFrom, LocalDate dateTo, Set<Long> candidateIds, List<Long> sourceCountryIds) {
        return toRows(candidateRepository.
                countByMaxEducationLevelOrderByCount(
                        genderStr(gender), sourceCountryIds, dateFrom, dateTo, candidateIds));
    }

    @Override
    public List<DataRow> computeMostCommonOccupationStats(Gender gender, LocalDate dateFrom, LocalDate dateTo, List<Long> sourceCountryIds) {
        List<DataRow> rows = toRows(candidateRepository.
                countByMostCommonOccupationOrderByCount(
                        genderStr(gender), sourceCountryIds, dateFrom, dateTo));
        return limitRows(rows, 15);
    }

    @Override
    public List<DataRow> computeMostCommonOccupationStats(Gender gender, LocalDate dateFrom, LocalDate dateTo, Set<Long> candidateIds, List<Long> sourceCountryIds) {
        List<DataRow> rows = toRows(candidateRepository.
                countByMostCommonOccupationOrderByCount(
                        genderStr(gender), sourceCountryIds, dateFrom, dateTo, candidateIds));
        return limitRows(rows, 15);
    }

    @Override
    public List<DataRow> computeNationalityStats(Gender gender, String country, LocalDate dateFrom, LocalDate dateTo, List<Long> sourceCountryIds) {
        List<DataRow> rows = toRows(candidateRepository.
                countByNationalityOrderByCount(
                        genderStr(gender), countryStr(country),
                        sourceCountryIds, dateFrom, dateTo));
        return limitRows(rows, 15);
    }

    @Override
    public List<DataRow> computeNationalityStats(Gender gender, String country, LocalDate dateFrom, LocalDate dateTo, Set<Long> candidateIds, List<Long> sourceCountryIds) {
        List<DataRow> rows = toRows(candidateRepository.
                countByNationalityOrderByCount(
                        genderStr(gender), countryStr(country),
                        sourceCountryIds, dateFrom, dateTo, candidateIds));
        return limitRows(rows, 15);
    }

    @Override
    public List<DataRow> computeOccupationStats(Gender gender, LocalDate dateFrom, LocalDate dateTo, List<Long> sourceCountryIds) {
        return toRows(candidateRepository.
                countByOccupationOrderByCount(
                        genderStr(gender), sourceCountryIds, dateFrom, dateTo));
    }

    @Override
    public List<DataRow> computeOccupationStats(Gender gender, LocalDate dateFrom, LocalDate dateTo, Set<Long> candidateIds, List<Long> sourceCountryIds) {
        return toRows(candidateRepository.
                countByOccupationOrderByCount(
                        genderStr(gender), sourceCountryIds, dateFrom, dateTo, candidateIds));
    }

    @Override
    public List<DataRow> computeReferrerStats(Gender gender, String country, LocalDate dateFrom, LocalDate dateTo, List<Long> sourceCountryIds) {
        return toRows(candidateRepository.
            countByReferrerOrderByCount(
                genderStr(gender), countryStr(country),
                sourceCountryIds, dateFrom, dateTo));
    }

    @Override
    public List<DataRow> computeReferrerStats(Gender gender, String country, LocalDate dateFrom, LocalDate dateTo, Set<Long> candidateIds, List<Long> sourceCountryIds) {
        return toRows(candidateRepository.
            countByReferrerOrderByCount(
                genderStr(gender), countryStr(country),
                sourceCountryIds, dateFrom, dateTo, candidateIds));
    }

    @Override
    public List<DataRow> computeRegistrationOccupationStats(LocalDate dateFrom, LocalDate dateTo, List<Long> sourceCountryIds) {
        final List<DataRow> rows = toRows(candidateRepository.countByOccupationOrderByCount(
                sourceCountryIds, dateFrom, dateTo));
        return limitRows(rows, 15);
    }

    @Override
    public List<DataRow> computeRegistrationOccupationStats(LocalDate dateFrom, LocalDate dateTo, Set<Long> candidateIds, List<Long> sourceCountryIds) {
        final List<DataRow> rows = toRows(candidateRepository.countByOccupationOrderByCount(
                sourceCountryIds, dateFrom, dateTo, candidateIds));
        return limitRows(rows, 15);
    }

    @Override
    public List<DataRow> computeRegistrationStats(LocalDate dateFrom, LocalDate dateTo, List<Long> sourceCountryIds) {
        return toRows(candidateRepository.countByCreatedDateOrderByCount(
                sourceCountryIds, dateFrom, dateTo));
    }

    @Override
    public List<DataRow> computeRegistrationStats(LocalDate dateFrom, LocalDate dateTo, Set<Long> candidateIds, List<Long> sourceCountryIds) {
        return toRows(candidateRepository.countByCreatedDateOrderByCount(
                sourceCountryIds, dateFrom, dateTo, candidateIds));
    }

    @Override
    public List<DataRow> computeSpokenLanguageLevelStats(Gender gender, String language, LocalDate dateFrom, LocalDate dateTo, List<Long> sourceCountryIds) {
        return toRows(candidateRepository.
                countBySpokenLanguageLevelByCount(genderStr(gender), language,
                        sourceCountryIds, dateFrom, dateTo));
    }

    @Override
    public List<DataRow> computeSpokenLanguageLevelStats(Gender gender, String language, LocalDate dateFrom, LocalDate dateTo, Set<Long> candidateIds, List<Long> sourceCountryIds) {
        return toRows(candidateRepository.
                countBySpokenLanguageLevelByCount(genderStr(gender), language,
                        sourceCountryIds, dateFrom, dateTo, candidateIds));
    }

    @Override
    public List<DataRow> computeSurveyStats(Gender gender, String country, LocalDate dateFrom, LocalDate dateTo, List<Long> sourceCountryIds) {
        return toRows(candidateRepository.
                countBySurveyOrderByCount(
                        genderStr(gender), countryStr(country),
                        sourceCountryIds, dateFrom, dateTo));
    }

    @Override
    public List<DataRow> computeSurveyStats(Gender gender, String country, LocalDate dateFrom, LocalDate dateTo, Set<Long> candidateIds, List<Long> sourceCountryIds) {
        return toRows(candidateRepository.
                countBySurveyOrderByCount(
                        genderStr(gender), countryStr(country),
                        sourceCountryIds, dateFrom, dateTo, candidateIds));
    }

    @Override
    public List<DataRow> computeStatusStats(Gender gender, String country, LocalDate dateFrom, LocalDate dateTo, List<Long> sourceCountryIds) {
        return toRows(candidateRepository.
                countByStatusOrderByCount(
                        genderStr(gender), countryStr(country),
                        sourceCountryIds, dateFrom, dateTo));
    }

    @Override
    public List<DataRow> computeStatusStats(Gender gender, String country, LocalDate dateFrom, LocalDate dateTo, Set<Long> candidateIds, List<Long> sourceCountryIds) {
        return toRows(candidateRepository.
                countByStatusOrderByCount(
                        genderStr(gender), countryStr(country),
                        sourceCountryIds, dateFrom, dateTo, candidateIds));
    }

    @Override
    public Resource generateCv(Candidate candidate, Boolean showName, Boolean showContact) {
       return pdfHelper.generatePdf(candidate, showName, showContact);
    }

    // List export
    @Override
    public void exportToCsv(
            long savedListId, SavedListGetRequest request, PrintWriter writer)
            throws ExportFailedException {
        try (CSVWriter csvWriter = new CSVWriter(writer)) {
            csvWriter.writeNext(getExportTitles());

            request.setPageNumber(0);
            request.setPageSize(500);
            boolean hasMore = true;
            while (hasMore) {
                Page<Candidate> result = getSavedListCandidates(savedListId, request);
                for (Candidate candidate : result.getContent()) {
                    candidate.setContextSavedListId(savedListId);
                    csvWriter.writeNext(getExportCandidateStrings(candidate));
                }

                if ((long) result.getNumber() * request.getPageSize() < result.getTotalElements()) {
                    request.setPageNumber(request.getPageNumber()+1);
                } else {
                    hasMore = false;
                }
            }
        } catch (IOException e) {
            throw new ExportFailedException( e);
        }
    }

    public String[] getExportTitles() {
        return new String[]{
            "Candidate Number", "Candidate First Name", "Candidate Last Name", "Gender", "Country Residing", "Nationality",
            "Dob", "Email", "Max Education Level", "Education Major", "English Spoken Level", "Occupation", "Context Note", "Link"
        };
    }

    public String[] getExportCandidateStrings(Candidate candidate) {
        User loggedInUser = authService.getLoggedInUser()
                .orElseThrow(() -> new InvalidSessionException("Not logged in"));

        Role role = loggedInUser.getRole();

        //If the candidate is managed by the logged-in user's partner, then data is visible based
        //on the user's role. However, if the candidate is managed by another partner, the user is
        //treated as if their role was semilimited.
        Partner userPartner = loggedInUser.getPartner();
        Partner candidatePartner = candidate.getUser().getPartner();
        if (userPartner == null || candidatePartner == null ||
            !userPartner.getId().equals(candidatePartner.getId())) {
            role = Role.semilimited;
        }

        if (role == Role.semilimited) {
            return new String[] {
                    candidate.getCandidateNumber(),
                    NOT_AUTHORIZED, //First name
                    NOT_AUTHORIZED, //Last name
                    candidate.getGender() != null ? candidate.getGender().toString() : null,
                    candidate.getCountry() != null ? candidate.getCountry().getName() : candidate.getMigrationCountry(),
                    candidate.getNationality() != null ? candidate.getNationality().getName() : null,
                    candidate.getDob() != null ? candidate.getDob().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)) : null,
                    NOT_AUTHORIZED, //Email
                    candidate.getMaxEducationLevel() != null ? candidate.getMaxEducationLevel().getName() : null,
                    formatCandidateMajor(candidate.getCandidateEducations()),
                    getEnglishSpokenProficiency(candidate.getCandidateLanguages()),
                    formatCandidateOccupation(candidate.getCandidateOccupations()),
                    candidate.getContextNote() != null ? candidate.getContextNote() : null,
                    getCandidateExternalHref(candidate.getCandidateNumber())
            };
        } else if (role == Role.limited) {
            return new String[] {
                    candidate.getCandidateNumber(),
                    NOT_AUTHORIZED, //First name
                    NOT_AUTHORIZED, //Last name
                    candidate.getGender() != null ? candidate.getGender().toString() : null,
                    NOT_AUTHORIZED, //Country
                    NOT_AUTHORIZED, //Nationality
                    candidate.getDob() != null ? candidate.getDob().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)) : null,
                    NOT_AUTHORIZED, //Email
                    candidate.getMaxEducationLevel() != null ? candidate.getMaxEducationLevel().getName() : null,
                    formatCandidateMajor(candidate.getCandidateEducations()),
                    getEnglishSpokenProficiency(candidate.getCandidateLanguages()),
                    formatCandidateOccupation(candidate.getCandidateOccupations()),
                    candidate.getContextNote() != null ? candidate.getContextNote() : null,
                    getCandidateExternalHref(candidate.getCandidateNumber())
            };
        } else {
            return new String[] {
                    candidate.getCandidateNumber(),
                    candidate.getUser().getFirstName(),
                    candidate.getUser().getLastName(),
                    candidate.getGender() != null ? candidate.getGender().toString() : null,
                    candidate.getCountry() != null ? candidate.getCountry().getName() : candidate.getMigrationCountry(),
                    candidate.getNationality() != null ? candidate.getNationality().getName() : null,
                    candidate.getDob() != null ? candidate.getDob().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)) : null,
                    candidate.getUser().getEmail(),
                    candidate.getMaxEducationLevel() != null ? candidate.getMaxEducationLevel().getName() : null,
                    formatCandidateMajor(candidate.getCandidateEducations()),
                    getEnglishSpokenProficiency(candidate.getCandidateLanguages()),
                    formatCandidateOccupation(candidate.getCandidateOccupations()),
                    candidate.getContextNote() != null ? candidate.getContextNote() : null,
                    getCandidateExternalHref(candidate.getCandidateNumber())
            };
        }
    }

    private String getCandidateExternalHref(String candidateNumber) {
        return "https://tctalent.org/admin-portal/candidate/" + candidateNumber;
    }

    public String formatCandidateMajor(List<CandidateEducation> candidateEducations){
        StringBuilder buffer = new StringBuilder();
        if (!CollectionUtils.isEmpty(candidateEducations)){
            for (CandidateEducation candidateEducation : candidateEducations) {
                if (candidateEducation.getEducationMajor() != null){
                    buffer.append(candidateEducation.getEducationMajor().getName()).append("\n");
                }
            }
        }
        return buffer.toString();

    }

    public String formatCandidateOccupation(List<CandidateOccupation> candidateOccupations){
        StringBuilder buffer = new StringBuilder();
        if (!CollectionUtils.isEmpty(candidateOccupations)){
            for (CandidateOccupation candidateOccupation : candidateOccupations) {
                if (candidateOccupation.getOccupation() != null){
                    buffer.append(candidateOccupation.getOccupation().getName()).append("\n");
                }
            }
        }
        return buffer.toString();

    }

    public String getEnglishSpokenProficiency(List<CandidateLanguage> candidateLanguages){
        StringBuilder buffer = new StringBuilder();
        if (!CollectionUtils.isEmpty(candidateLanguages)){
            for (CandidateLanguage candidateLanguage : candidateLanguages) {
                if ((candidateLanguage.getLanguage() != null) && "english".equalsIgnoreCase(candidateLanguage.getLanguage().getName())){
                    if(candidateLanguage.getSpokenLevel() != null) {
                        buffer.append(candidateLanguage.getSpokenLevel().getName()).append("\n");
                    }
                }
            }
        }
        return buffer.toString();

    }

    private List<DataRow> limitRows(List<DataRow> rawData, int limit) {
        if (rawData.size() > limit) {
            List<DataRow> result = new ArrayList<>(rawData.subList(0, limit - 1));
            BigDecimal other = BigDecimal.ZERO;
            for (int i = limit-1; i < rawData.size(); i++) {
                other = other.add(rawData.get(i).getValue());
            }
            if (other.compareTo(BigDecimal.ZERO) != 0) {
                result.add(new DataRow("Other", other));
            }
            return result;
        } else {
            return rawData;
        }
    }

    @Override
    public Candidate save(Candidate candidate, boolean updateCandidateEs) {
        candidate = candidateRepository.save(candidate);

        if (updateCandidateEs) {
            candidate = updateElasticProxy(candidate);
        }
        return candidate;
    }

    /**
     * Does whatever is needed to bring the Elastic proxy into sync with
     * its parent candidate on the normal database.
     * <p>
     *     Handles the following cases:
     * </p>
     * <ul>
     *     <li>Normal case: updates proxy indicated by textSearchId of master</li>
     *     <li>Master has no linked proxy (no textSearchId) - create one </li>
     *     <li>Master has a textSearchId, but no such proxy is found,
     *     log warning but create a proxy</li>
     * </ul>
     * @param candidate Candidate entity (the master) from thr normal database
     * @return Potentially modified candidate entity (with latest textSearchId)
     */
    private Candidate updateElasticProxy(Candidate candidate) {
        //Find/create Elasticsearch twin candidate
        CandidateEs twin;
        //Get textSearchId, if any
        String textSearchId = candidate.getTextSearchId();
        String originalTextSearchId = textSearchId;
        if (textSearchId == null) {
            //No twin - create one
            twin = new CandidateEs();
            twin.copy(candidate, textExtracter);

        } else {
            //Get twin
            twin = candidateEsRepository.findById(textSearchId)
                    .orElse(null);
            if (twin == null) {
                //Candidate is referring to non existent twin.
                //Create new twin
                twin = new CandidateEs();
                twin.copy(candidate, textExtracter);

                //Shouldn't really happen (except during a complete reload)
                // so log warning
                log.warn("Candidate " + candidate.getId() +
                        " refers to non existent Elasticsearch id "
                        + textSearchId + ". Creating new twin.");
            } else {
                //Update twin from candidate
                twin.copy(candidate, textExtracter);
            }
        }
        twin = candidateEsRepository.save(twin);
        textSearchId = twin.getId();

        //Update textSearchId on candidate if necessary
        if (!textSearchId.equals(originalTextSearchId)) {
            candidate.setTextSearchId(textSearchId);
            candidate = candidateRepository.save(candidate);
        }
        return candidate;
    }

    @Override
    @NonNull
    public String getCandidateSubfolderName(CandidateSubfolderType type) {
        return candidateSubfolderNames.get(type);
    }

    @Override
    @Nullable
    public String getCandidateSubfolderlink(Candidate candidate, CandidateSubfolderType type) {
        String link = null;
        switch (type) {
            case address:
                link = candidate.getFolderlinkAddress();
                break;
            case character:
                link = candidate.getFolderlinkCharacter();
                break;
            case employer:
                link = candidate.getFolderlinkEmployer();
                break;
            case engagement:
                link = candidate.getFolderlinkEngagement();
                break;
            case experience:
                link = candidate.getFolderlinkExperience();
                break;
            case family:
                link = candidate.getFolderlinkFamily();
                break;
            case identity:
                link = candidate.getFolderlinkIdentity();
                break;
            case immigration:
                link = candidate.getFolderlinkImmigration();
                break;
            case language:
                link = candidate.getFolderlinkLanguage();
                break;
            case medical:
                link = candidate.getFolderlinkMedical();
                break;
            case qualification:
                link = candidate.getFolderlinkQualification();
                break;
            case registration:
                link = candidate.getFolderlinkRegistration();
                break;
        }
        return link;
    }

    @Override
    public void setCandidateSubfolderlink(Candidate candidate, CandidateSubfolderType type,
        @Nullable String link) {

        switch (type) {
            case address:
                candidate.setFolderlinkAddress(link);
                break;
            case character:
                candidate.setFolderlinkCharacter(link);
                break;
            case employer:
                candidate.setFolderlinkEmployer(link);
                break;
            case engagement:
                candidate.setFolderlinkEngagement(link);
                break;
            case experience:
                candidate.setFolderlinkExperience(link);
                break;
            case family:
                candidate.setFolderlinkFamily(link);
                break;
            case identity:
                candidate.setFolderlinkIdentity(link);
                break;
            case immigration:
                candidate.setFolderlinkImmigration(link);
                break;
            case language:
                candidate.setFolderlinkLanguage(link);
                break;
            case medical:
                candidate.setFolderlinkMedical(link);
                break;
            case qualification:
                candidate.setFolderlinkQualification(link);
                break;
            case registration:
                candidate.setFolderlinkRegistration(link);
                break;
        }
    }

    @Override
    public Candidate createCandidateFolder(long id)
            throws NoSuchObjectException, IOException {
        Candidate candidate = getCandidate(id);

        GoogleFileSystemDrive candidateDrive;
        GoogleFileSystemFolder folder;

        String folderlink = candidate.getFolderlink();

        //If we already have a folderlink stored, that gives us the folder object
        if (folderlink != null
            //Avoid any crap links - eg blank field which is not null
            && folderlink.startsWith("http")) {
            folder = new GoogleFileSystemFolder(folderlink);
        } else {
            //If we don't have a folderlink stored, look for the folder, creating one if needed.
            //The folder should be located in the current candidate drive and candidate root folder
            candidateDrive = googleDriveConfig.getCandidateDataDrive();
            GoogleFileSystemFolder candidateRoot = googleDriveConfig.getCandidateRootFolder();

            String candidateNumber = candidate.getCandidateNumber();
            folder = fileSystemService.findAFolder(candidateDrive, candidateRoot, candidateNumber);
            if (folder == null) {
                //No folder exists on drive, create it
                folder = fileSystemService.createFolder(
                    candidateDrive, candidateRoot, candidateNumber);
            }
            //Store the link
            candidate.setFolderlink(folder.getUrl());
        }

        //Check that all candidate subfolders are present and links are stored
        //Use the candidate drive associated with the main folder
        candidateDrive = fileSystemService.getDriveFromEntity(folder);
        for (CandidateSubfolderType cstype: CandidateSubfolderType.values()) {
            if (getCandidateSubfolderlink(candidate, cstype) == null) {
                //No link stored = check if folder exists
                final String subfolderName = getCandidateSubfolderName(cstype);
                GoogleFileSystemFolder subfolder =
                    fileSystemService.findAFolder(candidateDrive, folder, subfolderName);
                if (subfolder == null) {
                    //No subfolder exists - create one
                    subfolder = fileSystemService.createFolder(candidateDrive, folder, subfolderName);
                    //Make publicly viewable
                    fileSystemService.publishFolder(subfolder);
                }

                setCandidateSubfolderlink(candidate, cstype, subfolder.getUrl());
            }
        }

        save(candidate, false);
        return candidate;
    }

    @Override
    public void createCandidateFolder(Collection<Long> candidateIds)
        throws NoSuchObjectException, IOException {
        for (Long candidateId : candidateIds) {
            createCandidateFolder(candidateId);
        }
    }

    @Override
    public Candidate createUpdateSalesforce(long id)
            throws NoSuchObjectException, SalesforceException,
            WebClientException {
        Candidate candidate = getCandidate(id);

        Contact candidateSf = salesforceService.createOrUpdateContact(candidate);
        candidate.setSflink(candidateSf.getUrl());

        save(candidate, false);
        return candidate;
    }

    @Override
    public void updateIntakeData(long id, CandidateIntakeDataUpdate data)
            throws NoSuchObjectException {
        Candidate candidate = getCandidate(id);

        //If there is a non null citizen nationality, that means that this
        //is a citizenship update.
        final Long citizenNationalityId = data.getCitizenNationalityId();
        if (citizenNationalityId != null) {
            candidateCitizenshipService
                    .updateIntakeData(citizenNationalityId, candidate, data);
        }

        //If there is a non null dependent relation, that means that this
        //is a dependant update.
        final DependantRelations dependantRelation = data.getDependantRelation();
        if (dependantRelation != null) {
            candidateDependantService
                    .updateIntakeData(candidate, data);
        }

        //If there is a non null destination country, that means that this
        //is a destination update.
        final Long destinationCountryId = data.getDestinationCountryId();
        if (destinationCountryId != null) {
            candidateDestinationService
                    .updateIntakeData(destinationCountryId, candidate, data);
        }

        //If there is a non null visa id, that means that this
        //is a visa check update.
        final Long visaId = data.getVisaId();
        if (visaId != null) {
            candidateVisaService
                    .updateIntakeData(visaId, candidate, data);
        }

        //If there is a non null visa job id, that means that this
        //is a visa job check update.
        final Long visaJobId = data.getVisaJobId();
        if (visaJobId != null) {
            candidateVisaJobCheckService
                    .updateIntakeData(visaJobId, candidate, data);
        }

        //If there is a non null exam type, that means that this
        //is a exam update.
        final Exam exam = data.getExamType();
        if (exam != null) {
            updateExamIntakeData(candidate, data);
        }

        //Get the partner candidate object from the id in the data request and pass into the populateIntakeData method
        final Long partnerCandId = data.getPartnerCandId();
        Candidate partnerCandidate = null;
        if (partnerCandId != null) {
            partnerCandidate = candidateRepository.findById(partnerCandId).orElse(null);
        }

        //Get the partner education level object from the id in the data request and pass into the populateIntakeData method
        final Long partnerEduLevelId = data.getPartnerEduLevelId();
        EducationLevel partnerEducationLevel = null;
        if (partnerEduLevelId != null) {
            partnerEducationLevel = educationLevelRepository.findById(partnerEduLevelId).orElse(null);
        }

        final Long partnerOccupationId = data.getPartnerOccupationId();
        Occupation partnerOccupation = null;
        if (partnerOccupationId != null) {
            partnerOccupation = occupationRepository.findById(partnerOccupationId).orElse(null);
        }

        final Long partnerEnglishLevelId = data.getPartnerEnglishLevelId();
        LanguageLevel partnerEnglishLevel = null;
        if (partnerEnglishLevelId != null) {
            partnerEnglishLevel = languageLevelRepository.findById(partnerEnglishLevelId).orElse(null);
        }

        final Long partnerCitizenshipId = data.getPartnerCitizenshipId();
        Country partnerCitizenship = null;
        if (partnerCitizenshipId != null) {
            partnerCitizenship = countryRepository.findById(partnerCitizenshipId).orElse(null);
        }

        final Long drivingLicenseCountryId = data.getDrivingLicenseCountryId();
        Country drivingLicenseCountry = null;
        if (drivingLicenseCountryId != null) {
            drivingLicenseCountry = countryRepository.findById(drivingLicenseCountryId).orElse(null);
        }

        final Long birthCountryId = data.getBirthCountryId();
        Country birthCountry = null;
        if (birthCountryId != null) {
            birthCountry = countryRepository.findById(birthCountryId).orElse(null);
        }

        populateIntakeData(candidate, data, partnerCandidate, partnerEducationLevel,
                partnerOccupation, partnerEnglishLevel, partnerCitizenship, drivingLicenseCountry, birthCountry);

        save(candidate, true);

    }

    /**
     * Updates the candidate exam intake data associated with the given
     * nationality and given candidate.
     * @param candidate Candidate
     * @param data Partially populated CandidateIntakeData record. Null data
     *             fields are ignored. Only non null fields are updated.
     * @throws NoSuchObjectException if the there is no Nationality with the
     * given id or no CandidateExam record with the id given in the data
     */
    private void updateExamIntakeData( @NonNull Candidate candidate, CandidateIntakeDataUpdate data)
        throws NoSuchObjectException, EntityExistsException {
        CandidateExam ce;
        Long id = data.getExamId();
        ce = candidateExamRepository.findById(id)
            .orElseThrow(() -> new NoSuchObjectException(CandidateExam.class, id));

        populateExamIntakeData(ce, candidate, data);

        // Check that the requested exam type doesnt already exist to avoid duplicates of ielts exams
        CandidateExam existingExam = candidateExamRepository.findDuplicateByExamType(ce.getExam(), candidate.getId(), ce.getId()).orElse(null);
        if (existingExam != null) {
            if (existingExam.getExam().equals(Exam.IELTSGen) || existingExam.getExam().equals(Exam.IELTSAca)) {
                throw new EntityExistsException("exam type");
            }
        }

        candidateExamRepository.save(ce);

        computeIeltsScore(candidate);

    }

    private void populateExamIntakeData(@NonNull CandidateExam candidateExam,
        @NonNull Candidate candidate, CandidateIntakeDataUpdate data) {
        candidateExam.setCandidate(candidate);
        if (data.getExamType() != null) {
            candidateExam.setExam(data.getExamType());
        }
        if (data.getOtherExam() != null) {
            candidateExam.setOtherExam(data.getOtherExam());
        }
        if (data.getExamScore() != null) {
            candidateExam.setScore(data.getExamScore());
        }
        if (data.getExamYear() != null) {
            candidateExam.setYear(data.getExamYear());
        }
        if (data.getExamNotes() != null) {
            candidateExam.setNotes(data.getExamNotes());
        }
    }

    private void populateIntakeData(Candidate candidate, CandidateIntakeDataUpdate data,
        @Nullable Candidate partnerCandidate,
        @Nullable EducationLevel partnerEduLevel,
        @Nullable Occupation partnerOccupation,
        @Nullable LanguageLevel partnerEnglishLevel,
        @Nullable Country partnerCitizenship,
        @Nullable Country drivingLicenseCountry,
        @Nullable Country birthCountry) {
        if (data.getAsylumYear() != null) {
            candidate.setAsylumYear(data.getAsylumYear());
        }
        if (data.getAvailImmediate() != null) {
            candidate.setAvailImmediate(data.getAvailImmediate());
        }
        if (data.getAvailImmediateJobOps() != null) {
            candidate.setAvailImmediateJobOps(data.getAvailImmediateJobOps());
        }
        if (data.getAvailImmediateReason() != null) {
            candidate.setAvailImmediateReason(data.getAvailImmediateReason());
        }
        if (data.getAvailImmediateNotes() != null) {
            candidate.setAvailImmediateNotes(data.getAvailImmediateNotes());
        }
        if (data.getBirthCountryId() != null) {
            candidate.setBirthCountry(birthCountry);
        }
        if (data.getCanDrive() != null) {
            candidate.setCanDrive(data.getCanDrive());
        }
        if (data.getConflict() != null) {
            candidate.setConflict(data.getConflict());
        }
        if (data.getConflictNotes() != null) {
            candidate.setConflictNotes(data.getConflictNotes());
        }
        if (data.getCovidVaccinated() != null) {
            candidate.setCovidVaccinated(data.getCovidVaccinated());
        }
        if (data.getCovidVaccinatedStatus() != null) {
            candidate.setCovidVaccinatedStatus(data.getCovidVaccinatedStatus());
        }
        if (data.getCovidVaccinatedDate() != null) {
            candidate.setCovidVaccinatedDate(data.getCovidVaccinatedDate());
        }
        if (data.getCovidVaccineName() != null) {
            candidate.setCovidVaccineName(data.getCovidVaccineName());
        }
        if (data.getCovidVaccineNotes() != null) {
            candidate.setCovidVaccineNotes(data.getCovidVaccineNotes());
        }
        if (data.getCrimeConvict() != null) {
            candidate.setCrimeConvict(data.getCrimeConvict());
        }
        if (data.getCrimeConvictNotes() != null) {
            candidate.setCrimeConvictNotes(data.getCrimeConvictNotes());
        }
        if (data.getDestLimit() != null) {
            candidate.setDestLimit(data.getDestLimit());
        }
        if (data.getDestLimitNotes() != null) {
            candidate.setDestLimitNotes(data.getDestLimitNotes());
        }
        if (data.getDrivingLicense() != null) {
            candidate.setDrivingLicense(data.getDrivingLicense());
        }
        if (data.getDrivingLicenseExp() != null) {
            candidate.setDrivingLicenseExp(data.getDrivingLicenseExp());
        }
        if (data.getDrivingLicenseCountryId() != null) {
            candidate.setDrivingLicenseCountry(drivingLicenseCountry);
        }
        if (data.getFamilyMove() != null) {
            candidate.setFamilyMove(data.getFamilyMove());
        }
        if (data.getFamilyMoveNotes() != null) {
            candidate.setFamilyMoveNotes(data.getFamilyMoveNotes());
        }
        if (data.getHealthIssues() != null) {
            candidate.setHealthIssues(data.getHealthIssues());
        }
        if (data.getHealthIssuesNotes() != null) {
            candidate.setHealthIssuesNotes(data.getHealthIssuesNotes());
        }
        if (data.getHomeLocation() != null) {
            candidate.setHomeLocation(data.getHomeLocation());
        }
        if (data.getHostChallenges() != null) {
            candidate.setHostChallenges(data.getHostChallenges());
        }
        if (data.getHostEntryYear() != null) {
            candidate.setHostEntryYear(data.getHostEntryYear());
        }
        if (data.getHostEntryYearNotes() != null) {
            candidate.setHostEntryYearNotes(data.getHostEntryYearNotes());
        }
        if (data.getHostEntryLegally() != null) {
            candidate.setHostEntryLegally(data.getHostEntryLegally());
        }
        if (data.getHostEntryLegallyNotes() != null) {
            candidate.setHostEntryLegallyNotes(data.getHostEntryLegallyNotes());
        }
        if (data.getIntRecruitReasons() != null) {
            candidate.setIntRecruitReasons(data.getIntRecruitReasons());
        }
        if (data.getIntRecruitOther() != null) {
            candidate.setIntRecruitOther(data.getIntRecruitOther());
        }
        if (data.getIntRecruitRural() != null) {
            candidate.setIntRecruitRural(data.getIntRecruitRural());
        }
        if (data.getIntRecruitRuralNotes() != null) {
            candidate.setIntRecruitRuralNotes(data.getIntRecruitRuralNotes());
        }
        if (data.getLangAssessment() != null) {
            candidate.setLangAssessment(data.getLangAssessment());
        }

        if (data.getLangAssessmentScore() != null) {
            BigDecimal score;
            // If the LangAssessmentScore is NoResponse set to null in database.
            if (data.getLangAssessmentScore().equals("NoResponse")) {
                candidate.setLangAssessmentScore(null);
                score = null;
            } else {
                candidate.setLangAssessmentScore(data.getLangAssessmentScore());
            }
            computeIeltsScore(candidate);
        }

        if (data.getLeftHomeReasons() != null) {
            candidate.setLeftHomeReasons(data.getLeftHomeReasons());
        }
        if (data.getLeftHomeNotes() != null) {
            candidate.setLeftHomeNotes(data.getLeftHomeNotes());
        }
        if (data.getMilitaryService() != null) {
            candidate.setMilitaryService(data.getMilitaryService());
        }
        if (data.getMilitaryWanted() != null) {
            candidate.setMilitaryWanted(data.getMilitaryWanted());
        }
        if (data.getMilitaryNotes() != null) {
            candidate.setMilitaryNotes(data.getMilitaryNotes());
        }
        if (data.getMilitaryStart() != null) {
            candidate.setMilitaryStart(data.getMilitaryStart());
        }
        if (data.getMilitaryEnd() != null) {
            candidate.setMilitaryEnd(data.getMilitaryEnd());
        }
        if (data.getMaritalStatus() != null) {
            candidate.setMaritalStatus(data.getMaritalStatus());
        }
        if (data.getMaritalStatusNotes() != null) {
            candidate.setMaritalStatusNotes(data.getMaritalStatusNotes());
        }
        if (data.getPartnerRegistered() != null) {
            candidate.setPartnerRegistered(data.getPartnerRegistered());
        }
        if (data.getPartnerCandId() != null) {
            candidate.setPartnerCandidate(partnerCandidate);
        }
        if (data.getPartnerEduLevelId() != null) {
            candidate.setPartnerEduLevel(partnerEduLevel);
        }
        if (data.getPartnerEduLevelNotes() != null) {
            candidate.setPartnerEduLevelNotes(data.getPartnerEduLevelNotes());
        }
        if (data.getPartnerOccupationId() != null) {
            candidate.setPartnerOccupation(partnerOccupation);
        }
        if (data.getPartnerOccupationNotes() != null) {
            candidate.setPartnerOccupationNotes(data.getPartnerOccupationNotes());
        }
        if (data.getPartnerEnglish() != null) {
            candidate.setPartnerEnglish(data.getPartnerEnglish());
        }
        if (data.getPartnerEnglishLevelId() != null) {
            candidate.setPartnerEnglishLevel(partnerEnglishLevel);
        }
        if (data.getPartnerIelts() != null) {
            candidate.setPartnerIelts(data.getPartnerIelts());
        }
        if (data.getPartnerIeltsScore() != null) {
            candidate.setPartnerIeltsScore(data.getPartnerIeltsScore());
        }
        if (data.getPartnerIeltsYr() != null) {
            candidate.setPartnerIeltsYr(data.getPartnerIeltsYr());
        }
        if (data.getPartnerCitizenshipId() != null) {
            candidate.setPartnerCitizenship(partnerCitizenship);
        }
        if (data.getResidenceStatus() != null) {
            candidate.setResidenceStatus(data.getResidenceStatus());
        }
        if (data.getResidenceStatusNotes() != null) {
            candidate.setResidenceStatusNotes(data.getResidenceStatusNotes());
        }
        if (data.getReturnedHome() != null) {
            candidate.setReturnedHome(data.getReturnedHome());
        }
        if (data.getReturnedHomeReason() != null) {
            candidate.setReturnedHomeReason(data.getReturnedHomeReason());
        }
        if (data.getReturnedHomeReasonNo() != null) {
            candidate.setReturnedHomeReasonNo(data.getReturnedHomeReasonNo());
        }
        if (data.getReturnHomeSafe() != null) {
            candidate.setReturnHomeSafe(data.getReturnHomeSafe());
        }
        if (data.getReturnHomeFuture() != null) {
            candidate.setReturnHomeFuture(data.getReturnHomeFuture());
        }
        if (data.getReturnHomeWhen() != null) {
            candidate.setReturnHomeWhen(data.getReturnHomeWhen());
        }
        if (data.getResettleThird() != null) {
            candidate.setResettleThird(data.getResettleThird());
        }
        if (data.getResettleThirdStatus() != null) {
            candidate.setResettleThirdStatus(data.getResettleThirdStatus());
        }
        if (data.getUnhcrRegistered() != null) {
            candidate.setUnhcrRegistered(data.getUnhcrRegistered());
        }
        if (data.getUnhcrStatus() != null) {
            candidate.setUnhcrStatus(data.getUnhcrStatus());
        }
        if (data.getUnhcrNotRegStatus() != null) {
            candidate.setUnhcrNotRegStatus(data.getUnhcrNotRegStatus());
        }
        if (data.getUnhcrNumber() != null) {
            candidate.setUnhcrNumber(data.getUnhcrNumber());
        }
        if (data.getUnhcrFile() != null) {
            candidate.setUnhcrFile(data.getUnhcrFile());
        }
        if (data.getUnhcrConsent() != null) {
            candidate.setUnhcrConsent(data.getUnhcrConsent());
        }
        if (data.getUnhcrNotes() != null) {
            candidate.setUnhcrNotes(data.getUnhcrNotes());
        }
        if (data.getUnrwaRegistered() != null) {
            candidate.setUnrwaRegistered(data.getUnrwaRegistered());
        }
        if (data.getUnrwaNumber() != null) {
            candidate.setUnrwaNumber(data.getUnrwaNumber());
        }
        if (data.getUnrwaFile() != null) {
            candidate.setUnrwaFile(data.getUnrwaFile());
        }
        if (data.getUnrwaNotRegStatus() != null) {
            candidate.setUnrwaNotRegStatus(data.getUnrwaNotRegStatus());
        }
        if (data.getUnrwaNotes() != null) {
            candidate.setUnrwaNotes(data.getUnrwaNotes());
        }
        if (data.getVisaReject() != null) {
            candidate.setVisaReject(data.getVisaReject());
        }
        if (data.getVisaRejectNotes() != null) {
            candidate.setVisaRejectNotes(data.getVisaRejectNotes());
        }
        if (data.getVisaIssues() != null) {
            candidate.setVisaIssues(data.getVisaIssues());
        }
        if (data.getVisaIssuesNotes() != null) {
            candidate.setVisaIssuesNotes(data.getVisaIssuesNotes());
        }
        if (data.getWorkAbroad() != null) {
            candidate.setWorkAbroad(data.getWorkAbroad());
        }
        if (data.getWorkAbroadNotes() != null) {
            candidate.setWorkAbroadNotes(data.getWorkAbroadNotes());
        }
        if (data.getWorkPermit() != null) {
            candidate.setWorkPermit(data.getWorkPermit());
        }
        if (data.getWorkPermitDesired() != null) {
            candidate.setWorkPermitDesired(data.getWorkPermitDesired());
        }
        if (data.getWorkPermitDesiredNotes() != null) {
            candidate.setWorkPermitDesiredNotes(data.getWorkPermitDesiredNotes());
        }
        if (data.getWorkDesired() != null) {
            candidate.setWorkDesired(data.getWorkDesired());
        }
        if (data.getWorkDesiredNotes() != null) {
            candidate.setWorkDesiredNotes(data.getWorkDesiredNotes());
        }

    }

    /**
     * Depending on where the request comes from (candidate or admin portal) need to get the candidate differently.
     * @param requestCandidateId If from admin portal, id will be present from the request.
     *                    If null, it will come from candidate portal and candidate will be loggedInCandidate.
     * @return The candidate that is being populated/updated.
     */
    @Override
    public Candidate getCandidateFromRequest(@Nullable Long requestCandidateId) {
        User loggedInUser = authService.getLoggedInUser().orElse(null);
        Candidate candidate;
        if (requestCandidateId != null && loggedInUser.getRole() != Role.user) {
            // Coming from Admin Portal
            candidate = candidateRepository.findById(requestCandidateId)
                    .orElseThrow(() -> new NoSuchObjectException(Candidate.class, requestCandidateId));
        } else {
            // Coming from Candidate Portal
            candidate = authService.getLoggedInCandidate();
            if (candidate == null) {
                throw new InvalidSessionException("Not logged in");
            }
        }
        return candidate;
    }

    @Override
    public boolean deleteCandidateExam(long examId)
            throws EntityReferencedException, InvalidRequestException {
        CandidateExam ce = candidateExamRepository.findByIdLoadCandidate(examId)
                .orElseThrow(() -> new NoSuchObjectException(CandidateExam.class, examId));

        boolean ieltsExamGen = false;
        if (ce.getExam() != null) {
            ieltsExamGen = ce.getExam().equals(Exam.IELTSGen);
        }

        Candidate candidate = ce.getCandidate();

        candidateExamRepository.deleteById(examId);

        computeIeltsScore(candidate);
        save(candidate, true);
        return true;
    }

    private String checkStatusValidity(long countryReq, long nationalityReq, Candidate candidate) {
        String newStatus = "";
        // If candidate pending, but they are updating country & nationality as same. Change to ineligible.
        // EXCEPTION: UNLESS AFGHAN IN AFGHANISTAN or Ukrainian in Ukraine
        if (candidate.getStatus() == CandidateStatus.pending) {
            if (countryReq == nationalityReq
                && countryReq != afghanistanCountryId && countryReq != ukraineCountryId) {
                newStatus = "ineligible";
            }
        // If candidate ineligible & it has country & nationality the same (causing the status) BUT the request
        // has nationality & country as different. We can change ineligible status to pending. This determines
        // that the cause of the ineligible status was due to country & nationality being the same, and not another reason.
        } else if (candidate.getStatus() == CandidateStatus.ineligible) {
            if (candidate.getCountry().getId().equals(candidate.getNationality().getId())) {
                //If the candidate currently has country and nationality the same, that is
                //probably why they were ineligible.
                if (countryReq != nationalityReq) {
                    //But if requested country and nationality are now different, set status back to pending
                    newStatus = "pending";
                } else if (countryReq == afghanistanCountryId || countryReq == ukraineCountryId) {
                    //or if country and nationality are the same, but either Afghanistan or Ukraine
                    //that can also be changed to pending.
                    newStatus = "pending";
                }
            }
        }
        return newStatus;
    }

    /**
     * Generates a task assigment which is optional or not, completed or not, overdue or not
     * based on the input parameters.
     * <p/>
     * Used for testing.
     * @return Generated task assignment
     */
    private TaskAssignmentImpl makeFakeTaskAssignment
        (boolean optional, boolean completed, boolean overdue) {

        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDate tomorrow =  LocalDate.now().plusDays(1);

        UploadTaskImpl task = new UploadTaskImpl();
        String name = (optional ? "Optional" : "Required") + " " +
            (completed ? "Complete" : "Incomplete") + " " +
            (overdue ? "Overdue" : "Not overdue") + " task";
        task.setName(name);
        task.setOptional(optional);

        TaskAssignmentImpl ta = new TaskAssignmentImpl();
        ta.setTask(task);
        ta.setDueDate(overdue ? yesterday : tomorrow);
        ta.setCompletedDate(completed ? OffsetDateTime.of(yesterday, LocalTime.MIDNIGHT, ZoneOffset.UTC) : null);

        return ta;
    }

    @Override
    public void resolveOutstandingTaskAssignments(ResolveTaskAssignmentsRequest request) {
        User loggedInUser = authService.getLoggedInUser()
                .orElseThrow(() -> new InvalidSessionException("Not logged in"));

        Set<Country> sourceCountries = userService.getDefaultSourceCountries(loggedInUser);

        //Update status for all given ids
        Collection<Long> ids = request.getCandidateIds();
        for (Long id : ids) {
            Candidate candidate = this.candidateRepository.findByIdLoadUser(id, sourceCountries)
                    .orElse(null);
            if (candidate == null) {
                log.error("updateCandidateStatus: No candidate exists for id " + id);
            } else {
                resolveOutstandingRequiredTaskAssignments(candidate);
            }
        }
    }

    private void resolveOutstandingRequiredTaskAssignments(Candidate candidate) {
        List<TaskAssignmentImpl> taskAssignments = candidate.getTaskAssignments();
        for (TaskAssignmentImpl ta : taskAssignments) {
            // All outstanding required tasks, need to be set as completed. And if not abandoned, need to also be set as abandoned.
            if (ta.getStatus() != Status.deleted) {
                if (ta.getCompletedDate() == null && !ta.getTask().isOptional()) {
                    ta.setCompletedDate(OffsetDateTime.now());
                    if (ta.getAbandonedDate() == null) {
                        ta.setAbandonedDate(OffsetDateTime.now());
                    }
                }
                taskAssignmentRepository.save(ta);
            }
        }
    }
}
