import {AuthenticationService} from "../../../../../services/authentication.service";
import {JobService} from "../../../../../services/job.service";
import {ComponentFixture,  TestBed } from "@angular/core/testing";
import {JobIntakeTabComponent} from "./job-intake-tab.component";
import {MockJob} from "../../../../../MockData/MockJob";
import {NgbAccordionModule, NgbModule} from "@ng-bootstrap/ng-bootstrap";
import {
  CostCommitEmployerComponent
} from "../../../intake/cost-commit-employer/cost-commit-employer.component";
import {
  RecruitmentProcessComponent
} from "../../../intake/recruitment-process/recruitment-process.component";
import {FormBuilder, FormsModule, ReactiveFormsModule} from "@angular/forms";
import {NgxWigModule} from "ngx-wig";
import {MockPartner} from "../../../../../MockData/MockPartner";
import {MinSalaryComponent} from "../../../intake/min-salary/min-salary.component";
import {AutosaveStatusComponent} from "../../../../util/autosave-status/autosave-status.component";
import {OccupationCodeComponent} from "../../../intake/occupation-code/occupation-code.component";
import {VisaPathwaysComponent} from "../../../intake/visa-pathways/visa-pathways.component";
import {JobSkillsComponent} from "../../../intake/job-skills/job-skills.component";
import {JobExperienceComponent} from "../../../intake/job-experience/job-experience.component";
import {JobLanguageComponent} from "../../../intake/job-language/job-language.component";
import {JobBenefitsComponent} from "../../../intake/job-benefits/job-benefits.component";
import {JobLocationComponent} from "../../../intake/job-location/job-location.component";
import {
  JobLocationDetailsComponent
} from "../../../intake/job-location-details/job-location-details.component";
import {JobSalaryComponent} from "../../../intake/job-salary/job-salary.component";
import {JobEducationComponent} from "../../../intake/job-education/job-education.component";
import {JobOppIntake} from "../../../../../model/job-opp-intake";
import {MockJobOppIntake} from "../../../../../MockData/MockJobOppIntake";
import {HttpClientModule} from "@angular/common/http";

fdescribe('JobIntakeTabComponent', () => {
  let component: JobIntakeTabComponent;
  let fixture: ComponentFixture<JobIntakeTabComponent>;
  let authServiceSpy: jasmine.SpyObj<AuthenticationService>;
  let jobServiceSpy: jasmine.SpyObj<JobService>;
  beforeEach(async () => {
    const authServiceSpyObj = jasmine.createSpyObj('AuthenticationService', ['getLoggedInUser']);
    const jobServiceObj = jasmine.createSpyObj('JobService', ['updateIntakeData', 'get']);
    await TestBed.configureTestingModule({
      declarations: [ JobIntakeTabComponent,CostCommitEmployerComponent,RecruitmentProcessComponent,MinSalaryComponent,JobEducationComponent,AutosaveStatusComponent,OccupationCodeComponent,VisaPathwaysComponent,JobSkillsComponent,JobExperienceComponent,JobLanguageComponent,JobBenefitsComponent,JobLocationDetailsComponent,JobSalaryComponent,JobLocationComponent ],
      imports:[HttpClientModule,NgbAccordionModule,NgbModule,NgxWigModule,FormsModule,ReactiveFormsModule],
      providers: [
        { provide: AuthenticationService, useValue: authServiceSpyObj },
        { provide: JobService, useValue: jobServiceObj  },
        FormBuilder
      ]
    })
    .compileComponents();

    authServiceSpy = TestBed.inject(AuthenticationService) as jasmine.SpyObj<AuthenticationService>;
    jobServiceSpy = TestBed.inject(JobService) as jasmine.SpyObj<JobService>; // Inject the spy object
    authServiceSpyObj.getLoggedInUser.and.returnValue(MockPartner);
   });

  beforeEach(() => {
    fixture = TestBed.createComponent(JobIntakeTabComponent);
    component = fixture.componentInstance;
    component.job = MockJob;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
    expect(jobServiceSpy).toBeTruthy(); // Add this line to check if the spy is injected
  });
  it('should display loading state when loading is true', () => {
    component.loading = true;
    fixture.detectChanges();
    const loadingElement = fixture.nativeElement.querySelector('.fa-spinner');
    expect(loadingElement).toBeTruthy();
  });
  it('should display error message when error is set', () => {
    const errorMessage = 'An error occurred.';
    component.error = errorMessage;
    fixture.detectChanges();
    const errorElement = fixture.nativeElement.querySelector('div.error-message');
    expect(errorElement.textContent).toContain(errorMessage);
  });
  it('should emit intakeChanged event when onIntakeChanged is called', () => {
    const jobOppIntake: JobOppIntake = new MockJobOppIntake(); // Add your test data here
    const emitSpy = spyOn(component.intakeChanged, 'emit');
    component.onIntakeChanged(jobOppIntake);
    expect(emitSpy).toHaveBeenCalledWith(jobOppIntake);
  });
});
