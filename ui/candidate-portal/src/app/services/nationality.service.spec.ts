import { TestBed } from '@angular/core/testing';

import { NationalityService } from './nationality.service';

describe('NationalityService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: NationalityService = TestBed.get(NationalityService);
    expect(service).toBeTruthy();
  });
});
