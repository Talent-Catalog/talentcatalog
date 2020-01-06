import {TestBed} from '@angular/core/testing';

import {CandidateOccupationService} from './candidate-occupation.service';

describe('CandidateOccupationService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: CandidateOccupationService = TestBed.get(CandidateOccupationService);
    expect(service).toBeTruthy();
  });
});
