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

import static org.springframework.data.jpa.domain.Specification.where;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.validation.constraints.NotNull;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.tbbtalent.server.configuration.GoogleDriveConfig;
import org.tbbtalent.server.exception.EntityExistsException;
import org.tbbtalent.server.exception.InvalidRequestException;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.exception.RegisteredListException;
import org.tbbtalent.server.model.db.Candidate;
import org.tbbtalent.server.model.db.ExportColumn;
import org.tbbtalent.server.model.db.SavedList;
import org.tbbtalent.server.model.db.SavedSearch;
import org.tbbtalent.server.model.db.Status;
import org.tbbtalent.server.model.db.User;
import org.tbbtalent.server.repository.db.CandidateRepository;
import org.tbbtalent.server.repository.db.ExportColumnRepository;
import org.tbbtalent.server.repository.db.GetCandidateSavedListsQuery;
import org.tbbtalent.server.repository.db.GetSavedListsQuery;
import org.tbbtalent.server.repository.db.SavedListRepository;
import org.tbbtalent.server.repository.db.UserRepository;
import org.tbbtalent.server.request.candidate.EmployerCandidateDecision;
import org.tbbtalent.server.request.candidate.EmployerCandidateFeedbackData;
import org.tbbtalent.server.request.candidate.PublishListRequest;
import org.tbbtalent.server.request.candidate.PublishedDocBuilder;
import org.tbbtalent.server.request.candidate.PublishedDocColumnDef;
import org.tbbtalent.server.request.candidate.PublishedDocColumnSetUp;
import org.tbbtalent.server.request.candidate.PublishedDocColumnType;
import org.tbbtalent.server.request.candidate.PublishedDocImportReport;
import org.tbbtalent.server.request.candidate.UpdateDisplayedFieldPathsRequest;
import org.tbbtalent.server.request.candidate.source.CopySourceContentsRequest;
import org.tbbtalent.server.request.candidate.source.UpdateCandidateSourceDescriptionRequest;
import org.tbbtalent.server.request.list.ContentUpdateType;
import org.tbbtalent.server.request.list.IHasSetOfCandidates;
import org.tbbtalent.server.request.list.SearchSavedListRequest;
import org.tbbtalent.server.request.list.UpdateExplicitSavedListContentsRequest;
import org.tbbtalent.server.request.list.UpdateSavedListContentsRequest;
import org.tbbtalent.server.request.list.UpdateSavedListInfoRequest;
import org.tbbtalent.server.request.search.UpdateSharingRequest;
import org.tbbtalent.server.security.AuthService;
import org.tbbtalent.server.service.db.CandidateSavedListService;
import org.tbbtalent.server.service.db.DocPublisherService;
import org.tbbtalent.server.service.db.ExportColumnsService;
import org.tbbtalent.server.service.db.FileSystemService;
import org.tbbtalent.server.service.db.SalesforceService;
import org.tbbtalent.server.service.db.SavedListService;
import org.tbbtalent.server.util.filesystem.GoogleFileSystemDrive;
import org.tbbtalent.server.util.filesystem.GoogleFileSystemFile;
import org.tbbtalent.server.util.filesystem.GoogleFileSystemFolder;

/**
 * Saved List service
 *
 * @author John Cameron
 */
@Service
public class SavedListServiceImpl implements SavedListService {

    private final static String LIST_CVS_SUBFOLDER = "CvsForEmployer";
    private final static String LIST_JOB_DESCRIPTION_SUBFOLDER = "JobDescription";
    private final static String REGISTERED_NAME_SUFFIX = "*";
    private final CandidateRepository candidateRepository;
    private final ExportColumnRepository exportColumnRepository;
    private final ExportColumnsService exportColumnsService;
    private final SavedListRepository savedListRepository;
    private final CandidateSavedListService candidateSavedListService;
    private final DocPublisherService docPublisherService;
    private final FileSystemService fileSystemService;
    private final GoogleDriveConfig googleDriveConfig;
    private final SalesforceService salesforceService;
    private final UserRepository userRepository;
    private final AuthService authService;

