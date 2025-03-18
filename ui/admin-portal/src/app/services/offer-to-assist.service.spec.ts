import {TestBed} from '@angular/core/testing';

import {OfferToAssistService} from './offer-to-assist.service';

describe('OfferToAssistService', () => {
  let service: OfferToAssistService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(OfferToAssistService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
