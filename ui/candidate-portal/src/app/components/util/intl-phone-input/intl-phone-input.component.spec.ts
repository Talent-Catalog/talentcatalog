import type {Mock, MockedObject} from "vitest";
import {Component, forwardRef, Input, ChangeDetectionStrategy} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {ControlValueAccessor, FormsModule, NG_VALUE_ACCESSOR, UntypedFormControl} from '@angular/forms';
import {By} from '@angular/platform-browser';
import {provideHttpClientTesting} from '@angular/common/http/testing';
import {provideHttpClient, withInterceptorsFromDi, withXhr} from '@angular/common/http';
import {NgSelectModule} from '@ng-select/ng-select';
import {of, throwError} from 'rxjs';
import {PhoneNumber} from 'libphonenumber-js';

import {IntlPhoneInputComponent} from './intl-phone-input.component';
import {CountryService} from '../../../services/country.service';
import {Country} from '../../../model/country';

@Component({
    selector: 'tc-input',
    template: `
    <input
      [type]="type"
      [placeholder]="placeholder"
      [value]="value || ''"
      (input)="handleInput($event)"
      (blur)="onTouched()"
    >
  `,
    providers: [
        {
            provide: NG_VALUE_ACCESSOR,
            useExisting: forwardRef(() => TcInputStubComponent),
            multi: true
        }
    ],
    changeDetection: ChangeDetectionStrategy.Eager,
    standalone: false
})
class TcInputStubComponent implements ControlValueAccessor {
    @Input()
    type: string;
    @Input()
    placeholder: string;

    value: string;

    private onChange: (value: string) => void = () => {
    };
    onTouched: () => void = () => {
    };

    writeValue(value: string): void {
        this.value = value;
    }

    registerOnChange(fn: (value: string) => void): void {
        this.onChange = fn;
    }

    registerOnTouched(fn: () => void): void {
        this.onTouched = fn;
    }

    setDisabledState(isDisabled: boolean): void {
        // Not needed for these tests.
    }

    handleInput(event: Event): void {
        const value = (event.target as HTMLInputElement).value;
        this.value = value;
        this.onChange(value);
    }
}

@Component({
    selector: 'tc-error-message',
    template: '<ng-content></ng-content>',
    changeDetection: ChangeDetectionStrategy.Eager,
    standalone: false
})
class TcErrorMessageStubComponent {
}

