import {Component, Input, OnInit} from '@angular/core';
import {AbstractControl} from "@angular/forms";
import {Country} from "../../../model/country";
import {CountryService} from "../../../services/country.service";
import {
  CountryCode,
  getCountryCallingCode,
  getExampleNumber,
  isValidPhoneNumber,
  parsePhoneNumberFromString,
  PhoneNumber
} from 'libphonenumber-js';
import examples from "libphonenumber-js/examples.mobile.json";

@Component({
  selector: 'app-intl-phone-input',
  templateUrl: './intl-phone-input.component.html',
  styleUrls: ['./intl-phone-input.component.scss']
})
export class IntlPhoneInputComponent implements OnInit {
  @Input() control: AbstractControl;
  isValidNumber: boolean;
  countries: Country[];
  error: string;
  isoCode: CountryCode;
  number: string;
  loading: boolean;


  constructor(private countryService: CountryService) {}

  ngOnInit(): void {
    this.loading = true;
    // Get countries for the country code dropdown
    this.getCountries();
    // Parse the phone number supplied to separate the iso code from the number
    const phoneNumber = this.getPhoneNumberFromString(this.control.value, null)
    // Set the separate iso code and number values for inputs if they exist
    if (phoneNumber as PhoneNumber) {
      this.isoCode = phoneNumber.country;
      this.number = phoneNumber.nationalNumber;
    } else {
      // If phone can't be parsed due to there being no country code detectable, or it is null, then just set the phone number as is
      this.number = this.control.value
    }
    this.checkForValidity();
  }

  get countryCodeRequired(): boolean {
    return this.isoCode == null && (this.number != '' && this.number != null);
  };

  get isInvalidNumber(): boolean {
    return this.isValidNumber != null && this.isValidNumber == false;
  }

  getCountries() {
    this.countryService.listCountries().subscribe(
      (results) => {
        // Filter out countries without isocodes (e.g. Stateless) as this won't have numbers associated with it
        // Map the country code to each country so that we can then use the country code in the search
        this.countries = results.filter(c => c.isoCode != null).map(c => ({
          ...c,
          countryCode: this.getCountryCodeStringFromIso(c.isoCode)
        }));
        this.loading = false;

      },
      (error) => {
        this.error = error;
        this.loading = false;
      }
    )
  }

  // Checks the two phone inputs (iso code & phone number) for validity and sets any errors if needed to be passed onto
  // the parent component for form validation.
  checkForValidity() {
    // If there is a number and a country code, check if the phone number is valid
    // Else if there is no number or country code, remove any previously set errors as phone isn't a required field
    if (this.number && this.isoCode) {
      this.validatePhoneNumber()
    } else if ((this.number == '' && this.isoCode) || (!this.isoCode && this.number)){
      // If a country code has been set without a number (or vice versa) set errors, we need both to validate a number
      this.control.setErrors({phoneInvalid: true});
    } else {
      this.control.setErrors(null);
    }
  }

  searchCountry = (term: string, item: Country) => {
    const lowerTerm = term.toLowerCase();
    return (
      item.name.toLowerCase().includes(lowerTerm) ||
      item.countryCode?.toLowerCase().includes(lowerTerm)
    );
  };

  // Checks if the two phone inputs create a valid phone number and if so sets the control to the valid international number which
  // is formatted back to a string
  validatePhoneNumber() {
    this.isValidNumber = this.getPhoneNumberValidity(this.number, this.isoCode)
    if (this.isValidNumber) {
      const validNumber = this.getPhoneNumberFromString(this.number, this.isoCode);
      const formattedValidNumber = this.getInternationalFormat(validNumber);
      this.control.patchValue(formattedValidNumber);
    } else {
      // If the number is invalid, set errors to parent so that the form can't be submitted
      this.control.setErrors({phoneInvalid: true});
    }
  }

  // WRAP LIB-PHONENUMBERJS FUNCTIONS IN OWN METHOD FOR ABILTIY TO MOCK IN TESTS

  // Gets an example telephone number format for the selected country code, assists user if the number entered is invalid.
  getExampleNumberForCountry() {
    const phoneExamples = getExampleNumber(this.isoCode, examples);
    return phoneExamples.formatNational();
  }

  getPhoneNumberFromString(number: string, isoCode: CountryCode): PhoneNumber {
    return parsePhoneNumberFromString(number, isoCode)
  }

  getPhoneNumberValidity(number: string, isoCode: CountryCode) {
    return isValidPhoneNumber(number, isoCode);
  }

  getInternationalFormat(phoneNumber: PhoneNumber) {
    return phoneNumber.formatInternational();
  }

  // Uses the Iso Codes to fetch the corresponding emoji flags for dropdown display
  getFlag(isoCode: string): string {
    return String.fromCodePoint(...[...isoCode.toUpperCase()].map(c => 127397 + c.charCodeAt(0)));
  }

  // Uses the Iso Codes to get the country calling codes for dropdown display
  getCountryCodeStringFromIso(isoCode: string): string {
    try {
      return "(+" + getCountryCallingCode(isoCode.toUpperCase() as CountryCode) + ")";
    } catch {
      return null;
    }
  }
}
