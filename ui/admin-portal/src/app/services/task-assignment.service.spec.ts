import { TestBed } from '@angular/core/testing';

import { TaskAssignmentService } from './task-assignment.service';

describe('TaskAssignmentService', () => {
  let service: TaskAssignmentService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(TaskAssignmentService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
