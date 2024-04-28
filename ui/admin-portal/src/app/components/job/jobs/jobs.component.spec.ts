import {ComponentFixture, TestBed, waitForAsync} from '@angular/core/testing';
import {JobsComponent} from './jobs.component';
import {FormBuilder, ReactiveFormsModule} from "@angular/forms";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {LocalStorageModule} from "angular-2-local-storage";
import {SortedByComponent} from "../../util/sort/sorted-by.component";
import {NgbPaginationModule} from "@ng-bootstrap/ng-bootstrap";
import {NgSelectModule} from "@ng-select/ng-select";
import {SearchOppsBy} from "../../../model/base";
import {SearchJobRequest} from "../../../model/job";
// Import necessary modules and components for testing

fdescribe('JobsComponent', () => { // Focused describe block for JobsComponent

  let component: JobsComponent; // Declare component variable
  let fixture: ComponentFixture<JobsComponent>; // Declare fixture variable

  let formBuilder: FormBuilder; // Declare formBuilder variable

  beforeEach(waitForAsync(() => { // Perform async setup before each test

    TestBed.configureTestingModule({
      declarations: [JobsComponent, SortedByComponent], // Declare component and its dependencies
      imports: [
        HttpClientTestingModule, // Import HttpClientTestingModule for HTTP testing
        ReactiveFormsModule, // Import ReactiveFormsModule for form handling
        NgbPaginationModule, // Import NgbPaginationModule for pagination
        LocalStorageModule.forRoot({}), // Import LocalStorageModule for local storage
        NgSelectModule // Import NgSelectModule for dropdown select
      ],
    })
    .compileComponents(); // Compile component and its dependencies asynchronously

    formBuilder = TestBed.inject(FormBuilder); // Inject FormBuilder

  }));

  beforeEach(() => { // Perform setup before each test

    fixture = TestBed.createComponent(JobsComponent); // Create component fixture
    component = fixture.componentInstance; // Get component instance

    component.searchForm = formBuilder.group({ // Initialize searchForm with formBuilder
      keyword: [''], // Add form controls as per your component's searchForm structure
      myOppsOnly: [false],
      showClosedOpps: [false],
      showInactiveOpps: [false],
      withUnreadMessages: [false],
      selectedStages: [[]],
      destinationIds: [[]]
    });
    component.searchBy = SearchOppsBy.live;

    fixture.detectChanges(); // Detect changes to the component

  });

  it('should create', () => { // Test if component is created successfully
    expect(component).toBeTruthy(); // Expect component to be truthy
  });

  it('should call search function when the search form is submitted',   (() => {
    spyOn(component, 'search'); // Spy on the search function
    const form = fixture.nativeElement.querySelector('form'); // Get form element from fixture
    form.dispatchEvent(new Event('submit')); // Dispatch submit event on form

    expect(component.search).toHaveBeenCalled(); // Expect that search function is called
  }));
});
