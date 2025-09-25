import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {
  AbstractControl,
  FormArray,
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  ValidationErrors,
  Validators
} from '@angular/forms';
import {NgForOf, NgIf} from "@angular/common";
import {NgSelectModule} from '@ng-select/ng-select';

import {
  FamilyDocFormData,
  FamilyMemberDoc,
  ItalyCandidateTravelDocType,
  RelocatingFamilyMember
} from '../../../model/form';
import {Candidate, DependantRelations} from "../../../model/candidate";
import {CandidateFormService} from '../../../services/candidate-form.service';
import {ICandidateFormComponent} from '../../../model/candidate-form';
import {EnumOption, enumOptions} from "../../util/enum";
import {TranslateModule} from "@ngx-translate/core";

@Component({
  selector: 'app-family-doc-form',
  templateUrl: './family-doc-form.component.html',
  standalone: true,
  imports: [ReactiveFormsModule, NgIf, NgForOf, NgSelectModule, TranslateModule],
  styleUrls: ['./family-doc-form.component.scss']
})
export class FamilyDocFormComponent implements OnInit, ICandidateFormComponent<FamilyDocFormData> {
  @Input() readOnly = false;
  @Input() candidate: Candidate;
  @Output() submitted = new EventEmitter<FamilyDocFormData>();

  form: FormGroup;
  error: any = null;
  submitting = false;

  // existing
  relationships: EnumOption[] = enumOptions(DependantRelations);

  constructor(
    private fb: FormBuilder,
    private candidateFormService: CandidateFormService
  ) {
  }

  ngOnInit(): void {
    this.form = this.fb.nonNullable.group({
      noEligibleFamilyMembers: [false, []],
      noEligibleNotes: [{value: '', disabled: false}],
      members: this.fb.nonNullable.array([] as FormGroup[])
    });

    // "no eligible" toggle
    this.form.get('noEligibleFamilyMembers')!.valueChanges.subscribe(checked => {
      const members = this.members;
      const notesCtrl = this.form.get('noEligibleNotes')!;
      if (checked) {
        members.disable({emitEvent: false});
      } else {
        notesCtrl.clearValidators();
        members.enable({emitEvent: false});
      }
      notesCtrl.updateValueAndValidity({emitEvent: false});
    });

    // Load existing
    this.error = null;
    this.candidateFormService.getFamilyDocsForm().subscribe({
      next: data => this.hydrateForm(data),
      error: () => this.form.reset({noEligibleFamilyMembers: false, noEligibleNotes: ''})
    });
  }

  get members(): FormArray<FormGroup> {
    return this.form.get('members') as FormArray<FormGroup>;
  }

  private newDocGroup(value?: Partial<FamilyMemberDoc>) {
    return this.fb.nonNullable.group({
      docType: [value?.docType ?? ItalyCandidateTravelDocType.Passport, [Validators.required]],
      docNumber: [value?.docNumber ?? '', [Validators.required, Validators.maxLength(64)]],
      issuer: [value?.issuer ?? '', [Validators.required, Validators.maxLength(128)]],
      issuedOn: [value?.issuedOn ?? '', [Validators.required]],
      expiresOn: [value?.expiresOn ?? '', [Validators.required]]
    }, {validators: [this.expiryMinMonthsValidator(9)]});
  }

  // ✅ UPDATED: Added the new dependant-* fields
  private newMemberGroup(value?: Partial<RelocatingFamilyMember>) {
    const group = this.fb.nonNullable.group({
      relationship: [value?.relationship ?? DependantRelations.Partner, [Validators.required]],
      dependantRelationOther: [(value as any)?.dependantRelationOther ?? ''],

      firstName: [value?.firstName ?? '', [Validators.required, Validators.maxLength(100)]],
      lastName: [value?.lastName ?? '', [Validators.required, Validators.maxLength(100)]],
      dateOfBirth: [value?.dateOfBirth ?? '', [Validators.required]],
      gender: [value?.gender ?? 'other', [Validators.required]],
      countryOfBirth: [value?.countryOfBirth ?? '', [Validators.required]],
      placeOfBirth: [value?.placeOfBirth ?? ''],

      // NEW fields
      dependantHealthConcerns: [(value as any)?.dependantHealthConcerns ?? null],
      dependantHealthNotes: [(value as any)?.dependantHealthNotes ?? ''],
      dependantRegistered: [(value as any)?.dependantRegistered ?? null],
      dependantRegisteredNumber: [(value as any)?.dependantRegisteredNumber ?? ''],
      dependantRegisteredNotes: [(value as any)?.dependantRegisteredNotes ?? ''],

      travelDoc: this.newDocGroup(value?.travelDoc)
    }, {
      validators: [
        this.childAgeIfChildValidator(),
        this.otherRelationshipNotesIfOtherValidator(),
        this.requireNotesIfHealthConcernYesValidator(),
        this.requireRegNumberIfRegisteredYesValidator()
      ]
    });

    return group;
  }

