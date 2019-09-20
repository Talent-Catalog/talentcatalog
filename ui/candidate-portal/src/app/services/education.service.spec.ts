import { TestBed } from '@angular/core/testing';

import { EducationService } from './education.service';

describe('EducationService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: EducationService = TestBed.get(EducationService);
    expect(service).toBeTruthy();
  });
});
