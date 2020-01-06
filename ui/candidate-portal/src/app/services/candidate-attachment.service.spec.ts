import {TestBed} from '@angular/core/testing';

import {CandidateAttachmentService} from './candidate-attachment.service';

describe('CandidateAttachmentService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: CandidateAttachmentService = TestBed.get(CandidateAttachmentService);
    expect(service).toBeTruthy();
  });
});
