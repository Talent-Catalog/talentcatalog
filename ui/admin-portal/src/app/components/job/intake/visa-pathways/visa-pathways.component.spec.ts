import {ComponentFixture, TestBed } from '@angular/core/testing';
import {ReactiveFormsModule} from '@angular/forms';
import {VisaPathwaysComponent} from './visa-pathways.component';
import {NgxWigModule} from 'ngx-wig';
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {AutosaveStatusComponent} from "../../../util/autosave-status/autosave-status.component";

fdescribe('VisaPathwaysComponent', () => {
  let component: VisaPathwaysComponent;
  let fixture: ComponentFixture<VisaPathwaysComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ VisaPathwaysComponent, AutosaveStatusComponent ],
      imports: [ ReactiveFormsModule, NgxWigModule , HttpClientTestingModule]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(VisaPathwaysComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form control with correct initial value and disabled state', () => {
    // Access the form control
    const visaPathwaysControl = component.form.get('visaPathways');

    // Check if the form control is initialized
    expect(visaPathwaysControl).toBeTruthy();

    // Check if the initial value is set correctly
    expect(visaPathwaysControl.value).toEqual(component.jobIntakeData?.visaPathways);

    // Check if the disabled state is set correctly
    expect(visaPathwaysControl.disabled).toBe(!component.editable);
  });

  it('should make form control valid if editable', () => {
    component.editable = true;
    fixture.detectChanges();

    expect(component.form.get('visaPathways').valid).toBe(true);
  });

  it('should update form control value on input change', () => {
    // Simulate user input
    const newVisaPathways = 'List of immigration pathways';
    component.editable = true; // Enable editing
    fixture.detectChanges();

    const visaPathways = fixture.nativeElement.querySelector('.nw-editor__res');
    visaPathways.innerText = newVisaPathways;
    visaPathways.dispatchEvent(new Event('input'));
    fixture.detectChanges();
    // Check if the form control value is updated correctly
    expect(component.form.get('visaPathways').value).toEqual(newVisaPathways);
  });

});
