import {TestBed} from '@angular/core/testing';

import {CandidateFormService} from './candidate-form.service';

describe('CandidateFormService', () => {
  let service: CandidateFormService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(CandidateFormService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
