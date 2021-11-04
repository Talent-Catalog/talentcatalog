import { TestBed } from '@angular/core/testing';

import { SalesforceService } from './salesforce.service';

describe('SalesforceService', () => {
  let service: SalesforceService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(SalesforceService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
