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

import {DependantsInfoFormData, RelocatingDependant, TravelDocType} from '../../../model/form';
import {Candidate, DependantRelations} from "../../../model/candidate";
import {CandidateFormService} from '../../../services/candidate-form.service';
import {ICandidateFormComponent} from '../../../model/candidate-form';
import {EnumOption, enumOptions} from "../../util/enum";
import {TranslateModule} from "@ngx-translate/core";

@Component({
  selector: 'app-dependants-travel-info-form',
  templateUrl: './dependants-travel-info-form.component.html',
  standalone: true,
  imports: [ReactiveFormsModule, NgIf, NgForOf, NgSelectModule, TranslateModule],
  styleUrls: ['./dependants-travel-info-form.component.scss']
})
export class DependantsTravelInfoFormComponent implements OnInit, ICandidateFormComponent<DependantsInfoFormData> {
  @Input() readOnly = false;
  @Input() candidate: Candidate;
  @Output() submitted = new EventEmitter<DependantsInfoFormData>();

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
    this.candidateFormService.getDependantsInfoForm().subscribe({
      next: data => this.hydrateForm(data),
      error: () => this.form.reset({noEligibleFamilyMembers: false, noEligibleNotes: ''})
    });
  }

  get members(): FormArray<FormGroup> {
    return this.form.get('members') as FormArray<FormGroup>;
  }

  private newMemberGroup(value?: Partial<RelocatingDependant>) {
    const group = this.fb.nonNullable.group({
      relationship: [value?.relationship ?? DependantRelations.Partner, [Validators.required]],
      relationOther: [(value as any)?.relationOther ?? ''],

      //Names must match the TC published column field keys/property names for a normal (non dependant) candidate.
      //they already match the published column fields.
      'user.firstName': [value?.['user.firstName'] ?? '', [Validators.required, Validators.maxLength(100)]],
      'user.lastName': [value?.['user.lastName'] ?? '', [Validators.required, Validators.maxLength(100)]],
      dob: [value?.dob ?? '', [Validators.required]],
      gender: [value?.gender ?? 'other', [Validators.required]],
      'birthCountry.name': [value?.['birthCountry.name'] ?? '', [Validators.required]],
      placeOfBirth: [value?.placeOfBirth ?? ''],

      // NEW fields
      healthConcerns: [value?.healthConcerns ?? null],
      healthNotes: [value?.healthNotes ?? ''],
      registered: [value?.registered ?? null],
      registeredNumber: [value?.registeredNumber ?? ''],
      registeredNotes: [value?.registeredNotes ?? ''],

      TRAVEL_DOC_TYPE: [value?.TRAVEL_DOC_TYPE ?? '', [Validators.required]],
      TRAVEL_DOC_NUMBER: [value?.TRAVEL_DOC_NUMBER ?? '', [Validators.required, Validators.maxLength(64)]],
      TRAVEL_DOC_ISSUED_BY: [value?.TRAVEL_DOC_ISSUED_BY ?? '', [Validators.required, Validators.maxLength(128)]],
      TRAVEL_DOC_ISSUE_DATE: [value?.TRAVEL_DOC_ISSUE_DATE ?? '', [Validators.required]],
      TRAVEL_DOC_EXPIRY_DATE: [value?.TRAVEL_DOC_EXPIRY_DATE ?? '', [Validators.required]]
    }, {
      validators: [
        this.childAgeIfChildValidator(),
        this.otherRelationshipNotesIfOtherValidator(),
        this.requireNotesIfHealthConcernYesValidator(),
        this.requireRegNumberIfRegisteredYesValidator(),
        this.expiryMinMonthsValidator(9)
      ]
    });

    return group;
  }

  addMember(prefill?: Partial<RelocatingDependant>) {
    this.members.push(this.newMemberGroup(prefill));
  }

  removeMember(ix: number) {
    this.members.removeAt(ix);
  }

  // --- validators ---
  private expiryMinMonthsValidator(months: number) {
    return (group: FormGroup) => {
      const expiresOn = group.get('TRAVEL_DOC_EXPIRY_DATE')?.value as string;
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
      const dob = group.get('dob')?.value as string;
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
      const other = group.get('relationOther')?.value as string;
      if (rel !== DependantRelations.Other) return null;
      return other && other.trim().length >= 2 ? null : {otherRelationshipRequired: true};
    };
  }

  private requireNotesIfHealthConcernYesValidator() {
    return (group: AbstractControl): ValidationErrors | null => {
      const hc = group.get('healthConcerns')?.value;
      const notes = group.get('healthNotes')?.value as string;
      if (hc !== 'yes') return null;
      return notes && notes.trim().length > 0 ? null : {healthNotesRequired: true};
    };
  }

  private requireRegNumberIfRegisteredYesValidator() {
    return (group: AbstractControl): ValidationErrors | null => {
      const reg = group.get('registered')?.value;
      const num = group.get('registeredNumber')?.value as string;
      if (reg !== 'yes') return null;
      return num && num.trim().length > 0 ? null : {registeredNumberRequired: true};
    };
  }

  // --- load/save mapping ---
  private hydrateForm(data: DependantsInfoFormData) {
    this.form.reset({
      noEligibleFamilyMembers: data.noEligibleDependants ?? false,
      noEligibleNotes: data.noEligibleNotes ?? ''
    });

    this.members.clear();

    let parsed: RelocatingDependant[] = [];
    if (data.dependantsJson) {
      try {
        parsed = JSON.parse(data.dependantsJson) as RelocatingDependant[];
      } catch { /* ignore bad payload */
      }
    }
    parsed.forEach(m => this.addMember(m));

    if (!this.form.get('noEligibleFamilyMembers')!.value && this.members.length === 0) {
      this.addMember();
    }
  }

  canSubmit(): boolean {
    //todo Removed this.form.valid
    // return this.form.valid && !this.form.pending && !this.submitting && !this.readOnly;
    return !this.form.pending && !this.submitting && !this.readOnly;
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.submitting = true;
    this.error = null;

    const value = this.form.getRawValue();
    let members = (value.noEligibleFamilyMembers ? [] :
      this.members.controls.map(c => c.getRawValue() as RelocatingDependant));

    const payload: DependantsInfoFormData = {
      noEligibleDependants: value.noEligibleFamilyMembers,
      noEligibleNotes: value.noEligibleFamilyMembers ? value.noEligibleNotes ?? '' : '',
      dependantsJson: JSON.stringify(members)
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
      }
    });
  }

  protected readonly TravelDocType = TravelDocType;
}