describe('IntlPhoneInputComponent', () => {
    let component: IntlPhoneInputComponent;
    let fixture: ComponentFixture<IntlPhoneInputComponent>;
    let countryServiceSpy: MockedObject<CountryService>;

    const mockCountries: Country[] = [
        {
            id: 1,
            name: 'Canada',
            isoCode: 'CA',
            status: 'active',
            translatedName: null
        },
        {
            id: 99,
            name: 'Stateless',
            isoCode: null,
            status: 'active',
            translatedName: null
        }
    ];

    beforeEach(async () => {
        countryServiceSpy = {
            listCountries: vi.fn().mockName("CountryService.listCountries")
        } as unknown as MockedObject<CountryService>;

        await TestBed.configureTestingModule({
            declarations: [
                IntlPhoneInputComponent,
                TcInputStubComponent,
                TcErrorMessageStubComponent
            ],
            imports: [
                FormsModule,
                NgSelectModule
            ],
            providers: [
                {
                    provide: CountryService,
                    useValue: countryServiceSpy
                },
                provideHttpClient(withXhr(), withInterceptorsFromDi()),
                provideHttpClientTesting()
            ]
        }).compileComponents();
    });

    beforeEach(() => {
        countryServiceSpy.listCountries.mockReturnValue(of(mockCountries));

        fixture = TestBed.createComponent(IntlPhoneInputComponent);
        component = fixture.componentInstance;

        component.control = new UntypedFormControl('+61400123456');

        const mockPhoneNumber = {
            nationalNumber: '400123456',
            country: 'AU'
        } as PhoneNumber;

        vi.spyOn(component, 'checkForValidity');
        vi.spyOn(component, 'getPhoneNumberFromString').mockReturnValue(mockPhoneNumber);
        vi.spyOn(component, 'getPhoneNumberValidity').mockReturnValue(true);
        vi.spyOn(component, 'getInternationalFormat').mockReturnValue('+61 400 123 456');

        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should load and filter countries without iso codes', () => {
        expect(countryServiceSpy.listCountries).toHaveBeenCalled();

        expect(component.countries.length).toBe(1);
        expect(component.countries[0].isoCode).toBe('CA');
        expect(component.countries[0].countryCode).toBe('(+1)');
        expect(component.loading).toBe(false);
    });

    it('should set the iso code and number from the initial phone number', () => {
        expect(component.isoCode).toBe('AU');
        expect(component.number).toBe('400123456');
    });

    it('should return the calling code for a valid iso code', () => {
        expect(component.getCountryCodeStringFromIso('CA')).toBe('(+1)');
    });

    it('should return null for an invalid iso code', () => {
        expect(component.getCountryCodeStringFromIso('INVALID')).toBeNull();
    });

    it('should display an error when a country code is required', () => {
        component.isoCode = null;
        component.number = '123456789';

        fixture.detectChanges();

        const error = fixture.debugElement.query(By.css('#codeRequired'));

        expect(error).toBeTruthy();
    });

    it('should display an invalid-number error', () => {
        component.isValidNumber = false;
        component.isoCode = 'CA';
        component.number = '123456789';

        fixture.detectChanges();

        const error = fixture.debugElement.query(By.css('#invalidNumber'));

        expect(error).toBeTruthy();
    });

    it('should call checkForValidity when the phone number changes', () => {
        (component.checkForValidity as Mock).mockClear();

        const input = fixture.debugElement.query(By.css('tc-input input'));

        input.nativeElement.value = '123456789';
        input.nativeElement.dispatchEvent(new Event('input'));

        fixture.detectChanges();

        expect(component.number).toBe('123456789');
        expect(component.checkForValidity).toHaveBeenCalled();
    });

    it('should validate when both number and iso code exist', () => {
        component.number = '0420123456';
        component.isoCode = 'AU';

        vi.spyOn(component, 'validatePhoneNumber').mockReturnValue(undefined);

        component.checkForValidity();

        expect(component.validatePhoneNumber).toHaveBeenCalled();
    });

    it('should require an iso code when a number exists', () => {
        component.number = '123456789';
        component.isoCode = null;

        component.checkForValidity();

        expect(component.control.hasError('phoneInvalid')).toBe(true);
    });

    it('should update the control when the phone number is valid', () => {
        component.number = '070 123 4567';
        component.isoCode = 'AF';

        const phoneNumber = {
            nationalNumber: '701234567',
            country: 'AF'
        } as PhoneNumber;

        (component.getPhoneNumberFromString as Mock).mockReturnValue(phoneNumber);

        (component.getPhoneNumberValidity as Mock).mockReturnValue(true);

        (component.getInternationalFormat as Mock).mockReturnValue('+93 70 123 4567');

        component.validatePhoneNumber();

        expect(component.control.value).toBe('+93 70 123 4567');
        expect(component.isValidNumber).toBe(true);
    });

    it('should set an error when the phone number is invalid', () => {
        component.number = '070 123 4567';
        component.isoCode = 'GB';

        (component.getPhoneNumberValidity as Mock).mockReturnValue(false);

        component.validatePhoneNumber();

        expect(component.control.hasError('phoneInvalid')).toBe(true);
        expect(component.isValidNumber).toBe(false);
    });

    it('should remove the phone number when number is empty', () => {
        component.number = '';
        component.isoCode = 'CA';
        component.control.setValue('+1 555 555 5555');
        component.control.setErrors({ phoneInvalid: true });

        component.checkForValidity();

        expect(component.control.value).toBeNull();
        expect(component.control.errors).toBeNull();
    });

    it('should search countries by name', () => {
        expect(component.searchCountry('can', mockCountries[0])).toBe(true);
    });

    it('should search countries by calling code', () => {
        const country = {
            ...mockCountries[0],
            countryCode: '(+1)'
        };

        expect(component.searchCountry('+1', country)).toBe(true);
    });

    it('should set an error when loading countries fails', () => {
        const errorFixture = TestBed.createComponent(IntlPhoneInputComponent);

        const errorComponent = errorFixture.componentInstance;
        errorComponent.control = new UntypedFormControl(null);

        countryServiceSpy.listCountries.mockReturnValue(throwError('Unable to load countries'));

        errorFixture.detectChanges();

        expect(errorComponent.error).toBe('Unable to load countries');
        expect(errorComponent.loading).toBe(false);
    });
});