    private static final Logger log = LoggerFactory.getLogger(SavedListServiceImpl.class);
    private static final String PUBLISHED_DOC_CANDIDATE_NUMBER_RANGE_NAME = "CandidateNumber";

    @Autowired
    public SavedListServiceImpl(
        CandidateRepository candidateRepository,
        ExportColumnRepository exportColumnRepository,
        ExportColumnsService exportColumnsService,
        SavedListRepository savedListRepository,
        CandidateSavedListService candidateSavedListService,
        DocPublisherService docPublisherService,
        FileSystemService fileSystemService,
        GoogleDriveConfig googleDriveConfig,
        SalesforceService salesforceService, UserRepository userRepository,
        AuthService authService
    ) {
        this.candidateRepository = candidateRepository;
        this.exportColumnRepository = exportColumnRepository;
        this.exportColumnsService = exportColumnsService;
        this.savedListRepository = savedListRepository;
        this.candidateSavedListService = candidateSavedListService;
        this.docPublisherService = docPublisherService;
        this.fileSystemService = fileSystemService;
        this.googleDriveConfig = googleDriveConfig;
        this.salesforceService = salesforceService;
        this.userRepository = userRepository;
        this.authService = authService;
    }

    @Override
    public boolean clearSavedList(long savedListId) throws InvalidRequestException {
        SavedList savedList = savedListRepository.findByIdLoadCandidates(savedListId)
                .orElse(null);

        boolean done = true;
        if (savedList == null) {
            done = false;
        } else {
            candidateSavedListService.clearSavedListCandidates(savedList);
        }
        return done;
    }

    @Override
    public SavedList copy(long id, CopySourceContentsRequest request) 
            throws EntityExistsException, NoSuchObjectException {
        SavedList sourceList = savedListRepository.findByIdLoadCandidates(id)
                .orElseThrow(() -> new NoSuchObjectException(SavedList.class, id));
        return copy(sourceList, request);
    }

    @Override
    public SavedList copy(SavedList sourceList, CopySourceContentsRequest request)
        throws EntityExistsException, NoSuchObjectException {
        SavedList targetList;
        final Long targetId = request.getSavedListId();
        boolean newList = targetId == 0;
        if (newList) {
            //Request is to create a new list
            //Name for this new list will be in the newListName field - copy that down to the
            //name field which is where the standard createSavedList is expecting to find the name.
            request.setName(request.getNewListName());
            targetList = createSavedList(request);
        } else {
            targetList = savedListRepository.findByIdLoadCandidates(targetId)
                .orElseThrow(() -> new NoSuchObjectException(SavedList.class, targetId));
        }


        //Set any specified Salesforce Job Opportunity
        if (request.getSfJoblink() != null) {
            targetList.setSfJoblink(request.getSfJoblink());
        }

        boolean replace = request.getUpdateType() == ContentUpdateType.replace;
        //New or replaced list inherits source's savedSearchSource, if any
        if (newList || replace) {
            final SavedSearch savedSearchSource = sourceList.getSavedSearchSource();
            if (savedSearchSource != null) {
                targetList.setSavedSearchSource(savedSearchSource);
            }
        }

        //Copy across list contents (which includes context notes)
        copyContents(sourceList, targetList, replace);

        return targetList;
    }

    @Override
    public void copyContents(
            SavedList source, SavedList destination, boolean replace) {
        //Get candidates in source list
        final Set<Candidate> candidates = source.getCandidates();

        //Add or replace them to destination as requested.
        if (replace) {
            clearSavedList(destination.getId());
        }
        destination.addCandidates(candidates, source);

        saveIt(destination);
    }

