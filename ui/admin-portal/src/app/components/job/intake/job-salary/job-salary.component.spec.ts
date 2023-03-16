import {ComponentFixture, TestBed} from '@angular/core/testing';

import {JobSalaryComponent} from './job-salary.component';

describe('JobSalaryComponent', () => {
  let component: JobSalaryComponent;
  let fixture: ComponentFixture<JobSalaryComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ JobSalaryComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(JobSalaryComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
