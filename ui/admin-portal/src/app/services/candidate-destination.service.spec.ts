import {TestBed} from '@angular/core/testing';
import {HttpClientTestingModule, HttpTestingController} from '@angular/common/http/testing';
import {CandidateDestinationService} from './candidate-destination.service';
import {environment} from '../../environments/environment';
import {CandidateDestination} from '../model/candidate-destination';
import {YesNoUnsure} from "../model/candidate";
import {MockJob} from "../MockData/MockJob";
import {MockCandidate} from "../MockData/MockCandidate";

fdescribe('CandidateDestinationService', () => {
  let service: CandidateDestinationService;
  let httpMock: HttpTestingController;
  const apiUrl = environment.apiUrl + '/candidate-destination';

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [CandidateDestinationService]
    });
    service = TestBed.inject(CandidateDestinationService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should create a candidate destination', () => {
    const candidateId = 1;
    const countryName = { name: 'USA' };
    const mockResponse: CandidateDestination = {
      id: 1,
      country: MockJob.country,
      candidate: new MockCandidate(),
      interest:YesNoUnsure.Yes,
      notes:'Notes'
    };

    service.create(candidateId, countryName).subscribe((destination) => {
      expect(destination).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(`${apiUrl}/${candidateId}`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(countryName);
    req.flush(mockResponse);
  });
});
