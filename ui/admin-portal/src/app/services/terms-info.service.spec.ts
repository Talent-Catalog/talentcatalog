import {TestBed} from '@angular/core/testing';

import {TermsInfoService} from './terms-info.service';
import {HttpClientTestingModule} from "@angular/common/http/testing";

describe('TermsInfoService', () => {
  let service: TermsInfoService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [TermsInfoService]
    });
    service = TestBed.inject(TermsInfoService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
