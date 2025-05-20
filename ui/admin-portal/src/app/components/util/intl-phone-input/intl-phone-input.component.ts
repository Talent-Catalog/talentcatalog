import {Component} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {Country} from "../../../model/country";
import {PhoneNumberFormat, PhoneNumberUtil} from 'google-libphonenumber';
import {CountryService} from "../../../services/country.service";

@Component({
  selector: 'app-intl-phone-input',
  templateUrl: './intl-phone-input.component.html',
  styleUrls: ['./intl-phone-input.component.scss']
})
export class IntlPhoneInputComponent {
  phoneForm!: FormGroup;
  phoneUtil = PhoneNumberUtil.getInstance();
  formattedNumber: string;
  isValidNumber: boolean;
  countries: Country[];
  error: string;

  constructor(private fb: FormBuilder,
              private countryService: CountryService) {}

  ngOnInit(): void {
    this.getCountries();

    this.phoneForm = this.fb.group({
      country: [this.countries[0], Validators.required],
      phone: ['', Validators.required]
    });

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

  get fullPhoneNumber(): string {
    const { country, phone } = this.phoneForm.value;
    return `${country.dialCode}${phone}`;
  }

  validatePhone() {
    const { country, phone } = this.phoneForm.value;
    const fullNumber = `${country.dialCode}${phone}`.replace(/\s+/g, '');
    try {
      const parsed = this.phoneUtil.parse(fullNumber, country.iso);
      this.isValidNumber = this.phoneUtil.isValidNumber(parsed);
      this.formattedNumber = this.phoneUtil.format(parsed, PhoneNumberFormat.INTERNATIONAL);
    } catch (e) {
      this.isValidNumber = false;
      this.formattedNumber = null;
    }
  }

  onSubmit() {
    this.validatePhone();
    if (this.phoneForm.valid && this.isValidNumber) {
      console.log('Valid phone number:', this.formattedNumber);
      alert('Submitted: ' + this.formattedNumber);
    }
  }
}
