import {TestBed} from '@angular/core/testing';

import {CandidateFieldService} from './candidate-field.service';

describe('CandidateFieldService', () => {
  let service: CandidateFieldService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(CandidateFieldService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
