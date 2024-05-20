import {ComponentFixture, TestBed} from '@angular/core/testing';
import {FormBuilder, ReactiveFormsModule} from '@angular/forms';
import {MinSalaryComponent} from './min-salary.component';
import {AutosaveStatusComponent} from "../../../util/autosave-status/autosave-status.component";
import {NgxWigModule} from "ngx-wig";
import {HttpClientTestingModule} from "@angular/common/http/testing";

fdescribe('MinSalaryComponent', () => {
  let component: MinSalaryComponent;
  let fixture: ComponentFixture<MinSalaryComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ MinSalaryComponent, AutosaveStatusComponent ],
      imports: [ ReactiveFormsModule, NgxWigModule, HttpClientTestingModule ],
      providers: [ FormBuilder ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(MinSalaryComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form control with correct initial value and disabled state', () => {
    // Access the form control
    const minSalaryControl = component.form.get('minSalary');

    // Check if the form control is initialized
    expect(minSalaryControl).toBeTruthy();

    // Check if the initial value is set correctly
    expect(minSalaryControl.value).toEqual(component.jobIntakeData?.minSalary);

    // Check if the disabled state is set correctly
    expect(minSalaryControl.disabled).toBe(!component.editable);
  });

  it('should make form control valid if editable', () => {
    component.editable = true;
    fixture.detectChanges();

    expect(component.form.get('minSalary').valid).toBe(true);
  });

  it('should update form control value on input change', () => {
    // Simulate user input
    const newMinSalary = 50000;
    component.editable = true; // Enable editing
    fixture.detectChanges();

    // Find the input element
    const minSalaryInput: HTMLInputElement = fixture.nativeElement.querySelector('#minSalary');

    // Simulate user input
    minSalaryInput.value = newMinSalary.toString();
    minSalaryInput.dispatchEvent(new Event('input'));

    // Detect changes
    fixture.detectChanges();

    // Check if the form control value is updated correctly
    expect(component.form.get('minSalary').value).toEqual(newMinSalary);
  });

  it('should display a warning message when salary is not confirmed with employer', () => {
    const warningMessage = fixture.nativeElement.querySelector('.form-text');
    expect(warningMessage.textContent).toContain('Do not enter salary UNLESS it is confirmed with the employer.');
  });
});
