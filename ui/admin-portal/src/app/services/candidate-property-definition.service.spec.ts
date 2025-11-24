import {TestBed} from '@angular/core/testing';

import {CandidatePropertyDefinitionService} from './candidate-property-definition.service';
import {HttpClientTestingModule, HttpTestingController} from "@angular/common/http/testing";
import {SpringDataCandidatePropertyDefinitionsPage} from "../model/candidate-property-definition";
import {environment} from "../../environments/environment";

describe('CandidatePropertyDefinitionService', () => {
  let service: CandidatePropertyDefinitionService;
  let httpMock: HttpTestingController;
  const apiUrl = environment.halApiUrl + '/candidate-property-definitions';

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
    });
    service = TestBed.inject(CandidatePropertyDefinitionService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should return a page of property definitions', () => {
    const mockDefinitions: SpringDataCandidatePropertyDefinitionsPage = {
      _embedded: {
        candidatePropertyDefinitions: []
      },
      page: {
        size: 20,
        totalElements: 0,
        totalPages: 0,
        number: 0
      }
    };

    service.get().subscribe((definitions) => {
      expect(definitions).toEqual(mockDefinitions);
    })


    const req = httpMock.expectOne(`${apiUrl}?page=0&size=20`);
    expect(req.request.method).toBe('GET');
    //Simulate returned data
    req.flush(mockDefinitions);

  })
});
