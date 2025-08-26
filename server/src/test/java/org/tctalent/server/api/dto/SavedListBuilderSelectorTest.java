/*
 * Copyright (c) 2025 Talent Catalog.
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

package org.tctalent.server.api.dto;

import org.junit.jupiter.api.Test;
import org.tctalent.server.util.dto.DtoBuilder;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SavedListBuilderSelectorTest {

  private final SavedListBuilderSelector selector = new SavedListBuilderSelector();

  @Test
  void selectBuilder_minimal_buildsExpectedShape() {
    // given
    SavedListStub sl = new SavedListStub()
        .setId(1L)
        .setPublicId("pub-1")
        .setName("My List")
        .setDisplayedFieldsLong("long")
        .setDisplayedFieldsShort("short")
        .setSfJobOpp(new JobOppStub().setId(101L).setSfId("SF-101"))
        .setCreatedBy(new UserStub().setId(11L).setFirstName("Alice").setLastName("Smith"))
        // fields that SHOULD NOT appear in MINIMAL
        .setDescription("desc should not be present on minimal")
        .setStatus("OPEN");

    DtoBuilder b = selector.selectBuilder(DtoType.MINIMAL);

    // when
    Map<String, Object> out = b.build(sl);

    // then (present)
    assertEquals(1L, out.get("id"));
    assertEquals("pub-1", out.get("publicId"));
    assertEquals("My List", out.get("name"));
    assertEquals("long", out.get("displayedFieldsLong"));
    assertEquals("short", out.get("displayedFieldsShort"));

    @SuppressWarnings("unchecked")
    Map<String, Object> sfJobOpp = (Map<String, Object>) out.get("sfJobOpp");
    assertNotNull(sfJobOpp);
    assertEquals(101L, sfJobOpp.get("id"));
    assertEquals("SF-101", sfJobOpp.get("sfId"));

    @SuppressWarnings("unchecked")
    Map<String, Object> createdBy = (Map<String, Object>) out.get("createdBy");
    assertNotNull(createdBy);
    assertEquals(11L, createdBy.get("id"));
    assertEquals("Alice", createdBy.get("firstName"));
    assertEquals("Smith", createdBy.get("lastName"));

    // then (absent on minimal)
    assertFalse(out.containsKey("description"));
    assertFalse(out.containsKey("status"));
    assertFalse(out.containsKey("users"));
    assertFalse(out.containsKey("exportColumns"));
  }

  @Test
  void selectBuilder_full_buildsExpectedShape() {
    // given
    SavedListStub sl = new SavedListStub()
        .setId(2L)
        .setPublicId("pub-2")
        .setName("Full List")
        .setDescription("A long description")
        .setStatus("ACTIVE")
        .setFixed(true)
        .setGlobal(true)
        .setDisplayedFieldsLong("long")
        .setDisplayedFieldsShort("short")
        .setSfJobOpp(new JobOppStub().setId(202L).setSfId("SF-202"))
        .setCreatedBy(new UserStub().setId(12L).setFirstName("Bob").setLastName("Jones"))
        .setUpdatedBy(new UserStub().setId(13L).setFirstName("Carol").setLastName("Lee"))
        .setCreatedDate(OffsetDateTime.parse("2024-01-01T00:00:00Z"))
        .setUpdatedDate(OffsetDateTime.parse("2024-01-02T00:00:00Z"))
        .setUsers(List.of(
            new UserStub().setId(21L).setFirstName("U1").setLastName("A"),
            new UserStub().setId(22L).setFirstName("U2").setLastName("B")
        ));

    DtoBuilder b = selector.selectBuilder(null); // full

    // when
    Map<String, Object> out = b.build(sl);

    // then (a few representative keys)
    assertEquals(2L, out.get("id"));
    assertEquals("pub-2", out.get("publicId"));
    assertEquals("A long description", out.get("description"));
    assertEquals("ACTIVE", out.get("status"));
    assertEquals(true, out.get("fixed"));
    assertEquals(true, out.get("global"));
    assertEquals("long", out.get("displayedFieldsLong"));
    assertEquals("short", out.get("displayedFieldsShort"));

    // nested: sfJobOpp
    @SuppressWarnings("unchecked")
    Map<String, Object> sfJobOpp = (Map<String, Object>) out.get("sfJobOpp");
    assertNotNull(sfJobOpp);
    assertEquals(202L, sfJobOpp.get("id"));
    assertEquals("SF-202", sfJobOpp.get("sfId"));

    // nested: createdBy / updatedBy
    @SuppressWarnings("unchecked")
    Map<String, Object> createdBy = (Map<String, Object>) out.get("createdBy");
    assertEquals(12L, createdBy.get("id"));
    assertEquals("Bob", createdBy.get("firstName"));
    assertEquals("Jones", createdBy.get("lastName"));

    @SuppressWarnings("unchecked")
    Map<String, Object> updatedBy = (Map<String, Object>) out.get("updatedBy");
    assertEquals(13L, updatedBy.get("id"));
    assertEquals("Carol", updatedBy.get("firstName"));
    assertEquals("Lee", updatedBy.get("lastName"));

    // collection: users
    @SuppressWarnings("unchecked")
    List<Map<String, Object>> users = (List<Map<String, Object>>) out.get("users");
    assertNotNull(users);
    assertEquals(2, users.size());
    assertEquals(21L, users.get(0).get("id"));
    assertEquals("U1", users.get(0).get("firstName"));
    assertEquals("A", users.get(0).get("lastName"));

    // Note: exportColumns is only populated if the source has a non-null property.
    // We didn't set it on the stub, so absence is expected.
    assertFalse(out.containsKey("exportColumns"));
  }

  @Test
  void selectBuilder_noArg_returnsFullBuilder() {
    SavedListStub sl = new SavedListStub()
        .setId(3L)
        .setDescription("present only on full");

    Map<String, Object> fullOut = selector.selectBuilder().build(sl);
    Map<String, Object> minimalOut = selector.selectBuilder(DtoType.MINIMAL).build(sl);

    assertTrue(fullOut.containsKey("description"));
    assertFalse(minimalOut.containsKey("description"));
  }

  // ---- Test-only POJOs with JavaBean getters ----

  public static class SavedListStub {
    private Long id;
    private String publicId;
    private String name;
    private String description;
    private String status;
    private Boolean fixed;
    private Boolean global;
    private String displayedFieldsLong;
    private String displayedFieldsShort;
    private JobOppStub sfJobOpp;
    private String fileJdLink;
    private String fileJdName;
    private String fileJoiLink;
    private String fileJoiName;
    private String fileInterviewGuidanceLink;
    private String fileInterviewGuidanceName;
    private String fileMouLink;
    private String fileMouName;
    private String folderlink;
    private String folderjdlink;
    private String publishedDocLink;
    private String registeredJob;
    private String sfJobCountry;
    private String sfJobStage;
    private String tcShortName;
    private UserStub createdBy;
    private OffsetDateTime createdDate;
    private UserStub updatedBy;
    private OffsetDateTime updatedDate;
    private List<UserStub> users;
    private List<ExportColumnStub> exportColumns; // can be null
    private SavedSearchSourceStub savedSearchSource;   // may be null
    private List<TaskStub> tasks;                      // may be null

    public Long getId() { return id; }
    public String getPublicId() { return publicId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getStatus() { return status; }
    public Boolean getFixed() { return fixed; }
    public Boolean getGlobal() { return global; }
    public String getDisplayedFieldsLong() { return displayedFieldsLong; }
    public String getDisplayedFieldsShort() { return displayedFieldsShort; }
    public JobOppStub getSfJobOpp() { return sfJobOpp; }
    public String getFileJdLink() { return fileJdLink; }
    public String getFileJdName() { return fileJdName; }
    public String getFileJoiLink() { return fileJoiLink; }
    public String getFileJoiName() { return fileJoiName; }
    public String getFileInterviewGuidanceLink() { return fileInterviewGuidanceLink; }
    public String getFileInterviewGuidanceName() { return fileInterviewGuidanceName; }
    public String getFileMouLink() { return fileMouLink; }
    public String getFileMouName() { return fileMouName; }
    public String getFolderlink() { return folderlink; }
    public String getFolderjdlink() { return folderjdlink; }
    public String getPublishedDocLink() { return publishedDocLink; }
    public String getRegisteredJob() { return registeredJob; }
    public String getSfJobCountry() { return sfJobCountry; }
    public String getSfJobStage() { return sfJobStage; }
    public String getTcShortName() { return tcShortName; }
    public UserStub getCreatedBy() { return createdBy; }
    public OffsetDateTime getCreatedDate() { return createdDate; }
    public UserStub getUpdatedBy() { return updatedBy; }
    public OffsetDateTime getUpdatedDate() { return updatedDate; }
    public List<UserStub> getUsers() { return users; }
    public List<ExportColumnStub> getExportColumns() { return exportColumns; }
    public SavedSearchSourceStub getSavedSearchSource() { return savedSearchSource; }
    public List<TaskStub> getTasks() { return tasks; }

    public SavedListStub setId(Long id) { this.id = id; return this; }
    public SavedListStub setPublicId(String publicId) { this.publicId = publicId; return this; }
    public SavedListStub setName(String name) { this.name = name; return this; }
    public SavedListStub setDescription(String description) { this.description = description; return this; }
    public SavedListStub setStatus(String status) { this.status = status; return this; }
    public SavedListStub setFixed(Boolean fixed) { this.fixed = fixed; return this; }
    public SavedListStub setGlobal(Boolean global) { this.global = global; return this; }
    public SavedListStub setDisplayedFieldsLong(String v) { this.displayedFieldsLong = v; return this; }
    public SavedListStub setDisplayedFieldsShort(String v) { this.displayedFieldsShort = v; return this; }
    public SavedListStub setSfJobOpp(JobOppStub sfJobOpp) { this.sfJobOpp = sfJobOpp; return this; }
    public SavedListStub setFileJdLink(String s) { this.fileJdLink = s; return this; }
    public SavedListStub setFileJdName(String s) { this.fileJdName = s; return this; }
    public SavedListStub setFileJoiLink(String s) { this.fileJoiLink = s; return this; }
    public SavedListStub setFileJoiName(String s) { this.fileJoiName = s; return this; }
    public SavedListStub setFileInterviewGuidanceLink(String s) { this.fileInterviewGuidanceLink = s; return this; }
    public SavedListStub setFileInterviewGuidanceName(String s) { this.fileInterviewGuidanceName = s; return this; }
    public SavedListStub setFileMouLink(String s) { this.fileMouLink = s; return this; }
    public SavedListStub setFileMouName(String s) { this.fileMouName = s; return this; }
    public SavedListStub setFolderlink(String s) { this.folderlink = s; return this; }
    public SavedListStub setFolderjdlink(String s) { this.folderjdlink = s; return this; }
    public SavedListStub setPublishedDocLink(String s) { this.publishedDocLink = s; return this; }
    public SavedListStub setRegisteredJob(String s) { this.registeredJob = s; return this; }
    public SavedListStub setSfJobCountry(String s) { this.sfJobCountry = s; return this; }
    public SavedListStub setSfJobStage(String s) { this.sfJobStage = s; return this; }
    public SavedListStub setTcShortName(String s) { this.tcShortName = s; return this; }
    public SavedListStub setCreatedBy(UserStub u) { this.createdBy = u; return this; }
    public SavedListStub setCreatedDate(OffsetDateTime t) { this.createdDate = t; return this; }
    public SavedListStub setUpdatedBy(UserStub u) { this.updatedBy = u; return this; }
    public SavedListStub setUpdatedDate(OffsetDateTime t) { this.updatedDate = t; return this; }
    public SavedListStub setUsers(List<UserStub> users) { this.users = users; return this; }
    public SavedListStub setExportColumns(List<ExportColumnStub> exportColumns) { this.exportColumns = exportColumns; return this; }
    public SavedListStub setSavedSearchSource(SavedSearchSourceStub v) { this.savedSearchSource = v; return this; }
    public SavedListStub setTasks(List<TaskStub> v) { this.tasks = v; return this; }
  }

  public static class JobOppStub {
    private Long id;
    private String sfId;
    public Long getId() { return id; }
    public String getSfId() { return sfId; }
    public JobOppStub setId(Long id) { this.id = id; return this; }
    public JobOppStub setSfId(String sfId) { this.sfId = sfId; return this; }
  }

  public static class UserStub {
    private Long id;
    private String firstName;
    private String lastName;
    public Long getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public UserStub setId(Long id) { this.id = id; return this; }
    public UserStub setFirstName(String firstName) { this.firstName = firstName; return this; }
    public UserStub setLastName(String lastName) { this.lastName = lastName; return this; }
  }

  public static class ExportColumnStub {
    private String key;
    private List<PublishedDocColumnPropsStub> properties;
    public String getKey() { return key; }
    public List<PublishedDocColumnPropsStub> getProperties() { return properties; }
    public ExportColumnStub setKey(String key) { this.key = key; return this; }
    public ExportColumnStub setProperties(List<PublishedDocColumnPropsStub> properties) { this.properties = properties; return this; }
  }

  public static class PublishedDocColumnPropsStub {
    private String header;
    private String constant;
    public String getHeader() { return header; }
    public String getConstant() { return constant; }
    public PublishedDocColumnPropsStub setHeader(String header) { this.header = header; return this; }
    public PublishedDocColumnPropsStub setConstant(String constant) { this.constant = constant; return this; }
  }

  public static class SavedSearchSourceStub {
    private Long id;
    public Long getId() { return id; }
    public SavedSearchSourceStub setId(Long id) { this.id = id; return this; }
  }

  public static class TaskStub {
    private Long id;
    private String name;
    public Long getId() { return id; }
    public String getName() { return name; }
    public TaskStub setId(Long id) { this.id = id; return this; }
    public TaskStub setName(String name) { this.name = name; return this; }
  }
}