    @Override
    public void copyContents(UpdateExplicitSavedListContentsRequest request,
        SavedList destination) {
        
        if (request.getUpdateType() == ContentUpdateType.replace) {
            clearSavedList(destination.getId());
        }

        //Retrieve source list, if any
        SavedList sourceList = fetchSourceList(request);

        //New list inherits source's savedSearchSource, if any
        if (sourceList != null) {
            final SavedSearch savedSearchSource = sourceList.getSavedSearchSource();
            if (savedSearchSource != null) {
                destination.setSavedSearchSource(savedSearchSource);
            }
        }

        //Retrieve candidates
        Set<Candidate> candidates = fetchCandidates(request);

        //Add candidates to created list, together with any context if source 
        //list was supplied.
        destination.addCandidates(candidates, sourceList);

        saveIt(destination);
    }

    /**
     * Finds folder for the given list on Google Drive, creating one if none found.
     *
     * @param id ID of list
     * @throws NoSuchObjectException if no list is found with that id
     * @throws IOException           if there is a problem creating the folder.
     */
    private void findOrCreateListFolder(long id) 
        throws NoSuchObjectException, IOException {
        SavedList savedList = get(id);

        GoogleFileSystemDrive foldersDrive = googleDriveConfig.getListFoldersDrive();
        GoogleFileSystemFolder foldersRoot = googleDriveConfig.getListFoldersRoot();
        GoogleFileSystemFile jobOppIntakeTemplate = googleDriveConfig.getJobOppIntakeTemplate();

        String folderName = Long.toString(id);

        GoogleFileSystemFolder folder = fileSystemService.findAFolder(
            foldersDrive, foldersRoot, folderName);
        if (folder == null) {
            // CREATE FOLDERS
            // List ID folder
            folder = fileSystemService.createFolder(foldersDrive, foldersRoot, folderName);
            // Job name folder
            folderName = savedList.getName();
            GoogleFileSystemFolder subfolder = fileSystemService.createFolder(foldersDrive, folder, folderName);
            savedList.setFolderlink(subfolder.getUrl());
            // JD folder
            GoogleFileSystemFolder jdfolder =
                fileSystemService.createFolder(foldersDrive, subfolder, LIST_JOB_DESCRIPTION_SUBFOLDER);
            savedList.setFolderjdlink(jdfolder.getUrl());
            // CREATE JOB OPPORTUNITY INTAKE FILE IN JD FOLDER
            String joiFileName = "JobOpportunityIntake - " + savedList.getName();
            fileSystemService.copyFile(jdfolder, joiFileName, jobOppIntakeTemplate);
        }
    } 
    
    @Override
    public SavedList createListFolder(long id) throws NoSuchObjectException, IOException {
        SavedList savedList = get(id);

        findOrCreateListFolder(id);

        saveIt(savedList);
        return savedList;
    }

    @Override
    public SavedList createSavedList(User user, UpdateSavedListInfoRequest request)
        throws EntityExistsException, RegisteredListException {

        final boolean isRegisteredList =
            request.getRegisteredJob() != null && request.getRegisteredJob();
        if (isRegisteredList) {
            //Check for a registered list with same sfJobLink (owned any user)
            final String sfJoblink = request.getSfJoblink();
            if (sfJoblink == null) {
                throw new RegisteredListException("Missing Salesforce link for registered job list");
            }
            final String jobName = request.getName();
            if (jobName == null) {
                throw new RegisteredListException("Missing name for registered job list");
            }
            SavedList registeredList = savedListRepository.findRegisteredJobList(sfJoblink)
                .orElse(null);
            //If we already have a registered list for this job, just return it
            if (registeredList != null) {
                return registeredList;
            }
            //Modify registered name to avoid clashes with unregistered list names
            request.setName(jobName + REGISTERED_NAME_SUFFIX);
        } else {
            if (user != null) {
                checkDuplicates(null, request.getName(), user);
            }
        }
        
        SavedList savedList = new SavedList();
        request.populateFromRequest(savedList);

        //Save created list so that we get its id from the database
        savedList.setAuditFields(user);
        return savedListRepository.save(savedList);
    }

