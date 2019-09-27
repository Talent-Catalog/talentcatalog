import { TestBed } from '@angular/core/testing';

import { CandidateJobExperienceService } from './candidate-job-experience.service';

describe('JobExperienceService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: CandidateJobExperienceService = TestBed.get(CandidateJobExperienceService);
    expect(service).toBeTruthy();
  });
});
