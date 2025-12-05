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

  relocatingDependants: RelocatingDependant[] = [];

  constructor(
    private fb: FormBuilder,
    private candidateFormService: CandidateFormService
  ) {
  }

  ngOnInit(): void {
    this.form = this.fb.nonNullable.group({
      noEligibleDependants: [false, []],
      noEligibleNotes: [{value: '', disabled: false}],
      members: this.fb.nonNullable.array([] as FormGroup[])
    });

    // "no eligible" toggle
    this.form.get('noEligibleDependants')!.valueChanges.subscribe(checked => {
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
      error: () => this.form.reset({noEligibleDependants: false, noEligibleNotes: ''})
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
      dob: [value?.dob ?? '', Validators.required],
      gender: value?.gender ?? 'other',
      'birthCountry.name': value?.['birthCountry.name'] ?? '',
      placeOfBirth: value?.placeOfBirth ?? '',

      TTH_IT$TRAVEL_DOC_TYPE: [value?.TTH_IT$TRAVEL_DOC_TYPE ?? '', Validators.required],
      TTH_IT$TRAVEL_DOC_NUMBER: [value?.TTH_IT$TRAVEL_DOC_NUMBER ?? '', [Validators.required, Validators.maxLength(64)]],
      TTH_IT$TRAVEL_DOC_ISSUED_BY: [value?.TTH_IT$TRAVEL_DOC_ISSUED_BY ?? '', Validators.maxLength(128)],
      TTH_IT$TRAVEL_DOC_ISSUE_DATE: value?.TTH_IT$TRAVEL_DOC_ISSUE_DATE ?? '',
      TTH_IT$TRAVEL_DOC_EXPIRY_DATE: value?.TTH_IT$TRAVEL_DOC_EXPIRY_DATE ?? '',
      TTH_IT$TRAVEL_INFO_COMMENT: value?.TTH_IT$TRAVEL_INFO_COMMENT ?? ''
    }, {
      validators: [
        this.childAgeIfChildValidator(),
        this.otherRelationshipNotesIfOtherValidator(),
        this.expiryMinMonthsValidator(9)
      ]
    });

    return group;
  }

  addMember(prefill?: Partial<RelocatingDependant>) {
    this.members.push(this.newMemberGroup(prefill));

    //When this is called with no parameter we add a new empty member to the form.
    if (!prefill) {
      this.relocatingDependants.push({});
    }
  }

  removeMember(ix: number) {
    this.members.removeAt(ix);
    this.relocatingDependants.splice(ix, 1);
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

  // --- load/save mapping ---
  private hydrateForm(dependantsInfoFormData: DependantsInfoFormData) {
    this.form.reset({
      noEligibleDependants: dependantsInfoFormData.noEligibleDependants ?? false,
      noEligibleNotes: dependantsInfoFormData.noEligibleNotes ?? ''
    });

    this.members.clear();

    const dependantsInfoJson = dependantsInfoFormData?.dependantsInfoJson;
    if (dependantsInfoJson) {
      try {
        this.relocatingDependants = JSON.parse(dependantsInfoJson) as RelocatingDependant[];
      } catch { /* ignore bad payload */
      }
    }
    this.relocatingDependants.forEach(m => this.addMember(m));

    if (!this.noEligibleDependants && this.members.length === 0) {
      this.addMember();
    }
  }

  canSubmit(): boolean {
    let ok =  this.form.valid && !this.form.pending && !this.submitting && !this.readOnly;
    if (ok) {
      if (this.members.length === 0 && !this.noEligibleDependants) {
        ok = false
      }
    }
    return ok;
  }

  get noEligibleDependants(): boolean {
    return this.form.get('noEligibleDependants')!.value;
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.submitting = true;
    this.error = null;

    const value = this.form.getRawValue();
    //Get data from form
    let members = (value.noEligibleDependants ? [] :
      this.members.controls.map(c => c.getRawValue() as RelocatingDependant));

    //Update the existing dependants with the new data
    if (members.length == 0) {
      this.relocatingDependants = [];
    } else {
      this.relocatingDependants.forEach(
        (dep, index)=> Object.assign(dep, members[index])
      );
    }

    const payload: DependantsInfoFormData = {
      noEligibleDependants: value.noEligibleDependants,
      noEligibleNotes: value.noEligibleDependants ? value.noEligibleNotes ?? '' : '',
      dependantsInfoJson: JSON.stringify(this.relocatingDependants)
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
