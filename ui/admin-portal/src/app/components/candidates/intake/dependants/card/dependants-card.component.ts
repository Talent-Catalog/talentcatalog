/*
 * Copyright (c) 2021 Talent Beyond Boundaries.
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
import {CandidateDependant, DependantRelations, YesNo} from '../../../../../model/candidate';
import {FormBuilder} from '@angular/forms';
import {CandidateService} from '../../../../../services/candidate.service';
import {CandidateDependantService} from '../../../../../services/candidate-dependant.service';
import {IntakeComponentBase} from '../../../../util/intake/IntakeComponentBase';
import {NgbDateStruct} from '@ng-bootstrap/ng-bootstrap';

@Component({
  selector: 'app-dependants-card',
  templateUrl: './dependants-card.component.html',
  styleUrls: ['./dependants-card.component.scss']
})
export class DependantsCardComponent extends IntakeComponentBase implements OnInit {

  @Output() delete = new EventEmitter();

  public maxDate: NgbDateStruct;
  public today: Date;
  public age: number;

  //Drop down values for enumeration
  dependantRelations: EnumOption[] = enumOptions(DependantRelations);
  dependentHealthConcerns: EnumOption[] = enumOptions(YesNo);

  constructor(fb: FormBuilder, candidateService: CandidateService,
              private candidateDependantService: CandidateDependantService) {
    super(fb, candidateService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      dependantId: [this.myRecord?.id],
      dependantRelation: [this.myRecord?.relation],
      dependantDob: [this.myRecord?.dob],
      dependantHealthConcerns: [this.myRecord?.healthConcern],
      dependantNotes: [this.myRecord?.notes],
    });
    this.today = new Date();
    this.maxDate = {year: this.today.getFullYear(), month: this.today.getMonth() + 1, day: this.today.getDate()};
  }

  get hasHealthConcern(): string {
    return this.form.value.dependantHealthConcerns;
  }

  private get myRecord(): CandidateDependant {
    return this.candidateIntakeData.candidateDependants ?
      this.candidateIntakeData.candidateDependants[this.myRecordIndex]
      : null;
  }

  get dependantAge(): number {
    if (this.form?.value.dependantDob) {
      const timeDiff = Math.abs(Date.now() - new Date(this.form.value.dependantDob).getTime());
      return Math.floor(timeDiff / (1000 * 3600 * 24) / 365.25);
    }
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
}
