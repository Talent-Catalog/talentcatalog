import { TestBed } from '@angular/core/testing';

import { CandidateLanguageService } from './candidate-language.service';

describe('CandidateLanguageService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: CandidateLanguageService = TestBed.get(CandidateLanguageService);
    expect(service).toBeTruthy();
  });
});
