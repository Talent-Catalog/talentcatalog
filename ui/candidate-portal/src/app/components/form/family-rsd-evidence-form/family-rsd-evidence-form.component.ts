import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {FormArray, FormBuilder, FormGroup, Validators} from '@angular/forms';
import {CandidateFormService} from '../../../services/candidate-form.service';
import {ICandidateFormComponent} from '../../../model/candidate-form';
import {
  DependantsInfoFormData,
  RefugeeStatusEvidenceDocumentType,
  RelocatingFamilyMember,
  RsdRefugeeStatus,
} from '../../../model/form';

@Component({
  selector: 'app-family-rsd-evidence-form',
  templateUrl: './family-rsd-evidence-form.component.html',
  styleUrls: ['./family-rsd-evidence-form.component.scss'],
})
export class FamilyRsdEvidenceFormComponent
  implements OnInit, ICandidateFormComponent<DependantsInfoFormData> {

  @Input() readOnly = false;
  @Input() candidate: any | null = null;
  @Output() submitted = new EventEmitter<DependantsInfoFormData>();

  form: FormGroup;
  error: any = null;
  submitting = false;
  loadingMembers = true;

  private dependantsInfoFormData: DependantsInfoFormData;
  familyMembers: RelocatingFamilyMember[] = [];

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

    const entries: RelocatingFamilyMember[] = this.members.controls.map(ctrl => ctrl.getRawValue());
    const payload: DependantsInfoFormData = {
      noEligibleFamilyMembers: this.dependantsInfoFormData?.noEligibleFamilyMembers ?? true,
      noEligibleNotes: this.dependantsInfoFormData?.noEligibleNotes ?? '',
      familyMembersJson: JSON.stringify(entries),
    };

    this.candidateFormService.createOrUpdateFamilyDocsForm(payload).subscribe({
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
      next: familyDocFormData => {
        this.dependantsInfoFormData = familyDocFormData;
        this.createFormsFromFamilyMembers(familyDocFormData);
        console.log('Family Docs Form Data:', familyDocFormData);
        this.loadingMembers = false;
      },
      error: err => {
        console.error('Error fetching Family Docs Form Data:', err);
        this.error = err;
        this.loadingMembers = false;
      }
    })
  }


  private createFormsFromFamilyMembers(familyDocFormData: DependantsInfoFormData) {
    const familyMembersJson = familyDocFormData?.familyMembersJson;
    if (!familyMembersJson) return [];
    try {
      this.familyMembers = JSON.parse(familyMembersJson) as RelocatingFamilyMember[];
      this.members.clear();

      this.familyMembers.forEach(member => {
        this.members.push(this.buildMemberGroup(member));
      });

    } catch {
      console.error('Failed to parse relocating family members JSON');
    }
  }

  private buildMemberGroup(member: RelocatingFamilyMember): FormGroup {
    return this.fb.nonNullable.group({
      'user.firstName': [member['user.firstName'] ?? ''],
      'user.lastName': [member['user.lastName'] ?? ''],
      dob: [member.dob ?? ''],
      REFUGEE_STATUS: [member?.REFUGEE_STATUS ?? '', [Validators.required]],
      EVIDENCE_DOCUMENT_TYPE: [member?.EVIDENCE_DOCUMENT_TYPE ?? '', [Validators.required]],
      EVIDENCE_DOCUMENT_NUMBER: [member?.EVIDENCE_DOCUMENT_NUMBER ?? '', [Validators.required, Validators.maxLength(30)]],
    });
  }

  public composeDisplayName(member: RelocatingFamilyMember): string {
    const first = member['user.firstName']?.trim() ?? '';
    const last = member['user.lastName']?.trim() ?? '';
    const fullName = `${first} ${last}`.trim();
    return fullName || member.relationship || 'Unnamed Member';
  }
}
