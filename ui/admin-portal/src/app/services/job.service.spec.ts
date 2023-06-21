import {TestBed} from '@angular/core/testing';

import {JobOppIntakeService} from './job.service';

describe('JobService', () => {
  let service: JobOppIntakeService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(JobOppIntakeService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
