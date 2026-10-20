import {TestBed} from '@angular/core/testing';

import {TermsInfoService} from './terms-info.service';
import {provideHttpClientTesting} from "@angular/common/http/testing";
import {provideHttpClient, withInterceptorsFromDi, withXhr} from "@angular/common/http";

describe('TermsInfoService', () => {
  let service: TermsInfoService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [TermsInfoService, provideHttpClient(withXhr(), withInterceptorsFromDi()), provideHttpClientTesting()]
    });
    service = TestBed.inject(TermsInfoService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
