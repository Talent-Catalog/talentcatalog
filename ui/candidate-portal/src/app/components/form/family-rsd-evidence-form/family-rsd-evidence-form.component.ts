import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {FormArray, FormBuilder, FormGroup, Validators} from '@angular/forms';
import {forkJoin, of} from 'rxjs';
import {catchError} from 'rxjs/operators';
import {CandidateFormService} from '../../../services/candidate-form.service';
import {ICandidateFormComponent} from '../../../model/candidate-form';
import {
  FamilyDocFormData,
  FamilyRsdEvidenceEntry,
  FamilyRsdEvidenceFormData,
  RelocatingFamilyMember,
  RsdEvidenceDocumentType,
  RsdRefugeeStatus,
} from '../../../model/form';

@Component({
  selector: 'app-family-rsd-evidence-form',
  templateUrl: './family-rsd-evidence-form.component.html',
  styleUrls: ['./family-rsd-evidence-form.component.scss'],
})
export class FamilyRsdEvidenceFormComponent
  implements OnInit, ICandidateFormComponent<FamilyRsdEvidenceFormData> {

  @Input() readOnly = false;
  @Input() candidate: any | null = null;
  @Output() submitted = new EventEmitter<FamilyRsdEvidenceFormData>();

  form: FormGroup;
  error: any = null;
  submitting = false;
  loadingMembers = true;

  RsdRefugeeStatus = RsdRefugeeStatus;
  RsdEvidenceDocumentType = RsdEvidenceDocumentType;

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

    const entries: FamilyRsdEvidenceEntry[] = this.members.controls.map(ctrl => ctrl.getRawValue());
    const payload: FamilyRsdEvidenceFormData = {
      familyRsdEvidenceJson: JSON.stringify(entries),
    };

    this.candidateFormService.createOrUpdateFamilyRsdEvidenceForm(payload).subscribe({
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

    const familyDocs$ = this.candidateFormService.getFamilyDocsForm()
    .pipe(catchError(() => of<FamilyDocFormData | null>(null)));
    const rsd$ = this.candidateFormService.getFamilyRsdEvidenceForm()
    .pipe(catchError(() => of<FamilyRsdEvidenceFormData>({familyRsdEvidenceJson: undefined})));

    forkJoin({familyDocs: familyDocs$, rsd: rsd$}).subscribe({
      next: ({familyDocs, rsd}) => {
        const members = this.extractMembers(familyDocs);
        const evidenceMap = this.parseEvidence(rsd?.familyRsdEvidenceJson);
        this.members.clear();

        members.forEach(member => {
          const key = this.computeMemberKey(member);
          const existing = evidenceMap.get(key);
          this.members.push(this.buildMemberGroup(member, key, existing));
        });

        this.loadingMembers = false;
      },
      error: err => {
        this.error = err;
        this.loadingMembers = false;
      },
    });
  }

  private extractMembers(data: FamilyDocFormData | null | undefined): RelocatingFamilyMember[] {
    if (!data?.familyMembersJson) return [];
    try {
      return JSON.parse(data.familyMembersJson) as RelocatingFamilyMember[];
    } catch {
      console.error('Failed to parse relocating family members JSON');
      return [];
    }
  }

  private parseEvidence(json?: string): Map<string, FamilyRsdEvidenceEntry> {
    const map = new Map<string, FamilyRsdEvidenceEntry>();
    if (!json) return map;

    try {
      const parsed = JSON.parse(json) as FamilyRsdEvidenceEntry[];
      parsed.forEach(entry => {
        if (entry?.memberKey) map.set(entry.memberKey, entry);
      });
    } catch {
      console.error('Failed to parse family RSD evidence JSON');
    }
    return map;
  }

  private buildMemberGroup(
    member: RelocatingFamilyMember,
    key: string,
    existing?: FamilyRsdEvidenceEntry
  ): FormGroup {
    const displayName = this.composeDisplayName(member);
    return this.fb.nonNullable.group({
      memberKey: [key],
      firstName: [member.firstName ?? ''],
      lastName: [member.lastName ?? ''],
      dateOfBirth: [member.dateOfBirth ?? ''],
      displayName: [existing?.displayName ?? displayName],
      refugeeStatus: [existing?.refugeeStatus ?? '', [Validators.required]],
      documentType: [existing?.documentType ?? '', [Validators.required]],
      documentNumber: [existing?.documentNumber ?? '', [Validators.required, Validators.maxLength(30)]],
    });
  }

  private composeDisplayName(member: RelocatingFamilyMember): string {
    const first = member.firstName?.trim() ?? '';
    const last = member.lastName?.trim() ?? '';
    const fullName = `${first} ${last}`.trim();
    return fullName || member.relationship || 'Unnamed Member';
  }

  private computeMemberKey(member: RelocatingFamilyMember): string {
    const first = (member.firstName ?? '').trim().toLowerCase();
    const last = (member.lastName ?? '').trim().toLowerCase();
    const dob = (member.dateOfBirth ?? '').trim();
    return `${first}|${last}|${dob}`;
  }
}
