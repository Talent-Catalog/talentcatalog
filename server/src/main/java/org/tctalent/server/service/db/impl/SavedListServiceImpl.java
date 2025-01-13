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

package org.tctalent.server.service.db.impl;

import static org.springframework.data.jpa.domain.Specification.where;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClientException;
import org.tctalent.server.configuration.GoogleDriveConfig;
import org.tctalent.server.exception.EntityExistsException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.exception.RegisteredListException;
import org.tctalent.server.exception.SalesforceException;
import org.tctalent.server.logging.LogBuilder;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateSavedList;
import org.tctalent.server.model.db.ExportColumn;
import org.tctalent.server.model.db.SalesforceJobOpp;
import org.tctalent.server.model.db.SavedList;
import org.tctalent.server.model.db.Status;
import org.tctalent.server.model.db.TaskAssignmentImpl;
import org.tctalent.server.model.db.TaskImpl;
import org.tctalent.server.model.db.User;
import org.tctalent.server.model.db.task.Task;
import org.tctalent.server.model.db.task.TaskAssignment;
import org.tctalent.server.repository.db.CandidateRepository;
import org.tctalent.server.repository.db.CandidateSavedListRepository;
import org.tctalent.server.repository.db.GetCandidateSavedListsQuery;
import org.tctalent.server.repository.db.GetSavedListsQuery;
import org.tctalent.server.repository.db.SavedListRepository;
import org.tctalent.server.repository.db.UserRepository;
import org.tctalent.server.request.IdsRequest;
import org.tctalent.server.request.candidate.EmployerCandidateDecision;
import org.tctalent.server.request.candidate.EmployerCandidateFeedbackData;
import org.tctalent.server.request.candidate.PublishListRequest;
import org.tctalent.server.request.candidate.PublishedDocColumnDef;
import org.tctalent.server.request.candidate.PublishedDocColumnSetUp;
import org.tctalent.server.request.candidate.PublishedDocColumnType;
import org.tctalent.server.request.candidate.PublishedDocImportReport;
import org.tctalent.server.request.candidate.UpdateCandidateListOppsRequest;
import org.tctalent.server.request.candidate.UpdateDisplayedFieldPathsRequest;
import org.tctalent.server.request.candidate.source.UpdateCandidateSourceDescriptionRequest;
import org.tctalent.server.request.link.UpdateShortNameRequest;
import org.tctalent.server.request.list.ContentUpdateType;
import org.tctalent.server.request.list.IHasSetOfCandidates;
import org.tctalent.server.request.list.SearchSavedListRequest;
import org.tctalent.server.request.list.UpdateExplicitSavedListContentsRequest;
import org.tctalent.server.request.list.UpdateSavedListContentsRequest;
import org.tctalent.server.request.list.UpdateSavedListInfoRequest;
import org.tctalent.server.request.search.UpdateSharingRequest;
import org.tctalent.server.service.db.CandidateOpportunityService;
import org.tctalent.server.service.db.DocPublisherService;
import org.tctalent.server.service.db.ExportColumnsService;
import org.tctalent.server.service.db.FileSystemService;
import org.tctalent.server.service.db.PublicIDService;
import org.tctalent.server.service.db.SalesforceJobOppService;
import org.tctalent.server.service.db.SalesforceService;
import org.tctalent.server.service.db.SavedListService;
import org.tctalent.server.service.db.TaskAssignmentService;
import org.tctalent.server.service.db.UserService;
import org.tctalent.server.util.filesystem.GoogleFileSystemDrive;
import org.tctalent.server.util.filesystem.GoogleFileSystemFile;
import org.tctalent.server.util.filesystem.GoogleFileSystemFolder;

/**
 * Saved List service
 *
 * @author John Cameron
 */
@Service
@Slf4j
public class SavedListServiceImpl implements SavedListService {

