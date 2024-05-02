import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { JobsComponent } from './jobs.component';
import { FormBuilder, ReactiveFormsModule } from "@angular/forms";
import { HttpClientTestingModule } from "@angular/common/http/testing";
import {LocalStorageModule} from "angular-2-local-storage";
import { SortedByComponent } from "../../util/sort/sorted-by.component";
import { NgbPaginationModule } from "@ng-bootstrap/ng-bootstrap";
import { NgSelectModule } from "@ng-select/ng-select";
import { SearchOppsBy } from "../../../model/base";
import { SearchJobRequest} from "../../../model/job";

class TestJobsComponent extends JobsComponent {
  // Expose createSearchRequest method
  public exposeCreateSearchRequest(): SearchJobRequest {
    return this.createSearchRequest();
  }
}
fdescribe('JobsComponent', () => {
  let jobsComponent: TestJobsComponent;
  let fixture: ComponentFixture<TestJobsComponent>;
  let formBuilder: FormBuilder;

  // Setup for the test suite
  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [TestJobsComponent, SortedByComponent],
      imports: [
        HttpClientTestingModule,
        ReactiveFormsModule,
        NgbPaginationModule,
        LocalStorageModule.forRoot({}),
        NgSelectModule
      ],
      providers: [
          { provide: FormBuilder, useClass: FormBuilder },
      ]
    }).compileComponents();
    formBuilder = TestBed.inject(FormBuilder);
   }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TestJobsComponent);
    jobsComponent = fixture.componentInstance;
    jobsComponent.searchForm = formBuilder.group({
      keyword: [''],
      selectedStages: [[]],
      destinationIds: [[]]
    });
    // Set searchBy to live
    jobsComponent.searchBy = SearchOppsBy.live;
    // Detect changes
    fixture.detectChanges();
  });

  // Test cases
  it('should create', () => {
    expect(jobsComponent).toBeTruthy();
  });

  it('should call search function when the search form is submitted', () => {
    spyOn(jobsComponent, 'search');
    const form = fixture.nativeElement.querySelector('form');
    form.dispatchEvent(new Event('submit'));
    expect(jobsComponent.search).toHaveBeenCalled();
  });

  it('should generate correct search request for live search', () => {
    const searchRequest = jobsComponent.exposeCreateSearchRequest();
    expect(searchRequest.sfOppClosed).toBe(false);
    expect((searchRequest as any).published).toBe(true); // Cast to 'any' to get private members
    expect(searchRequest.activeStages).toBe(true);
  });

  it('should generate correct search request for starredByMe search', () => {
    jobsComponent.searchBy = SearchOppsBy.starredByMe;
    const searchRequest = jobsComponent.exposeCreateSearchRequest();
    expect(searchRequest.starred).toBe(true);
  });

});