    @Override
    @Transactional
    public SavedList createSavedList(UpdateSavedListInfoRequest request) 
            throws EntityExistsException {
        final User loggedInUser = authService.getLoggedInUser().orElse(null);
        return createSavedList(loggedInUser, request);
    }

    @Override
    @Transactional
    public boolean deleteSavedList(long savedListId) {
        SavedList savedList = savedListRepository.findByIdLoadCandidates(savedListId)
                .orElse(null);

        final User loggedInUser = authService.getLoggedInUser().orElse(null);
        if (savedList != null && loggedInUser != null) {

            // Check if user owns this list
            if(savedList.getCreatedBy().getId().equals(loggedInUser.getId())) {

                //Need to clear out many to many relationships before deleting
                //the list otherwise we will have other entities pointing to 
                //this list.
                clearSavedList(savedList.getId());
                savedList.setWatcherIds(null);
                savedList.setUsers(null);
                
                //Delete list
                savedListRepository.delete(savedList);
                
                return true;
            } else {
                throw new InvalidRequestException("You can't delete other user's saved lists.");
            }

        }
        return false;
    }

    @Override
    @NonNull
    public SavedList get(long savedListId) throws NoSuchObjectException {
        return savedListRepository.findById(savedListId)
                .orElseThrow(() -> new NoSuchObjectException(SavedList.class, savedListId));
    }

    @Override
    @Nullable
    public SavedList get(@NonNull User user, String listName) {
        return listName == null ? null : 
            savedListRepository.findByNameIgnoreCase(listName, user.getId()).orElse(null);
    }

    @Override
    public void mergeSavedList(long savedListId,
        UpdateExplicitSavedListContentsRequest request) throws NoSuchObjectException {
        SavedList savedList = savedListRepository.findByIdLoadCandidates(savedListId)
                .orElse(null);
        if (savedList == null) {
            throw new NoSuchObjectException(SavedList.class, savedListId);
        }

        SavedList sourceList = fetchSourceList(request);
        Set<Candidate> candidates = fetchCandidates(request);
        savedList.addCandidates(candidates, sourceList);

        saveIt(savedList);
    }

