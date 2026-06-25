package org.tctalent.server.api.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.FileList;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.Job;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.tctalent.server.batchjob.candidate.CandidateBatchJobFactory;
import org.tctalent.server.batchjob.candidate.CandidateBatchJobFactory.CandidateBatchJobBuilder;
import org.tctalent.server.casi.application.providers.linkedin.LinkedInService;
import org.tctalent.server.configuration.GoogleDriveConfig;
import org.tctalent.server.configuration.SalesforceConfig;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.AttachmentType;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateNote;
import org.tctalent.server.model.db.CandidateOpportunity;
import org.tctalent.server.model.db.CandidateStatus;
import org.tctalent.server.model.db.Country;
import org.tctalent.server.model.db.EducationType;
import org.tctalent.server.model.db.JobChat;
import org.tctalent.server.model.db.JobOpportunityStage;
import org.tctalent.server.model.db.NoteType;
import org.tctalent.server.model.db.PartnerImpl;
import org.tctalent.server.model.db.SalesforceJobOpp;
import org.tctalent.server.model.db.SavedList;
import org.tctalent.server.model.db.SavedSearch;
import org.tctalent.server.model.db.Status;
import org.tctalent.server.model.db.User;
import org.tctalent.server.model.db.partner.Partner;
import org.tctalent.server.model.sf.Contact;
import org.tctalent.server.repository.db.CandidateNoteRepository;
import org.tctalent.server.repository.db.CandidateOpportunityRepository;
import org.tctalent.server.repository.db.CandidateRepository;
import org.tctalent.server.repository.db.ChatPostRepository;
import org.tctalent.server.repository.db.JobChatRepository;
import org.tctalent.server.repository.db.JobChatUserRepository;
import org.tctalent.server.repository.db.PartnerRepository;
import org.tctalent.server.repository.db.SalesforceJobOppRepository;
import org.tctalent.server.repository.db.SavedListRepository;
import org.tctalent.server.repository.db.SavedSearchRepository;
import org.tctalent.server.request.candidate.SavedListGetRequest;
import org.tctalent.server.request.candidate.SearchCandidateRequest;
import org.tctalent.server.request.job.SearchJobRequest;
import org.tctalent.server.request.job.UpdateJobRequest;
import org.tctalent.server.response.DuolingoCouponResponse;
import org.tctalent.server.response.DuolingoDashboardResponse;
import org.tctalent.server.response.DuolingoVerifyScoreResponse;
import org.tctalent.server.security.AuthService;
import org.tctalent.server.service.api.TcApiService;
import org.tctalent.server.service.db.BackgroundProcessingService;
import org.tctalent.server.service.db.BatchJobService;
import org.tctalent.server.service.db.CandidateOppBackgroundProcessingService;
import org.tctalent.server.service.db.CandidateOpportunityService;
import org.tctalent.server.service.db.CandidateService;
import org.tctalent.server.service.db.CountryService;
import org.tctalent.server.service.db.DataSharingService;
import org.tctalent.server.service.db.DuolingoApiService;
import org.tctalent.server.service.db.DuolingoCouponService;
import org.tctalent.server.service.db.FileSystemService;
import org.tctalent.server.service.db.JobService;
import org.tctalent.server.service.db.LanguageService;
import org.tctalent.server.service.db.NotificationService;
import org.tctalent.server.service.db.PartnerService;
import org.tctalent.server.service.db.SalesforceService;
import org.tctalent.server.service.db.SavedListService;
import org.tctalent.server.service.db.SavedSearchService;
import org.tctalent.server.service.db.UserService;
import org.tctalent.server.service.db.aws.S3ResourceHelper;
import org.tctalent.server.service.db.cache.CacheService;
import org.tctalent.server.storage.TranslationMigrationService;
import org.tctalent.server.util.filesystem.GoogleFileSystemDrive;
import org.tctalent.server.util.filesystem.GoogleFileSystemFile;
import org.tctalent.server.util.filesystem.GoogleFileSystemFolder;

@ExtendWith(MockitoExtension.class)
class SystemAdminApiTest {

  @Mock
  private BackgroundProcessingService backgroundProcessingService;

  @Mock
  private AuthService authService;

  @Mock
  private TcApiService tcApiService;

  @Mock
  private CandidateOpportunityRepository candidateOpportunityRepository;

  @Mock
  private CandidateOpportunityService candidateOpportunityService;

  @InjectMocks
  private SystemAdminApi systemAdminApi;

  @Mock
  private SalesforceService salesforceService;

  @Mock
  private CandidateRepository candidateRepository;

  @Mock
  private S3ResourceHelper s3ResourceHelper;

  @Mock
  private GoogleDriveConfig googleDriveConfig;

  @Mock
  private SalesforceConfig salesforceConfig;

  @Mock
  private DataSharingService dataSharingService;

  @Mock
  private Drive googleDriveService;

  @Mock
  private Drive.Files driveFiles;

  @Mock
  private Drive.Files.List fileListRequest;

  @Mock
  private Connection connection;

  @Mock
  private Statement statement;

  @Mock
  private ResultSet resultSet;

  @Mock
  private PartnerRepository partnerRepository;

  @Mock
  private CandidateBatchJobFactory candidateBatchJobFactory;

  @Mock
  private CandidateNoteRepository candidateNoteRepository;

  @Mock
  private CandidateService candidateService;

  @Mock
  private CountryService countryService;

  @Mock
  private FileSystemService fileSystemService;

  @Mock
  private JobService jobService;

  @Mock
  private LanguageService languageService;

  @Mock
  private NotificationService notificationService;

  @Mock
  private SalesforceJobOppRepository salesforceJobOppRepository;

  @Mock
  private SavedListService savedListService;

  @Mock
  private SavedListRepository savedListRepository;

  @Mock
  private JobChatRepository jobChatRepository;

  @Mock
  private JobChatUserRepository jobChatUserRepository;

  @Mock
  private ChatPostRepository chatPostRepository;

  @Mock
  private SavedSearchRepository savedSearchRepository;

  @Mock
  private TranslationMigrationService translationMigrationService;

  @Mock
  private CacheService cacheService;

  @Mock
  private BatchJobService batchJobService;

  @Mock
  private SavedSearchService savedSearchService;

  @Mock
  private PartnerService partnerService;

  @Mock
  private CandidateOppBackgroundProcessingService candidateOppBackgroundProcessingService;

  @Mock
  private DuolingoApiService duolingoApiService;

  @Mock
  private DuolingoCouponService duolingoCouponService;

  @Mock
  private LinkedInService linkedInService;

  @Mock
  private UserService userService;

  @Mock private EntityManager entityManager;

  @Mock
  private CandidateBatchJobBuilder candidateBatchJobBuilder;

  @Mock
  private Job batchJob;

  @Mock
  private PreparedStatement preparedStatement;

  @Captor
  private ArgumentCaptor<SearchCandidateRequest> searchCandidateRequestCaptor;
  @Captor private ArgumentCaptor<UpdateJobRequest> updateJobRequestCaptor;
  @Captor private ArgumentCaptor<CandidateNote> candidateNoteCaptor;

  private User loggedInUser;


  @BeforeEach
  void setUp() {
    loggedInUser = new User();
    loggedInUser.setId(42L);
    loggedInUser.setUsername("system-admin");
    lenient().when(authService.getLoggedInUser()).thenReturn(Optional.of(loggedInUser));
    ReflectionTestUtils.setField(systemAdminApi, "entityManager", entityManager);
    ReflectionTestUtils.setField(systemAdminApi, "candidateDataDriveId", "candidate-drive");
    ReflectionTestUtils.setField(systemAdminApi, "candidateRootFolderId", "candidate-root");
    ReflectionTestUtils.setField(systemAdminApi, "listFoldersDriveId", "list-drive");
    ReflectionTestUtils.setField(systemAdminApi, "listFoldersRootId", "list-root");
  }

  @Test
  void testSetPublicIds() {
    // Act
    systemAdminApi.setPublicIds();

    // Verify
    verify(backgroundProcessingService, times(1)).setCandidatePublicIds();
    verify(backgroundProcessingService, times(1)).setPartnerPublicIds();
    verify(backgroundProcessingService, times(1)).setSavedListPublicIds();
    verify(backgroundProcessingService, times(1)).setSavedSearchPublicIds();
    verifyNoMoreInteractions(backgroundProcessingService);
  }

  @Test
  void testRunApiMigration_Success() {
    // Arrange
    String migrationResponse = "Migration completed successfully";
    when(tcApiService.runApiMigration()).thenReturn(migrationResponse);

    // Act
    ResponseEntity<String> response = systemAdminApi.runApiMigration();

    // Verify
    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(migrationResponse, response.getBody());
    verify(tcApiService, times(1)).runApiMigration();
  }

  @Test
  void testRunApiMigrationByListId_Success() {
    // Arrange
    long listId = 123L;
    String migrationResponse = "Migration for list 123 completed";
    when(tcApiService.runApiMigrationByListId(listId)).thenReturn(migrationResponse);

    // Act
    ResponseEntity<String> response = systemAdminApi.runApiMigrationByListId(listId);

    // Verify
    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(migrationResponse, response.getBody());
    verify(tcApiService, times(1)).runApiMigrationByListId(listId);
  }

  @Test
  void testRunApiMigrationByName_Mongo_Success() {
    // Arrange
    String migrationName = "mongo";
    String migrationResponse = "Mongo migration completed";
    when(tcApiService.runMongoMigration()).thenReturn(migrationResponse);

    // Act
    ResponseEntity<String> response = systemAdminApi.runApiMigration(migrationName);

    // Verify
    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(migrationResponse, response.getBody());
    verify(tcApiService, times(1)).runMongoMigration();
    verify(tcApiService, never()).runAuroraMigration();
  }

  @Test
  void testRunApiMigrationByName_Aurora_Success() {
    // Arrange
    String migrationName = "aurora";
    String migrationResponse = "Aurora migration completed";
    when(tcApiService.runAuroraMigration()).thenReturn(migrationResponse);

    // Act
    ResponseEntity<String> response = systemAdminApi.runApiMigration(migrationName);

    // Verify
    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(migrationResponse, response.getBody());
    verify(tcApiService, times(1)).runAuroraMigration();
    verify(tcApiService, never()).runMongoMigration();
  }

  @Test
  void testRunApiMigrationByName_InvalidName() {
    // Arrange
    String migrationName = "invalid";

    // Act
    ResponseEntity<String> response = systemAdminApi.runApiMigration(migrationName);

    // Verify
    assertNotNull(response);
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertEquals("Invalid migration name: " + migrationName, response.getBody());
    verify(tcApiService, never()).runMongoMigration();
    verify(tcApiService, never()).runAuroraMigration();
  }

  @Test
  void testRunApiMigrationByName_NullName() {
    // Arrange
    String migrationName = null;

    // Act
    ResponseEntity<String> response = systemAdminApi.runApiMigration(migrationName);

    // Verify
    assertNotNull(response);
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertEquals("Migration name cannot be null or empty", response.getBody());
    verify(tcApiService, never()).runMongoMigration();
    verify(tcApiService, never()).runAuroraMigration();
  }

  @Test
  void testFixNullCaseSfids_Success() {
    // Arrange
    CandidateOpportunity opp1 = new CandidateOpportunity();
    opp1.setId(1L);
    CandidateOpportunity opp2 = new CandidateOpportunity();
    opp2.setId(2L);
    List<CandidateOpportunity> opps = Arrays.asList(opp1, opp2);
    when(candidateOpportunityRepository.findAllBySfIdIsNull()).thenReturn(opps);
    when(candidateOpportunityService.fetchSalesforceId(opp1)).thenReturn("SFID1");
    when(candidateOpportunityService.fetchSalesforceId(opp2)).thenReturn("SFID2");
    when(authService.getLoggedInUser()).thenReturn(Optional.of(new org.tctalent.server.model.db.User()));

    // Act
    systemAdminApi.fixNullCaseSfids();

    // Verify
    verify(candidateOpportunityRepository, times(1)).findAllBySfIdIsNull();
    verify(candidateOpportunityService, times(1)).fetchSalesforceId(opp1);
    verify(candidateOpportunityService, times(1)).fetchSalesforceId(opp2);
    verify(candidateOpportunityRepository, times(1)).save(opp1);
    verify(candidateOpportunityRepository, times(1)).save(opp2);
    assertEquals("SFID1", opp1.getSfId());
    assertEquals("SFID2", opp2.getSfId());
  }

