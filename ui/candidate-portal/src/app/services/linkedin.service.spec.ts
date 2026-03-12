import {TestBed} from '@angular/core/testing';

import {LinkedinService} from './linkedin.service';

describe('LinkedinPremiumService', () => {
  let service: LinkedinService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(LinkedinService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
