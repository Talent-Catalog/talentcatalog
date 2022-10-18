import {ComponentFixture, TestBed} from '@angular/core/testing';

import {ViewJobSubmissionListComponent} from './view-job-submission-list.component';

describe('ViewJobSubmissionListComponent', () => {
  let component: ViewJobSubmissionListComponent;
  let fixture: ComponentFixture<ViewJobSubmissionListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ViewJobSubmissionListComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ViewJobSubmissionListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
