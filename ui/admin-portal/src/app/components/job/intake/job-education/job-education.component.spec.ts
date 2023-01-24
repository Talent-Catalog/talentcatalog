import {ComponentFixture, TestBed} from '@angular/core/testing';

import {JobEducationComponent} from './job-education.component';

describe('JobEducationComponent', () => {
  let component: JobEducationComponent;
  let fixture: ComponentFixture<JobEducationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ JobEducationComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(JobEducationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
