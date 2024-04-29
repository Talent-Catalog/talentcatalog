import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { JobsComponent } from './jobs.component';
import { FormBuilder, ReactiveFormsModule } from "@angular/forms";
import { HttpClientTestingModule } from "@angular/common/http/testing";
import {LocalStorageModule, LocalStorageService} from "angular-2-local-storage";
import { SortedByComponent } from "../../util/sort/sorted-by.component";
import { NgbPaginationModule } from "@ng-bootstrap/ng-bootstrap";
import { NgSelectModule } from "@ng-select/ng-select";
import { SearchOppsBy } from "../../../model/base";
import {Job, SearchJobRequest} from "../../../model/job";
import { Router } from '@angular/router';
import { Location } from '@angular/common';
import { Observable, of } from 'rxjs';
import {JobService} from "../../../services/job.service";
import {ChatService} from "../../../services/chat.service";
import {AuthorizationService} from "../../../services/authorization.service";
import {SalesforceService} from "../../../services/salesforce.service";
import {CountryService} from "../../../services/country.service";
import {PartnerService} from "../../../services/partner.service";
import {LOCALE_ID} from "@angular/core";

// Subclass of JobsComponent to expose createSearchRequest method for testing
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
        // Mock providers
          { provide: FormBuilder, useClass: FormBuilder },
      ]
    }).compileComponents();
    // Inject dependencies and spies
    formBuilder = TestBed.inject(FormBuilder);
   }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TestJobsComponent);
    jobsComponent = fixture.componentInstance;
    // Create an instance of TestJobsComponent with provided dependencies
    jobsComponent = new TestJobsComponent(
      TestBed.inject(ChatService),
      TestBed.inject(FormBuilder),
      TestBed.inject(AuthorizationService),
      TestBed.inject(LocalStorageService),
      TestBed.inject(JobService),
      TestBed.inject(SalesforceService),
      TestBed.inject(CountryService),
      TestBed.inject(PartnerService),
      TestBed.inject(LOCALE_ID)
    );

    jobsComponent = fixture.componentInstance;
    jobsComponent.searchForm = formBuilder.group({
      keyword: [''],
      myOppsOnly: [false],
      showClosedOpps: [false],
      showInactiveOpps: [false],
      withUnreadMessages: [false],
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
    expect((searchRequest as any).published).toBe(true); // Cast to 'any' to avoid TypeScript error
    expect(searchRequest.activeStages).toBe(true);
  });

  it('should generate correct search request for starredByMe search', () => {
    jobsComponent.searchBy = SearchOppsBy.starredByMe;
    const searchRequest = jobsComponent.exposeCreateSearchRequest();
    expect(searchRequest.starred).toBe(true);
  });

});
