import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {JobInterestComponent} from './job-interest.component';

describe('JobInterestComponent', () => {
  let component: JobInterestComponent;
  let fixture: ComponentFixture<JobInterestComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ JobInterestComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(JobInterestComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
