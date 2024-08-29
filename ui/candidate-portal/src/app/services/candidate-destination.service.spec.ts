import {TestBed} from '@angular/core/testing';

import {CandidateDestinationService} from './candidate-destination.service';

describe('CandidateDestinationService', () => {
  let service: CandidateDestinationService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(CandidateDestinationService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