  @Test
  void testFixNullCaseSfids_NoSalesforceId() {
    // Arrange
    CandidateOpportunity opp = new CandidateOpportunity();
    opp.setId(1L);
    List<CandidateOpportunity> opps = List.of(opp);
    when(candidateOpportunityRepository.findAllBySfIdIsNull()).thenReturn(opps);
    when(candidateOpportunityService.fetchSalesforceId(opp)).thenReturn(null);
    when(authService.getLoggedInUser()).thenReturn(Optional.of(new org.tctalent.server.model.db.User()));

    // Act
    systemAdminApi.fixNullCaseSfids();

    // Verify
    verify(candidateOpportunityRepository, times(1)).findAllBySfIdIsNull();
    verify(candidateOpportunityService, times(1)).fetchSalesforceId(opp);
    verify(candidateOpportunityRepository, never()).save(opp);
    assertNull(opp.getSfId());
  }

  @Test
  void testUpdateCandidateSalesforceLinks_withExistingCandidates() throws Exception {
    Contact contact1 = new Contact();
    contact1.setTbbId(123L);

    Contact contact2 = new Contact();
    contact2.setTbbId(456L);

    Candidate candidate1 = new Candidate();
    Candidate candidate2 = new Candidate();

    when(salesforceService.findCandidateContacts()).thenReturn(List.of(contact1, contact2));
    when(candidateRepository.findByCandidateNumber("123")).thenReturn(candidate1);
    when(candidateRepository.findByCandidateNumber("456")).thenReturn(candidate2);

    String result = systemAdminApi.updateCandidateSalesforceLinks();

    assertEquals("done", result);
    verify(candidateRepository, times(2)).save(any(Candidate.class));
  }

  @Test
  void testUpdateContactsMatchingCondition_withValidTbbIds() throws Exception {
    Contact contact = new Contact();
    contact.setTbbId(789L);

    Candidate candidate = new Candidate();

    when(salesforceService.findContacts("status:active")).thenReturn(List.of(contact));
    when(candidateRepository.findByCandidateNumber("789")).thenReturn(candidate);

    String result = systemAdminApi.updateContactsMatchingCondition("status:active");

    assertEquals("done", result);
    verify(salesforceService).updateContact(candidate);
  }

  @Test
  void testMigrateGoogleDriveFolders_singlePage_successfulMigration() throws Exception {
    User mockUser = new User();
    mockUser.setUsername("test-user");

    when(authService.getLoggedInUser()).thenReturn(Optional.of(mockUser));
    when(googleDriveConfig.getGoogleDriveService()).thenReturn(googleDriveService);
    when(googleDriveService.files()).thenReturn(driveFiles);
    com.google.api.services.drive.model.File folder = new com.google.api.services.drive.model.File();
    folder.setId("folder-id");
    folder.setName("Candidate_12345");
    folder.setWebViewLink("https://drive.google.com/folderview?id=folder-id");

    FileList mockFileList = new FileList()
        .setFiles(List.of(folder))
        .setNextPageToken(null);

    when(driveFiles.list()).thenReturn(fileListRequest);
    when(fileListRequest.setQ(any())).thenReturn(fileListRequest);
    when(fileListRequest.setSupportsAllDrives(true)).thenReturn(fileListRequest);
    when(fileListRequest.setIncludeItemsFromAllDrives(true)).thenReturn(fileListRequest);
    when(fileListRequest.setCorpora("drive")).thenReturn(fileListRequest);
    when(fileListRequest.setDriveId(any())).thenReturn(fileListRequest);
    when(fileListRequest.setPageToken(any())).thenReturn(fileListRequest);
    when(fileListRequest.setPageSize(anyInt())).thenReturn(fileListRequest);
    when(fileListRequest.setFields(any())).thenReturn(fileListRequest);
    when(fileListRequest.execute()).thenReturn(mockFileList);

    Candidate candidate = new Candidate();
    when(candidateRepository.findByCandidateNumber("12345")).thenReturn(candidate);

    String result = systemAdminApi.migrateGoogleDriveFolders();

    assertEquals("done", result);
    assertEquals("https://drive.google.com/folderview?id=folder-id", candidate.getFolderlink());
    verify(candidateRepository).save(candidate);
  }
  @Test
  void testSetCandidateFolderLink_candidateNotFound_logsError() {
    com.google.api.services.drive.model.File folder = new com.google.api.services.drive.model.File();
    folder.setName("CN99999");
    folder.setWebViewLink("https://drive.google.com/folderview?id=fake");

    when(candidateRepository.findByCandidateNumber("99999")).thenReturn(null);

    systemAdminApi.setCandidateFolderLink(folder);

    verify(candidateRepository, never()).save(any());
  }
  @Test
  void testCheckForCN_validCandidateNumber() {
    String result = systemAdminApi.checkForCN("CN12345 Folder");
    assertEquals("12345", result);
  }

  @Test
  void testCheckForCN_noNumberInName() {
    String result = systemAdminApi.checkForCN("NoDigitsHere");
    assertEquals("", result);
  }
  @Test
  void testDoDbCopy_callsServiceAndReturnsDone() throws Exception {
    String result = systemAdminApi.doDBCopy();

    assertEquals("done", result);
    verify(dataSharingService).dbCopy();
  }

  @Test
  void testLoadCandidateOccupations() throws Exception {
    // Mock the connection to return a mock statement
    when(connection.createStatement()).thenReturn(statement);

    // Mock the statement to return a mock result set
    when(statement.executeQuery("select id, candidate_id, occupation_id from candidate_occupation"))
        .thenReturn(resultSet);

    // Mock the result set rows
    when(resultSet.next()).thenReturn(true, true, false);
    when(resultSet.getLong(1)).thenReturn(1L, 2L); // id
    when(resultSet.getLong(2)).thenReturn(100L, 200L); // candidate_id
    when(resultSet.getLong(3)).thenReturn(300L, 400L); // occupation_id

    //MODEL: Using ReflectionTestUtils to invoke the private method 'loadCandidateOccupations' on the 'systemAdminApi' instance.
    // This approach allows testing internal logic without changing the method's visibility.
    // Call the actual method
    Map<String, Long> result = ReflectionTestUtils.invokeMethod(systemAdminApi, "loadCandidateOccupations", connection);

    // Assert results
    assertEquals(2, result.size());
    assertEquals(1L, result.get("100~300"));
    assertEquals(2L, result.get("200~400"));
  }
  @Test
  void testGetSkillTimePeriod_knownCode() {
    String result = ReflectionTestUtils.invokeMethod(systemAdminApi, "getSkillTimePeriod", 336);
    assertEquals("1 year or less", result);
  }

  @Test
  void testGetSkillTimePeriod_unknownCode() {
    String result = ReflectionTestUtils.invokeMethod(systemAdminApi, "getSkillTimePeriod", 999);
    assertNull(result);
  }

  @Test
  void testGetPaid() {
    Boolean result1 = ReflectionTestUtils.invokeMethod(systemAdminApi, "getPaid", 9557);
    Boolean result2 = ReflectionTestUtils.invokeMethod(systemAdminApi, "getPaid", 0);
    assertEquals(Boolean.TRUE, result1);
    assertNotEquals(Boolean.TRUE, result2);
  }

  @Test
  void testGetFullTime() {
    Boolean result1 = ReflectionTestUtils.invokeMethod(systemAdminApi, "getFullTime", 9561);
    Boolean result2 = ReflectionTestUtils.invokeMethod(systemAdminApi, "getFullTime", 9562);
    assertEquals(Boolean.TRUE, result1);
    assertNotEquals(Boolean.TRUE, result2);
  }

  @Test
  void testGetEducationLevel_knownValues() {
    String result1 = ReflectionTestUtils.invokeMethod(systemAdminApi, "getEducationLevel", 6864);
    String result2 = ReflectionTestUtils.invokeMethod(systemAdminApi, "getEducationLevel", 0);
    assertEquals("Bachelor", result1);
    assertNull(result2);
  }

  @Test
  void testGetEducationType_someUniversity() {
    String result = ReflectionTestUtils.invokeMethod(systemAdminApi, "getEducationType", "Some University");
    assertEquals("Bachelor", result);
  }

  @Test
  void testGetUNStatus() {
    Boolean result1 = ReflectionTestUtils.invokeMethod(systemAdminApi, "getUNStatus", 1);
    Boolean result2 = ReflectionTestUtils.invokeMethod(systemAdminApi, "getUNStatus", 2);
    Boolean result3 = ReflectionTestUtils.invokeMethod(systemAdminApi, "getUNStatus", 3);
    assertEquals(Boolean.TRUE, result1);
    assertEquals(Boolean.TRUE, result2);
    assertNotEquals(Boolean.TRUE, result3);
  }
  @Test
  void testGetGender_valid() {
    String result1 = ReflectionTestUtils.invokeMethod(systemAdminApi, "getGender", "M");
    String result2 = ReflectionTestUtils.invokeMethod(systemAdminApi, "getGender", "F");
    assertEquals("male", result1);
    assertEquals("female", result2);
  }

  @Test
  void testGetGender_invalid() {
    String result = ReflectionTestUtils.invokeMethod(systemAdminApi, "getGender", "X");
    assertNull(result);
  }

  @Test
  void testGetCandidateStatus_draftDueToNullNationality() {
    String result = ReflectionTestUtils.invokeMethod(systemAdminApi, "getCandidateStatus", 1, null, 10);
    assertEquals("draft", result);
  }

  @Test
  void testGetCandidateStatus_deleted() {
    String result = ReflectionTestUtils.invokeMethod(systemAdminApi, "getCandidateStatus", 0, 1, 1);
    assertEquals("deleted", result);
  }

  @Test
  void testGetCandidateStatus_active() {
    String result = ReflectionTestUtils.invokeMethod(systemAdminApi, "getCandidateStatus", 11, 1, 1);
    assertEquals("active", result);
  }

  @Test
  void testGetUserStatus_deleted() {
    String result = ReflectionTestUtils.invokeMethod(systemAdminApi, "getUserStatus", 0);
    assertEquals("inactive", result);
  }

  @Test
  void testGetUserStatus_knownActive() {
    String result1 = ReflectionTestUtils.invokeMethod(systemAdminApi, "getUserStatus", 1);
    String result2 = ReflectionTestUtils.invokeMethod(systemAdminApi, "getUserStatus", 10);
    assertEquals("active", result1);
    assertEquals("active", result2);
  }

  @Test
  void testGetUserStatus_unknown() {
    String result = ReflectionTestUtils.invokeMethod(systemAdminApi, "getUserStatus", 99);
    assertEquals("deleted", result);
  }

  @Test
  void testCheckReference_shouldReturnValidReference() {
    Set<Long> refs = Set.of(1L, 2L, 3L);
    Long result = ReflectionTestUtils.invokeMethod(systemAdminApi, "checkReference", 2, refs);
    assertEquals(2L, result);
  }

  @Test
  void testLoadCandidateIds() throws Exception {
    when(connection.createStatement()).thenReturn(statement);
    when(authService.getLoggedInUser()).thenReturn(Optional.of(new User()));
    when(statement.executeQuery("select id, user_id from candidate")).thenReturn(resultSet);

    when(resultSet.next()).thenReturn(true, false);
    when(resultSet.getLong(1)).thenReturn(1L); // id
    when(resultSet.getLong(2)).thenReturn(100L); // user_id

    Map<Long, Long> result = ReflectionTestUtils.invokeMethod(systemAdminApi, "loadCandidateIds", connection);

    assertEquals(1, result.size());
    assertEquals(1L, result.get(100L));
  }

  @Test
  void testLoadAdminIds() throws Exception {
    when(connection.createStatement()).thenReturn(statement);
    when(authService.getLoggedInUser()).thenReturn(Optional.of(new User()));
    when(statement.executeQuery("select id from users where role = 'admin'")).thenReturn(resultSet);
    when(resultSet.next()).thenReturn(true, true, false);
    when(resultSet.getLong(1)).thenReturn(1L, 2L);

    Set<Long> result = ReflectionTestUtils.invokeMethod(systemAdminApi, "loadAdminIds", connection);

    assertTrue(result.contains(1L));
    assertTrue(result.contains(2L));
  }

  @Test
  void testWhackyExtraCountryLookup_found() {
    Long result = ReflectionTestUtils.invokeMethod(systemAdminApi, "whackyExtraCountryLookup", 358);
    assertEquals(6288L, result);
  }

  @Test
  void testWhackyExtraCountryLookup_notFound() {
    Long result = ReflectionTestUtils.invokeMethod(systemAdminApi, "whackyExtraCountryLookup", 9999);
    assertNull(result);
  }

