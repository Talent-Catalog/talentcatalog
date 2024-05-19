
import {JobExperienceComponent} from "./job-experience.component";
import {JobService} from "../../../../services/job.service";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {FormBuilder, FormsModule, ReactiveFormsModule} from "@angular/forms";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {NgxWigModule} from "ngx-wig";
import {AutosaveStatusComponent} from "../../../util/autosave-status/autosave-status.component";

fdescribe('JobExperienceComponent', () => {
  let component: JobExperienceComponent;
  let fixture: ComponentFixture<JobExperienceComponent>;
  let jobServiceSpy: jasmine.SpyObj<JobService>;

  beforeEach(async () => {
    const spy = jasmine.createSpyObj('JobService', ['get']);
    await TestBed.configureTestingModule({
      declarations: [ JobExperienceComponent,AutosaveStatusComponent ],
      imports: [ReactiveFormsModule, FormsModule, NgxWigModule ,HttpClientTestingModule],
      providers: [
        { provide: JobService, useValue: spy },
        FormBuilder
      ]
    })
    .compileComponents();
    jobServiceSpy = TestBed.inject(JobService) as jasmine.SpyObj<JobService>;
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(JobExperienceComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form with provided employment experience', () => {
    const employmentExperience = 'Minimum 2 years of experience';
    component.jobIntakeData = { employmentExperience: employmentExperience };
    component.editable = true;
    component.ngOnInit();
    expect(component.form.get('employmentExperience').value).toEqual(employmentExperience);
  });

  it('should disable form control when not editable', () => {
    const employmentExperience = 'Minimum 2 years of experience';
    component.jobIntakeData = { employmentExperience: employmentExperience };
    component.editable = false;
    component.ngOnInit();
    expect(component.form.get('employmentExperience').disabled).toBeTrue();
  });

  it('should display error message when error is present', () => {
    const error = 'An error occurred';
    component.error = error;
    fixture.detectChanges();
    const errorElement = fixture.nativeElement.querySelector('div');
    expect(errorElement.textContent).toContain(error);
  });

  it('should not display error message when error is not present', () => {
    const error = null;
    component.error = error;
    fixture.detectChanges();
    const errorElement = fixture.nativeElement.querySelector('.error');
    expect(errorElement).toBeNull();
  });

  it('should render autosave status component when editable', () => {
    component.editable = true;
    fixture.detectChanges();
    const autosaveStatusComponent = fixture.nativeElement.querySelector('app-autosave-status');
    expect(autosaveStatusComponent).toBeTruthy();
  });

  it('should not render autosave status component when not editable', () => {
    component.editable = false;
    fixture.detectChanges();
    const autosaveStatusComponent = fixture.nativeElement.querySelector('app-autosave-status');
    expect(autosaveStatusComponent).toBeNull();
  });

  it('should update form control value when employment experience changes', () => {
    const newExperience = 'Minimum 3 years of experience';
    component.editable = true;
    component.jobIntakeData = { employmentExperience: 'Minimum 2 years of experience' };
    fixture.detectChanges();
    component.ngOnInit();
    component.jobIntakeData = { employmentExperience: newExperience };
    component.ngOnInit();
    expect(component.form.get('employmentExperience').value).toEqual(newExperience);
  });

  it('should disable form control when not editable', () => {
    component.editable = false;
    component.jobIntakeData = { employmentExperience: 'Minimum 2 years of experience' };
    fixture.detectChanges();
    component.ngOnInit();
    expect(component.form.get('employmentExperience').disabled).toBeTrue();
  });
});
