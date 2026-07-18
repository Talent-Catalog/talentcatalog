import {TestBed} from '@angular/core/testing';
import {HttpClientTestingModule, HttpTestingController} from '@angular/common/http/testing';

import {SkillsService, ExtractSkillsRequest} from './skills.service';
import {SkillName} from '../model/skill';

describe('SkillsService', () => {
  let service: SkillsService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule]
    });
    service = TestBed.inject(SkillsService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('extractSkills should POST to the extract_skills endpoint and return results', () => {
    const reqBody: ExtractSkillsRequest = {lang: 'en', text: 'java, spring'};
    const mockResponse: SkillName[] = [
      {lang: 'en', name: 'java'},
      {lang: 'en', name: 'spring'}
    ];

    service.extractSkills(reqBody).subscribe(result => {
      expect(result).toEqual(mockResponse);
    });

    const httpReq = httpMock.expectOne(`${service.apiUrl}/extract_skills`);
    expect(httpReq.request.method).toBe('POST');
    expect(httpReq.request.body).toEqual(reqBody);

    httpReq.flush(mockResponse);
  });
});