  @Test
  void testGetExtraCountryMappings() {
    Map<Integer, Integer> map = ReflectionTestUtils.invokeMethod(systemAdminApi, "getExtraCountryMappings");
    assertEquals(8, map.size());
    assertEquals(6288, map.get(358));
    assertEquals(6327, map.get(9444));
  }

  @Test
  void testConvertToDate_invalid() {
    Date result = ReflectionTestUtils.invokeMethod(systemAdminApi, "convertToDate", "not-a-date");
    assertNull(result);
  }

  @Test
  void testIsDateValid_true() {
    Boolean result = ReflectionTestUtils.invokeMethod(systemAdminApi, "isDateValid", "11-11-2024 ");
    assertEquals(Boolean.TRUE, result);
  }

  @Test
  void testIsDateValid_false() {
    Boolean result = ReflectionTestUtils.invokeMethod(systemAdminApi, "isDateValid", "invalid-date");
    assertNotEquals(Boolean.TRUE, result);
  }

  @Test
  void testConvertToTimestamp_valid() {
    Long epoch = 1609459200L; // Jan 1, 2021
    Timestamp ts = ReflectionTestUtils.invokeMethod(systemAdminApi, "convertToTimestamp", epoch);
    assertNotNull(ts);
    assertEquals(Instant.ofEpochMilli(epoch * 1000), ts.toInstant());
  }

  @Test
  void testGetDate_exceptionInParsing() throws Exception {
    when(resultSet.getDate("dob")).thenThrow(new SQLException("error"));
    when(resultSet.getString("dob")).thenReturn("invalid");

    // avoid actual logging failures
    when(authService.getLoggedInUser()).thenReturn(Optional.empty());

    Date date = ReflectionTestUtils.invokeMethod(systemAdminApi, "getDate", resultSet, "dob", 1L);
    assertNull(date);
  }



  @Test
  void migrateTranslations_normalizesPrefixesAndReturnsResultMap() {
    when(translationMigrationService.migrateBucketContents(
        "legacy", "translations/", "new", "custom/")).thenReturn(3);

    Map<String, Object> result = systemAdminApi.migrateTranslations(
        "legacy", "new", null, "custom");

    assertEquals("success", result.get("status"));
    assertEquals("relay-copy", result.get("mode"));
    assertEquals(3, result.get("copiedCount"));
    assertEquals("legacy", result.get("sourceBucket"));
    assertEquals("translations/", result.get("sourcePrefix"));
    assertEquals("new", result.get("destinationBucket"));
    assertEquals("custom/", result.get("destinationPrefix"));
    assertTrue((Long) result.get("durationMs") >= 0L);
  }

  @Test
  void clearFirstDpaSeen_clearsAndSavesPartner() {
    PartnerImpl partner = mock(PartnerImpl.class);
    when(partnerService.getPartner(99L)).thenReturn(partner);

    systemAdminApi.clearFirstDpaSeen(99L);

    verify(partner).setFirstDpaSeenDate(null);
    verify(partnerRepository).save(partner);
  }

  @Test
  void setCandidateText_allCandidatesBuildsDefaultSearchAndLaunchesJob() throws Exception {
    when(candidateBatchJobFactory.builder(
        eq("candidateTextJob"), any(SearchCandidateRequest.class), any(ItemProcessor.class)))
        .thenReturn(candidateBatchJobBuilder);
    when(candidateBatchJobBuilder.percentageOfCpu(75)).thenReturn(candidateBatchJobBuilder);
    when(candidateBatchJobBuilder.build()).thenReturn(batchJob);
    when(batchJobService.launchJob(batchJob, false)).thenReturn("started");

    ResponseEntity<String> response = systemAdminApi.setCandidateText(75);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("started", response.getBody());
    verify(candidateBatchJobFactory).builder(
        eq("candidateTextJob"), searchCandidateRequestCaptor.capture(), any(ItemProcessor.class));
    SearchCandidateRequest request = searchCandidateRequestCaptor.getValue();
    assertTrue(request.getStatuses().contains(CandidateStatus.active));
    assertFalse(request.getStatuses().contains(CandidateStatus.deleted));
    assertFalse(request.getStatuses().contains(CandidateStatus.withdrawn));
  }

  @Test
  void setCandidateTextByList_buildsListScopedJob() throws Exception {
    SavedList savedList = new SavedList();
    when(savedListService.get(7)).thenReturn(savedList);
    when(candidateBatchJobFactory.builder(
        eq("candidateTextJob"), eq(savedList), any(ItemProcessor.class)))
        .thenReturn(candidateBatchJobBuilder);
    when(candidateBatchJobBuilder.percentageOfCpu(20)).thenReturn(candidateBatchJobBuilder);
    when(candidateBatchJobBuilder.build()).thenReturn(batchJob);
    when(batchJobService.launchJob(batchJob, false)).thenReturn("list-started");

    ResponseEntity<String> response = systemAdminApi.setCandidateTextByList(7, 20);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("list-started", response.getBody());
  }

  @Test
  void setCandidateTextBySearch_buildsSearchScopedJob() throws Exception {
    SavedSearch savedSearch = new SavedSearch();
    when(savedSearchService.getSavedSearch(12)).thenReturn(savedSearch);
    when(candidateBatchJobFactory.builder(
        eq("candidateTextJob"), eq(savedSearch), any(ItemProcessor.class)))
        .thenReturn(candidateBatchJobBuilder);
    when(candidateBatchJobBuilder.percentageOfCpu(15)).thenReturn(candidateBatchJobBuilder);
    when(candidateBatchJobBuilder.build()).thenReturn(batchJob);
    when(batchJobService.launchJob(batchJob, false)).thenReturn("search-started");

    ResponseEntity<String> response = systemAdminApi.setCandidateTextBySearch(12, 15);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("search-started", response.getBody());
  }

  @Test
  void apiMigrationEndpoints_delegateToTcApiService() {
    when(tcApiService.listApiMigrations()).thenReturn("listed");
    when(tcApiService.stopApiMigration(4L)).thenReturn("stopped");
    when(tcApiService.restartApiMigration(5L)).thenReturn("restarted");
    when(tcApiService.runMongoMigration()).thenReturn("mongo");
    when(tcApiService.runAuroraMigration()).thenReturn("aurora");

    assertEquals("listed", systemAdminApi.listApiMigrations().getBody());
    assertEquals("stopped", systemAdminApi.stopApiMigration(4L).getBody());
    assertEquals("restarted", systemAdminApi.restartApiMigration(5L).getBody());
    assertEquals("mongo", systemAdminApi.runApiMigration("MoNgO").getBody());
    assertEquals("aurora", systemAdminApi.runApiMigration("AURORA").getBody());
    assertEquals(HttpStatus.BAD_REQUEST, systemAdminApi.runApiMigration("   ").getStatusCode());
  }

  @Test
  void duolingoEndpoints_delegateAndHandleAuthFailures() {
    DuolingoDashboardResponse dashboardResponse = mock(DuolingoDashboardResponse.class);
    LocalDateTime min = LocalDateTime.parse("2024-01-01T10:15:30");
    LocalDateTime max = LocalDateTime.parse("2024-02-01T10:15:30");
    when(duolingoApiService.getDashboardResults(min, max)).thenReturn(List.of(dashboardResponse));
    DuolingoVerifyScoreResponse verifyScoreResponse = mock(DuolingoVerifyScoreResponse.class);
    when(duolingoApiService.verifyScore("cert", "2000-01-01")).thenReturn(verifyScoreResponse);
    DuolingoCouponResponse couponResponse = mock(DuolingoCouponResponse.class);
    when(duolingoCouponService.reassignProctoredCouponToCandidate("123", loggedInUser))
        .thenReturn(couponResponse);

    assertEquals(List.of(dashboardResponse), systemAdminApi.fetchDashboardResults(
        "2024-01-01T10:15:30", "2024-02-01T10:15:30"));
    assertSame(verifyScoreResponse, systemAdminApi.verifyScore("cert", "2000-01-01"));
    assertSame(couponResponse, systemAdminApi.reassignDuolingoCoupon("123").getBody());

    when(authService.getLoggedInUser()).thenReturn(Optional.empty());
    ResponseEntity<DuolingoCouponResponse> badResponse = systemAdminApi.reassignDuolingoCoupon("123");
    assertEquals(HttpStatus.BAD_REQUEST, badResponse.getStatusCode());
    assertNull(badResponse.getBody());
  }

  @Test
  void reassignDuolingoCoupon_serviceExceptionReturnsBadRequest() {
    when(duolingoCouponService.reassignProctoredCouponToCandidate("123", loggedInUser))
        .thenThrow(new NoSuchObjectException("none left"));

    ResponseEntity<DuolingoCouponResponse> response = systemAdminApi.reassignDuolingoCoupon("123");

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertNull(response.getBody());
  }

  @Test
  void simpleServiceEndpoints_delegateToServices() {
    systemAdminApi.setPublicIds();
    systemAdminApi.flushUserCache();
    systemAdminApi.notifyOfNewChatPosts();
    systemAdminApi.loadCandidateOpportunityLastActiveStages();
    systemAdminApi.createEmployerForAllJobs();
    systemAdminApi.loadJobOppsAndCandidateOpps();
    when(countryService.updateIsoCodes()).thenReturn("countries");
    when(languageService.updateIsoCodes()).thenReturn("languages");

    assertEquals("Done: Countries: countries\nLanguages: languages", systemAdminApi.updateIsoCodes());
    verify(backgroundProcessingService).setCandidatePublicIds();
    verify(backgroundProcessingService).setPartnerPublicIds();
    verify(backgroundProcessingService).setSavedListPublicIds();
    verify(backgroundProcessingService).setSavedSearchPublicIds();
    verify(cacheService).flushUserCache();
    verify(notificationService).notifyUsersOfChatsWithNewUnreadPosts();
    verify(candidateOpportunityService).loadCandidateOpportunityLastActiveStages();
    verify(jobService).createEmployerForAllJobs();
    verify(jobService).loadJobOppsAndCandidateOpps();
  }

  @Test
  void closeCandidateOpportunitiesForClosedJobs_updatesEveryClosedJobAndContinuesOnError() {
    SalesforceJobOpp job1 = new SalesforceJobOpp();
    job1.setId(1L);
    job1.setStage(JobOpportunityStage.noJobOffer);
    SalesforceJobOpp job2 = new SalesforceJobOpp();
    job2.setId(2L);
    job2.setStage(JobOpportunityStage.noJobOffer);
    when(jobService.searchJobsUnpaged(any(SearchJobRequest.class))).thenReturn(List.of(job1, job2));
    doThrow(new RuntimeException("failed")).when(jobService).updateJob(eq(1L), any(UpdateJobRequest.class));

    systemAdminApi.closeCandidateOpportunitiesForClosedJobs();

    verify(jobService).updateJob(eq(1L), updateJobRequestCaptor.capture());
    verify(jobService).updateJob(eq(2L), updateJobRequestCaptor.capture());
    assertEquals(JobOpportunityStage.noJobOffer, updateJobRequestCaptor.getAllValues().get(0).getStage());
    assertEquals(JobOpportunityStage.noJobOffer, updateJobRequestCaptor.getAllValues().get(1).getStage());
  }

  @Test
  void salesforceSyncEndpoints_returnOkAndInternalServerError() {
    assertEquals(HttpStatus.OK, systemAdminApi.sfSyncOpenJobs().getStatusCode());
    verify(jobService).initiateOpenJobSyncFromSf();

    doThrow(new RuntimeException("boom")).when(jobService).initiateOpenJobSyncFromSf();
    ResponseEntity<?> failedJobs = systemAdminApi.sfSyncOpenJobs();
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, failedJobs.getStatusCode());
    assertNotNull(failedJobs.getBody());

    assertEquals(HttpStatus.OK, systemAdminApi.sfSyncOpenCases().getStatusCode());
    verify(candidateOppBackgroundProcessingService).initiateBackgroundCaseUpdate();

