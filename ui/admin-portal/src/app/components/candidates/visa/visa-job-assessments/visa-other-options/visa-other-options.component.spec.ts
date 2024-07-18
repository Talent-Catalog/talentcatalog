import {ComponentFixture, TestBed} from '@angular/core/testing';
import {VisaOtherOptionsComponent} from './visa-other-options.component';
import {FormBuilder, ReactiveFormsModule} from '@angular/forms';
import {AutosaveStatusComponent} from "../../../../util/autosave-status/autosave-status.component";
import {NgSelectModule} from "@ng-select/ng-select";
import {HttpClientTestingModule} from "@angular/common/http/testing";

fdescribe('VisaOtherOptionsComponent', () => {
  let component: VisaOtherOptionsComponent;
  let fixture: ComponentFixture<VisaOtherOptionsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [VisaOtherOptionsComponent,AutosaveStatusComponent],
      imports: [HttpClientTestingModule,ReactiveFormsModule,NgSelectModule],
      providers: [FormBuilder] // Provide the FormBuilder
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(VisaOtherOptionsComponent);
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

  it('should return true if eligibleOther value is not NoResponse', () => {
    component.form.patchValue({ visaJobEligibleOther: 'Yes' });
    expect(component.hasNotes).toBe(true);
    component.form.patchValue({ visaJobEligibleOther: 'No' });
    expect(component.hasNotes).toBe(true);
  });

  it('should return false if eligibleOther value is NoResponse', () => {
    component.form.patchValue({ visaJobEligibleOther: 'NoResponse' });
    expect(component.hasNotes).toBe(false);
  });
});
