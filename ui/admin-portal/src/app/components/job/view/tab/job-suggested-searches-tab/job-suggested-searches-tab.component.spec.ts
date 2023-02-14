import {ComponentFixture, TestBed} from '@angular/core/testing';

import {JobSuggestedSearchesTabComponent} from './job-suggested-searches-tab.component';

describe('JobSuggestedSearchesTabComponent', () => {
  let component: JobSuggestedSearchesTabComponent;
  let fixture: ComponentFixture<JobSuggestedSearchesTabComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ JobSuggestedSearchesTabComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(JobSuggestedSearchesTabComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
