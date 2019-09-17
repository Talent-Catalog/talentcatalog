import { TestBed } from '@angular/core/testing';

import { WorkExperienceService } from './work-experience.service';

describe('WorkExperienceService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: WorkExperienceService = TestBed.get(WorkExperienceService);
    expect(service).toBeTruthy();
  });
});
