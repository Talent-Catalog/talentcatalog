import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {Router} from '@angular/router';

import {CandidateFormService} from '../../../services/candidate-form.service';
import {CountryService} from '../../../services/country.service';
import {profileMatchValidator} from '../../util/validators/profile-match-validator';

import {TravelDocFormData, TravelDocType} from '../../../model/form';
import {ICandidateFormComponent} from '../../../model/candidate-form';
import {Country} from '../../../model/country';

/**
 * Candidate Travel Document Form (Italy)
 *
 * This component renders a form where candidates can enter and verify their
 * travel document details. It ensures consistency by comparing certain fields
 * against the candidate’s profile. If mismatches are found, users are prompted
 * to update their profile before submitting.
 */
@Component({
  selector: 'app-travel-doc-form',
  templateUrl: './travel-doc-form.component.html',
  styleUrls: ['./travel-doc-form.component.scss'],
})
export class TravelDocFormComponent
  implements OnInit, ICandidateFormComponent<TravelDocFormData> {
  /** When true, the form is read-only and cannot be submitted. */
  @Input() readOnly = false;

  /** Candidate object passed in for profile validation. */
  @Input() candidate: any | null = null;

  /** Emits after successful submission with saved form data. */
  @Output() submitted = new EventEmitter<TravelDocFormData>();

  /** Holds the reactive form instance. */
  form: FormGroup | null = null;

  /** Holds any network/server error messages. */
  error: any = null;

  /** Indicates if a submission request is in progress. */
  submitting = false;

  /** Cached list of countries for dropdown. */
  countries: Country[] = [];

  /** Expose enum for binding in template. */
  protected readonly TravelDocType = TravelDocType;

  constructor(
    private fb: FormBuilder,
    private candidateFormService: CandidateFormService,
    private countryService: CountryService,
    private router: Router
  ) {
  }

  // ---------------------------
  // Lifecycle
  // ---------------------------

  ngOnInit(): void {
    // Load countries first, then initialize form
    this.countryService.listCountries().subscribe({
      next: (list) => {
        this.countries = list;
        this.initForm();
      },
      error: () => {
        this.countries = [];
        this.initForm();
      },
    });
  }

  // ---------------------------
  // Form setup
  // ---------------------------

  /**
   * Builds the form and attaches profile validators.
   */
  private initForm(): void {
    const groupValidator = this.candidate
      ? profileMatchValidator(this.candidate, {
        firstName: 'user.firstName',
        lastName: 'user.lastName',
        dateOfBirth: 'dob',
        gender: 'gender',
        birthCountry: 'birthCountry.id',
      })
      : null;

    this.form = this.fb.group(
      {
        firstName: [{value: '', disabled: this.readOnly}, [Validators.required]],
        lastName: [{value: '', disabled: this.readOnly}, [Validators.required]],
        dateOfBirth: [{value: '', disabled: this.readOnly}, [Validators.required]],
        gender: [{value: '', disabled: this.readOnly}, [Validators.required]],
        birthCountry: [{value: '', disabled: this.readOnly}, [Validators.required]],
        placeOfBirth: [{value: '', disabled: this.readOnly}],
        travelDocType: [{value: '', disabled: this.readOnly}, [Validators.required]],
        travelDocNumber: [{value: '', disabled: this.readOnly}, [Validators.required]],
        travelDocIssuedBy: [{value: '', disabled: this.readOnly}, [Validators.required]],
        travelDocIssueDate: [{value: '', disabled: this.readOnly}, [Validators.required]],
        travelDocExpiryDate: [{value: '', disabled: this.readOnly}, [Validators.required]],
      },
      {validators: groupValidator}
    );

    // Load previously saved data into form
    this.candidateFormService.getTravelDocumentForm().subscribe({
      next: (data) => {
        const match = this.countries.find((c) => c.id === data.birthCountry?.id);
        this.form?.patchValue({
          ...data,
          birthCountry: match ?? null,
        });
      },
      error: () => this.form?.reset(),
    });
  }

  // ---------------------------
  // Utilities
  // ---------------------------

  /**
   * Compare function for country objects in dropdown.
   */
  compareCountry = (c1: Country, c2: Country): boolean => {
    return c1 && c2 ? c1.id === c2.id : c1 === c2;
  };

  /**
   * Navigate to profile tab for candidate to fix mismatches.
   */
  goToProfile(): void {
    this.router
    .navigateByUrl('/', {skipLocationChange: true})
    .then(() => this.router.navigate(['/profile'], {queryParams: {tab: 'Profile'}}));
  }

  // ---------------------------
  // Validation helpers
  // ---------------------------

  /** Return list of fields that mismatch profile. */
  get mismatchFields(): string[] {
    return this.form?.errors?.mismatch ? Object.keys(this.form.errors.mismatch) : [];
  }

  /** True if there are any mismatched fields. */
  get hasProfileMismatch(): boolean {
    return this.mismatchFields.length > 0;
  }

  /** True if a specific control has mismatch error. */
  controlHasProfileMismatch(ctrlName: string): boolean {
    return !!this.form?.get(ctrlName)?.errors?.mismatch;
  }

  /** True if form can be submitted. */
  canSubmit(): boolean {
    return (
      !!this.form &&
      this.form.valid &&
      !this.form.pending &&
      !this.submitting &&
      !this.readOnly &&
      !this.hasProfileMismatch
    );
  }

  /** Check if control has a given validation error. */
  hasError(ctrlName: keyof TravelDocFormData, validationName: string): boolean {
    if (!this.form) return false;
    const ctrl = this.form.get(ctrlName as string);
    return !this.readOnly && !!ctrl && ctrl.touched && ctrl.hasError(validationName);
  }

  // ---------------------------
  // Submission
  // ---------------------------

  /**
   * Submit handler: validates form and saves data.
   */
  onSubmit(): void {
    if (!this.canSubmit()) {
      this.form?.markAllAsTouched();
      return;
    }

    this.submitting = true;
    this.error = null;

    const data: TravelDocFormData = this.form!.getRawValue();

    this.candidateFormService.createOrUpdateTravelDocumentForm(data).subscribe({
      next: (value) => {
        this.submitted.emit(value);
        this.form!.markAsPristine();
        this.form!.markAsUntouched();
        this.submitting = false;
      },
      error: (err) => {
        this.error = err;
        this.submitting = false;
      },
    });
  }
}
