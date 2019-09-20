import { TestBed } from '@angular/core/testing';

import { EducationTypeService } from './education-type.service';

describe('EducationTypeService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: EducationTypeService = TestBed.get(EducationTypeService);
    expect(service).toBeTruthy();
  });
});
