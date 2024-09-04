import {ComponentFixture, TestBed} from '@angular/core/testing';
import {VisaOneEightSixComponent} from './visa-one-eight-six.component';
import {FormBuilder, ReactiveFormsModule} from '@angular/forms';
import {AutosaveStatusComponent} from "../../../../util/autosave-status/autosave-status.component";
import {NgSelectModule} from "@ng-select/ng-select";
import {HttpClientTestingModule} from "@angular/common/http/testing";

describe('VisaOneEightSixComponent', () => {
  let component: VisaOneEightSixComponent;
  let fixture: ComponentFixture<VisaOneEightSixComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [VisaOneEightSixComponent,AutosaveStatusComponent],
      imports: [HttpClientTestingModule,ReactiveFormsModule,NgSelectModule],
      providers: [FormBuilder] // Provide the FormBuilder
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(VisaOneEightSixComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should display error message if error is set', () => {
    component.error = 'Some error occurred';
    fixture.detectChanges();
    const errorElement: HTMLElement = fixture.nativeElement.querySelector('div');
    expect(errorElement.textContent).toContain('Some error occurred');
  });

  it('should return true if eligible186 value is Yes or No', () => {
    component.form.patchValue({ visaJobEligible186: 'Yes' });
    expect(component.hasNotes).toBe(true);
    component.form.patchValue({ visaJobEligible186: 'No' });
    expect(component.hasNotes).toBe(true);
  });

  it('should return false if eligible186 value is undefined or null', () => {
    component.form.patchValue({ visaJobEligible186: undefined });
    expect(component.hasNotes).toBe(false);
    component.form.patchValue({ visaJobEligible186: null });
    expect(component.hasNotes).toBe(false);
  });
});