  addMember(prefill?: Partial<RelocatingFamilyMember>) {
    this.members.push(this.newMemberGroup(prefill));
  }

  removeMember(ix: number) {
    this.members.removeAt(ix);
  }

  // --- validators ---
  private expiryMinMonthsValidator(months: number) {
    return (group: FormGroup) => {
      const expiresOn = group.get('expiresOn')?.value as string;
      if (!expiresOn) return null;
      const now = new Date();
      const min = new Date(now);
      min.setMonth(min.getMonth() + months);
      const exp = new Date(expiresOn);
      return exp >= min ? null : {expiryTooSoon: true};
    };
  }

  private childAgeIfChildValidator() {
    return (group: AbstractControl): ValidationErrors | null => {
      const rel = group.get('relationship')?.value as DependantRelations;
      const dob = group.get('dateOfBirth')?.value as string;
      if (rel !== DependantRelations.Child || !dob) return null;
      const birth = new Date(dob);
      const today = new Date();
      const age =
        today.getFullYear() - birth.getFullYear() -
        (today < new Date(today.getFullYear(), birth.getMonth(), birth.getDate()) ? 1 : 0);
      return age < 18 ? null : {childAgeInvalid: true};
    };
  }

  private otherRelationshipNotesIfOtherValidator() {
    return (group: AbstractControl): ValidationErrors | null => {
      const rel = group.get('relationship')?.value as DependantRelations;
      const other = group.get('dependantRelationOther')?.value as string;
      if (rel !== DependantRelations.Other) return null;
      return other && other.trim().length >= 2 ? null : {otherRelationshipRequired: true};
    };
  }

  private requireNotesIfHealthConcernYesValidator() {
    return (group: AbstractControl): ValidationErrors | null => {
      const hc = group.get('dependantHealthConcerns')?.value;
      const notes = group.get('dependantHealthNotes')?.value as string;
      if (hc !== 'yes') return null;
      return notes && notes.trim().length > 0 ? null : {healthNotesRequired: true};
    };
  }

  private requireRegNumberIfRegisteredYesValidator() {
    return (group: AbstractControl): ValidationErrors | null => {
      const reg = group.get('dependantRegistered')?.value;
      const num = group.get('dependantRegisteredNumber')?.value as string;
      if (reg !== 'yes') return null;
      return num && num.trim().length > 0 ? null : {registeredNumberRequired: true};
    };
  }

  // --- load/save mapping ---
  private hydrateForm(data: FamilyDocFormData) {
    this.form.reset({
      noEligibleFamilyMembers: data.noEligibleFamilyMembers ?? false,
      noEligibleNotes: data.noEligibleNotes ?? ''
    });

    this.members.clear();

    let parsed: RelocatingFamilyMember[] = [];
    if (data.familyMembersJson) {
      try {
        parsed = JSON.parse(data.familyMembersJson) as RelocatingFamilyMember[];
      } catch { /* ignore bad payload */
      }
    }
    parsed.forEach(m => this.addMember(m));

    if (!this.form.get('noEligibleFamilyMembers')!.value && this.members.length === 0) {
      this.addMember();
    }
  }

  canSubmit(): boolean {
    return this.form.valid && !this.form.pending && !this.submitting && !this.readOnly;
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.submitting = true;
    this.error = null;

    const value = this.form.getRawValue();
    const members = (value.noEligibleFamilyMembers ? [] :
      this.members.controls.map(c => c.getRawValue() as RelocatingFamilyMember));

    const payload: FamilyDocFormData = {
      noEligibleFamilyMembers: value.noEligibleFamilyMembers,
      noEligibleNotes: value.noEligibleFamilyMembers ? value.noEligibleNotes ?? '' : '',
      familyMembersJson: JSON.stringify(members)
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
      }
    });
  }

  protected readonly ItalyCandidateTravelDocType = ItalyCandidateTravelDocType;
}
