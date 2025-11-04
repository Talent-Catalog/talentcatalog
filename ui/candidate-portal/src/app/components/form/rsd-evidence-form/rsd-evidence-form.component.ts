import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {CandidateFormService} from "../../../services/candidate-form.service";
import {RsdEvidenceDocumentType, RsdEvidenceFormData, RsdRefugeeStatus} from "../../../model/form";
import {ICandidateFormComponent} from "../../../model/candidate-form";
import {Candidate} from "../../../model/candidate";

@Component({
  selector: 'app-rsd-evidence-form',
  templateUrl: './rsd-evidence-form.component.html'
})
export class RsdEvidenceFormComponent implements OnInit, ICandidateFormComponent<RsdEvidenceFormData> {
  @Input() readOnly = false;
  @Input() candidate :Candidate;
  @Output() submitted = new EventEmitter<RsdEvidenceFormData>();

  form: FormGroup;
  error: any = null;
  submitting = false;

  readonly refugeeStatusOptions = [
    { value: RsdRefugeeStatus.RecognizedByUnhcr, label: 'Recognized by UNHCR' },
    { value: RsdRefugeeStatus.RecognizedByHostCountry, label: 'Recognized by host country' },
    { value: RsdRefugeeStatus.Pending, label: 'Pending' }
  ];

  readonly documentTypeOptions = [
    { value: RsdEvidenceDocumentType.UnhcrCertificate, label: 'UNHCR Certificate of Recognition' },
    { value: RsdEvidenceDocumentType.HostCountryId, label: 'Host Country Refugee ID or Residence Permit' },
    { value: RsdEvidenceDocumentType.OfficialCampRegistration, label: 'Registration Document from Official Refugee Camp' }
  ];


  constructor(
    private candidateFormService: CandidateFormService,
    private fb: FormBuilder
  ) {
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      refugeeStatus: [null as RsdRefugeeStatus | null, [Validators.required]],
      documentType: [null as RsdEvidenceDocumentType | null, [Validators.required]],
      documentNumber: ['', [Validators.required, Validators.maxLength(30)]]
    });

    if (this.readOnly) {
      this.form.disable({emitEvent: false});
    }

    this.error = null;
    this.candidateFormService.getRsdEvidenceForm().subscribe({
      next: formData => this.form.reset(formData),
      error: () => this.form.reset()
    });
  }

  canSubmit(): boolean {
    return this.form.valid && !this.form.pending && !this.submitting && !this.readOnly;
  }

  hasError(ctrlName: keyof RsdEvidenceFormData, validationName: string): boolean {
    const control = this.form.get(ctrlName);
    return !this.readOnly && !!control && control.touched && control.hasError(validationName);
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.submitting = true;
    this.error = null;

    const formData: RsdEvidenceFormData = this.form.getRawValue();

    this.candidateFormService.createOrUpdateRsdEvidenceForm(formData).subscribe({
      next: value => {
        this.submitted.emit(value);
        this.form.markAsPristine();
        this.form.markAsUntouched();
        this.submitting = false;
      },
      error: err => {
        this.error = err;
        this.submitting = false;
      }
    });
  }
}
