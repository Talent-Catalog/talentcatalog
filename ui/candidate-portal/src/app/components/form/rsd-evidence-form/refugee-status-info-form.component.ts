import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {CandidateFormService} from "../../../services/candidate-form.service";
import {
  RefugeeStatusEvidenceDocumentType,
  RefugeeStatusInfoFormData,
  RsdRefugeeStatus
} from "../../../model/form";
import {ICandidateFormComponent} from "../../../model/candidate-form";
import {Candidate} from "../../../model/candidate";

@Component({
  selector: 'app-refugee-status-info-form',
  templateUrl: './refugee-status-info-form.component.html'
})
export class RefugeeStatusInfoFormComponent implements OnInit, ICandidateFormComponent<RefugeeStatusInfoFormData> {
  @Input() readOnly = false;
  @Input() candidate :Candidate;
  @Output() submitted = new EventEmitter<RefugeeStatusInfoFormData>();

  form: FormGroup;
  error: any = null;
  submitting = false;

  readonly refugeeStatusOptions = [
    { value: RsdRefugeeStatus.RecognizedByUnhcr, label: 'Recognized by UNHCR' },
    { value: RsdRefugeeStatus.RecognizedByHostCountry, label: 'Recognized by host country' },
    { value: RsdRefugeeStatus.Pending, label: 'Pending' }
  ];

  readonly documentTypeOptions = [
    { value: RefugeeStatusEvidenceDocumentType.UnhcrCertificate, label: 'UNHCR Certificate of Recognition' },
    { value: RefugeeStatusEvidenceDocumentType.HostCountryId, label: 'Host Country Refugee ID or Residence Permit' },
    { value: RefugeeStatusEvidenceDocumentType.OfficialCampRegistration, label: 'Registration Document from Official Refugee Camp' }
  ];


  constructor(
    private candidateFormService: CandidateFormService,
    private fb: FormBuilder
  ) {
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      refugeeStatus: [null as RsdRefugeeStatus | null, Validators.required],
      documentType: [null as RefugeeStatusEvidenceDocumentType | null, Validators.required],
      documentNumber: ['', Validators.maxLength(30)],
      refugeeStatusComment: ['']
    });

    if (this.readOnly) {
      this.form.disable({emitEvent: false});
    }

    this.error = null;
    this.candidateFormService.getRefugeeStatusInfoForm().subscribe({
      next: formData => this.form.reset(formData),
      error: () => this.form.reset()
    });
  }

  canSubmit(): boolean {
    return this.form.valid && !this.form.pending && !this.submitting && !this.readOnly;
  }

  hasError(ctrlName: keyof RefugeeStatusInfoFormData, validationName: string): boolean {
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

    const formData: RefugeeStatusInfoFormData = this.form.getRawValue();

    this.candidateFormService.createOrUpdateRefugeeStatusInfoForm(formData).subscribe({
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
