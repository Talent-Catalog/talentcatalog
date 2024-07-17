import {ComponentFixture, TestBed} from '@angular/core/testing';
import {ReactiveFormsModule} from '@angular/forms';
import {OccupationCodeComponent} from './occupation-code.component';
import {AutosaveStatusComponent} from "../../../util/autosave-status/autosave-status.component";
import {HttpClientTestingModule} from "@angular/common/http/testing";

fdescribe('OccupationCodeComponent', () => {
  let component: OccupationCodeComponent;
  let fixture: ComponentFixture<OccupationCodeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ OccupationCodeComponent, AutosaveStatusComponent ],
      imports: [ ReactiveFormsModule, HttpClientTestingModule ],
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(OccupationCodeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form control with correct initial value and disabled state', () => {
    // Access the form control
    const occupationCodeControl = component.form.get('occupationCode');

    // Check if the form control is initialized
    expect(occupationCodeControl).toBeTruthy();

    // Check if the initial value is set correctly
    expect(occupationCodeControl.value).toEqual(component.jobIntakeData?.occupationCode);

    // Check if the disabled state is set correctly
    expect(occupationCodeControl.disabled).toBe(!component.editable);
  });

  it('should make form control valid if editable', () => {
    component.editable = true;
    fixture.detectChanges();

    expect(component.form.get('occupationCode').valid).toBe(true);
  });

  it('should update form control value on input change', () => {
    // Simulate user input
    const newOccupationCode = '123456';
    component.editable = true; // Enable editing
    fixture.detectChanges();

    // Find the input element
    const occupationCodeInput: HTMLInputElement = fixture.nativeElement.querySelector('#occupationCode');

    // Simulate user input
    occupationCodeInput.value = newOccupationCode;
    occupationCodeInput.dispatchEvent(new Event('input'));

    // Detect changes
    fixture.detectChanges();

    // Check if the form control value is updated correctly
    expect(component.form.get('occupationCode').value).toEqual(newOccupationCode);
  });

});
