import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {FormArray, FormBuilder, FormGroup, Validators} from '@angular/forms';
import {CandidateFormService} from '../../../services/candidate-form.service';
import {ICandidateFormComponent} from '../../../model/candidate-form';
import {
  DependantsInfoFormData,
  RefugeeStatusEvidenceDocumentType,
  RelocatingDependant,
  RsdRefugeeStatus,
} from '../../../model/form';

@Component({
  selector: 'app-dependants-refugee-status-info-form',
  templateUrl: './dependants-refugee-status-info-form.component.html',
  styleUrls: ['./dependants-refugee-status-info-form.component.scss'],
})
export class DependantsRefugeeStatusInfoFormComponent
  implements OnInit, ICandidateFormComponent<DependantsInfoFormData> {

  @Input() readOnly = false;
  @Input() candidate: any | null = null;
  @Output() submitted = new EventEmitter<DependantsInfoFormData>();

  form: FormGroup;
  error: any = null;
  submitting = false;
  loadingMembers = true;

  private dependantsInfoFormData: DependantsInfoFormData;
  relocatingDependants: RelocatingDependant[] = [];

  RsdRefugeeStatus = RsdRefugeeStatus;
  RefugeeStatusEvidenceDocumentType = RefugeeStatusEvidenceDocumentType;

  constructor(
    private fb: FormBuilder,
    private candidateFormService: CandidateFormService
  ) {
  }

  ngOnInit(): void {
    this.form = this.fb.nonNullable.group({
      members: this.fb.nonNullable.array([] as FormGroup[]),
    });
    this.loadData();
  }

  get members(): FormArray<FormGroup> {
    return this.form.get('members') as FormArray<FormGroup>;
  }

  get noEligibleDependants(): boolean {
    return this.dependantsInfoFormData?.noEligibleDependants ?? true;
  }

  canSubmit(): boolean {
    return this.form.valid && !this.form.pending && !this.submitting && !this.readOnly;
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.members.controls.forEach(ctrl => ctrl.markAllAsTouched());
      return;
    }

    this.submitting = true;
    this.error = null;

    //Get data from form
    const entries: RelocatingDependant[] = this.members.controls.map(ctrl => ctrl.getRawValue());
    //Update the existing dependants with the new data
    this.relocatingDependants.forEach(
      (dep, index)=> Object.assign(dep, entries[index])
    );
    const payload: DependantsInfoFormData = {
      noEligibleDependants: this.dependantsInfoFormData?.noEligibleDependants ?? true,
      noEligibleNotes: this.dependantsInfoFormData?.noEligibleNotes ?? '',
      dependantsInfoJson: JSON.stringify(this.relocatingDependants),
    };

    this.candidateFormService.createOrUpdateDependantsInfoForm(payload).subscribe({
      next: saved => {
        this.submitted.emit(saved);
        this.form.markAsPristine();
        this.form.markAsUntouched();
        this.submitting = false;
      },
      error: err => {
        this.error = err;
        this.submitting = false;
      },
    });
  }

  private nothingToDoAutoSubmit(): void {
    this.submitting = true;
    this.error = null;
    this.candidateFormService.createOrUpdateDependantsInfoForm(this.dependantsInfoFormData).subscribe({
      next: saved => {
        this.submitted.emit(saved);
        this.form.markAsPristine();
        this.form.markAsUntouched();
        this.submitting = false;
      },
      error: err => {
        this.error = err;
        this.submitting = false;
      },
    });
  }

  private loadData(): void {
    this.loadingMembers = true;

    this.candidateFormService.getDependantsInfoForm().subscribe({
      next: dependantsInfoFormData => {
        this.dependantsInfoFormData = dependantsInfoFormData;
        if (dependantsInfoFormData.noEligibleDependants) {
          //No dependants, so we can skip the form.
          this.nothingToDoAutoSubmit();
        } else {
          this.createDependantForms(dependantsInfoFormData);
        }
        this.loadingMembers = false;
      },
      error: err => {
        this.error = err;
        this.loadingMembers = false;
      }
    })
  }


  private createDependantForms(dependantsInfoFormData: DependantsInfoFormData) {
    const dependantsInfoJson = dependantsInfoFormData?.dependantsInfoJson;
    if (!dependantsInfoJson) return [];
    try {
      this.relocatingDependants = JSON.parse(dependantsInfoJson) as RelocatingDependant[];
      this.members.clear();

      this.relocatingDependants.forEach(member => {
        this.members.push(this.buildMemberGroup(member));
      });

    } catch {
      console.error('Failed to parse relocating family members JSON');
    }
  }

  private buildMemberGroup(member: RelocatingDependant): FormGroup {
    return this.fb.nonNullable.group({
      'user.firstName': [member['user.firstName'] ?? ''],
      'user.lastName': [member['user.lastName'] ?? ''],
      dob: [member.dob ?? ''],
      TTH_IT$REFUGEE_STATUS: [member?.TTH_IT$REFUGEE_STATUS ?? '', Validators.required],
      TTH_IT$REFUGEE_STATUS_EVIDENCE_DOCUMENT_TYPE: [member?.TTH_IT$REFUGEE_STATUS_EVIDENCE_DOCUMENT_TYPE ?? '',
        Validators.required],
      TTH_IT$REFUGEE_STATUS_EVIDENCE_DOCUMENT_NUMBER: [member?.TTH_IT$REFUGEE_STATUS_EVIDENCE_DOCUMENT_NUMBER ?? '',
        Validators.maxLength(30)],
      TTH_IT$REFUGEE_STATUS_COMMENT: [member?.TTH_IT$REFUGEE_STATUS_COMMENT ?? ''],
    });
  }

  public composeDisplayName(member: RelocatingDependant): string {
    const first = member['user.firstName']?.trim() ?? '';
    const last = member['user.lastName']?.trim() ?? '';
    const fullName = `${first} ${last}`.trim();
    return fullName || member.relationship || 'Unnamed Member';
  }
}
