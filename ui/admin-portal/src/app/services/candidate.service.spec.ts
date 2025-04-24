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
import {CandidateService, DownloadCVRequest, IntakeAuditRequest} from './candidate.service';
import {environment} from '../../environments/environment';
import {
  Candidate,
  CandidateIntakeData,
  CandidateOpportunityParams,
  UpdateCandidateShareableDocsRequest,
  UpdateCandidateShareableNotesRequest,
  UpdateCandidateStatusRequest
} from '../model/candidate';
import {SearchResults} from '../model/search-results';
import {CandidateSource} from "../model/base";

describe('CandidateService', () => {
  let service: CandidateService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [CandidateService]
    });

    service = TestBed.inject(CandidateService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should search candidates', () => {
    const dummyRequest = {};
    const dummyResponse: SearchResults<Candidate> = { content: [], totalElements: 0 } as SearchResults<Candidate>;

    service.search(dummyRequest).subscribe(response => {
      expect(response).toEqual(dummyResponse);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/candidate/search`);
    expect(req.request.method).toBe('POST');
    req.flush(dummyResponse);
  });

  it('should find candidate by email', () => {
    const dummyRequest = {};
    const dummyResponse: SearchResults<Candidate> = { content: [], totalElements: 0 } as SearchResults<Candidate>;

    service.findByCandidateEmail(dummyRequest).subscribe(response => {
      expect(response).toEqual(dummyResponse);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/candidate/findbyemail`);
    expect(req.request.method).toBe('POST');
    req.flush(dummyResponse);
  });

  it('should find candidate by email phone or whatsapp', () => {
    const dummyRequest = {};
    const dummyResponse: SearchResults<Candidate> = { content: [], totalElements: 0 } as SearchResults<Candidate>;

    service.findByCandidateEmailPhoneOrWhatsapp(dummyRequest).subscribe(response => {
      expect(response).toEqual(dummyResponse);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/candidate/findbyemailphoneorwhatsapp`);
    expect(req.request.method).toBe('POST');
    req.flush(dummyResponse);
  });

  it('should find candidate by number or name', () => {
    const dummyRequest = {};
    const dummyResponse: SearchResults<Candidate> = { content: [], totalElements: 0 } as SearchResults<Candidate>;

    service.findByCandidateNumberOrName(dummyRequest).subscribe(response => {
      expect(response).toEqual(dummyResponse);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/candidate/findbynumberorname`);
    expect(req.request.method).toBe('POST');
    req.flush(dummyResponse);
  });

  it('should find candidate by external ID', () => {
    const dummyRequest = {};
    const dummyResponse: SearchResults<Candidate> = { content: [], totalElements: 0 } as SearchResults<Candidate>;

    service.findByExternalId(dummyRequest).subscribe(response => {
      expect(response).toEqual(dummyResponse);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/candidate/findbyexternalid`);
    expect(req.request.method).toBe('POST');
    req.flush(dummyResponse);
  });

  it('should get candidate by number', () => {
    const dummyNumber = '123';
    const dummyCandidate: Candidate = { id: 1, candidateNumber: dummyNumber } as Candidate;

    service.getByNumber(dummyNumber).subscribe(candidate => {
      expect(candidate).toEqual(dummyCandidate);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/candidate/number/${dummyNumber}`);
    expect(req.request.method).toBe('GET');
    req.flush(dummyCandidate);
  });

  it('should get candidate by ID', () => {
    const dummyId = 1;
    const dummyCandidate: Candidate = { id: dummyId } as Candidate;

    service.get(dummyId).subscribe(candidate => {
      expect(candidate).toEqual(dummyCandidate);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/candidate/${dummyId}`);
    expect(req.request.method).toBe('GET');
    req.flush(dummyCandidate);
  });

  it('should get candidate intake data', () => {
    const dummyId = 1;
    const dummyIntakeData: CandidateIntakeData = {} as CandidateIntakeData;

    service.getIntakeData(dummyId).subscribe(intakeData => {
      expect(intakeData).toEqual(dummyIntakeData);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/candidate/${dummyId}/intake`);
    expect(req.request.method).toBe('GET');
    req.flush(dummyIntakeData);
  });

  it('should update candidate links', () => {
    const dummyId = 1;
    const dummyDetails = {};
    const dummyCandidate: Candidate = { id: dummyId } as Candidate;

    service.updateLinks(dummyId, dummyDetails).subscribe(candidate => {
      expect(candidate).toEqual(dummyCandidate);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/candidate/${dummyId}/links`);
    expect(req.request.method).toBe('PUT');
    req.flush(dummyCandidate);
  });

  it('should update shareable notes', () => {
    const dummyId = 1;
    const dummyRequest = {} as UpdateCandidateShareableNotesRequest;
    const dummyCandidate: Candidate = { id: dummyId } as Candidate;

    service.updateShareableNotes(dummyId, dummyRequest).subscribe(candidate => {
      expect(candidate).toEqual(dummyCandidate);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/candidate/${dummyId}/shareable-notes`);
    expect(req.request.method).toBe('PUT');
    req.flush(dummyCandidate);
  });

  it('should update shareable docs', () => {
    const dummyId = 1;
    const dummyRequest = {} as UpdateCandidateShareableDocsRequest;
    const dummyCandidate: Candidate = { id: dummyId } as Candidate;

    service.updateShareableDocs(dummyId, dummyRequest).subscribe(candidate => {
      expect(candidate).toEqual(dummyCandidate);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/candidate/${dummyId}/shareable-docs`);
    expect(req.request.method).toBe('PUT');
    req.flush(dummyCandidate);
  });

  it('should update candidate status', () => {
    const dummyRequest = {} as UpdateCandidateStatusRequest;

    service.updateStatus(dummyRequest).subscribe(() => {
      expect(true).toBeTrue();
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/candidate/status`);
    expect(req.request.method).toBe('PUT');
    req.flush({});
  });

  it('should update candidate info', () => {
    const dummyId = 1;
    const dummyDetails = {};
    const dummyCandidate: Candidate = { id: dummyId } as Candidate;

    service.updateInfo(dummyId, dummyDetails).subscribe(candidate => {
      expect(candidate).toEqual(dummyCandidate);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/candidate/${dummyId}/info`);
    expect(req.request.method).toBe('PUT');
    req.flush(dummyCandidate);
  });

  it('should update candidate survey', () => {
    const dummyId = 1;
    const dummyDetails = {};
    const dummyCandidate: Candidate = { id: dummyId } as Candidate;

    service.updateSurvey(dummyId, dummyDetails).subscribe(candidate => {
      expect(candidate).toEqual(dummyCandidate);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/candidate/${dummyId}/survey`);
    expect(req.request.method).toBe('PUT');
    req.flush(dummyCandidate);
  });

  it('should update candidate media', () => {
    const dummyId = 1;
    const dummyDetails = {};
    const dummyCandidate: Candidate = { id: dummyId } as Candidate;

    service.updateMedia(dummyId, dummyDetails).subscribe(candidate => {
      expect(candidate).toEqual(dummyCandidate);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/candidate/${dummyId}/media`);
    expect(req.request.method).toBe('PUT');
    req.flush(dummyCandidate);
  });

  it('should update candidate registration', () => {
    const dummyId = 1;
    const dummyDetails = {};
    const dummyCandidate: Candidate = { id: dummyId } as Candidate;

    service.updateRegistration(dummyId, dummyDetails).subscribe(candidate => {
      expect(candidate).toEqual(dummyCandidate);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/candidate/${dummyId}/registration`);
    expect(req.request.method).toBe('PUT');
    req.flush(dummyCandidate);
  });

  it('should delete candidate', () => {
    const dummyId = 1;

    service.delete(dummyId).subscribe(response => {
      expect(response).toBeTrue();
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/candidate/${dummyId}`);
    expect(req.request.method).toBe('DELETE');
    req.flush(true);
  });

  it('should export candidates as CSV', () => {
    const dummyRequest = {};
    const dummyBlob = new Blob();

    service.export(dummyRequest).subscribe(blob => {
      expect(blob).toEqual(dummyBlob);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/candidate/export/csv`);
    expect(req.request.method).toBe('POST');
    req.flush(dummyBlob);
  });

  it('should download CV', () => {
    const dummyRequest: DownloadCVRequest = { candidateId: 1, showName: true, showContact: true };
    const dummyBlob = new Blob();

    service.downloadCv(dummyRequest).subscribe(blob => {
      expect(blob).toEqual(dummyBlob);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/candidate/${dummyRequest.candidateId}/cv.pdf`);
    expect(req.request.method).toBe('POST');
    req.flush(dummyBlob);
  });

  it('should create candidate folder', () => {
    const dummyId = 1;
    const dummyCandidate: Candidate = { id: dummyId } as Candidate;

    service.createCandidateFolder(dummyId).subscribe(candidate => {
      expect(candidate).toEqual(dummyCandidate);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/candidate/${dummyId}/create-folder`);
    expect(req.request.method).toBe('PUT');
    req.flush(dummyCandidate);
  });

  it('should create/update live candidate', () => {
    const dummyId = 1;
    const dummyCandidate: Candidate = { id: dummyId } as Candidate;

    service.createUpdateLiveCandidate(dummyId).subscribe(candidate => {
      expect(candidate).toEqual(dummyCandidate);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/candidate/${dummyId}/update-live`);
    expect(req.request.method).toBe('PUT');
    req.flush(dummyCandidate);
  });

  it('should create/update opportunities from candidate list', () => {
    const dummySource = { id: 1 } as CandidateSource;
    const dummyParams = {} as CandidateOpportunityParams;

    service.createUpdateOppsFromCandidateList(dummySource, dummyParams).subscribe(() => {
      expect(true).toBeTrue();
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/candidate/update-opps-by-list`);
    expect(req.request.method).toBe('PUT');
    req.flush({});
  });

  it('should create/update opportunities from candidates', () => {
    const dummyCandidateIds = [1, 2, 3];
    const dummySfJobOpp = 'sfJobOppId';
    const dummyParams = {} as CandidateOpportunityParams;

    service.createUpdateOppsFromCandidates(dummyCandidateIds, dummySfJobOpp, dummyParams).subscribe(() => {
      expect(true).toBeTrue();
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/candidate/update-opps`);
    expect(req.request.method).toBe('PUT');
    req.flush({});
  });

  it('should update intake data', () => {
    const dummyCandidateId = 1;
    const dummyFormData = {};

    service.updateIntakeData(dummyCandidateId, dummyFormData).subscribe(() => {
      expect(true).toBeTrue();
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/candidate/${dummyCandidateId}/intake`);
    expect(req.request.method).toBe('PUT');
    req.flush({});
  });

  it('should complete intake', () => {
    const dummyCandidateId = 1;
    const dummyRequest: IntakeAuditRequest = { completedDate: new Date(), fullIntake: true };
    const dummyCandidate: Candidate = { id: dummyCandidateId } as Candidate;

    service.completeIntake(dummyCandidateId, dummyRequest).subscribe(candidate => {
      expect(candidate).toEqual(dummyCandidate);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/candidate/${dummyCandidateId}/intake`);
    expect(req.request.method).toBe('POST');
    req.flush(dummyCandidate);
  });

  it('should resolve outstanding tasks', () => {
    const dummyDetails = {};

    service.resolveOutstandingTasks(dummyDetails).subscribe(() => {
      expect(true).toBeTrue();
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/candidate/resolve-tasks`);
    expect(req.request.method).toBe('PUT');
    req.flush({});
  });

});
