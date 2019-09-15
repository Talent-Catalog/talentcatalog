import {TestBed} from '@angular/core/testing';

import {IndustryService} from './industry.service';

describe('IndustryService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: IndustryService = TestBed.get(IndustryService);
    expect(service).toBeTruthy();
  });
});
