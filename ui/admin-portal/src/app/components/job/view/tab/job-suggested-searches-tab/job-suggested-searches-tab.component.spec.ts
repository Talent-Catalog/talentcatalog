import {ComponentFixture, TestBed} from '@angular/core/testing';
import {JobSuggestedSearchesTabComponent} from './job-suggested-searches-tab.component';
import {Job} from "../../../../../model/job";
import {MockJob} from "../../../../../MockData/MockJob";
import {By} from '@angular/platform-browser';
import {JobPrepItem} from "../../../../../model/job-prep-item";
import {ViewJobSuggestedSearchesComponent} from "../../suggested-searches/view-job-suggested-searches/view-job-suggested-searches.component";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {LocalStorageModule} from "angular-2-local-storage";
import {MockJobPrepItem} from "../../../../../MockData/MockJobPrepItem";
import {RouterLinkStubDirective} from "../../../../login/login.component.spec";

fdescribe('JobSuggestedSearchesTabComponent', () => {
  let component: JobSuggestedSearchesTabComponent;
  let fixture: ComponentFixture<JobSuggestedSearchesTabComponent>;
  // Define mock data
  const mockJob: Job = MockJob;
  const mockHighlightItem: JobPrepItem = new MockJobPrepItem("Mock Item Description", "Mock Tab", true);


  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports:[HttpClientTestingModule,LocalStorageModule.forRoot({})],
      declarations: [ JobSuggestedSearchesTabComponent,ViewJobSuggestedSearchesComponent,RouterLinkStubDirective ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(JobSuggestedSearchesTabComponent);
    component = fixture.componentInstance;
    component.job = MockJob;
    fixture.detectChanges();
  });

  it('should emit jobUpdated event with updated job data', () => {
    // Define mock job data
    const updatedJob: Job = mockJob;
    updatedJob.name = "NEW JOB";
    // Spy on the jobUpdated EventEmitter
    spyOn(component.jobUpdated, 'emit').and.callThrough();
    // Simulate job update

    component.onJobUpdated(updatedJob);
    // Expect the jobUpdated event to be emitted with the updated job data
    expect(component.jobUpdated.emit).toHaveBeenCalledWith(updatedJob);
  });

  it('should highlight the correct item', () => {
    component.highlightItem = mockHighlightItem;

    // Trigger change detection
    fixture.detectChanges();

    const viewJobSuggestedSearchesComponent = fixture.debugElement.query(By.directive(ViewJobSuggestedSearchesComponent)).componentInstance;
    // Assert that the component instance is found
    expect(viewJobSuggestedSearchesComponent).toBeTruthy();

    // Check the highlightItem property of the component instance
    expect(viewJobSuggestedSearchesComponent.highlightItem).toEqual(mockHighlightItem);

  });
});
