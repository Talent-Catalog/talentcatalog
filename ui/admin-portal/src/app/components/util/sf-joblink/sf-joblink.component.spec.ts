import {By} from '@angular/platform-browser';
import {SfJoblinkComponent, SfJoblinkValidationEvent} from "./sf-joblink.component";
import {ComponentFixture, fakeAsync, TestBed, tick, waitForAsync} from "@angular/core/testing";
import {SalesforceService} from "../../../services/salesforce.service";
import {FormBuilder, FormsModule, ReactiveFormsModule} from "@angular/forms";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {NgSelectModule} from "@ng-select/ng-select";
import {of, throwError} from "rxjs";
import {mockCandidateOpportunity} from "../../../MockData/MockCandidateOpportunity";

describe('SfJoblinkComponent', () => {
  let component: SfJoblinkComponent;
  let fixture: ComponentFixture<SfJoblinkComponent>;
  let salesforceServiceSpy: jasmine.SpyObj<SalesforceService>;

  beforeEach(waitForAsync(() => {
    const spy = jasmine.createSpyObj('SalesforceService', ['getOpportunity'],['pipe']);

    TestBed.configureTestingModule({
      declarations: [SfJoblinkComponent],
      imports: [ReactiveFormsModule,FormsModule,HttpClientTestingModule,NgSelectModule],
      providers: [
        FormBuilder,
        { provide: SalesforceService, useValue: spy }
      ]
    }).compileComponents();

    salesforceServiceSpy = TestBed.inject(SalesforceService) as jasmine.SpyObj<SalesforceService>;
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SfJoblinkComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form with sfJoblink control', () => {
    expect(component.form.contains('sfJoblink')).toBe(true);
  });

  it('should show error message for invalid URL pattern', () => {
    const input = component.form.controls['sfJoblink'];
    input.setValue('invalid-url');
    input.markAsTouched();
    fixture.detectChanges();

    const errorMessage = fixture.debugElement.query(By.css('.alert-danger'));
    expect(errorMessage).toBeTruthy();
  });

  it('should validate empty URL as valid', (done: DoneFn) => {
    component.ngOnInit();
    const control = component.form.controls['sfJoblink'];
    control.setValue('');

    (component as any).sfJoblinkValidator()(control).subscribe(result => {
      expect(result).toBeNull();
      done();
    });
  });


  it('should validate valid salesforce URL', (done: DoneFn) => {
    salesforceServiceSpy.getOpportunity.and.returnValue(of(mockCandidateOpportunity));
    spyOn(component.sfJoblinkValidation, 'emit').and.callThrough();
    component.ngOnInit();
    const control = component.form.controls['sfJoblink'];
    control.setValue('https://valid.salesforce.com');

    (component as any).sfJoblinkValidator()(control).subscribe(result => {
      expect(result).toBeNull();
      expect(component.sfJoblinkValidation.emit).toHaveBeenCalledWith(jasmine.any(SfJoblinkValidationEvent));
      expect(salesforceServiceSpy.getOpportunity).toHaveBeenCalled();
      done();
    });
  });


  it('should invalidate URL with null opportunity', (done: DoneFn) => {
    salesforceServiceSpy.getOpportunity.and.returnValue(of(null));

    component.ngOnInit();
    const control = component.form.controls['sfJoblink'];
    control.setValue('https://invalid.salesforce.com');

    (component as any).sfJoblinkValidator()(control).subscribe(result => {
      expect(result).toEqual({ 'invalidSfJoblink': true });
      done();
    });
  });

  it('should handle error during validation', (done: DoneFn) => {
    const error = new Error('Server error');
    salesforceServiceSpy.getOpportunity.and.returnValue(throwError(error));
    spyOn(component.updateError, 'emit').and.callThrough();
    component.ngOnInit();
    const control = component.form.controls['sfJoblink'];
    control.setValue('https://error.salesforce.com');

    (component as any).sfJoblinkValidator()(control).subscribe(result => {
      expect(result).toBeNull();
      expect(component.updateError.emit).toHaveBeenCalledWith(error);
      done();
    });
  });
});
