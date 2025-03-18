import {TestBed} from '@angular/core/testing';

import {OfferToAssistService} from './offer-to-assist.service';
import {SearchResults} from "../model/search-results";
import {environment} from "../../environments/environment";
import {CandidateAssistanceType, OfferToAssist} from "../model/offer-to-assist";
import {HttpClientTestingModule, HttpTestingController} from "@angular/common/http/testing";
import {MockUser} from "../MockData/MockUser";
import {MockPartner} from "../MockData/MockPartner";

describe('OfferToAssistService', () => {
  let service: OfferToAssistService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [OfferToAssistService]
    });
    service = TestBed.inject(OfferToAssistService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should search otas', () => {
    const searchRequest = { keyword: 'test' };
    const dummySearchResults: SearchResults<OfferToAssist> = {
      content: [{ id: 1, partner: new MockPartner(), publicId: '1234abcd',
          reason: CandidateAssistanceType.JOB_OPPORTUNITY , additionalNotes: 'test',
          createdBy: new MockUser(), createdDate:  new Date("2024-05-01"),
          updatedBy: new MockUser(), updatedDate: new Date("2024-05-02"),},
      ],
      totalElements: 1
    } as SearchResults<OfferToAssist>

    service.search(searchRequest).subscribe((searchResults) => {
      expect(searchResults.content.length).toBe(1);
      expect(searchResults).toEqual(dummySearchResults);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/ota/search`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(searchRequest);
    req.flush(dummySearchResults);
  });
});
