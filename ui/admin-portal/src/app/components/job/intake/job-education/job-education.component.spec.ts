import {JobEducationComponent} from "./job-education.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {JobService} from "../../../../services/job.service";
import {FormBuilder, FormsModule, ReactiveFormsModule} from "@angular/forms";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {By} from '@angular/platform-browser';
import {NgxWigModule} from "ngx-wig";
import {AutosaveStatusComponent} from "../../../util/autosave-status/autosave-status.component";
fdescribe('JobEducationComponent', () => {
  let component: JobEducationComponent;
  let fixture: ComponentFixture<JobEducationComponent>;
  let jobServiceSpy: jasmine.SpyObj<JobService>;

  beforeEach(async () => {
    const spy = jasmine.createSpyObj('JobService', ['get']);
    await TestBed.configureTestingModule({
      declarations: [ JobEducationComponent,AutosaveStatusComponent ],
      imports: [ReactiveFormsModule, FormsModule,NgxWigModule, HttpClientTestingModule],
      providers: [
        { provide: JobService, useValue: spy },
        FormBuilder
      ]
    })
    .compileComponents();
    jobServiceSpy = TestBed.inject(JobService) as jasmine.SpyObj<JobService>;
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(JobEducationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form with provided education requirements', () => {
    const educationRequirements = 'Bachelor\'s Degree';
    component.jobIntakeData = { educationRequirements: educationRequirements };
    component.editable = true;
    component.ngOnInit();
    expect(component.form.get('educationRequirements').value).toEqual(educationRequirements);
  });

  it('should disable form control when not editable', () => {
    const educationRequirements = 'Bachelor\'s Degree';
    component.jobIntakeData = { educationRequirements: educationRequirements };
    component.editable = false;
    component.ngOnInit();
    expect(component.form.get('educationRequirements').disabled).toBeTrue();
  });

  it('should not display error when error is not present', () => {
    const error = null;
    component.error = error;
    fixture.detectChanges();
    const errorElement = fixture.nativeElement.querySelector('.error');
    expect(errorElement).toBeNull();
  });

  it('should display error when error is present', () => {
    const error = 'An error occurred';
    component.error = error;
    fixture.detectChanges();
    const errorDiv = fixture.nativeElement.querySelector('div');
    expect(errorDiv.textContent).toContain(error);
  });
  
});
