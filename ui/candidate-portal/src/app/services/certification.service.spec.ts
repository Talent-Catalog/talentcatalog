import { TestBed } from '@angular/core/testing';

import { CertificationService } from './certification.service';

describe('CertificationService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: CertificationService = TestBed.get(CertificationService);
    expect(service).toBeTruthy();
  });
});