    private final static String LIST_JOB_DESCRIPTION_SUBFOLDER = "JobDescription";
    private final static String REGISTERED_NAME_SUFFIX = "*";
    private final static String EXCLUSION_LIST_SUFFIX = "Exclude";
    private final CandidateRepository candidateRepository;
    private final CandidateSavedListRepository candidateSavedListRepository;
    private final CandidateOpportunityService candidateOpportunityService;
    private final ExportColumnsService exportColumnsService;
    private final SavedListRepository savedListRepository;
    private final DocPublisherService docPublisherService;
    private final FileSystemService fileSystemService;
    private final GoogleDriveConfig googleDriveConfig;
    private final PublicIDService publicIDService;
    private final SalesforceService salesforceService;
    private final SalesforceJobOppService salesforceJobOppService;
    private final TaskAssignmentService taskAssignmentService;
    private final UserRepository userRepository;
    private final UserService userService;

    private static final String PUBLISHED_DOC_CANDIDATE_NUMBER_RANGE_NAME = "CandidateNumber";

    @Autowired
    public SavedListServiceImpl(
        CandidateRepository candidateRepository,
        CandidateSavedListRepository candidateSavedListRepository,
        CandidateOpportunityService candidateOpportunityService, ExportColumnsService exportColumnsService,
        SavedListRepository savedListRepository,
        DocPublisherService docPublisherService,
        FileSystemService fileSystemService,
        GoogleDriveConfig googleDriveConfig, PublicIDService publicIDService,
        SalesforceService salesforceService,
        SalesforceJobOppService salesforceJobOppService, TaskAssignmentService taskAssignmentService,
        UserRepository userRepository,
        UserService userService) {
        this.candidateRepository = candidateRepository;
        this.candidateSavedListRepository = candidateSavedListRepository;
        this.candidateOpportunityService = candidateOpportunityService;
        this.exportColumnsService = exportColumnsService;
        this.savedListRepository = savedListRepository;
        this.docPublisherService = docPublisherService;
        this.fileSystemService = fileSystemService;
        this.googleDriveConfig = googleDriveConfig;
        this.publicIDService = publicIDService;
        this.salesforceService = salesforceService;
        this.salesforceJobOppService = salesforceJobOppService;
        this.taskAssignmentService = taskAssignmentService;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @Override
    public void addCandidateToList(@NonNull SavedList destinationList, @NonNull Candidate candidate,
        @Nullable String contextNote) {

        //Create new candidate/list link
        final CandidateSavedList csl =
            new CandidateSavedList(candidate, destinationList);
        //Copy across context
        if (contextNote != null) {
            csl.setContextNote(contextNote);
        }

        //If destination list does not already contain the candidate...
        if (!destinationList.getCandidateSavedLists().contains(csl)) {
            //Add candidate to the collection of candidates in this list
            destinationList.getCandidateSavedLists().add(csl);
            //Also update other side of many-to-many relationship, adding this
            //list to the candidate's collection of lists that they belong to.
            candidate.getCandidateSavedLists().add(csl);

            assignListTasksToCandidate(destinationList, candidate);

            //If a submission list, automatically create a candidate opp if needed
            final Boolean isSubmissionList = destinationList.getRegisteredJob();
            final SalesforceJobOpp jobOpp = destinationList.getSfJobOpp();
            if (isSubmissionList && jobOpp != null ) {
                //With no params specified will not change any existing opp associated with this job,
                //but will create a new opp if needed, with stage defaulting to "prospect"
                candidateOpportunityService.createUpdateCandidateOpportunities(
                    Collections.singletonList(candidate), jobOpp, null);
            }
        }
    }

    @Override
    public void addCandidateToList(@NonNull SavedList destinationList, @NonNull Candidate candidate,
        @Nullable SavedList sourceList) {
        //Find any context note for the given candidate and sourceList
        String contextNote = null;
        if (sourceList != null) {
            //Need to copy the context across from the source list.
            //Construct the csl we are looking for...
            CandidateSavedList targetCsl = new CandidateSavedList(candidate, sourceList);

            //Now search for that csl in the candidate's csl's.
            Set<CandidateSavedList> sourceCsls = candidate.getCandidateSavedLists();
            for (CandidateSavedList sourceCsl : sourceCsls) {
                if (sourceCsl.equals(targetCsl)) {
                    //Found context note for the candidate and the sourceList.
                    contextNote = sourceCsl.getContextNote();
                    break;
                }
            }
        }

        addCandidateToList(destinationList, candidate, contextNote);
    }

    @Override
    public void addCandidatesToList(@NonNull SavedList destinationList, @NonNull Iterable<Candidate> candidates,
        @Nullable SavedList sourceList) {
        for (Candidate candidate : candidates) {
            addCandidateToList(destinationList, candidate, sourceList);
        }
    }

    @Override
    public void removeCandidateFromList(@NonNull Candidate candidate, @NonNull SavedList savedList) {
        final CandidateSavedList csl = new CandidateSavedList(candidate, savedList);
        try {
            candidateSavedListRepository.delete(csl);
            csl.getCandidate().getCandidateSavedLists().remove(csl);
            csl.getSavedList().getCandidateSavedLists().remove(csl);

            deactivateIncompleteCandidateListTasks(savedList, candidate);
        } catch (Exception ex) {
            LogBuilder.builder(log)
                .action("RemoveCandidateFromList")
                .message("Could not delete candidate saved list " + csl.getId())
                .logWarn(ex);
        }
    }

    @Override
    public void removeCandidateFromList(long savedListId,
        UpdateExplicitSavedListContentsRequest request) throws NoSuchObjectException {
        SavedList savedList = savedListRepository.findByIdLoadCandidates(savedListId)
            .orElse(null);
        if (savedList == null) {
            throw new NoSuchObjectException(SavedList.class, savedListId);
        }

        Set<Candidate> candidates = fetchCandidates(request);
        for (Candidate candidate : candidates) {
            removeCandidateFromList(candidate, savedList);
        }
    }

    /**
     * Returns active tasks assigned to given candidate
     * @param candidate Candidate whose task assignments we are looking at
     * @return Set of active tasks. Not null, but can be empty set if no active tasks are assigned.
     */
    @NonNull
    private Set<TaskImpl> findActiveCandidateTasks(Candidate candidate) {
        final List<TaskAssignmentImpl> candidateTaskAssignments = candidate.getTaskAssignments();
        //Extract tasks which are actively assigned to the candidate.
        // We don't want to duplicate them.
        Set<TaskImpl> activeCandidateTasks = candidateTaskAssignments.stream()
            .filter(taskAssignment -> taskAssignment.getStatus() == Status.active)
            .map(TaskAssignmentImpl::getTask).collect(Collectors.toSet());
        return activeCandidateTasks;
    }

    /**
     * Assigns each task associated with the given list to the given candidate, unless the candidate
     * already has been assigned the task.
     * <p/>
     * Tasks that are assigned, have the relatedList attribute for the task assignment set to the
     * given list.
     * @param savedList List whose tasks are being assigned
     * @param candidate Candidate being assigned tasks
     */
    private void assignListTasksToCandidate(SavedList savedList, Candidate candidate) {
        Set<TaskImpl> listTasks = savedList.getTasks();

        if (!listTasks.isEmpty()) {
            final User loggedInUser = userService.getLoggedInUser();

            Set<TaskImpl> activeCandidateTasks = findActiveCandidateTasks(candidate);

            //Assign all list tasks to the candidate that they don't already have assigned.
            for (TaskImpl listTask : listTasks) {
                if (!activeCandidateTasks.contains(listTask)) {
                    taskAssignmentService.assignTaskToCandidate(
                        loggedInUser, listTask, candidate, savedList, null);
                }
            }
        }
    }

    /**
     * Checks whether a task assignment could be deactivated
     * @param taskAssignment Task assignment
     * @param savedList Saved list
     * @return True if the given task assignment is active and incomplete and related to the given
     * list
     */
    private boolean isActiveIncompleteListRelatedTaskAssignment(
        TaskAssignment taskAssignment, SavedList savedList) {
        return taskAssignment.getStatus() == Status.active
            && savedList.equals(taskAssignment.getRelatedList())
            && taskAssignment.getCompletedDate() == null
            && taskAssignment.getAbandonedDate() == null;
    }

    /**
     * Deactivates candidate task assignments corresponding to each task associated with the given
     * list where the task assignment is related to this list and where the assigned candidate task
     * has not yet been completed or abandoned.
     * @param savedList List whose task assignments are being deactivated
     * @param candidate Candidate whose task assignments may be deactivated
     */
    private void deactivateIncompleteCandidateListTasks(@NonNull SavedList savedList, @NonNull Candidate candidate) {
        Set<TaskImpl> listTasks = savedList.getTasks();

        if (!listTasks.isEmpty()) {
            final User loggedInUser = userService.getLoggedInUser();

            final List<TaskAssignmentImpl> candidateTaskAssignments = candidate.getTaskAssignments();

            for (TaskAssignmentImpl taskAssignment : candidateTaskAssignments) {
                //If this candidate task assignment is related to this list - and it is still
                //one of the tasks associated with this list, then deactivate it if it is incomplete
                //and still active.
                boolean canDeactivate = listTasks.contains(taskAssignment.getTask())
                    && isActiveIncompleteListRelatedTaskAssignment(taskAssignment, savedList);

                if (canDeactivate) {
                    taskAssignmentService.deactivateTaskAssignment(loggedInUser, taskAssignment.getId());
                }
            }
        }
    }

    @Override
    public void deassociateTaskFromList(User user, TaskImpl task, SavedList list) {

        final Set<TaskImpl> listTasks = list.getTasks();
        listTasks.remove(task);
        savedListRepository.save(list);

        //See if any candidates in the list have task assignments for this task which should be
        //deactivated.
        Set<Candidate> candidates = list.getCandidates();
        for (Candidate candidate : candidates) {
            //Deactivate any active, incomplete candidate task assignments for this task and
            //related to this list.
            final List<TaskAssignmentImpl> taskAssignments = candidate.getTaskAssignments();
            for (TaskAssignmentImpl taskAssignment : taskAssignments) {
                if (taskAssignment.getTask().equals(task)
                    && isActiveIncompleteListRelatedTaskAssignment(taskAssignment, list)) {
                    taskAssignmentService.deactivateTaskAssignment(user, taskAssignment.getId());
                }
            }
        }
    }

    /**
     * Finds folder for the given list on Google Drive, creating one if none found.
     *
     * @param savedList List
     * @throws IOException           if there is a problem creating the folder.
     */
    private void findOrCreateListFolder(SavedList savedList) throws IOException {
        long id = savedList.getId();

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

            // List name folder
            folderName = savedList.getName();
            GoogleFileSystemFolder subfolder = fileSystemService.createFolder(foldersDrive, folder, folderName);
            //The named list folder and its contents are viewable by anyone with the link.
            fileSystemService.publishFolder(subfolder);
            savedList.setFolderlink(subfolder.getUrl());

            createJdSubfolder(savedList, foldersDrive, jobOppIntakeTemplate, subfolder);
        } else {
            //Cope with cases where the list folder exists, but sub folders don't.
            //This is unusual but if it does happen, we should handle it and create those subfolders.

            // List name folder
            folderName = savedList.getName();
            GoogleFileSystemFolder subfolder = fileSystemService.findAFolder(foldersDrive, folder, folderName);
            if (subfolder == null) {
                subfolder = fileSystemService.createFolder(foldersDrive, folder, folderName);
                //The named list folder and its contents are viewable by anyone with the link.
                fileSystemService.publishFolder(subfolder);
            }
            savedList.setFolderlink(subfolder.getUrl());

            // JD folder
            GoogleFileSystemFolder jdfolder =
                fileSystemService.findAFolder(foldersDrive, subfolder,
                    LIST_JOB_DESCRIPTION_SUBFOLDER);
            if (jdfolder == null) {
                jdfolder = createJdSubfolder(savedList, foldersDrive, jobOppIntakeTemplate, subfolder);
            }

            savedList.setFolderjdlink(jdfolder.getUrl());
        }
    }

