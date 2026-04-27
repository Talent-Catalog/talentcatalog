import {ComponentFixture, fakeAsync, TestBed, tick} from '@angular/core/testing';

import {IntlPhoneInputComponent} from './intl-phone-input.component';
import {NgSelectModule} from "@ng-select/ng-select";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {CountryService} from "../../../services/country.service";
import {of} from "rxjs";
import {Country} from "../../../model/country";
import {FormsModule, UntypedFormControl} from "@angular/forms";
import {PhoneNumber} from 'libphonenumber-js';
import {By} from "@angular/platform-browser";

describe('IntlPhoneInputComponent', () => {
  let component: IntlPhoneInputComponent;
  let fixture: ComponentFixture<IntlPhoneInputComponent>;
  let countryServiceSpy: jasmine.SpyObj<CountryService>;
  let mockCountries: Country[] = [
    { id: 1, name: 'Canada', isoCode: 'CA', status: 'active', translatedName: null},
    { id: 99, name: 'Stateless', isoCode: null, status: 'active', translatedName: null}
  ];

  beforeEach(async () => {
    const countrySpy = jasmine.createSpyObj('CountryService', ['listCountries']);
    await TestBed.configureTestingModule({
      declarations: [IntlPhoneInputComponent],
      imports: [NgSelectModule, HttpClientTestingModule, FormsModule],
      providers: [
        {provide: CountryService, useValue: countrySpy}
      ],
    }).compileComponents();

    countryServiceSpy = TestBed.inject(CountryService) as jasmine.SpyObj<CountryService>;
  });

  beforeEach(() => {
    countryServiceSpy.listCountries.and.returnValue(of(mockCountries));

    fixture = TestBed.createComponent(IntlPhoneInputComponent);
    component = fixture.componentInstance;

    component.control = new UntypedFormControl('+61400123456');
    spyOn(component, 'checkForValidity').and.callThrough();

    // Mock the lib-phonenumberjs functions called onInit
    const mockPhoneNumber = { nationalNumber: '0400123456', country: 'AU'  } as PhoneNumber;
    spyOn(component, 'getPhoneNumberFromString').and.returnValue(mockPhoneNumber);
    spyOn(component, 'getPhoneNumberValidity').and.returnValue(true);
    spyOn(component, 'getInternationalFormat').and.returnValue('+61 400 123 456');

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load and filter countries without iso code on init', fakeAsync(() => {
    tick();
    // Expect Stateless country to be removed
    expect(component.countries.length).toBe(1);
    expect(component.countries[0].isoCode).toBe('CA');
    expect(component.countries[0].countryCode).toBeTruthy();
    expect(component.loading).toBeFalse();
  }));

  it('should set the iso code and number from a phone number on init', () => {
    expect(component.isoCode).toBe('AU');
    expect(component.number).toBe('0400123456')
  });

  it('should get country code from an iso code', () => {
    component.getCountryCodeStringFromIso("CA");
    expect(component.countries[0].countryCode).toBe('(+1)');
  });

  it('display error message if country code required', () => {
    component.isoCode = null;
    component.number = '123456789';
    fixture.detectChanges();
    const codeRequiredMsg = fixture.nativeElement.querySelector('#codeRequired');
    expect(codeRequiredMsg).toBeTruthy();
  });

  it('display error message if number invalid', () => {
    component.isValidNumber = false;
    component.isoCode = 'CA';
    fixture.detectChanges();
    const invalidNumberMsg = fixture.nativeElement.querySelector('#invalidNumber');
    expect(invalidNumberMsg).toBeTruthy();
  });

  it('should call checkForValidity on phone number input ngModelChange', () => {
    // Find the input field
    const inputDebugElement = fixture.debugElement.query(By.css('input'));

    // Simulate Angular's ngModelChange event with new value
    inputDebugElement.triggerEventHandler('ngModelChange', '123456789');

    fixture.detectChanges();

    expect(component.checkForValidity).toHaveBeenCalled();
  });

  it('should call validatePhoneNumber if number and isocode exists', () => {
    component.number = '0420123456';
    component.isoCode = 'AU';
    spyOn(component, 'validatePhoneNumber');

    component.checkForValidity();

    expect(component.validatePhoneNumber).toHaveBeenCalled();
  });

  it('should update control value if valid number', () => {
    component.number = '070 123 4567';
    component.isoCode = 'AF';

    const mockPhoneNumber = { nationalNumber: '070 123 4567', country: 'AF'  } as PhoneNumber;
    (component.getPhoneNumberFromString as jasmine.Spy).and.returnValue(mockPhoneNumber);
    (component.getInternationalFormat as jasmine.Spy).and.returnValue('+93 70 123 4567');

    component.validatePhoneNumber();

    expect(component.control.value).toBe('+93 70 123 4567');
  });

  it('should set error on control if invalid number', () => {
    component.number = '070 123 4567';
    component.isoCode = 'GB';
    (component.getPhoneNumberValidity as jasmine.Spy).and.returnValue(false);

    component.validatePhoneNumber();

    expect(component.control.hasError('phoneInvalid')).toBeTrue();
  });

});
