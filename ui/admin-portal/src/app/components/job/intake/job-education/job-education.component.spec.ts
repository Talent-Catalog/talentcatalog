import {JobEducationComponent} from "./job-education.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {ReactiveFormsModule} from "@angular/forms";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {NgxWigModule} from "ngx-wig";
import {AutosaveStatusComponent} from "../../../util/autosave-status/autosave-status.component";
fdescribe('JobEducationComponent', () => {
  let component: JobEducationComponent;
  let fixture: ComponentFixture<JobEducationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ JobEducationComponent,AutosaveStatusComponent ],
      imports: [ReactiveFormsModule,NgxWigModule, HttpClientTestingModule]
    })
    .compileComponents();
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
