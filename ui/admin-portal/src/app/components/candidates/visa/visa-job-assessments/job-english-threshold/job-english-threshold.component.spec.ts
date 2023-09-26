import {ComponentFixture, TestBed} from '@angular/core/testing';

import {JobEnglishThresholdComponent} from './job-english-threshold.component';

describe('JobEnglishThresholdComponent', () => {
  let component: JobEnglishThresholdComponent;
  let fixture: ComponentFixture<JobEnglishThresholdComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ JobEnglishThresholdComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(JobEnglishThresholdComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
