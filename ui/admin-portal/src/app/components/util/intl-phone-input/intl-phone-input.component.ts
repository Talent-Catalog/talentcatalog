import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {Country} from "../../../model/country";
import {PhoneNumberFormat, PhoneNumberUtil} from 'google-libphonenumber';
import {CountryService} from "../../../services/country.service";
import {CountryCode, getCountryCallingCode} from 'libphonenumber-js';

@Component({
  selector: 'app-intl-phone-input',
  templateUrl: './intl-phone-input.component.html',
  styleUrls: ['./intl-phone-input.component.scss']
})
export class IntlPhoneInputComponent implements OnInit {
  phoneForm!: FormGroup;
  phoneUtil = PhoneNumberUtil.getInstance();
  formattedNumber: string;
  isValidNumber: boolean;
   countries: Country[];
  error: string;

  constructor(private fb: FormBuilder,
              private countryService: CountryService) {}

  ngOnInit(): void {
    this.phoneForm = this.fb.group({
      country: [null, Validators.required],
      phone: ['', Validators.required]
    });
    this.getCountries();
  }

  getCountries() {
    this.countryService.listCountries().subscribe(
      (results) => {
        this.countries = results;
      },
      (error) => {
        this.error = error;
      }
    )
  }

  getFlag(code: string): string {
    return String.fromCodePoint(...[...code.toUpperCase()].map(c => 127397 + c.charCodeAt(0)));
  }

  getDialCodeFromIso(isoCode: string): string {
    try {
      return '(+' + getCountryCallingCode(isoCode.toUpperCase() as CountryCode) + ')';
    } catch {
      return '';
    }
  }

  get fullPhoneNumber(): string {
    const { country, phone } = this.phoneForm.value;
    return `${country.dialCode}${phone}`;
  }

  validatePhone() {
    const country = this.phoneForm.value.country;
    const phone = this.phoneForm.value.phone;
    //const fullNumber = `${country.dialCode}${phone}`.replace(/\s+/g, '');
    try {
      const parsed = this.phoneUtil.parse(phone, country.isoCode);
      this.isValidNumber = this.phoneUtil.isValidNumber(parsed);
      this.formattedNumber = this.phoneUtil.format(parsed, PhoneNumberFormat.INTERNATIONAL);
    } catch (e) {
      this.isValidNumber = false;
      this.formattedNumber = null;
    }
  }

  // todo searchFn so can search country code by country name, iso code or country code

  onSubmit() {
    this.validatePhone();
    if (this.phoneForm.valid && this.isValidNumber) {
      console.log('Valid phone number:', this.formattedNumber);
      alert('Submitted: ' + this.formattedNumber);
    }
  }
}
