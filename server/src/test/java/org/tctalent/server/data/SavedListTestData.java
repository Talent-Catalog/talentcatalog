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

package org.tctalent.server.data;

import static org.tctalent.server.data.CandidateTestData.getCandidate;
import static org.tctalent.server.data.SavedSearchTestData.getSavedSearch;
import static org.tctalent.server.data.TaskTestData.getTask;
import static org.tctalent.server.data.UserTestData.getAuditUser;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateSavedList;
import org.tctalent.server.model.db.ExportColumn;
import org.tctalent.server.model.db.SavedList;
import org.tctalent.server.model.db.Status;
import org.tctalent.server.request.candidate.PublishedDocColumnProps;

public class SavedListTestData {

    public static SavedList getSavedList() {
        SavedList savedList = new SavedList();
        savedList.setId(1L);
        savedList.setDescription("Saved list description");
        savedList.setDisplayedFieldsLong(List.of("user.firstName", "user.lastName"));
        savedList.setExportColumns(List.of(getExportColumn()));
        savedList.setStatus(Status.active);
        savedList.setName("Saved list name");
        savedList.setFixed(true);
        savedList.setGlobal(false);
        savedList.setSavedSearchSource(getSavedSearch());
        savedList.setSfJobOpp(SalesforceJobOppTestData.getSalesforceJobOppMinimal());
        savedList.setFileJdLink("http://file.jd.link");
        savedList.setFileJdName("JobDescriptionFileName");
        savedList.setFileJoiLink("http://file.joi.link");
        savedList.setFileJoiName("JoiFileName");
        savedList.setFileInterviewGuidanceLink("http://file.interview.guidance.link");
        savedList.setFileInterviewGuidanceName("InterviewGuidanceFileName");
        savedList.setFileMouLink("http://file.mou.link");
        savedList.setFileMouName("MouFileName");
        savedList.setFolderlink("http://folder.link");
        savedList.setFolderjdlink("http://folder.jd.link");
        savedList.setPublishedDocLink("http://published.doc.link");
        savedList.setRegisteredJob(true);
        savedList.setTcShortName("Saved list Tc short name");
        savedList.setCreatedBy(getAuditUser());
        savedList.setCreatedDate(OffsetDateTime.parse("2023-10-30T12:30:00+02:00"));
        savedList.setUpdatedBy(getAuditUser());
        savedList.setUpdatedDate(OffsetDateTime.parse("2023-10-30T12:30:00+02:00"));
        savedList.setUsers(Set.of(getAuditUser()));
        savedList.setTasks(Set.of(getTask()));
        savedList.setCandidateSavedLists(getSetOfCandidateSavedLists());

        return savedList;
    }

    public static SavedList getSavedListWithCandidates() {
        SavedList savedList = getSavedList();

        final Candidate candidate1 = getCandidate();
        candidate1.setId(101L);
        CandidateSavedList csl1 = new CandidateSavedList(candidate1, savedList);
        final Candidate candidate2 = getCandidate();
        candidate2.setId(102L);
        CandidateSavedList csl2 = new CandidateSavedList(candidate2, savedList);
        final Candidate candidate3 = getCandidate();
        candidate3.setId(103L);
        CandidateSavedList csl3 = new CandidateSavedList(candidate3, savedList);

        savedList.setCandidateSavedLists(Set.of(csl1, csl2, csl3));

        return savedList;
    }

    public static List<SavedList> getSavedLists() {
        return List.of(
            getSavedList()
        );
    }

    public static PublishedDocColumnProps getPublishedDocColumnProps() {
        PublishedDocColumnProps publishedDocColumnProps = new PublishedDocColumnProps();
        publishedDocColumnProps.setHeader("non default column header");
        publishedDocColumnProps.setConstant("non default constant column value");
        return publishedDocColumnProps;
    }

    public static ExportColumn getExportColumn() {
        ExportColumn exportColumn = new ExportColumn();
        exportColumn.setKey("key");
        exportColumn.setProperties(getPublishedDocColumnProps());
        return exportColumn;
    }

    static CandidateSavedList getCandidateSavedList() {
        CandidateSavedList csl = new CandidateSavedList();
        csl.setCandidate(getCandidate());
        return csl;
  }

    public static Set<CandidateSavedList> getSetOfCandidateSavedLists() {
        Set<CandidateSavedList> scsl = Set.of(getCandidateSavedList());
        return scsl;
  }
}
