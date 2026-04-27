import { TestBed } from '@angular/core/testing';

import { TermsInfoService } from './terms-info.service';

describe('TermsInfoService', () => {
  let service: TermsInfoService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(TermsInfoService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
