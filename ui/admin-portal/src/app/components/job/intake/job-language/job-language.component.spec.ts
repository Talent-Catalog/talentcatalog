import {JobLanguageComponent} from "./job-language.component";
import {ComponentFixture, fakeAsync, TestBed, tick} from "@angular/core/testing";
import {ReactiveFormsModule} from "@angular/forms";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {NgxWigModule} from "ngx-wig";
import {AutosaveStatusComponent} from "../../../util/autosave-status/autosave-status.component";

fdescribe('JobLanguageComponent', () => {
  let component: JobLanguageComponent;
  let fixture: ComponentFixture<JobLanguageComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ JobLanguageComponent,AutosaveStatusComponent ],
      imports: [ReactiveFormsModule, NgxWigModule, HttpClientTestingModule]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(JobLanguageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form with provided language requirements', () => {
    const languageRequirements = 'Proficiency in English';
    component.jobIntakeData = { languageRequirements: languageRequirements };
    component.editable = true;
    component.ngOnInit();
    expect(component.form.get('languageRequirements').value).toEqual(languageRequirements);
  });

  it('should disable form control when not editable', () => {
    const languageRequirements = 'Proficiency in English';
    component.jobIntakeData = { languageRequirements: languageRequirements };
    component.editable = false;
    component.ngOnInit();
    expect(component.form.get('languageRequirements').disabled).toBeTrue();
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

  it('should update form control value when language requirements change', fakeAsync(() => {
    const newLanguageRequirements = 'Proficiency in Spanish';
    component.editable = true;
    component.jobIntakeData = { languageRequirements: 'Proficiency in English' };
    fixture.detectChanges();
    component.ngOnInit();
    component.jobIntakeData = { languageRequirements: newLanguageRequirements };
    fixture.detectChanges();
    component.ngOnInit();
    tick();
    expect(component.form.get('languageRequirements').value).toEqual(newLanguageRequirements);
  }));

  it('should disable form control when not editable', () => {
    component.editable = false;
    component.jobIntakeData = { languageRequirements: 'Proficiency in English' };
    fixture.detectChanges();
    component.ngOnInit();
    expect(component.form.get('languageRequirements').disabled).toBeTrue();
  });

});