    private GoogleFileSystemFolder createJdSubfolder(SavedList savedList, GoogleFileSystemDrive foldersDrive,
        GoogleFileSystemFile jobOppIntakeTemplate, GoogleFileSystemFolder subfolder)
        throws IOException {
        // JD folder (note that this folder will inherit the published status of its parent)
        GoogleFileSystemFolder jdfolder =
            fileSystemService.createFolder(foldersDrive, subfolder, LIST_JOB_DESCRIPTION_SUBFOLDER);

        savedList.setFolderjdlink(jdfolder.getUrl());

        // CREATE JOB OPPORTUNITY INTAKE FILE IN JD FOLDER
        String joiFileName = "JobOpportunityIntake - " + savedList.getName();
        GoogleFileSystemFile joiFile = fileSystemService.copyFile(jdfolder, joiFileName, jobOppIntakeTemplate);
        savedList.setFileJoiName(joiFile.getName());
        savedList.setFileJoiLink(joiFile.getUrl());

        return jdfolder;
    }

    @Override
    public SavedList createListFolder(long id) throws NoSuchObjectException, IOException {
        SavedList savedList = get(id);

        findOrCreateListFolder(savedList);

        saveIt(savedList);
        return savedList;
    }

    @Override
    public SavedList createSavedList(User user, UpdateSavedListInfoRequest request)
        throws EntityExistsException, RegisteredListException {

        SalesforceJobOpp sfJobOpp = request.getSfJobOpp();
        if (sfJobOpp == null) {
            final Long jobId = request.getJobId();
            if (jobId != null) {
                sfJobOpp = salesforceJobOppService.getJobOpp(jobId);
            }
        }

        final boolean isRegisteredList =
            request.getRegisteredJob() != null && request.getRegisteredJob();
        if (isRegisteredList) {
            if (sfJobOpp == null) {
                throw new RegisteredListException("Missing Salesforce link for registered job list");
            }
            String jobName = request.getName();
            if (jobName == null) {
                jobName = sfJobOpp.getName();
            }

            //Check for a registered list with same sfJobOpp (owned any user)
            SavedList registeredList = savedListRepository.findRegisteredJobList(sfJobOpp.getSfId())
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
        savedList.setSfJobOpp(sfJobOpp);

        //Populate publicID
        savedList.setPublicId(publicIDService.generatePublicID());

        //Save created list so that we get its id from the database
        savedList.setAuditFields(user);
        return savedListRepository.save(savedList);
    }

    @Override
    @Transactional
    public SavedList createSavedList(UpdateSavedListInfoRequest request)
            throws EntityExistsException {
        final User loggedInUser = userService.getLoggedInUser();
        return createSavedList(loggedInUser, request);
    }

    @Override
    public void createUpdateSalesforce(UpdateCandidateListOppsRequest request)
        throws NoSuchObjectException, SalesforceException, WebClientException {
        SavedList savedList = get(request.getSavedListId());
        SalesforceJobOpp sfJobOpp = savedList.getSfJobOpp();
        candidateOpportunityService.createUpdateCandidateOpportunities(
            savedList.getCandidates(), sfJobOpp, request.getCandidateOppParams());
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
    public boolean isEmpty(long id) throws NoSuchObjectException {
        SavedList savedList = get(id);
        return savedList.getCandidates().isEmpty();
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
        addCandidatesToList(savedList, candidates, sourceList);

        saveIt(savedList);
    }

    @Override
    public void mergeSavedListFromInputStream(long savedListId, InputStream is)
        throws NoSuchObjectException, IOException {

        Set<Long> candidateIds = new HashSet<>();

        //Extract candidate numbers from file, look up the id and add to candidateIds
        //We need candidateIds to pass to other methods.
        CSVReader reader = new CSVReader(new InputStreamReader(is));
        String [] tokens;
        try {
            boolean possibleHeader = true;
            while ((tokens = reader.readNext()) != null) {
                //tokens[] is an array of values from the line
                //Ignore empty tokens
                if (tokens.length > 0 && !tokens[0].isEmpty()) {
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
    public List<SavedList> search(long candidateId, SearchSavedListRequest request) {
        final User loggedInUser = userService.getLoggedInUser();

        GetSavedListsQuery getSavedListsQuery = new GetSavedListsQuery(request, loggedInUser);

        GetCandidateSavedListsQuery getCandidateSavedListsQuery =
                new GetCandidateSavedListsQuery(candidateId);

        //Set standard sort to ascending by name.
        Sort sort = Sort.by(Sort.Direction.ASC, "name");
        return savedListRepository.findAll(
                where(getSavedListsQuery).and(getCandidateSavedListsQuery),
                sort);
    }

    @Override
    public List<SavedList> search(IdsRequest request) {
        return savedListRepository.findByIds(request.getIds());
    }

    @Override
    public List<SavedList> search(SearchSavedListRequest request) {
        final User loggedInUser = userService.getLoggedInUser();
        GetSavedListsQuery getSavedListsQuery = new GetSavedListsQuery(request, loggedInUser);

        //The request is not required to provide paging or sorting info and
        //we ignore any such info if present because we don't pass a PageRequest
        //to the repository findAll call.
        //But set standard sort to ascending by name.
        Sort sort = Sort.by(Sort.Direction.ASC, "name");
        return savedListRepository.findAll(getSavedListsQuery, sort);
    }

    @Override
    public Page<SavedList> searchPaged(SearchSavedListRequest request) {
        final User loggedInUser = userService.getLoggedInUser();
        GetSavedListsQuery getSavedListsQuery = new GetSavedListsQuery(request, loggedInUser);

        //The incoming request will have paging info but may have no sorting.
        //If not, default the sort to ascending by name.
        if (request.getSortDirection() == null) {
            request.setSortDirection(Sort.Direction.ASC);
            request.setSortFields(new String[]{"name"});
        }

        PageRequest pageRequest = request.getPageRequest();
        return savedListRepository.findAll(getSavedListsQuery, pageRequest);
    }

    @Override
    public void setCandidateContext(long savedListId, Iterable<Candidate> candidates) {
        for (Candidate candidate : candidates) {
            candidate.setContextSavedListId(savedListId);
        }
    }

    @Transactional
    @Override
    public void setPublicIds(List<SavedList> savedLists) {
        for (SavedList savedList : savedLists) {
            if (savedList.getPublicId() == null) {
                savedList.setPublicId(publicIDService.generatePublicID());
            }
        }
        if (!savedLists.isEmpty()) {
            savedListRepository.saveAll(savedLists);
        }
    }

    @Override
    public SavedList updateSavedList(long savedListId, UpdateSavedListInfoRequest request)
            throws NoSuchObjectException, EntityExistsException {
        final User loggedInUser = userService.getLoggedInUser();
        if (loggedInUser != null) {
            checkDuplicates(savedListId, request.getName(), loggedInUser);
        }
        SavedList savedList = get(savedListId);
        request.populateFromRequest(savedList);

        final Long jobId = request.getJobId();
        if (jobId != null) {
            final SalesforceJobOpp jobOpp =
                jobId < 0 ? null : salesforceJobOppService.getJobOpp(jobId);
            savedList.setSfJobOpp(jobOpp);
        }

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
    public SavedList updateTbbShortName(UpdateShortNameRequest request) throws  NoSuchObjectException {
        SavedList savedList = get(request.getSavedListId());
        // Check for duplicate short names if not null, can't have same short name.
        SavedList existingShortName = null;
        if (request.getTbbShortName() != null) {
            existingShortName = this.savedListRepository.findByShortNameIgnoreCase(request.getTbbShortName()).orElse(null);
        }
        if (existingShortName != null && !existingShortName.getId().equals(request.getSavedListId())) {
            throw new EntityExistsException("external link");
        }
        savedList.setTbbShortName(request.getTbbShortName());
        return saveIt(savedList);
    }

    @Override
    public SavedList findByShortName(String shortName) throws  NoSuchObjectException {
        return this.savedListRepository.findByShortNameIgnoreCase(shortName).orElse(null);
    }

    @Override
    @NonNull
    public List<SavedList> findListsAssociatedWithJobs() {
        return this.savedListRepository.findListsWithJobs();
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
    public void associateTaskWithList(User user, TaskImpl task, SavedList list) {

        final Set<TaskImpl> listTasks = list.getTasks();
        listTasks.add(task);
        savedListRepository.save(list);

        //Now assign tasks to candidates in list (if they do not already have the task actively assigned)
        Set<Candidate> candidates = list.getCandidates();
        for (Candidate candidate : candidates) {
            //Assign task if candidate does not already have this task active
            Set<? extends Task> activeCandidateTasks = findActiveCandidateTasks(candidate);
            if (!activeCandidateTasks.contains(task)) {
                taskAssignmentService.assignTaskToCandidate(user, task, candidate, list, null);
            }
        }
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
                    salesforceService.updateCandidateOpportunities(feedbacks, savedList.getSfJobOpp());
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

        List<PublishedDocColumnDef> columnInfos = request.getConfiguredColumns();

        //Create the doc in the list folder.
        GoogleFileSystemDrive drive = googleDriveConfig.getListFoldersDrive();
        GoogleFileSystemFolder listFolder = new GoogleFileSystemFolder(savedList.getFolderlink());

        //Set other data to publish.
        Map<String, Object> props = new HashMap<>();
        props.put("listDescription", savedList.getDescription());
        props.put("listId", savedList.getId());
        props.put("listName", savedList.getName());
        props.put("timeCreated", LocalDate.now().toString());
        User user = userService.getLoggedInUser();
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
            } else if (def.getType().equals(PublishedDocColumnType.YesNoDropdown)) {
                List<String> values = Arrays.asList("", "Yes", "No");
                columnSetUp.setDropDowns(values);
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

        //Candidates to publish
        List<Candidate> candidates;
        SalesforceJobOpp sfJob = savedList.getSfJobOpp();
        if (sfJob != null &&
            request.getPublishClosedOpps() != null && !request.getPublishClosedOpps()) {
            //For job lists where publishClosedOpps is false, filter out closed opps (unless they
            //are "won" - we always publish won opps, which are a special kind of closed).
            candidates = savedList.getCandidates().stream().filter(
                candidate -> {
                    return candidate.getCandidateOpportunities().stream()
                        .anyMatch(opp -> {
                            return opp.getJobOpp().getId().equals(sfJob.getId())
                                && (opp.isWon() || !opp.isClosed());
                        });
                }
            ).toList();
        } else {
            //Publish all for non job lists, or job lists where we have been asked to publish all
            candidates = new ArrayList<>(savedList.getCandidates());
        }

        //Create an empty doc - leaving room for the number of candidates
        String link = docPublisherService.createPublishedDoc(listFolder, savedList.getName(),
                publishedSheetDataRangeName, candidates.size() + 1, props, columnSetUpMap);

        //Populate candidate data in doc.
        //This is processed asynchronously so pass candidate ids, rather than candidate entities
        //which will not in a persistence context in the Async processing. They will need to
        //be reloaded from the database using their ids.
        List<Long> candidateIds = candidates.stream().map(Candidate::getId).collect(Collectors.toList());
        docPublisherService.populatePublishedDoc(link, savedList.getId(), candidateIds, columnInfos,
            publishedSheetDataRangeName);

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

    public @NonNull Set<Candidate> fetchCandidates(IHasSetOfCandidates request)
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

    @NonNull
    @Override
    public Set<Long> fetchCandidateIds(long listId) {
        return savedListRepository.findUnionOfCandidates(Collections.singletonList(listId));
    }

    @Nullable
    @Override
    public Set<Long> fetchUnionCandidateIds(@Nullable List<Long> listIds) {
        Set<Long> candidateIds;
        if (listIds == null) {
            candidateIds = null;
        } else {
            candidateIds = savedListRepository.findUnionOfCandidates(listIds);
        }
        return candidateIds;
    }

    @Nullable
    @Override
    public Set<Long> fetchIntersectionCandidateIds(@Nullable List<Long> listIds) {
        Set<Long> candidateIds;
        if (listIds == null) {
            candidateIds = null;
        } else {
            final Iterator<Long> iterator = listIds.iterator();
            if (iterator.hasNext()) {
                candidateIds = fetchCandidateIds(iterator.next());
                while (iterator.hasNext() && !candidateIds.isEmpty()) {
                    long listId = iterator.next();
                    candidateIds.retainAll(fetchCandidateIds(listId));
                }
            } else {
                //No lists provided. Return empty set of candidate ids.
                candidateIds = new HashSet<>();
            }
        }
        return candidateIds;
    }

    @Nullable
    public SavedList fetchSourceList(UpdateSavedListContentsRequest request)
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
    public SavedList saveIt(SavedList savedList) {
        savedList.setAuditFields(userService.getLoggedInUser());
        return savedListRepository.save(savedList);
    }

    public void updateAssociatedListsNames(SalesforceJobOpp job) {
        SavedList subList = job.getSubmissionList();
        SavedList excList = job.getExclusionList();

        if (subList != null) {
            subList.setName(job.getName() + REGISTERED_NAME_SUFFIX);
            saveIt(subList);
        }

        if (excList != null) {
            excList.setName(job.getName() + REGISTERED_NAME_SUFFIX + EXCLUSION_LIST_SUFFIX);
            saveIt(excList);
        }
    }

}
