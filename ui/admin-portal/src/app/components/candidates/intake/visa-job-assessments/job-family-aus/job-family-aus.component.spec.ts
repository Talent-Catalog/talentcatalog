import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {JobFamilyAusComponent} from './job-family-aus.component';

describe('JobFamilyAusComponent', () => {
  let component: JobFamilyAusComponent;
  let fixture: ComponentFixture<JobFamilyAusComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ JobFamilyAusComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(JobFamilyAusComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
