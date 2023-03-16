import {ComponentFixture, TestBed} from '@angular/core/testing';

import {JobLocationDetailsComponent} from './job-location-details.component';

describe('JobLocationDetailsComponent', () => {
  let component: JobLocationDetailsComponent;
  let fixture: ComponentFixture<JobLocationDetailsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ JobLocationDetailsComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(JobLocationDetailsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
