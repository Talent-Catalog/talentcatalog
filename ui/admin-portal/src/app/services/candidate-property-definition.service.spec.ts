import {TestBed} from '@angular/core/testing';

import {CandidatePropertyDefinitionService} from './candidate-property-definition.service';

describe('CandidatePropertyDefinitionService', () => {
  let service: CandidatePropertyDefinitionService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(CandidatePropertyDefinitionService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
