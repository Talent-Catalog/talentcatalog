import { TestBed } from '@angular/core/testing';

import { LanguageLevelService } from './language-level.service';

describe('LanguageLevelService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: LanguageLevelService = TestBed.get(LanguageLevelService);
    expect(service).toBeTruthy();
  });
});
