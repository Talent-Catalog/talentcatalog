import {ComponentFixture, TestBed, waitForAsync} from '@angular/core/testing';
import {ViewJobSubmissionListComponent } from './view-job-submission-list.component';
import {Job} from '../../../../../model/job';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {ShowCandidatesComponent} from "../../../../candidates/show/show-candidates.component";
import {FormBuilder,FormsModule, ReactiveFormsModule} from "@angular/forms";
import {LocalStorageModule} from "angular-2-local-storage";
import {DatePipe,TitleCasePipe} from "@angular/common";
import {ActivatedRoute} from "@angular/router";
import {of} from "rxjs";
import {SortedByComponent} from "../../../../util/sort/sorted-by.component";
import {NgSelectModule} from "@ng-select/ng-select";
import {
  CandidateSourceDescriptionComponent
} from "../../../../util/candidate-source-description/candidate-source-description.component";
import {AutosaveStatusComponent} from "../../../../util/autosave-status/autosave-status.component";
import {RouterTestingModule} from "@angular/router/testing";
import {NgbModule} from "@ng-bootstrap/ng-bootstrap";
import {Directive, Input} from "@angular/core";
import {MockJob} from "../../../../../MockData/MockJob";
 @Directive({
  selector: '[routerLink]'
})
export class RouterLinkDirectiveStub {
  @Input('routerLink') linkParams: any;
}
fdescribe('ViewJobSubmissionListComponent', () => {
  let component: ViewJobSubmissionListComponent;
  let fixture: ComponentFixture<ViewJobSubmissionListComponent>;
   beforeEach(waitForAsync(() => {
     TestBed.configureTestingModule({
      declarations: [ViewJobSubmissionListComponent, ShowCandidatesComponent,SortedByComponent, CandidateSourceDescriptionComponent,AutosaveStatusComponent
      ],
      imports: [
        HttpClientTestingModule,
        FormsModule,
        ReactiveFormsModule,
        LocalStorageModule.forRoot({}),
        NgbModule,
        NgSelectModule,
        RouterTestingModule
      ],

       providers:[
         DatePipe,
         TitleCasePipe,
         { provide: ActivatedRoute, useValue: { queryParams: of({}), snapshot: { data: {} } } }, // Mock ActivatedRoute
          FormBuilder,
      ]
    })
    .compileComponents();
   }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ViewJobSubmissionListComponent);
    component = fixture.componentInstance;
    component.job = MockJob;
    fixture.detectChanges();
  });


  it('should render correctly when submission list is available for the job', () => {
    const mockJob: Job = MockJob // Create a mock job with a submission list
    component.job = mockJob;

    fixture.detectChanges();

    const cardElement: HTMLElement = fixture.nativeElement.querySelector('.card');
    const cardHeaderElement: HTMLElement = cardElement.querySelector('.card-header');
    const cardBodyElement: HTMLElement = cardElement.querySelector('.card-body');
    const showCandidatesComponent: HTMLElement = cardBodyElement.querySelector('app-show-candidates');

    expect(cardElement).toBeTruthy(); // Check if the card element exists
    expect(cardHeaderElement.textContent).toContain('Submission List'); // Check if the card header contains "Submission List"
    expect(cardBodyElement).toBeTruthy(); // Check if the card body exists
    expect(showCandidatesComponent).toBeTruthy(); // Check if the ShowCandidatesComponent is rendered
  });
});
