package org.tctalent.server.api.admin;

import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.FileList;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.tctalent.server.configuration.GoogleDriveConfig;
import org.tctalent.server.configuration.SalesforceConfig;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateOpportunity;
import org.tctalent.server.model.db.User;
import org.tctalent.server.model.sf.Contact;
import org.tctalent.server.repository.db.CandidateOpportunityRepository;
import org.tctalent.server.repository.db.CandidateRepository;
import org.tctalent.server.security.AuthService;
import org.tctalent.server.service.api.TcApiService;
import org.tctalent.server.service.db.BackgroundProcessingService;
import org.tctalent.server.service.db.CandidateOpportunityService;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.tctalent.server.service.db.DataSharingService;
import org.tctalent.server.service.db.PopulateElasticsearchService;
import org.tctalent.server.service.db.SalesforceService;
import org.tctalent.server.service.db.aws.S3ResourceHelper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

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
  private PopulateElasticsearchService populateElasticsearchService;

  @Mock
  private Connection connection;

  @Mock
  private Statement statement;

  @Mock
  private ResultSet resultSet;



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
  void testUpdateAwsFileTypes_successAndFailureCases() {
    S3ObjectSummary obj1 = new S3ObjectSummary();
    obj1.setKey("file1");

    S3ObjectSummary obj2 = new S3ObjectSummary();
    obj2.setKey("file2");

    List<S3ObjectSummary> summaries = List.of(obj1, obj2);

    when(s3ResourceHelper.getObjectSummaries()).thenReturn(summaries);
    when(s3ResourceHelper.filterMigratedObjects(summaries)).thenReturn(summaries);

    doNothing().when(s3ResourceHelper).addObjectMetadata(obj1);
    doThrow(new RuntimeException("fail")).when(s3ResourceHelper).addObjectMetadata(obj2);

    String result = systemAdminApi.updateAwsFileTypes();

    assertEquals("done", result);
    verify(s3ResourceHelper, times(2)).addObjectMetadata(any());
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
  void testLoadElasticsearch_withResetFlag() {
    systemAdminApi.loadElasticsearch("true", 1, 5);

    verify(populateElasticsearchService).populateElasticCandidates(true, true, 1, 5);
  }

  @Test
  void testLoadElasticsearch_withoutResetFlag() {
    systemAdminApi.loadElasticsearch(null, null, null);

    verify(populateElasticsearchService).populateElasticCandidates(false, false, null, null);
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

}
