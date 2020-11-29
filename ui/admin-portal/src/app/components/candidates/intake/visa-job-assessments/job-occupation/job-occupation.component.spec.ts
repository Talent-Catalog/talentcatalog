import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {JobOccupationComponent} from './job-occupation.component';

describe('JobOccupationComponent', () => {
  let component: JobOccupationComponent;
  let fixture: ComponentFixture<JobOccupationComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ JobOccupationComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(JobOccupationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
