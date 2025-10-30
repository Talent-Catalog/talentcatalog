import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {AbstractControl, FormArray, FormBuilder, FormGroup, Validators} from '@angular/forms';
import {forkJoin, of} from 'rxjs';
import {catchError} from 'rxjs/operators';
import {TranslateService} from '@ngx-translate/core';

import {CandidateFormService} from '../../../services/candidate-form.service';
import {ICandidateFormComponent} from '../../../model/candidate-form';
import {
  FamilyDocFormData,
  FamilyRsdEvidenceEntry,
  FamilyRsdEvidenceFormData,
  RelocatingFamilyMember
} from '../../../model/form';

interface Option {
  value: string;
  label: string;
}

@Component({
  selector: 'app-family-rsd-evidence-form',
  templateUrl: './family-rsd-evidence-form.component.html',
  styleUrls: ['./family-rsd-evidence-form.component.scss'],
})
export class FamilyRsdEvidenceFormComponent implements
  OnInit, ICandidateFormComponent<FamilyRsdEvidenceFormData> {

  @Input() readOnly = false;
  @Input() candidate: any | null = null;
  @Output() submitted = new EventEmitter<FamilyRsdEvidenceFormData>();

  form: FormGroup;
  error: any = null;
  submitting = false;
  loadingMembers = true;

  readonly refugeeStatusOptions: Option[] = [
    {value: 'Recognized by UNHCR', label: 'FAMILY_RSD.STATUS.UNHCR'},
    {value: 'Recognized by host country', label: 'FAMILY_RSD.STATUS.HOST'},
    {value: 'Pending', label: 'FAMILY_RSD.STATUS.PENDING'}
  ];

  readonly documentTypeOptions: Option[] = [
    {value: 'UNHCR Certificate of Recognition', label: 'FAMILY_RSD.DOC_TYPE.UNHCR'},
    {value: 'Host Country Refugee ID or Residence Permit', label: 'FAMILY_RSD.DOC_TYPE.HOST'},
    {value: 'Registration Document from Official Refugee Camp',
      label: 'FAMILY_RSD.DOC_TYPE.CAMP'}
  ];

  constructor(private fb: FormBuilder,
              private candidateFormService: CandidateFormService,
              private translate: TranslateService) {
    this.registerLocalTranslations();
  }

  ngOnInit(): void {
    this.form = this.fb.nonNullable.group({
      members: this.fb.nonNullable.array([] as FormGroup[])
    });

    this.loadData();
  }

  get members(): FormArray<FormGroup> {
    return this.form.get('members') as FormArray<FormGroup>;
  }

  canSubmit(): boolean {
    return this.form.valid && !this.form.pending && !this.submitting && !this.readOnly;
  }

  hasError(ctrl: AbstractControl | null, errorName: string): boolean {
    return !this.readOnly && !!ctrl && ctrl.touched && ctrl.hasError(errorName);
  }

  removeDocument(ix: number): void {
    const group = this.members.at(ix);
    group.patchValue({
      attachmentId: null,
      attachmentName: '',
      attachmentLocation: ''
    });
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.members.controls.forEach(ctrl => ctrl.markAllAsTouched());
      return;
    }

    this.submitting = true;
    this.error = null;

    const entries: FamilyRsdEvidenceEntry[] = this.members.controls.map(ctrl => {
      const value = ctrl.getRawValue();
      return {
        memberKey: value.memberKey,
        firstName: value.firstName,
        lastName: value.lastName,
        dateOfBirth: value.dateOfBirth,
        displayName: value.displayName,
        refugeeStatus: value.refugeeStatus,
        documentType: value.documentType,
        documentNumber: value.documentNumber,
        attachmentId: value.attachmentId,
        attachmentName: value.attachmentName,
        attachmentLocation: value.attachmentLocation
      } as FamilyRsdEvidenceEntry;
    });

    const payload: FamilyRsdEvidenceFormData = {
      familyRsdEvidenceJson: JSON.stringify(entries)
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
      }
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
      }
    });
  }

  private extractMembers(data: FamilyDocFormData | null | undefined): RelocatingFamilyMember[] {
    if (!data?.familyMembersJson) {
      return [];
    }
    try {
      return JSON.parse(data.familyMembersJson) as RelocatingFamilyMember[];
    } catch (e) {
      console.error('Failed to parse relocating family members JSON', e);
      return [];
    }
  }

  private parseEvidence(json?: string): Map<string, FamilyRsdEvidenceEntry> {
    const map = new Map<string, FamilyRsdEvidenceEntry>();
    if (!json) {
      return map;
    }
    try {
      const parsed = JSON.parse(json) as FamilyRsdEvidenceEntry[];
      parsed.forEach(entry => {
        if (entry?.memberKey) {
          map.set(entry.memberKey, entry);
        }
      });
    } catch (e) {
      console.error('Failed to parse family RSD evidence JSON', e);
    }
    return map;
  }

  private buildMemberGroup(member: RelocatingFamilyMember, key: string,
                           existing?: FamilyRsdEvidenceEntry): FormGroup {
    const displayName = this.composeDisplayName(member);
    return this.fb.nonNullable.group({
      memberKey: [key],
      firstName: [member.firstName ?? ''],
      lastName: [member.lastName ?? ''],
      dateOfBirth: [member.dateOfBirth ?? ''],
      displayName: [existing?.displayName ?? displayName],
      refugeeStatus: [existing?.refugeeStatus ?? '', [Validators.required]],
      documentType: [existing?.documentType ?? '', [Validators.required]],
      documentNumber: [existing?.documentNumber ?? '', [Validators.required, Validators.maxLength(30)]]
    });
  }

  private composeDisplayName(member: RelocatingFamilyMember): string {
    const first = member.firstName?.trim() ?? '';
    const last = member.lastName?.trim() ?? '';
    const fullName = `${first} ${last}`.trim();
    if (fullName) {
      return fullName;
    }
    const relationship = member.relationship ?? '';
    return relationship ? this.translate.instant('FAMILY_RSD.LABEL.MEMBER_GENERIC', {relationship})
      : this.translate.instant('FAMILY_RSD.LABEL.MEMBER_FALLBACK');
  }

  private computeMemberKey(member: RelocatingFamilyMember): string {
    const first = (member.firstName ?? '').trim().toLowerCase();
    const last = (member.lastName ?? '').trim().toLowerCase();
    const dob = (member.dateOfBirth ?? '').trim();
    return `${first}|${last}|${dob}`;
  }


  private registerLocalTranslations(): void {
    const payload = {
      FAMILY_RSD: {
        TITLE: 'Relocating family refugee ID evidence',
        DESCRIPTION: 'Upload ID evidence and record refugee status details for each relocating family member.',
        NO_MEMBERS: 'You have not listed any relocating family members yet. Please complete the family members form first.',
        LABEL: {
          MEMBER: 'Family member',
          MEMBER_GENERIC: '{{relationship}} family member',
          MEMBER_FALLBACK: 'Relocating family member',
          REFUGEE_STATUS: 'Refugee status',
          DOCUMENT_TYPE: 'Type of document',
          DOCUMENT_NUMBER: 'Document number',
          UPLOAD: 'Refugee ID upload',
          UPLOADED_FILE: 'Uploaded file'
        },
        STATUS: {
          UNHCR: 'Recognized by UNHCR',
          HOST: 'Recognized by host country',
          PENDING: 'Pending'
        },
        DOC_TYPE: {
          UNHCR: 'UNHCR Certificate of Recognition',
          HOST: 'Host Country Refugee ID or Residence Permit',
          CAMP: 'Registration Document from Official Refugee Camp'
        },
        HELP: {
          UPLOAD_HINT: 'Accepted formats: PDF, JPG, PNG. Maximum one file per family member.'
        }
      }
    };

    this.translate.setTranslation('en', payload, true);
    const current = this.translate.currentLang;
    const defaultLang = this.translate.getDefaultLang();
    if (current && current !== 'en') {
      this.translate.setTranslation(current, payload, true);
    } else if (defaultLang && defaultLang !== 'en') {
      this.translate.setTranslation(defaultLang, payload, true);
    }
  }
}
