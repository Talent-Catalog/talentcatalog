import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ViewJobDescriptionComponent } from './view-job-description.component';
import { Job } from '../../../../../model/job';
import {SafePipe} from "../../../../../pipes/safe.pipe";
import {NgModule} from "@angular/core";
import {BrowserDynamicTestingModule} from "@angular/platform-browser-dynamic/testing";
 fdescribe('ViewJobDescriptionComponent', () => {
  let component: ViewJobDescriptionComponent;
  let fixture: ComponentFixture<ViewJobDescriptionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ViewJobDescriptionComponent,SafePipe ],
     })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ViewJobDescriptionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should safely load job description URL into iframe', () => {
    const job: Job = {
      closed: false,
      contactUser: undefined,
      country: undefined,
      employerEntity: undefined,
      exclusionList: undefined,
      hiringCommitment: "",
      jobCreator: undefined,
      jobOppIntake: undefined,
      jobSummary: "",
      name: "",
      opportunityScore: "",
      publishedBy: undefined,
      publishedDate: undefined,
      stage: undefined,
      starringUsers: [],
      submissionDueDate: undefined,
      submissionList: undefined,
      suggestedList: undefined,
      suggestedSearches: [],
      won: false
      // mock job data
    };

    component.job = job;

    fixture.detectChanges();

    const iframeElement: HTMLIFrameElement = fixture.nativeElement.querySelector('iframe');
    expect(iframeElement).toBeTruthy();
    expect(iframeElement.src).toBe(component.jobDescription);
  });
});