    @Override
    public void mergeSavedListFromFile(long savedListId, MultipartFile file)
        throws NoSuchObjectException, IOException {

        Set<Long> candidateIds = new HashSet<>();

        //Extract candidate numbers from file, look up the id and add to candidateIds
        //We need candidateIds to pass to other methods.
        CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream()));
        String [] tokens;
        try {
            boolean possibleHeader = true;
            while ((tokens = reader.readNext()) != null) {
                //tokens[] is an array of values from the line
                //Ignore empty tokens
                if (tokens.length > 0 && tokens[0].length() > 0) {
                    //A bit of logic to skip any header. Only checks once.
                    boolean skip = possibleHeader && !StringUtils.isNumeric(tokens[0]);
                    possibleHeader = false;

                    if (!skip) {
                        long candidateNumber = Long.parseLong(tokens[0]);
                        Candidate candidate =
                            candidateRepository.findByCandidateNumber(Long.toString(candidateNumber));
                        if (candidate == null) {
                            throw new NoSuchObjectException(Candidate.class, candidateNumber);
                        }
                        candidateIds.add(candidate.getId());
                    }
                }
            }
        } catch (NumberFormatException ex) {
            throw new NoSuchObjectException("Non numeric candidate number " + ex.getMessage());
        } catch (CsvValidationException ex) {
            throw new IOException("Bad file format: " + ex.getMessage());
        }

        UpdateExplicitSavedListContentsRequest request = new UpdateExplicitSavedListContentsRequest();
        request.setCandidateIds(candidateIds);
        request.setUpdateType(ContentUpdateType.add);
        mergeSavedList(savedListId, request);
    }

    @Override
    public void removeFromSavedList(long savedListId, 
        UpdateExplicitSavedListContentsRequest request) throws NoSuchObjectException {
        SavedList savedList = savedListRepository.findByIdLoadCandidates(savedListId)
                .orElse(null);
        if (savedList == null) {
            throw new NoSuchObjectException(SavedList.class, savedListId);
        }

        Set<Candidate> candidates = fetchCandidates(request);
        for (Candidate candidate : candidates) {
            candidateSavedListService.removeFromSavedList(candidate, savedList);
        }
    }

    @Override
    public List<SavedList> search(long candidateId, SearchSavedListRequest request) {
        final User loggedInUser = authService.getLoggedInUser().orElse(null);
        User userWithSharedSearches = loggedInUser == null ? null :
                userRepository.findByIdLoadSharedSearches(loggedInUser.getId());
        GetSavedListsQuery getSavedListsQuery =
                new GetSavedListsQuery(request, userWithSharedSearches);

        GetCandidateSavedListsQuery getCandidateSavedListsQuery = 
                new GetCandidateSavedListsQuery(candidateId);
        
        //Set standard sort to ascending by name.
        Sort sort = Sort.by(Sort.Direction.ASC, "name");
        return savedListRepository.findAll( 
                where(getSavedListsQuery).and(getCandidateSavedListsQuery), 
                sort);
    }

    @Override
    public List<SavedList> listSavedLists(SearchSavedListRequest request) {
        final User loggedInUser = authService.getLoggedInUser().orElse(null);
        User userWithSharedSearches = loggedInUser == null ? null :
                userRepository.findByIdLoadSharedSearches(
                        loggedInUser.getId());
        GetSavedListsQuery getSavedListsQuery = 
                new GetSavedListsQuery(request, userWithSharedSearches);
        
        //The request is not required to provide paging or sorting info and
        //we ignore any such info if present because we don't pass a PageRequest
        //to the repository findAll call.
        //But set standard sort to ascending by name.
        Sort sort = Sort.by(Sort.Direction.ASC, "name");
        return savedListRepository.findAll(getSavedListsQuery, sort);
    }

    @Override
    public Page<SavedList> searchSavedLists(SearchSavedListRequest request) {
        final User loggedInUser = authService.getLoggedInUser().orElse(null);
        User userWithSharedSearches = loggedInUser == null ? null :
                userRepository.findByIdLoadSharedSearches(
                        loggedInUser.getId());
        GetSavedListsQuery getSavedListsQuery = 
                new GetSavedListsQuery(request, userWithSharedSearches);
        
        //The incoming request will have paging info but no sorting.
        //So set standard ascending sort by name.
        request.setSortDirection(Sort.Direction.ASC);
        request.setSortFields(new String[] {"name"});
        
        PageRequest pageRequest = request.getPageRequest();
        return savedListRepository.findAll(getSavedListsQuery, pageRequest);
    }

    @Override
    public void setCandidateContext(long savedListId, Iterable<Candidate> candidates) {
        for (Candidate candidate : candidates) {
            candidate.setContextSavedListId(savedListId);
        }
    }

    @Override
    public SavedList updateSavedList(long savedListId, UpdateSavedListInfoRequest request) 
            throws NoSuchObjectException, EntityExistsException {
        final User loggedInUser = authService.getLoggedInUser().orElse(null);
        if (loggedInUser != null) {
            checkDuplicates(savedListId, request.getName(), loggedInUser);
        }
        SavedList savedList = get(savedListId);
        request.populateFromRequest(savedList);
        return saveIt(savedList);
    }

    @Override
    public void updateDescription(long savedListId, 
        UpdateCandidateSourceDescriptionRequest request)
        throws  NoSuchObjectException {
        SavedList savedList = get(savedListId);
        savedList.setDescription(request.getDescription());
        saveIt(savedList);
    }

    @Override
    public void updateDisplayedFieldPaths(
            long savedListId, UpdateDisplayedFieldPathsRequest request) 
            throws NoSuchObjectException {
        SavedList savedList = get(savedListId);
        if (request.getDisplayedFieldsLong() != null) {
            savedList.setDisplayedFieldsLong(request.getDisplayedFieldsLong());
        }
        if (request.getDisplayedFieldsShort() != null) {
            savedList.setDisplayedFieldsShort(request.getDisplayedFieldsShort());
        }
        saveIt(savedList);
    }

    @Override
    @Transactional
    public SavedList addSharedUser(long id, UpdateSharingRequest request) 
            throws NoSuchObjectException {
        SavedList savedList = savedListRepository.findById(id)
                .orElseThrow(() -> new NoSuchObjectException(SavedList.class, id));

        final Long userID = request.getUserId();
        User user = userRepository.findById(userID)
                .orElseThrow(() -> new NoSuchObjectException(User.class, userID));

        savedList.addUser(user);

        return savedListRepository.save(savedList);
    }

    @Override
    @Transactional
    public SavedList removeSharedUser(long id, UpdateSharingRequest request) 
            throws NoSuchObjectException {
        SavedList savedList = savedListRepository.findById(id)
                .orElseThrow(() -> new NoSuchObjectException(SavedList.class, id));

        final Long userID = request.getUserId();
        User user = userRepository.findById(userID)
                .orElseThrow(() -> new NoSuchObjectException(User.class, userID));

        savedList.removeUser(user);

        return savedListRepository.save(savedList);
    }

    @Override
    public PublishedDocImportReport importEmployerFeedback(long savedListId)
        throws NoSuchObjectException, GeneralSecurityException, IOException {
        SavedList savedList = get(savedListId);
        
        PublishedDocImportReport report = new PublishedDocImportReport();
        
        String link = savedList.getPublishedDocLink();
        if (link == null) {
            report.setMessage("No Salesforce job opportunity associated with list");
        } else {
            //Read data from linked sheet
            Map<String, List<Object>> feedback = docPublisherService.readPublishedDocColumns(link, 
                Arrays.asList(PUBLISHED_DOC_CANDIDATE_NUMBER_RANGE_NAME, 
                    PublishedDocColumnType.EmployerCandidateNotes.toString(),
                    PublishedDocColumnType.EmployerCandidateDecision.toString()
                    ));

            List<Object> candidateNumberColumnData = feedback.get(PUBLISHED_DOC_CANDIDATE_NUMBER_RANGE_NAME);
            int nCandidates = candidateNumberColumnData == null ? 0 : candidateNumberColumnData.size(); 
            report.setNumCandidates(nCandidates);

            List<EmployerCandidateFeedbackData> feedbacks = new ArrayList<>();
            if (nCandidates == 0) {
                report.setMessage("No candidate column found - nothing to import");
            } else {
                List<Object> notesData = feedback.get(PublishedDocColumnType.EmployerCandidateNotes.toString());
                List<Object> decisionData = feedback.get(PublishedDocColumnType.EmployerCandidateDecision.toString());
                
                //Use data to update Salesforce
                int index = 0;
                for (Object candidateNumber : candidateNumberColumnData) {
                    if (candidateNumber == null) {
                        throw new NoSuchObjectException("Missing candidate number");
                    } else {
                        Candidate candidate =
                            candidateRepository.findByCandidateNumber((String) candidateNumber);
                        if (candidate == null) {
                            throw new NoSuchObjectException(Candidate.class, (String) candidateNumber);
                        }

                        EmployerCandidateFeedbackData feedbackData = 
                            new EmployerCandidateFeedbackData(candidate);
                        feedbacks.add(feedbackData);
                        
                        //Notes
                        String notes = (String) fetchColumnValueByIndex(notesData, index);
                        feedbackData.setEmployerCandidateNotes(notes);
                        
                        //Decision
                        String val = (String) fetchColumnValueByIndex(decisionData, index);
                        if (val != null) {
                            EmployerCandidateDecision decision = EmployerCandidateDecision.textToEnum(val);
                            feedbackData.setEmployerCandidateDecision(decision);
                        }
                    }
                    index++;
                }

                int nFeedbacks = 0;
                int nJobOffers = 0;
                int nNoJobOffers = 0;
                for (EmployerCandidateFeedbackData feedbackData : feedbacks) {
                    if (feedbackData.getEmployerCandidateNotes() != null) {
                        nFeedbacks++;
                    }
                    final EmployerCandidateDecision decision = feedbackData.getEmployerCandidateDecision();
                    if (decision != null) {
                        switch (decision) {
                            case JobOffer:
                                nJobOffers++;
                                break;
                            case NoJobOffer:
                                nNoJobOffers++;
                                break;
                        }
                    }
                }
                
                report.setNumEmployerFeedbacks(nFeedbacks);
                report.setNumJobOffers(nJobOffers);
                report.setNumNoJobOffers(nNoJobOffers);
                
                if (nFeedbacks + nJobOffers + nNoJobOffers > 0) {
                    salesforceService.updateCandidateOpportunities(feedbacks, savedList.getSfJoblink());
                    report.setMessage("Import complete");
                } else {
                    report.setMessage("No feedback detected");
                }
            }
        }
        
        return report;
    }

    private Object fetchColumnValueByIndex(List<Object> columnData, int index) {
        Object val = null;
        if (columnData != null) {
            if (index < columnData.size()) {
                val = columnData.get(index);
            }
        }
        return val;
    }

    @Override
    public SavedList publish(long id, PublishListRequest request)
        throws GeneralSecurityException, IOException, NoSuchObjectException {

        //Get list, creating list folder if necessary
        SavedList savedList = createListFolder(id);
        
        //Fetch candidates in list
        Set<Candidate> candidates = savedList.getCandidates();

        //Set list context on candidates so that Candidate field contextNote can be accessed.
        setCandidateContext(savedList.getId(), candidates);

        List<PublishedDocColumnDef> columnInfos = request.getConfiguredColumns(); 

        PublishedDocBuilder builder = new PublishedDocBuilder();
        
        //This is what will be used to create the published doc
        List<List<Object>> publishedData = new ArrayList<>();

        //Title row
        List<Object> title = builder.buildTitle(columnInfos);
        publishedData.add(title);

        //Add row for each candidate
        for (Candidate candidate : candidates) {
            List<Object> candidateData = builder.buildRow(candidate, columnInfos);
            publishedData.add(candidateData);
        }

        //Create the doc in the list folder.
        GoogleFileSystemDrive drive = googleDriveConfig.getListFoldersDrive();
        GoogleFileSystemFolder listFolder = new GoogleFileSystemFolder(savedList.getFolderlink());
        
        //Set other data to publish.
        Map<String, Object> props = new HashMap<>();
        props.put("listDescription", savedList.getDescription());
        props.put("listId", savedList.getId());
        props.put("listName", savedList.getName());
        props.put("timeCreated", LocalDate.now().toString());
        User user = authService.getLoggedInUser().orElse(null);
        if (user != null) {
            props.put("createdByName", user.getDisplayName());
            props.put("createdByEmail", user.getEmail());
        }

        boolean foundCandidateNumber = false;
        //Format column display
        Map<Integer, PublishedDocColumnSetUp> columnSetUpMap = new HashMap<>();
        int columnCount = 0;
        for (PublishedDocColumnDef def : columnInfos) {
            final PublishedDocColumnSetUp columnSetUp = new PublishedDocColumnSetUp();
            columnSetUpMap.put(columnCount, columnSetUp);
            if (def.getType().equals(PublishedDocColumnType.EmployerCandidateDecision)) {
                columnSetUp.setDropDowns(EmployerCandidateDecision.getDisplayTextValues());
            }
            
            if (!def.getType().equals(PublishedDocColumnType.DisplayOnly)) {
                columnSetUp.setRangeName(def.getType().toString());
            }
            
            //Check for a candidate number column. We set up a range name for that column as well
            //as for non display columns.
            if (!foundCandidateNumber) {
                if (def.getContent().getValue() != null) {
                    String fieldName = def.getContent().getValue().getFieldName();
                    foundCandidateNumber = "candidateNumber".equals(fieldName);
                    if (foundCandidateNumber) {
                        columnSetUp.setRangeName(PUBLISHED_DOC_CANDIDATE_NUMBER_RANGE_NAME);
                    }
                } 
            }
            
            switch(def.getWidth()) {
                case Narrow:
                    columnSetUp.setColumnSize(googleDriveConfig.getPublishedSheetNarrowColumn());
                    columnSetUp.setAlignment("CENTER");
                    break;
                case Wide:
                    columnSetUp.setColumnSize(googleDriveConfig.getPublishedSheetWideColumn());
                    columnSetUp.setAlignment("LEFT");
                    break;
            }
            columnCount++;
        }
        
        String publishedSheetDataRangeName = googleDriveConfig.getPublishedSheetDataRangeName();
        String link = docPublisherService.createPublishedDoc(drive, listFolder, savedList.getName(), 
                publishedSheetDataRangeName, publishedData, props, columnSetUpMap);

        /*
         * Need to remove any existing columns - can't rely on the savedList.setExportColumns call
         * to do that. See the doc for {@link ExportColumnsService}
         */
        exportColumnsService.clearExportColumns(savedList);
        List<ExportColumn> cols = request.getExportColumns(savedList);
        savedList.setExportColumns(cols);
        savedList.setPublishedDocLink(link);
        saveIt(savedList);
        
        return savedList;
    }

    /**
     * Checks against a user having more than one list with the same name.
     * @param savedListId If not null, this must be the id of the list with the given name. 
     * @param name Name of list
     * @param user Owner of list
     * @throws EntityExistsException If a list exists already with that name, and the list is not
     * the list with the given id.
     */
    private void checkDuplicates(Long savedListId, String name, @NonNull User user) 
            throws EntityExistsException {
        SavedList existing = get(user, name);
        if (existing != null && existing.getStatus() != Status.deleted) {
            //We have a undeleted list with that name. Report duplicate unless the list is the
            //one we expect - ie with the given id.
            if (!existing.getId().equals(savedListId)) {
                throw new EntityExistsException("SavedList " + existing.getId());
            }
        }
    }

    private @NotNull Set<Candidate> fetchCandidates(IHasSetOfCandidates request) 
            throws NoSuchObjectException {

        Set<Candidate> candidates = new HashSet<>();

        Set<Long> candidateIds = request.getCandidateIds();
        if (candidateIds != null) {
            for (Long candidateId : candidateIds) {
                Candidate candidate = candidateRepository.findById(candidateId)
                        .orElse(null);
                if (candidate == null) {
                    throw new NoSuchObjectException(Candidate.class, candidateId);
                }
                candidates.add(candidate);
            }
        }

        return candidates;
    }

    @Nullable
    private SavedList fetchSourceList(UpdateSavedListContentsRequest request) 
            throws NoSuchObjectException {
        SavedList sourceList = null;
        Long sourceListId = request.getSourceListId();
        if (sourceListId != null) {
            sourceList = savedListRepository.findByIdLoadCandidates(sourceListId)
                    .orElseThrow(() -> new NoSuchObjectException(SavedList.class, sourceListId));
        }
        return sourceList;
    }

    /**
     * Update audit fields and use repository to save the SavedList
     * @param savedList Entity to save
     * @return Saved entity
     */
    private SavedList saveIt(SavedList savedList) {
        savedList.setAuditFields(authService.getLoggedInUser().orElse(null));
        return savedListRepository.save(savedList);
    }
    
}
