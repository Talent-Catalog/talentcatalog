/*
 * Copyright (c) 2024 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

import {Component, EventEmitter, OnInit, Output} from '@angular/core';
import {EnumOption, enumOptions} from '../../../../../util/enum';
import {
  CandidateDependant,
  DependantRelations,
  Gender,
  Registrations,
  YesNo
} from '../../../../../model/candidate';
import {UntypedFormBuilder} from '@angular/forms';
import {CandidateService} from '../../../../../services/candidate.service';
import {CandidateDependantService} from '../../../../../services/candidate-dependant.service';
import {IntakeComponentBase} from '../../../../util/intake/IntakeComponentBase';

@Component({
  selector: 'app-dependants-card',
  templateUrl: './dependants-card.component.html',
  styleUrls: ['./dependants-card.component.scss']
})
export class DependantsCardComponent extends IntakeComponentBase implements OnInit {

  @Output() delete = new EventEmitter();
  public age: number;

  //Drop down values for enumeration
  dependantRelations: EnumOption[] = enumOptions(DependantRelations);
  dependantGenders: EnumOption[] = enumOptions(Gender);
  dependantRegisterOptions: EnumOption[] = enumOptions(Registrations);
  dependentHealthConcerns: EnumOption[] = enumOptions(YesNo);

  constructor(fb: UntypedFormBuilder, candidateService: CandidateService,
              private candidateDependantService: CandidateDependantService) {
    super(fb, candidateService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      dependantId: [this.myRecord?.id],
      dependantRelation: [this.myRecord?.relation],
      dependantRelationOther: [this.myRecord?.relationOther],
      dependantDob: [this.myRecord?.dob],
      dependantGender: [this.myRecord?.gender],
      dependantName: [this.myRecord?.name],
      dependantRegistered: [this.myRecord?.registered],
      dependantRegisteredNumber: [this.myRecord?.registeredNumber],
      dependantRegisteredNotes: [this.myRecord?.registeredNotes],
      dependantHealthConcerns: [this.myRecord?.healthConcern],
      dependantHealthNotes: [this.myRecord?.healthNotes],
    });

    this.form.controls['dependantDob']?.valueChanges.subscribe(dob => {
      this.candidateIntakeData.candidateDependants[this.myRecordIndex].dob = dob;
    });

    this.form.controls['dependantGender']?.valueChanges.subscribe(gender => {
      this.candidateIntakeData.candidateDependants[this.myRecordIndex].gender = gender;
    });

    this.form.controls['dependantHealthConcerns']?.valueChanges.subscribe(health => {
      this.candidateIntakeData.candidateDependants[this.myRecordIndex].healthConcern = health;
    });
  }

  get hasHealthConcern(): string {
    return this.form.value.dependantHealthConcerns;
  }

  get myRecord(): CandidateDependant {
    return this.candidateIntakeData.candidateDependants ?
      this.candidateIntakeData.candidateDependants[this.myRecordIndex]
      : null;
  }

  get hasDependant(): boolean {
    let found: boolean;
    if (this.form?.value?.dependantRelation) {
      found = this.form.value.dependantRelation !== 'NoResponse'
    }
    return found;
  }

  get dependantRelationship(): string {
    return this.form.value.dependantRelation;
  }

  get dependantAge(): number {
    if (this.form?.value.dependantDob) {
      const timeDiff = Math.abs(Date.now() - new Date(this.form.value.dependantDob).getTime());
      return Math.floor(timeDiff / (1000 * 3600 * 24) / 365.25);
    }
  }

  get dependantRegistered(): string {
    return this.form.value.dependantRegistered;
  }

  doDelete() {
    this.candidateDependantService.delete(this.myRecord.id)
      .subscribe(
        ret => {
        },
        error => {
          this.error = error;
        }
      );
    this.delete.emit();
  }

  get dependantRegisteredNumber() {
    if (this.dependantRelationship === 'Child' || this.dependantRelationship === 'Partner') {
      if (this.dependantRegistered === 'UNHCR' || this.dependantRegistered === 'UNRWA' || this.dependantRegistered === 'UNHCRUNRWA') {
        return true;
      }
    }
  }
}
