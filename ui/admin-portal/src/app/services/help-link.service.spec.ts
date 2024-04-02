import {TestBed} from '@angular/core/testing';

import {HelpLinkService} from './help-link.service';

describe('HelpLinkService', () => {
  let service: HelpLinkService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(HelpLinkService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
