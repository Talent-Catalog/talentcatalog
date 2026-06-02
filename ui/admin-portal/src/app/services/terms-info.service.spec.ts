import {TestBed} from '@angular/core/testing';

import {TermsInfoService} from './terms-info.service';
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {AuthenticationService} from "./authentication.service";

describe('TermsInfoService', () => {
  let service: TermsInfoService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        { provide: AuthenticationService, useValue: {} }
      ]
    });
    service = TestBed.inject(TermsInfoService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
