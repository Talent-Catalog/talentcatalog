import {TestBed} from '@angular/core/testing';

import {CandidateCertificationService} from './candidate-certification.service';

describe('CertificationService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: CandidateCertificationService = TestBed.get(CandidateCertificationService);
    expect(service).toBeTruthy();
  });
});
