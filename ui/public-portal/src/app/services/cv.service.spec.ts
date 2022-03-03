import { TestBed } from '@angular/core/testing';

import { CvService } from './cv.service';

describe('CvService', () => {
  let service: CvService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(CvService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