    doThrow(new RuntimeException("boom")).when(candidateOppBackgroundProcessingService)
        .initiateBackgroundCaseUpdate();
    ResponseEntity<?> failedCases = systemAdminApi.sfSyncOpenCases();
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, failedCases.getStatusCode());
    assertNotNull(failedCases.getBody());
  }

  @Test
  void moveCandidate_movesOnlyWhenCandidateHasFolderOnOldDrive() throws Exception {
    GoogleFileSystemDrive oldDrive = mock(GoogleFileSystemDrive.class);
    GoogleFileSystemFolder rootFolder = mock(GoogleFileSystemFolder.class);
    when(oldDrive.getId()).thenReturn("old-drive");
    when(googleDriveConfig.getCandidateDataDriveId()).thenReturn("candidate-drive");
    when(googleDriveConfig.getCandidateRootFolder()).thenReturn(rootFolder);
    when(fileSystemService.getDriveFromEntity(any(GoogleFileSystemFolder.class))).thenReturn(oldDrive);
    Candidate candidate = new Candidate();
    candidate.setFolderlink("https://drive.google.com/folders/old");
    when(candidateService.findByCandidateNumber("111")).thenReturn(candidate);

    systemAdminApi.moveCandidate("111");

    verify(fileSystemService).moveEntityToFolder(any(GoogleFileSystemFolder.class), eq(rootFolder));
  }

  @Test
  void moveCandidate_noopsWhenCandidateMissingFolderOrAlreadyOnCurrentDrive() throws Exception {
    GoogleFileSystemDrive currentDrive = mock(GoogleFileSystemDrive.class);
    when(currentDrive.getId()).thenReturn("candidate-drive");
    when(googleDriveConfig.getCandidateDataDriveId()).thenReturn("candidate-drive");
    when(fileSystemService.getDriveFromEntity(any(GoogleFileSystemFolder.class))).thenReturn(currentDrive);
    Candidate noFolder = new Candidate();
    Candidate alreadyMoved = new Candidate();
    alreadyMoved.setFolderlink("https://drive.google.com/folders/current");
    when(candidateService.findByCandidateNumber("missing")).thenReturn(null);
    when(candidateService.findByCandidateNumber("no-folder")).thenReturn(noFolder);
    when(candidateService.findByCandidateNumber("current")).thenReturn(alreadyMoved);

    systemAdminApi.moveCandidate("missing");
    systemAdminApi.moveCandidate("no-folder");
    systemAdminApi.moveCandidate("current");

    verify(fileSystemService, never()).moveEntityToFolder(any(), any());
  }

  @Test
  void moveCandidates_handlesNullFolderCurrentDriveOldDriveAndDriveLookupError() throws Exception {
    Candidate noFolder = candidate("1", null);
    Candidate current = candidate("2", "https://drive/current");
    Candidate old = candidate("3", "https://drive/old");
    Candidate broken = candidate("4", "https://drive/broken");

    SavedList savedList = mock(SavedList.class);
    when(savedList.getCandidates()).thenReturn(
        new java.util.LinkedHashSet<>(List.of(noFolder, current, old, broken))
    );
    when(savedListService.get(10L)).thenReturn(savedList);

    GoogleFileSystemDrive currentDrive = mock(GoogleFileSystemDrive.class);
    GoogleFileSystemDrive oldDrive = mock(GoogleFileSystemDrive.class);

    when(currentDrive.getId()).thenReturn("candidate-drive");
    when(oldDrive.getId()).thenReturn("old-drive");
    when(googleDriveConfig.getCandidateDataDriveId()).thenReturn("candidate-drive");

    when(fileSystemService.getDriveFromEntity(any(GoogleFileSystemFolder.class)))
        .thenReturn(currentDrive) // candidate 2: already on current drive
        .thenReturn(oldDrive)     // candidate 3: first check says old drive
        .thenReturn(oldDrive)     // candidate 3: doMoveCandidate also sees old drive
        .thenThrow(new RuntimeException("lookup")); // candidate 4: lookup error

    when(candidateService.findByCandidateNumber("3")).thenReturn(old);
    when(googleDriveConfig.getCandidateRootFolder()).thenReturn(mock(GoogleFileSystemFolder.class));

    systemAdminApi.moveCandidates(10L);

    verify(fileSystemService).moveEntityToFolder(any(GoogleFileSystemFolder.class), any());
    verify(candidateService).findByCandidateNumber("3");
  }

  @Test
  void makeNamedListFoldersViewable_publishesSubfoldersAcrossPages() throws Exception {
    Drive.Files.List rootListRequest = mockDriveListRequest(new FileList()
        .setFiles(List.of(driveFile("root", "List 1", "root-link")))
        .setNextPageToken(null));
    Drive.Files.List subListRequest = mockDriveListRequest(new FileList()
        .setFiles(List.of(driveFile("sub", "List Folder", "sub-link"))));
    when(googleDriveConfig.getGoogleDriveService()).thenReturn(googleDriveService);
    when(googleDriveService.files()).thenReturn(driveFiles);
    when(driveFiles.list()).thenReturn(rootListRequest, subListRequest);

    assertEquals("Done", systemAdminApi.makeNamedListFoldersViewable());

    verify(fileSystemService).publishFile(any(GoogleFileSystemFile.class));
  }

  @Test
  void makeJobDescriptionFoldersViewable_publishesOnlyJobDescriptionAndContinuesOnPublishError()
      throws Exception {
    Drive.Files.List rootListRequest = mockDriveListRequest(new FileList()
        .setFiles(List.of(driveFile("root", "List 1", "root-link")))
        .setNextPageToken(null));
    Drive.Files.List subListRequest = mockDriveListRequest(new FileList()
        .setFiles(List.of(driveFile("sub", "List Folder", "sub-link"))));
    Drive.Files.List leafListRequest = mockDriveListRequest(new FileList()
        .setFiles(List.of(
            driveFile("jd", "JobDescription", "jd-link"),
            driveFile("other", "Other", "other-link"))));
    when(googleDriveConfig.getGoogleDriveService()).thenReturn(googleDriveService);
    when(googleDriveService.files()).thenReturn(driveFiles);
    when(driveFiles.list()).thenReturn(rootListRequest, subListRequest, leafListRequest);
    doThrow(new RuntimeException("publish failed")).when(fileSystemService)
        .publishFile(any(GoogleFileSystemFile.class));

    assertEquals("Done", systemAdminApi.makeJobDescriptionFoldersViewable());

    verify(fileSystemService, times(1)).publishFile(any(GoogleFileSystemFile.class));
  }

  @Test
  void renameCandidateFolders_renamesMappedFolderNamesOnly() throws Exception {
    Drive.Files.List rootListRequest = mockDriveListRequest(new FileList()
        .setFiles(List.of(driveFile("root", "123", "root-link")))
        .setNextPageToken(null));
    Drive.Files.List subListRequest = mockDriveListRequest(new FileList()
        .setFiles(List.of(
            driveFile("english", "English", "english-link"),
            driveFile("unknown", "Random", "random-link"))));
    when(googleDriveConfig.getGoogleDriveService()).thenReturn(googleDriveService);
    when(googleDriveService.files()).thenReturn(driveFiles);
    when(driveFiles.list()).thenReturn(rootListRequest, subListRequest);

    assertEquals("Done", systemAdminApi.renameCandidateFolders());

    verify(fileSystemService, times(1)).renameFile(any(GoogleFileSystemFile.class));
  }

  @Test
  void updateCandidateSalesforceLinks_savesExistingAndSkipsMissingCandidates() throws Exception {
    String baseUrl = "https://example.lightning.force.com";
    String contactUrl = "https://example.lightning.force.com/lightning/r/Contact/003123/view";

    Contact existingContact = spy(new Contact());
    existingContact.setTbbId(123L);
    doReturn(contactUrl).when(existingContact).getUrl(baseUrl);

    Contact missingContact = spy(new Contact());
    missingContact.setTbbId(456L);
    doReturn("https://example.lightning.force.com/lightning/r/Contact/003456/view")
        .when(missingContact).getUrl(baseUrl);

    Candidate candidate = new Candidate();

    when(salesforceConfig.getBaseLightningUrl()).thenReturn(baseUrl);
    when(salesforceService.findCandidateContacts()).thenReturn(List.of(existingContact, missingContact));
    when(candidateRepository.findByCandidateNumber("123")).thenReturn(candidate);
    when(candidateRepository.findByCandidateNumber("456")).thenReturn(null);

    assertEquals("done", systemAdminApi.updateCandidateSalesforceLinks());

    verify(candidateRepository).save(candidate);
    assertEquals(contactUrl, candidate.getSflink());
  }

  @Test
  void updateContactsMatchingCondition_handlesNonCandidateMissingCandidateAndUpdateException()
      throws Exception {
    Contact nonCandidate = new Contact();
    nonCandidate.setTbbId(null);
    Contact missingCandidate = new Contact();
    missingCandidate.setTbbId(123L);
    Contact updateFails = new Contact();
    updateFails.setTbbId(456L);
    Candidate candidate = new Candidate();
    when(salesforceConfig.getBaseLightningUrl()).thenReturn("https://example.lightning.force.com");
    when(salesforceService.findContacts("query"))
        .thenReturn(List.of(nonCandidate, missingCandidate, updateFails));
    when(candidateRepository.findByCandidateNumber("123")).thenReturn(null);
    when(candidateRepository.findByCandidateNumber("456")).thenReturn(candidate);
    doThrow(new RuntimeException("sf error")).when(salesforceService).updateContact(candidate);

    assertEquals("done", systemAdminApi.updateContactsMatchingCondition("query"));

    verify(salesforceService).updateContact(candidate);
  }

  @Test
  void updateStatusesIneligible_changesMatchingCandidatesAndWritesAdminNotes() {
    Country sameCountry = new Country();
    Candidate ineligible = new Candidate();
    ineligible.setCandidateNumber("101");
    ineligible.setCountry(sameCountry);
    ineligible.setNationality(sameCountry);
    Candidate staysPending = new Candidate();
    staysPending.setCandidateNumber("102");
    staysPending.setCountry(new Country());
    staysPending.setNationality(new Country());
    when(candidateRepository.findByStatuses(any()))
        .thenReturn(List.of(ineligible, staysPending));

    assertEquals("Done. Now run esload to update elasticsearch.", systemAdminApi.updateStatusesIneligible());

    assertEquals(CandidateStatus.ineligible, ineligible.getStatus());
    assertNull(staysPending.getStatus());
    verify(candidateRepository).save(ineligible);
    verify(candidateNoteRepository).save(candidateNoteCaptor.capture());
    CandidateNote note = candidateNoteCaptor.getValue();
    assertSame(ineligible, note.getCandidate());
    assertEquals(NoteType.admin, note.getNoteType());
  }

  @Test
  void migrateGoogleDriveFolders_setsCandidateFolderLinkAndReturnsDone() throws Exception {
    Drive.Files.List rootListRequest = mockDriveListRequest(new FileList()
        .setFiles(List.of(driveFile("folder", "Candidate 987", "folder-link")))
        .setNextPageToken(null));
    Candidate candidate = new Candidate();
    when(googleDriveConfig.getGoogleDriveService()).thenReturn(googleDriveService);
    when(googleDriveService.files()).thenReturn(driveFiles);
    when(driveFiles.list()).thenReturn(rootListRequest);
    when(candidateRepository.findByCandidateNumber("987")).thenReturn(candidate);

    assertEquals("done", systemAdminApi.migrateGoogleDriveFolders());

    assertEquals("folder-link", candidate.getFolderlink());
    verify(candidateRepository).save(candidate);
  }

  @Test
  void setCandidateFolderLink_skipsBlankCandidateNumberAndMissingCandidate() {
    systemAdminApi.setCandidateFolderLink(driveFile("1", "No digits", "link"));
    systemAdminApi.setCandidateFolderLink(driveFile("2", "Candidate 404", "link"));

    verify(candidateRepository).findByCandidateNumber("404");
    verify(candidateRepository, never()).save(any(Candidate.class));
    assertEquals("123", systemAdminApi.checkForCN("abc123def"));
    assertEquals("", systemAdminApi.checkForCN("abcdef"));
  }

  @Test
  void doDBCopy_delegatesAndReturnsDone() throws Exception {
    assertEquals("done", systemAdminApi.doDBCopy());
    verify(dataSharingService).dbCopy();
  }

  @Test
  void deleteJob_returnsNotFoundWhenJobMissing() {
    when(salesforceJobOppRepository.findById(99L)).thenReturn(Optional.empty());

    ResponseEntity<Void> response = systemAdminApi.deleteJob(99L);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    verify(salesforceJobOppRepository, never()).deleteById(anyLong());
  }

  @Test
  void deleteJob_clearsRelationshipsDeletesDependentRecordsAndDeletesJob() {
    SalesforceJobOpp job = new SalesforceJobOpp();
    SavedList submissionList = new SavedList();
    SavedList exclusionList = new SavedList();
    job.setSubmissionList(submissionList);
    job.setExclusionList(exclusionList);
    when(salesforceJobOppRepository.findById(11L)).thenReturn(Optional.of(job));
    JobChat chat = new JobChat();
    chat.setId(22L);
    when(jobChatRepository.findByJobOppId(11L)).thenReturn(List.of(chat));
    Query query = mock(Query.class);
    when(entityManager.createNativeQuery("DELETE FROM job_suggested_saved_search WHERE tc_job_id = :jobId"))
        .thenReturn(query);
    when(query.setParameter("jobId", 11L)).thenReturn(query);
    SavedList savedList = new SavedList();
    savedList.setSavedSearch(new SavedSearch());
    savedList.setSavedSearchSource(new SavedSearch());
    List<SavedList> savedLists = new ArrayList<>(List.of(savedList));
    when(savedListRepository.findByJobIds(11L)).thenReturn(savedLists);

    ResponseEntity<Void> response = systemAdminApi.deleteJob(11L);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNull(job.getSubmissionList());
    assertNull(job.getExclusionList());
    assertNull(savedList.getSavedSearch());
    assertNull(savedList.getSavedSearchSource());
    verify(salesforceJobOppRepository).save(job);
    verify(jobChatUserRepository).deleteByJobChatId(22L);
    verify(chatPostRepository).deleteByJobChatId(22L);
    verify(jobChatRepository).deleteAll(List.of(chat));
    verify(query).executeUpdate();
    verify(savedListRepository).saveAll(savedLists);
    verify(savedListRepository).flush();
    verify(savedListRepository).deleteAll(savedLists);
    verify(savedSearchRepository).deleteByJobId(11L);
    verify(salesforceJobOppRepository).deleteById(11L);
  }

  @Test
  void deleteJob_returnsInternalServerErrorWhenDeleteFails() {
    when(salesforceJobOppRepository.findById(11L)).thenThrow(new RuntimeException("db"));

    ResponseEntity<Void> response = systemAdminApi.deleteJob(11L);

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
  }

  @Test
  void reassignCandidates_listSourceReassignsPagesToActiveSourcePartner() {
    Partner partner = activeSourcePartner("New Partner");
    SavedList savedList = new SavedList();
    Page<Candidate> firstPage = mock(Page.class);
    Page<Candidate> secondPage = mock(Page.class);
    when(partnerService.getPartner(16)).thenReturn(partner);
    when(savedListService.get(393)).thenReturn(savedList);
    when(firstPage.getTotalPages()).thenReturn(1);
    when(firstPage.getTotalElements()).thenReturn(2L);
    when(secondPage.getTotalElements()).thenReturn(1L);
    when(candidateService.getSavedListCandidates(eq(savedList), any(SavedListGetRequest.class)))
        .thenReturn(firstPage, secondPage);

    ResponseEntity<?> response = systemAdminApi.reassignCandidates("list", 393, 16);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    verify(candidateService).reassignCandidatesOnPage(firstPage, partner);
  }

  @Test
  void reassignCandidates_searchSourceReassignsPagesToActiveSourcePartner() {
    Partner partner = activeSourcePartner("New Partner");
    SearchCandidateRequest request = new SearchCandidateRequest();
    Page<Candidate> firstPage = mock(Page.class);
    Page<Candidate> secondPage = mock(Page.class);
    when(partnerService.getPartner(16)).thenReturn(partner);
    when(savedSearchService.loadSavedSearch(211)).thenReturn(request);
    when(firstPage.getTotalPages()).thenReturn(1);
    when(firstPage.getTotalElements()).thenReturn(3L);
    when(secondPage.getTotalElements()).thenReturn(1L);
    when(savedSearchService.searchCandidates(request)).thenReturn(firstPage, secondPage);

    ResponseEntity<?> response = systemAdminApi.reassignCandidates("search", 211, 16);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    verify(candidateService).reassignCandidatesOnPage(firstPage, partner);
    assertEquals(1, request.getPageNumber());
  }

  @Test
  void processPotentialDuplicateCandidates_successAndFailure() {
    assertEquals(HttpStatus.OK, systemAdminApi.processPotentialDuplicateCandidates().getStatusCode());
    verify(backgroundProcessingService).processPotentialDuplicateCandidates();

    doThrow(new RuntimeException("boom")).when(backgroundProcessingService)
        .processPotentialDuplicateCandidates();
    ResponseEntity<?> failed = systemAdminApi.processPotentialDuplicateCandidates();

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, failed.getStatusCode());
    assertNotNull(failed.getBody());
  }

  @Test
  void reassignLinkedinCoupon_successAndFailure() {
    when(userService.getLoggedInUser()).thenReturn(loggedInUser);

    assertEquals(HttpStatus.OK, systemAdminApi.reassignLinkedinCoupon("123").getStatusCode());
    verify(linkedInService).reassignForCandidate("123", loggedInUser);

    doThrow(new RuntimeException("no coupon")).when(linkedInService)
        .reassignForCandidate("456", loggedInUser);
    ResponseEntity<?> failed = systemAdminApi.reassignLinkedinCoupon("456");

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, failed.getStatusCode());
    assertNotNull(failed.getBody());
  }

  @Test
  void jdbcLoaderHelpers_readRowsIntoCollections() throws Exception {
    when(connection.createStatement()).thenReturn(statement);
    when(statement.executeQuery("select id from country")).thenReturn(resultSet);
    when(resultSet.next()).thenReturn(true, true, false);
    when(resultSet.getLong(1)).thenReturn(1L, 2L);

    Set<Long> referenceIds = ReflectionTestUtils.invokeMethod(
        systemAdminApi, "loadReferenceIds", connection, "country");
    assertEquals(Set.of(1L, 2L), referenceIds);
    verify(resultSet).close();
    verify(statement).close();
  }

  @Test
  void loadOtherReferenceIds_readsOtherReferenceRows() throws Exception {
    when(statement.executeQuery("select id, name from frm_options_other where type = 'language_other'"))
        .thenReturn(resultSet);
    when(resultSet.next()).thenReturn(true, false);
    when(resultSet.getLong(1)).thenReturn(99L);
    when(resultSet.getString(2)).thenReturn("Klingon");

    Map<Long, String> result = ReflectionTestUtils.invokeMethod(
        systemAdminApi, "loadOtherReferenceIds", statement, "language_other");

    assertEquals(Map.of(99L, "Klingon"), result);
    verify(resultSet).close();
  }

  @Test
  void loadCandidateOccupationsAndIdsAndAdminIds_readExpectedMapsAndSets() throws Exception {
    ResultSet occupations = mock(ResultSet.class);
    ResultSet candidates = mock(ResultSet.class);
    ResultSet admins = mock(ResultSet.class);

    when(connection.createStatement()).thenReturn(statement);

    when(statement.executeQuery("select id, candidate_id, occupation_id from candidate_occupation"))
        .thenReturn(occupations);
    when(occupations.next()).thenReturn(true, false);
    when(occupations.getLong(1)).thenReturn(5L);
    when(occupations.getLong(2)).thenReturn(6L);
    when(occupations.getLong(3)).thenReturn(7L);

    when(statement.executeQuery("select id, user_id from candidate"))
        .thenReturn(candidates);
    when(candidates.next()).thenReturn(true, false);
    when(candidates.getLong(1)).thenReturn(10L);
    when(candidates.getLong(2)).thenReturn(20L);

    when(statement.executeQuery("select id from users where role = 'admin'"))
        .thenReturn(admins);
    when(admins.next()).thenReturn(true, false);
    when(admins.getLong(1)).thenReturn(42L);

    Map<String, Long> occupationMap = ReflectionTestUtils.invokeMethod(
        systemAdminApi, "loadCandidateOccupations", connection);
    Map<Long, Long> candidateIds = ReflectionTestUtils.invokeMethod(
        systemAdminApi, "loadCandidateIds", connection);
    Set<Long> adminIds = ReflectionTestUtils.invokeMethod(
        systemAdminApi, "loadAdminIds", connection);

    assertEquals(Map.of("6~7", 5L), occupationMap);
    assertEquals(Map.of(20L, 10L), candidateIds);
    assertEquals(Set.of(42L), adminIds);

    verify(occupations).close();
    verify(candidates).close();
    verify(admins).close();
  }

  @Test
  void addTranslation_setsAllColumnsAndAddsBatch() throws Exception {
    ReflectionTestUtils.invokeMethod(
        systemAdminApi, "addTranslation", preparedStatement, 123L, "country", "ar", "الأردن", 42L);

    verify(preparedStatement).setLong(1, 123L);
    verify(preparedStatement).setString(2, "country");
    verify(preparedStatement).setString(3, "ar");
    verify(preparedStatement).setString(4, "الأردن");
    verify(preparedStatement).setLong(5, 1L);
    verify(preparedStatement).setTimestamp(eq(6), any(Timestamp.class));
    verify(preparedStatement).addBatch();
  }

  @Test
  void migrateFormOption_insertsEducationLevelAndTranslations() throws Exception {
    PreparedStatement optionInsert = mock(PreparedStatement.class);
    PreparedStatement translationInsert = mock(PreparedStatement.class);
    when(connection.prepareStatement("insert into education_level (id, name, level, status, education_type) values (?, ?, ?, ?, ?) on conflict (id) do nothing"))
        .thenReturn(optionInsert);
    when(statement.executeQuery("select id, name, name_ar, `order` from frm_options where type = 'education_level'"))
        .thenReturn(resultSet);
    when(resultSet.next()).thenReturn(true, false);
    when(resultSet.getLong("id")).thenReturn(6864L);
    when(resultSet.getString("name")).thenReturn("Bachelor's Degree");
    when(resultSet.getInt("order")).thenReturn(1);
    when(resultSet.getString("name_ar")).thenReturn("بكالوريوس");

    try {
      ReflectionTestUtils.invokeMethod(systemAdminApi, "migrateFormOption", connection, statement,
          translationInsert, 42L, "education_level", "education_level", true);
      verify(optionInsert).setLong(1, 6864L);
      verify(optionInsert).setString(2, "Bachelor's Degree");
      verify(optionInsert).setInt(3, 1);
      verify(optionInsert).setString(4, "active");
      verify(optionInsert).setString(5, EducationType.Bachelor.name());
      verify(optionInsert).addBatch();
      verify(optionInsert, times(2)).executeBatch();
      verify(translationInsert).addBatch();
      verify(translationInsert, times(2)).executeBatch();
    } finally {
      optionInsert.close();
    }
  }

  @Test
  void migrateUsersAndAdmins_insertExpectedRows() throws Exception {
    PreparedStatement insert = mock(PreparedStatement.class);
    ResultSet users = mock(ResultSet.class);
    when(connection.prepareStatement("insert into users (id, username, first_name, last_name, email, role, status, password_enc, created_by, created_date, updated_date) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) on conflict (id) do nothing"))
        .thenReturn(insert);
    when(statement.executeQuery("select u.id, username, j.first_name, j.last_name, email, status, password_hash, created_at, updated_at from user u join user_jobseeker j on j.user_id = u.id"))
        .thenReturn(users);
    when(users.next()).thenReturn(true, false);
    when(users.getLong("id")).thenReturn(101L);
    when(users.getString("username")).thenReturn("candidate");
    when(users.getString("first_name")).thenReturn("First");
    when(users.getString("last_name")).thenReturn("Last");
    when(users.getString("email")).thenReturn("candidate@example.com");
    when(users.getInt("status")).thenReturn(10);
    when(users.getString("password_hash")).thenReturn("hash");
    when(users.getLong("created_at")).thenReturn(1609459200L);
    when(users.getLong("updated_at")).thenReturn(1609459300L);

    ReflectionTestUtils.invokeMethod(systemAdminApi, "migrateUsers", connection, statement);

    verify(insert).setLong(1, 101L);
    verify(insert).setString(6, "user");
    verify(insert).setString(7, Status.active.name());
    verify(insert, times(2)).executeBatch();
    verify(users).close();
    verify(insert).close();
    PreparedStatement adminInsert = mock(PreparedStatement.class);
    ResultSet admins = mock(ResultSet.class);
    when(connection.prepareStatement("insert into users (id, username, first_name, last_name, email, role, status, password_enc, created_by, created_date, updated_date) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) on conflict (id) do nothing"))
        .thenReturn(adminInsert);
    when(statement.executeQuery("select id, username, email, status, password_hash, created_at, updated_at from admin;"))
        .thenReturn(admins);
    when(admins.next()).thenReturn(true, false);
    when(admins.getLong("id")).thenReturn(201L);
    when(admins.getString("username")).thenReturn("admin");
    when(admins.getString("email")).thenReturn("admin@example.com");
    when(admins.getInt("status")).thenReturn(0);
    when(admins.getString("password_hash")).thenReturn("hash2");
    when(admins.getLong("created_at")).thenReturn(1609459200L);
    when(admins.getLong("updated_at")).thenReturn(1609459300L);

    ReflectionTestUtils.invokeMethod(systemAdminApi, "migrateAdmins", connection, statement);

    verify(adminInsert).setString(6, "admin");
    verify(adminInsert).setString(7, Status.inactive.name());
    verify(adminInsert).setNull(3, Types.VARCHAR);
    verify(adminInsert).setNull(4, Types.VARCHAR);
    verify(admins).close();
    verify(adminInsert).close();
  }

  @Test
  void migrateTranslations_preservesPrefixesAlreadyEndingInSlash() {
    when(translationMigrationService.migrateBucketContents(
        "legacy", "legacy-prefix/", "new", "dest-prefix/")).thenReturn(7);

    Map<String, Object> result = systemAdminApi.migrateTranslations(
        "legacy", "new", "legacy-prefix/", "dest-prefix/");

    assertEquals("success", result.get("status"));
    assertEquals(7, result.get("copiedCount"));
    assertEquals("legacy-prefix/", result.get("sourcePrefix"));
    assertEquals("dest-prefix/", result.get("destinationPrefix"));

    verify(translationMigrationService).migrateBucketContents(
        "legacy", "legacy-prefix/", "new", "dest-prefix/");
  }

  @Test
  void fetchDashboardResults_allowsNullFilters() {
    DuolingoDashboardResponse response = mock(DuolingoDashboardResponse.class);
    when(duolingoApiService.getDashboardResults(eq(null), eq(null)))
        .thenReturn(List.of(response));

    List<DuolingoDashboardResponse> result = systemAdminApi.fetchDashboardResults(null, null);

    assertEquals(List.of(response), result);
    verify(duolingoApiService).getDashboardResults(eq(null), eq(null));
  }

  @Test
  void jdbcSettersUpdateFields() {
    systemAdminApi.setTargetJdbcUrl("jdbc:test");
    systemAdminApi.setTargetUser("test-user");
    systemAdminApi.setTargetPwd("test-password");

    assertEquals("jdbc:test", ReflectionTestUtils.getField(systemAdminApi, "targetJdbcUrl"));
    assertEquals("test-user", ReflectionTestUtils.getField(systemAdminApi, "targetUser"));
    assertEquals("test-password", ReflectionTestUtils.getField(systemAdminApi, "targetPwd"));
  }

  @Test
  void reassignCandidates_invalidSourceReturnsInternalServerError() {
    Partner partner = mock(Partner.class);
    when(partnerService.getPartner(16)).thenReturn(partner);
    when(partner.isSourcePartner()).thenReturn(true);
    when(partner.getStatus()).thenReturn(Status.active);

    ResponseEntity<?> response = systemAdminApi.reassignCandidates("bad-source", 211, 16);

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    assertNotNull(response.getBody());

    verify(partnerService).getPartner(16);
  }

  @Test
  void reassignCandidates_nonSourcePartnerReturnsInternalServerError() {
    Partner partner = mock(Partner.class);
    when(partner.isSourcePartner()).thenReturn(false);
    when(partnerService.getPartner(16)).thenReturn(partner);

    ResponseEntity<?> response = systemAdminApi.reassignCandidates("list", 393, 16);

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    assertNotNull(response.getBody());
  }

  @Test
  void reassignCandidates_inactivePartnerReturnsInternalServerError() {
    Partner partner = mock(Partner.class);
    when(partner.isSourcePartner()).thenReturn(true);
    when(partner.getStatus()).thenReturn(Status.inactive);
    when(partnerService.getPartner(16)).thenReturn(partner);

    ResponseEntity<?> response = systemAdminApi.reassignCandidates("search", 211, 16);

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    assertNotNull(response.getBody());
  }

  @Test
  void closeCandidateOpportunitiesForClosedJobs_processesTwentiethJobBranch() {
    List<SalesforceJobOpp> jobs = new ArrayList<>();
    for (long i = 1; i <= 20; i++) {
      SalesforceJobOpp job = new SalesforceJobOpp();
      job.setId(i);
      job.setStage(JobOpportunityStage.noJobOffer);
      jobs.add(job);
    }

    when(jobService.searchJobsUnpaged(any(SearchJobRequest.class))).thenReturn(jobs);

    systemAdminApi.closeCandidateOpportunitiesForClosedJobs();

    verify(jobService).searchJobsUnpaged(any(SearchJobRequest.class));
    verify(jobService, times(20)).updateJob(anyLong(), any(UpdateJobRequest.class));
  }

  @Test
  void updateStatusesIneligible_continuesWhenCandidateSaveFails() {
    Country country = new Country();

    Candidate candidate = new Candidate();
    candidate.setCandidateNumber("101");
    candidate.setCountry(country);
    candidate.setNationality(country);

    when(candidateRepository.findByStatuses(any())).thenReturn(List.of(candidate));
    doThrow(new RuntimeException("save failed")).when(candidateRepository).save(candidate);

    String result = systemAdminApi.updateStatusesIneligible();

    assertEquals("Done. Now run esload to update elasticsearch.", result);
    assertEquals(CandidateStatus.ineligible, candidate.getStatus());
    verify(candidateRepository).save(candidate);
    verify(candidateNoteRepository, never()).save(any(CandidateNote.class));
  }

  @Test
  void updateStatusesIneligible_continuesWhenCandidateNoteSaveFails() {
    Country country = new Country();

    Candidate candidate = new Candidate();
    candidate.setCandidateNumber("102");
    candidate.setCountry(country);
    candidate.setNationality(country);

    when(candidateRepository.findByStatuses(any())).thenReturn(List.of(candidate));
    doThrow(new RuntimeException("note failed"))
        .when(candidateNoteRepository)
        .save(any(CandidateNote.class));

    String result = systemAdminApi.updateStatusesIneligible();

    assertEquals("Done. Now run esload to update elasticsearch.", result);
    assertEquals(CandidateStatus.ineligible, candidate.getStatus());
    verify(candidateRepository).save(candidate);
    verify(candidateNoteRepository).save(any(CandidateNote.class));
  }

  @Test
  void referenceSetterHelpers_coverKnownOtherAndNullBranches() throws Exception {
    ResultSet knownRef = mock(ResultSet.class);
    PreparedStatement knownRefInsert = mock(PreparedStatement.class);
    when(knownRef.getInt("country")).thenReturn(2);

    int nextIndex = ReflectionTestUtils.invokeMethod(
        systemAdminApi,
        "setRefIdOrNull",
        knownRef,
        knownRefInsert,
        "country",
        new HashSet<>(Set.of(2L)),
        1);

    assertEquals(2, nextIndex);
    verify(knownRefInsert).setLong(1, 2L);

    ResultSet missingRef = mock(ResultSet.class);
    PreparedStatement missingRefInsert = mock(PreparedStatement.class);
    when(missingRef.getInt("country")).thenReturn(0);

    nextIndex = ReflectionTestUtils.invokeMethod(
        systemAdminApi,
        "setRefIdOrNull",
        missingRef,
        missingRefInsert,
        "country",
        new HashSet<>(Set.of(2L)),
        3);

    assertEquals(4, nextIndex);
    verify(missingRefInsert).setNull(3, Types.BIGINT);

    ResultSet knownUnknownRef = mock(ResultSet.class);
    PreparedStatement knownUnknownInsert = mock(PreparedStatement.class);
    when(knownUnknownRef.getInt("language")).thenReturn(6);

    nextIndex = ReflectionTestUtils.invokeMethod(
        systemAdminApi,
        "setRefIdOrUnknown",
        knownUnknownRef,
        knownUnknownInsert,
        "language",
        new HashSet<>(Set.of(6L)),
        Map.of(),
        2,
        5);

    assertEquals(3, nextIndex);
    verify(knownUnknownInsert).setLong(2, 6L);
    verify(knownUnknownInsert).setNull(5, Types.VARCHAR);

    ResultSet otherRef = mock(ResultSet.class);
    PreparedStatement otherInsert = mock(PreparedStatement.class);
    when(otherRef.getInt("language")).thenReturn(99);

    ReflectionTestUtils.invokeMethod(
        systemAdminApi,
        "setRefIdOrUnknown",
        otherRef,
        otherInsert,
        "language",
        new HashSet<>(),
        Map.of(99L, "Klingon"),
        2,
        5);

    verify(otherInsert).setLong(2, 0);
    verify(otherInsert).setString(5, "Klingon");

    ResultSet nullUnknownRef = mock(ResultSet.class);
    PreparedStatement nullUnknownInsert = mock(PreparedStatement.class);
    when(nullUnknownRef.getInt("language")).thenReturn(123);

    ReflectionTestUtils.invokeMethod(
        systemAdminApi,
        "setRefIdOrUnknown",
        nullUnknownRef,
        nullUnknownInsert,
        "language",
        new HashSet<>(),
        Map.of(),
        2,
        5);

    verify(nullUnknownInsert).setNull(2, Types.BIGINT);
    verify(nullUnknownInsert).setNull(5, Types.VARCHAR);
  }

  @Test
  void mappingAndDateHelpers_coverRemainingBranches() throws Exception {
    assertEquals(CandidateStatus.draft.name(),
        ReflectionTestUtils.invokeMethod(systemAdminApi, "getCandidateStatus", 1, 0, 1));
    assertEquals(CandidateStatus.draft.name(),
        ReflectionTestUtils.invokeMethod(systemAdminApi, "getCandidateStatus", 1, 1, 0));
    assertEquals(CandidateStatus.incomplete.name(),
        ReflectionTestUtils.invokeMethod(systemAdminApi, "getCandidateStatus", 1, 1, 1));
    assertEquals(CandidateStatus.pending.name(),
        ReflectionTestUtils.invokeMethod(systemAdminApi, "getCandidateStatus", 2, 1, 1));
    assertEquals(CandidateStatus.unreachable.name(),
        ReflectionTestUtils.invokeMethod(systemAdminApi, "getCandidateStatus", 3, 1, 1));
    assertEquals(CandidateStatus.incomplete.name(),
        ReflectionTestUtils.invokeMethod(systemAdminApi, "getCandidateStatus", 7, 1, 1));
    assertEquals(CandidateStatus.unreachable.name(),
        ReflectionTestUtils.invokeMethod(systemAdminApi, "getCandidateStatus", 8, 1, 1));
    assertEquals(CandidateStatus.pending.name(),
        ReflectionTestUtils.invokeMethod(systemAdminApi, "getCandidateStatus", 9, 1, 1));
    assertEquals(CandidateStatus.deleted.name(),
        ReflectionTestUtils.invokeMethod(systemAdminApi, "getCandidateStatus", 99, 1, 1));

    assertNull(ReflectionTestUtils.invokeMethod(systemAdminApi, "getGender", ""));
    assertNull(ReflectionTestUtils.invokeMethod(systemAdminApi, "getGender", (Object) null));

    assertEquals(EducationType.Masters.name(),
        ReflectionTestUtils.invokeMethod(systemAdminApi, "getEducationLevel", 6865));
    assertEquals(EducationType.Doctoral.name(),
        ReflectionTestUtils.invokeMethod(systemAdminApi, "getEducationLevel", 6867));
    assertEquals(EducationType.Associate.name(),
        ReflectionTestUtils.invokeMethod(systemAdminApi, "getEducationLevel", 6868));
    assertEquals(EducationType.Vocational.name(),
        ReflectionTestUtils.invokeMethod(systemAdminApi, "getEducationLevel", 9442));

    assertEquals(EducationType.Masters.name(),
        ReflectionTestUtils.invokeMethod(systemAdminApi, "getEducationType", "Master's Degree"));
    assertEquals(EducationType.Doctoral.name(),
        ReflectionTestUtils.invokeMethod(systemAdminApi, "getEducationType", "Doctoral Degree"));
    assertEquals(EducationType.Associate.name(),
        ReflectionTestUtils.invokeMethod(systemAdminApi, "getEducationType", "Associate Degree"));
    assertEquals(EducationType.Vocational.name(),
        ReflectionTestUtils.invokeMethod(systemAdminApi, "getEducationType", "Vocational Degree"));
    assertEquals(EducationType.Bachelor.name(),
        ReflectionTestUtils.invokeMethod(systemAdminApi, "getEducationType", "Some University"));
    assertNull(ReflectionTestUtils.invokeMethod(systemAdminApi, "getEducationType", "Unknown"));
    assertNull(ReflectionTestUtils.invokeMethod(systemAdminApi, "getEducationType", (Object) null));

    assertEquals("1-2 years",
        ReflectionTestUtils.invokeMethod(systemAdminApi, "getSkillTimePeriod", 337));
    assertEquals("3-5 years",
        ReflectionTestUtils.invokeMethod(systemAdminApi, "getSkillTimePeriod", 338));
    assertEquals("5-7 years",
        ReflectionTestUtils.invokeMethod(systemAdminApi, "getSkillTimePeriod", 339));
    assertEquals("7-9 years",
        ReflectionTestUtils.invokeMethod(systemAdminApi, "getSkillTimePeriod", 340));
    assertEquals("10 years or more",
        ReflectionTestUtils.invokeMethod(systemAdminApi, "getSkillTimePeriod", 341));
    assertNull(ReflectionTestUtils.invokeMethod(systemAdminApi, "getSkillTimePeriod", 0));

    assertNull(ReflectionTestUtils.invokeMethod(systemAdminApi, "convertToTimestamp", (Object) null));
    assertNull(ReflectionTestUtils.invokeMethod(systemAdminApi, "convertToTimestamp", 0L));

    java.sql.Date validDate = java.sql.Date.valueOf("2024-01-02");
    ResultSet validDateResult = mock(ResultSet.class);
    when(validDateResult.getDate("dob")).thenReturn(validDate);

    assertEquals(validDate,
        ReflectionTestUtils.invokeMethod(systemAdminApi, "getDate", validDateResult, "dob", 99L));

    ResultSet epochDateResult = mock(ResultSet.class);
    when(epochDateResult.getDate("dob")).thenReturn(java.sql.Date.valueOf("1970-01-01"));

    assertNull(ReflectionTestUtils.invokeMethod(
        systemAdminApi, "getDate", epochDateResult, "dob", 99L));

    assertNotNull(ReflectionTestUtils.invokeMethod(systemAdminApi, "convertToDate", "2024-11-11"));
    assertNull(ReflectionTestUtils.invokeMethod(systemAdminApi, "convertToDate", ""));
    assertNull(ReflectionTestUtils.invokeMethod(systemAdminApi, "convertToDate", (Object) null));
  }
  @Test
  void migrateEntryPoints_returnDoneWhenLegacyConnectionsFail() {
    assertEquals("done", systemAdminApi.migrateStatus());
    assertEquals("done", systemAdminApi.migrateSurvey());
    assertEquals("done", systemAdminApi.migrate());
  }

  @Test
  void migrateCandidateCertifications_insertsKnownCandidateAndSkipsMissingCandidate()
      throws Exception {
    PreparedStatement insert = mock(PreparedStatement.class);
    ResultSet result = mock(ResultSet.class);
    java.sql.Date completedDate = java.sql.Date.valueOf("2024-01-01");

    when(connection.prepareStatement(
        "insert into candidate_certification (id, candidate_id, name, institution, date_completed) values (?, ?, ?, ?, ?) on conflict (id) do nothing"))
        .thenReturn(insert);
    when(statement.executeQuery(
        "select id, user_id, certification_name, institution_name, date_of_receipt from user_jobseeker_certification order by user_id"))
        .thenReturn(result);

    when(result.next()).thenReturn(true, true, false);
    when(result.getLong("user_id")).thenReturn(100L, 200L);
    when(result.getLong("id")).thenReturn(501L);
    when(result.getString("certification_name")).thenReturn("Java\u0000 Cert");
    when(result.getString("institution_name")).thenReturn("TC\u0000 Institute");
    when(result.getDate("date_of_receipt")).thenReturn(completedDate);

    ReflectionTestUtils.invokeMethod(
        systemAdminApi,
        "migrateCandidateCertifications",
        connection,
        statement,
        Map.of(100L, 10L));

    verify(insert).setLong(1, 501L);
    verify(insert).setLong(2, 10L);
    verify(insert).setString(3, "Java Cert");
    verify(insert).setString(4, "TC Institute");
    verify(insert).setDate(5, completedDate);
    verify(insert).addBatch();
    verify(insert, times(2)).executeBatch();
  }

  @Test
  void migrateCandidateSkills_insertsKnownCandidateAndSkipsMissingCandidate() throws Exception {
    PreparedStatement insert = mock(PreparedStatement.class);
    ResultSet result = mock(ResultSet.class);

    when(connection.prepareStatement(
        "insert into candidate_skill (id, candidate_id, skill, time_period) values (?, ?, ?, ?) on conflict (id) do nothing"))
        .thenReturn(insert);
    when(statement.executeQuery("select id, user_id, skill, time_period from user_jobseeker_skills"))
        .thenReturn(result);

    when(result.next()).thenReturn(true, true, false);
    when(result.getLong("user_id")).thenReturn(100L, 200L);
    when(result.getLong("id")).thenReturn(701L);
    when(result.getString("skill")).thenReturn("Java");
    when(result.getInt("time_period")).thenReturn(338);

    ReflectionTestUtils.invokeMethod(
        systemAdminApi,
        "migrateCandidateSkills",
        connection,
        statement,
        Map.of(100L, 10L));

    verify(insert).setLong(1, 701L);
    verify(insert).setLong(2, 10L);
    verify(insert).setString(3, "Java");
    verify(insert).setString(4, "3-5 years");
    verify(insert).addBatch();
    verify(insert, times(2)).executeBatch();
  }

  @Test
  void migrateCandidateLanguages_insertsKnownCandidateAndSkipsMissingCandidate()
      throws Exception {
    PreparedStatement insert = mock(PreparedStatement.class);
    Statement referenceStatement = mock(Statement.class);
    ResultSet languageIds = mock(ResultSet.class);
    ResultSet languageLevelIds = mock(ResultSet.class);
    ResultSet otherLanguages = mock(ResultSet.class);
    ResultSet rows = mock(ResultSet.class);

    when(connection.createStatement()).thenReturn(referenceStatement);

    when(referenceStatement.executeQuery("select id from language"))
        .thenReturn(languageIds);
    when(languageIds.next()).thenReturn(true, false);
    when(languageIds.getLong(1)).thenReturn(6L);

    when(referenceStatement.executeQuery("select id from language_level"))
        .thenReturn(languageLevelIds);
    when(languageLevelIds.next()).thenReturn(true, true, false);
    when(languageLevelIds.getLong(1)).thenReturn(1L, 2L);

    when(statement.executeQuery(
        "select id, name from frm_options_other where type = 'language_other'"))
        .thenReturn(otherLanguages);
    when(otherLanguages.next()).thenReturn(false);

    when(connection.prepareStatement(
        "insert into candidate_language (id, candidate_id, language_id, written_level_id, spoken_level_id, migration_language) values (?, ?, ?, ?, ?, ?) on conflict (id) do nothing"))
        .thenReturn(insert);

    when(statement.executeQuery(
        "select id, user_id, language, level, level_reading, if_other from user_jobseeker_languages order by user_id"))
        .thenReturn(rows);

    when(rows.next()).thenReturn(true, true, false);
    when(rows.getLong("user_id")).thenReturn(100L, 200L);
    when(rows.getLong("id")).thenReturn(501L);
    when(rows.getInt("language")).thenReturn(6);
    when(rows.getInt("level")).thenReturn(1);
    when(rows.getInt("level_reading")).thenReturn(2);

    ReflectionTestUtils.invokeMethod(
        systemAdminApi,
        "migrateCandidateLanguages",
        connection,
        statement,
        Map.of(100L, 10L));

    verify(insert).setLong(1, 501L);
    verify(insert).setLong(2, 10L);
    verify(insert).setLong(3, 6L);
    verify(insert).setLong(4, 1L);
    verify(insert).setLong(5, 2L);
    verify(insert).setNull(6, Types.VARCHAR);
    verify(insert).addBatch();
    verify(insert, times(2)).executeBatch();
  }

  @Test
  void migrateCandidateEducations_insertsKnownCandidateAndSkipsInvalidRows()
      throws Exception {
    PreparedStatement insert = mock(PreparedStatement.class);
    Statement referenceStatement = mock(Statement.class);
    ResultSet countryIds = mock(ResultSet.class);
    ResultSet rows = mock(ResultSet.class);

    when(connection.createStatement()).thenReturn(referenceStatement);

    when(referenceStatement.executeQuery("select id from country"))
        .thenReturn(countryIds);
    when(countryIds.next()).thenReturn(true, false);
    when(countryIds.getLong(1)).thenReturn(1L);

    when(connection.prepareStatement(
        "insert into candidate_education (id, candidate_id, country_id, institution, year_completed, education_type, course_name) values (?, ?, ?, ?, ?, ?, ?) on conflict (id) do nothing"))
        .thenReturn(insert);

    when(statement.executeQuery(
        "select j.id, user_id, country, f.name as university_school, graduation_year, degree, specification_emphasis from user_jobseeker_education  j "
            + " left join frm_options f on f.id = j.university_school order by user_id"))
        .thenReturn(rows);

    when(rows.next()).thenReturn(true, true, true, false);
    when(rows.getLong("user_id")).thenReturn(100L, 200L, 300L);
    when(rows.getInt("country")).thenReturn(1, 1, 9999);

    when(rows.getLong("id")).thenReturn(601L);
    when(rows.getString("university_school")).thenReturn("TC University");
    when(rows.getInt("graduation_year")).thenReturn(2024);
    when(rows.getInt("degree")).thenReturn(6864);
    when(rows.getString("specification_emphasis")).thenReturn("Computer Science");

    ReflectionTestUtils.invokeMethod(
        systemAdminApi,
        "migrateCandidateEducations",
        connection,
        statement,
        Map.of(100L, 10L, 300L, 30L));

    verify(insert).setLong(1, 601L);
    verify(insert).setLong(2, 10L);
    verify(insert).setLong(3, 1L);
    verify(insert).setString(4, "TC University");
    verify(insert).setInt(5, 2024);
    verify(insert).setString(6, EducationType.Bachelor.name());
    verify(insert).setString(7, "Computer Science");
    verify(insert).addBatch();
    verify(insert, times(2)).executeBatch();
  }

  @Test
  void migrateCandidateOccupations_insertsKnownCandidateAndNormalizesNegativeYears()
      throws Exception {
    PreparedStatement insert = mock(PreparedStatement.class);
    Statement referenceStatement = mock(Statement.class);
    ResultSet occupationIds = mock(ResultSet.class);
    ResultSet otherOccupations = mock(ResultSet.class);
    ResultSet rows = mock(ResultSet.class);

    when(connection.createStatement()).thenReturn(referenceStatement);

    when(referenceStatement.executeQuery("select id from occupation"))
        .thenReturn(occupationIds);
    when(occupationIds.next()).thenReturn(true, false);
    when(occupationIds.getLong(1)).thenReturn(7L);

    when(statement.executeQuery(
        "select id, name from frm_options_other where type = 'job_occupation'"))
        .thenReturn(otherOccupations);
    when(otherOccupations.next()).thenReturn(false);

    when(connection.prepareStatement(
        "insert into candidate_occupation (candidate_id, occupation_id, years_experience, verified, migration_occupation) values (?, ?, ?, ?, ?) on conflict (candidate_id, occupation_id) do nothing"))
        .thenReturn(insert);

    when(statement.executeQuery(
        "select j.user_id, job_occupation, sum(timestampdiff(YEAR, ifnull(start_date, sysdate()), ifnull(end_date, sysdate()))) as years, case when u.status = 10 or u.status = 11 then 1 else 0 end as verified "
            + " from user_jobseeker_experience j join user u on u.id = j.user_id group by j.user_id, job_occupation"))
        .thenReturn(rows);

    when(rows.next()).thenReturn(true, true, false);
    when(rows.getLong("user_id")).thenReturn(100L, 200L);
    when(rows.getInt("job_occupation")).thenReturn(7);
    when(rows.getInt("years")).thenReturn(-3);
    when(rows.getBoolean("verified")).thenReturn(true);

    ReflectionTestUtils.invokeMethod(
        systemAdminApi,
        "migrateCandidateOccupations",
        connection,
        statement,
        Map.of(100L, 10L));

    verify(insert).setLong(1, 10L);
    verify(insert).setLong(2, 7L);
    verify(insert).setNull(5, Types.VARCHAR);
    verify(insert).setInt(3, 0);
    verify(insert).setBoolean(4, true);
    verify(insert).addBatch();
    verify(insert, times(2)).executeBatch();
  }

  @Test
  void migrateCandidateExperiences_insertsKnownExperienceAndSkipsInvalidRows()
      throws Exception {
    PreparedStatement insert = mock(PreparedStatement.class);
    Statement referenceStatement = mock(Statement.class);
    ResultSet countryIds = mock(ResultSet.class);
    ResultSet occupationIds = mock(ResultSet.class);
    ResultSet candidateOccupationRows = mock(ResultSet.class);
    ResultSet rows = mock(ResultSet.class);

    when(connection.createStatement()).thenReturn(referenceStatement);

    when(referenceStatement.executeQuery("select id from country"))
        .thenReturn(countryIds);
    when(countryIds.next()).thenReturn(true, false);
    when(countryIds.getLong(1)).thenReturn(1L);

    when(referenceStatement.executeQuery("select id from occupation"))
        .thenReturn(occupationIds);
    when(occupationIds.next()).thenReturn(true, false);
    when(occupationIds.getLong(1)).thenReturn(7L);

    when(referenceStatement.executeQuery(
        "select id, candidate_id, occupation_id from candidate_occupation"))
        .thenReturn(candidateOccupationRows);
    when(candidateOccupationRows.next()).thenReturn(true, false);
    when(candidateOccupationRows.getLong(1)).thenReturn(55L);
    when(candidateOccupationRows.getLong(2)).thenReturn(10L);
    when(candidateOccupationRows.getLong(3)).thenReturn(0L);

    when(connection.prepareStatement(
        "insert into candidate_job_experience (id, candidate_id, candidate_occupation_id, company_name, country_id, role, start_date, end_date, full_time, paid, description) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) on conflict (id) do nothing"))
        .thenReturn(insert);

    when(statement.executeQuery(
        "select id, user_id, job_occupation, company_name, location, position_title, start_date, end_date, fulltime, paid, description from user_jobseeker_experience"))
        .thenReturn(rows);

    java.sql.Date startDate = java.sql.Date.valueOf("2020-01-01");
    java.sql.Date endDate = java.sql.Date.valueOf("2022-01-01");

    when(rows.next()).thenReturn(true, true, true, false);
    when(rows.getLong("user_id")).thenReturn(100L, 200L, 300L);
    when(rows.getInt("job_occupation")).thenReturn(99, 7, 7);

    when(rows.getLong("id")).thenReturn(701L);
    when(rows.getString("company_name")).thenReturn("TC\u0000 Company");
    when(rows.getInt("location")).thenReturn(1);
    when(rows.getString("position_title")).thenReturn("Developer\u0000");
    when(rows.getDate("start_date")).thenReturn(startDate);
    when(rows.getDate("end_date")).thenReturn(endDate);
    when(rows.getInt("fulltime")).thenReturn(9561);
    when(rows.getInt("paid")).thenReturn(9557);
    when(rows.getString("description")).thenReturn("Built things\u0000");

    ReflectionTestUtils.invokeMethod(
        systemAdminApi,
        "migrateCandidateExperiences",
        connection,
        statement,
        Map.of(100L, 10L, 300L, 30L));

    verify(insert).setLong(1, 701L);
    verify(insert).setLong(2, 10L);
    verify(insert).setLong(3, 55L);
    verify(insert).setString(4, "TC Company");
    verify(insert).setLong(5, 1L);
    verify(insert).setString(6, "Developer");
    verify(insert).setDate(7, startDate);
    verify(insert).setDate(8, endDate);
    verify(insert).setBoolean(9, true);
    verify(insert).setBoolean(10, true);
    verify(insert).setString(11, "Built things");
    verify(insert).addBatch();
    verify(insert, times(2)).executeBatch();
  }

  @Test
  void migrateCandidateAdminNotes_setsAdminCreatedByAndNullForUnknownAdmin()
      throws Exception {
    PreparedStatement insert = mock(PreparedStatement.class);
    Statement referenceStatement = mock(Statement.class);
    ResultSet adminIds = mock(ResultSet.class);
    ResultSet rows = mock(ResultSet.class);

    when(connection.createStatement()).thenReturn(referenceStatement);

    when(referenceStatement.executeQuery("select id from users where role = 'admin'"))
        .thenReturn(adminIds);
    when(adminIds.next()).thenReturn(true, false);
    when(adminIds.getLong(1)).thenReturn(42L);

    when(connection.prepareStatement(
        "insert into candidate_note (id, candidate_id, note_type, title, comment, created_date, created_by, updated_date) values (?, ?, ?, ?, ?, ?, ?, ?) on conflict (id) do nothing"))
        .thenReturn(insert);

    when(statement.executeQuery(
        "select id, user_id, profile_id, subject, comments, created_at, updated_at from admin_user_notes"))
        .thenReturn(rows);

    when(rows.next()).thenReturn(true, true, true, false);
    when(rows.getLong("profile_id")).thenReturn(100L, 101L, 200L);
    when(rows.getLong("id")).thenReturn(801L, 802L);
    when(rows.getString("subject")).thenReturn("Subject 1", "Subject 2");
    when(rows.getString("comments")).thenReturn("Comment 1", "Comment 2");
    when(rows.getLong("created_at")).thenReturn(1609459200L, 1609459300L);
    when(rows.getLong("updated_at")).thenReturn(1609459400L, 1609459500L);
    when(rows.getLong("user_id")).thenReturn(42L, 99L);

    ReflectionTestUtils.invokeMethod(
        systemAdminApi,
        "migrateCandidateAdminNotes",
        connection,
        statement,
        Map.of(100L, 10L, 101L, 11L));

    verify(insert).setLong(1, 801L);
    verify(insert).setLong(1, 802L);

    verify(insert).setLong(2, 10L);
    verify(insert).setLong(2, 11L);

    verify(insert, times(2)).setString(3, NoteType.admin.name());

    verify(insert).setString(4, "Subject 1");
    verify(insert).setString(4, "Subject 2");

    verify(insert).setString(5, "Comment 1");
    verify(insert).setString(5, "Comment 2");

    verify(insert, times(2)).setTimestamp(eq(6), any(Timestamp.class));

    verify(insert).setLong(7, 42L);
    verify(insert).setNull(7, Types.BIGINT);

    verify(insert, times(2)).setTimestamp(eq(8), any(Timestamp.class));

    verify(insert, times(2)).addBatch();
    verify(insert, times(2)).executeBatch();
  }

  @Test
  void migrateCandidateLinks_insertsLinkAndFileAttachmentsAndSkipsMissingCandidates()
      throws Exception {
    PreparedStatement linkInsert = mock(PreparedStatement.class);
    PreparedStatement fileInsert = mock(PreparedStatement.class);
    Statement referenceStatement = mock(Statement.class);
    ResultSet linkRows = mock(ResultSet.class);
    ResultSet adminIds = mock(ResultSet.class);
    ResultSet fileRows = mock(ResultSet.class);

    when(connection.createStatement()).thenReturn(referenceStatement);

    when(referenceStatement.executeQuery("select id from users where role = 'admin'"))
        .thenReturn(adminIds);
    when(adminIds.next()).thenReturn(true, false);
    when(adminIds.getLong(1)).thenReturn(42L);

    when(connection.prepareStatement(
        "insert into candidate_attachment (id, candidate_id, type, name, location, migrated, admin_only, created_date, created_by) values (?, ?, ?, ?, ?, ?, ?, ?, ?) on conflict (id) do nothing"))
        .thenReturn(linkInsert);

    when(connection.prepareStatement(
        "insert into candidate_attachment (candidate_id, type, name, location, file_type, migrated, admin_only, created_date, created_by) values (?, ?, ?, ?, ?, ?, ?, ?, ?) on conflict (id) do nothing"))
        .thenReturn(fileInsert);

    when(statement.executeQuery("select id, user_id, name, link from user_jobseeker_link"))
        .thenReturn(linkRows);

    when(linkRows.next()).thenReturn(true, true, false);
    when(linkRows.getLong("user_id")).thenReturn(100L, 999L);
    when(linkRows.getLong("id")).thenReturn(901L);
    when(linkRows.getString("name")).thenReturn("LinkedIn");
    when(linkRows.getString("link")).thenReturn("https://linkedin.example/profile");

    when(statement.executeQuery(
        "select id, user_id, admin_id, filename, extension, upload_date from user_jobseeker_attachments"))
        .thenReturn(fileRows);

    when(fileRows.next()).thenReturn(true, true, true, false);

    // The method calls getLong("user_id") twice for each valid file row:
    // first for candidate lookup, then again for created_by/admin handling.
    when(fileRows.getLong("user_id")).thenReturn(
        42L, 42L,   // valid row 1: candidate 420, created_by 42
        100L, 100L, // valid row 2: candidate 10, created_by 100
        999L        // missing candidate row
    );

    // filename is read twice per valid row: name and location.
    when(fileRows.getString("filename")).thenReturn(
        "cv.pdf", "cv.pdf",
        "portfolio.pdf", "portfolio.pdf"
    );
    when(fileRows.getString("extension")).thenReturn("pdf", "pdf");
    when(fileRows.getLong("upload_date")).thenReturn(1609459200L, 1609459300L);

    ReflectionTestUtils.invokeMethod(
        systemAdminApi,
        "migrateCandidateLinks",
        connection,
        statement,
        Map.of(100L, 10L, 42L, 420L));

    verify(linkInsert).setLong(1, 901L);
    verify(linkInsert).setLong(2, 10L);
    verify(linkInsert).setString(3, AttachmentType.link.name());
    verify(linkInsert).setString(4, "LinkedIn");
    verify(linkInsert).setString(5, "https://linkedin.example/profile");
    verify(linkInsert).setBoolean(6, true);
    verify(linkInsert).setBoolean(7, true);
    verify(linkInsert).setTimestamp(eq(8), any(Timestamp.class));
    verify(linkInsert).setLong(9, 100L);
    verify(linkInsert).addBatch();
    verify(linkInsert, times(2)).executeBatch();

    verify(fileInsert).setLong(1, 420L);
    verify(fileInsert).setLong(1, 10L);

    verify(fileInsert, times(2)).setString(2, AttachmentType.file.name());

    verify(fileInsert).setString(3, "cv.pdf");
    verify(fileInsert).setString(4, "cv.pdf");

    verify(fileInsert).setString(3, "portfolio.pdf");
    verify(fileInsert).setString(4, "portfolio.pdf");

    verify(fileInsert, times(2)).setString(5, "pdf");
    verify(fileInsert, times(2)).setBoolean(6, true);
    verify(fileInsert, times(2)).setBoolean(7, false);
    verify(fileInsert, times(2)).setTimestamp(eq(8), any(Timestamp.class));

    verify(fileInsert).setLong(9, 42L);
    verify(fileInsert).setLong(9, 100L);

    verify(fileInsert, times(2)).addBatch();
    verify(fileInsert, times(2)).executeBatch();
  }

  private Candidate candidate(String number, String folderLink) {
    Candidate candidate = new Candidate();
    candidate.setCandidateNumber(number);
    candidate.setFolderlink(folderLink);
    return candidate;
  }

  private Partner activeSourcePartner(String name) {
    Partner partner = mock(Partner.class);
    when(partner.isSourcePartner()).thenReturn(true);
    when(partner.getStatus()).thenReturn(Status.active);
    when(partner.getName()).thenReturn(name);
    return partner;
  }

  private com.google.api.services.drive.model.File driveFile(String id, String name, String link) {
    return new com.google.api.services.drive.model.File()
        .setId(id)
        .setName(name)
        .setWebViewLink(link);
  }

  private Drive.Files.List mockDriveListRequest(FileList result)
      throws java.io.IOException {
    Drive.Files.List request = mock(Drive.Files.List.class);
    lenient().when(request.setQ(anyString())).thenReturn(request);
    lenient().when(request.setSupportsAllDrives(true)).thenReturn(request);
    lenient().when(request.setIncludeItemsFromAllDrives(true)).thenReturn(request);
    lenient().when(request.setCorpora(anyString())).thenReturn(request);
    lenient().when(request.setDriveId(any())).thenReturn(request);
    lenient().when(request.setPageToken(any())).thenReturn(request);
    lenient().when(request.setPageSize(anyInt())).thenReturn(request);
    lenient().when(request.setFields(anyString())).thenReturn(request);
    when(request.execute()).thenReturn(result);
    return request;
  }

}
