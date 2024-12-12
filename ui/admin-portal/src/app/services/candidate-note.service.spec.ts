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


import {TestBed} from '@angular/core/testing';
import {HttpClientTestingModule, HttpTestingController} from '@angular/common/http/testing';
import {CandidateNoteService, CreateCandidateNoteRequest} from './candidate-note.service';
import {CandidateNote} from '../model/candidate-note';
import {SearchResults} from '../model/search-results';
import {environment} from '../../environments/environment';
import {MockUser} from "../MockData/MockUser";

describe('CandidateNoteService', () => {
  let service: CandidateNoteService;
  let httpMock: HttpTestingController;
  const apiUrl = environment.apiUrl + '/candidate-note';
  const mockUser = new MockUser();
  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [CandidateNoteService]
    });
    service = TestBed.inject(CandidateNoteService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should retrieve a list of candidate notes', () => {
    const mockNotes: CandidateNote[] = [
      { id: 1, title: 'Note 1', comment: 'Comment 1', noteType: 'General', createdBy: mockUser, createdDate: Date.now(), updatedBy: mockUser, updatedDate: Date.now() },
      { id: 2, title: 'Note 2', comment: 'Comment 2', noteType: 'Specific', createdBy: mockUser, createdDate: Date.now(), updatedBy: mockUser, updatedDate: Date.now() }
    ];

    service.list(1).subscribe((notes) => {
      expect(notes.length).toBe(2);
      expect(notes).toEqual(mockNotes);
    });

    const req = httpMock.expectOne(`${apiUrl}/1/list`);
    expect(req.request.method).toBe('GET');
    req.flush(mockNotes);
  });

  it('should search for candidate notes', () => {
    const mockSearchResults: SearchResults<CandidateNote> = {
      number: 1,
      size: 10,
      totalElements: 2,
      totalPages: 1,
      first: true,
      last: true,
      content: [
        { id: 1, title: 'Note 1', comment: 'Comment 1', noteType: 'General', createdBy: mockUser, createdDate: Date.now(), updatedBy: mockUser, updatedDate: Date.now() },
        { id: 2, title: 'Note 2', comment: 'Comment 2', noteType: 'Specific', createdBy: mockUser, createdDate: Date.now(), updatedBy: mockUser, updatedDate: Date.now() }
      ]
    };
    const requestData = { searchTerm: 'Note' };

    service.search(requestData).subscribe((data) => {
      expect(data.content.length).toBe(2);
      expect(data).toEqual(mockSearchResults);
    });

    const req = httpMock.expectOne(`${apiUrl}/search`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(requestData);
    req.flush(mockSearchResults);
  });

  it('should create a new candidate note and emit an event', () => {
    const newNote: CreateCandidateNoteRequest = {
      candidateId: 1,
      title: 'New Note',
      comment: 'New Comment'
    };

    const mockResponse: CandidateNote = {
      id: 3,
      title: 'New Note',
      comment: 'New Comment',
      noteType: 'General',
      createdBy: mockUser,
      createdDate: Date.now(),
      updatedBy: mockUser,
      updatedDate: Date.now()
    };

    service.create(newNote).subscribe((note) => {
      expect(note).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(apiUrl);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(newNote);
    req.flush(mockResponse);
  });

  it('should update an existing candidate note and emit an event', () => {
    const updatedNote: Partial<CandidateNote> = {
      id: 1,
      title: 'Updated Note',
      comment: 'Updated Comment',
      noteType: 'Specific',
      createdBy: mockUser,
      createdDate: Date.now(),
      updatedBy: mockUser,
      updatedDate: Date.now()
    };

    const mockResponse: CandidateNote = {
      id: 1,
      title: 'Updated Note',
      comment: 'Updated Comment',
      noteType: 'Specific',
      createdBy: mockUser,
      createdDate: Date.now(),
      updatedBy: mockUser,
      updatedDate: Date.now()
    };

    service.update(1, updatedNote).subscribe((note) => {
      expect(note).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(`${apiUrl}/1`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(updatedNote);
    req.flush(mockResponse);
  });
});
