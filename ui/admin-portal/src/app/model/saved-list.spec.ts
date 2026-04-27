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

import {
  ContentUpdateType,
  isSavedList, PublishedDocColumnContent, PublishedDocColumnDef, PublishedDocConstantSource,
  SavedList,
  UpdateSavedListContentsRequest
} from "./saved-list";
import {CandidateStatus} from "./candidate";

describe('SavedList Interface Tests', () => {

  // Sample SavedList object for testing
  let testSavedList: SavedList;

  beforeEach(() => {
    // Initialize testSavedList with sample data
    testSavedList = {
      id: 1,
      name: 'Sample Saved List',
      savedSearchSource: null,
      fileJdLink: 'https://example.com/file-jd.pdf',
      fileJdName: 'JD Document',
      folderlink: 'https://example.com/folder',
      publishedDocLink: 'https://example.com/published-doc',
      tcShortName: 'sample-short-name',
      sfJobCountry: 'Sample Country',
      sfJobStage: 'Stage 1',
      tasks: [],
      registeredJob: true
    } as SavedList;
  });

  it('should create a SavedList object', () => {
    expect(testSavedList).toBeDefined();
    expect(testSavedList.id).toBe(1);
    expect(testSavedList.name).toBe('Sample Saved List');
    expect(testSavedList.fileJdLink).toBe('https://example.com/file-jd.pdf');
    expect(testSavedList.tcShortName).toBe('sample-short-name');
    expect(testSavedList.sfJobCountry).toBe('Sample Country');
    expect(testSavedList.registeredJob).toBe(true);
  });

  it('should correctly determine if object is a SavedList', () => {
    expect(isSavedList(testSavedList)).toBe(true);
  });

});

describe('UpdateSavedListContentsRequest Interface Tests', () => {

  // Sample UpdateSavedListContentsRequest object for testing
  let updateRequest: UpdateSavedListContentsRequest;

  beforeEach(() => {
    // Initialize updateRequest with sample data
    updateRequest = {
      name: 'Updated Saved List Name',
      fixed: true,
      registeredJob: false,
      jobId: 123,
      sourceListId: 456,
      statusUpdateInfo: { status: CandidateStatus.active },
      updateType: ContentUpdateType.add
    };
  });

  it('should create an UpdateSavedListContentsRequest object', () => {
    expect(updateRequest).toBeDefined();
    expect(updateRequest.name).toBe('Updated Saved List Name');
    expect(updateRequest.fixed).toBe(true);
    expect(updateRequest.registeredJob).toBe(false);
    expect(updateRequest.jobId).toBe(123);
    expect(updateRequest.sourceListId).toBe(456);
    expect(updateRequest.statusUpdateInfo!.status).toBe(CandidateStatus.active);
    expect(updateRequest.updateType).toBe(ContentUpdateType.add);
  });

});

describe('PublishedDocColumnDef Class Tests', () => {

  // Sample PublishedDocColumnDef object for testing
  let columnDef: PublishedDocColumnDef;

  beforeEach(() => {
    // Initialize columnDef with sample data
    columnDef = new PublishedDocColumnDef('key1', 'Column Name');
    columnDef.content = new PublishedDocColumnContent();
    columnDef.content.value = new PublishedDocConstantSource('Sample Value');
  });

  it('should create a PublishedDocColumnDef object', () => {
    expect(columnDef).toBeDefined();
    expect(columnDef.key).toBe('key1');
    expect(columnDef.name).toBe('Column Name');
    expect(columnDef.content.value!.constant).toBe('Sample Value');
  });

});


