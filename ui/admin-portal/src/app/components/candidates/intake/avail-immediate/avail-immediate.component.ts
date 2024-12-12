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

import {Component, OnInit} from '@angular/core';
import {IntakeComponentBase} from '../../../util/intake/IntakeComponentBase';
import {EnumOption, enumOptions} from '../../../../util/enum';
import {AvailImmediateReason, YesNo, YesNoUnsure} from '../../../../model/candidate';
import {UntypedFormBuilder} from '@angular/forms';
import {CandidateService} from '../../../../services/candidate.service';

@Component({
  selector: 'app-avail-immediate',
  templateUrl: './avail-immediate.component.html',
  styleUrls: ['./avail-immediate.component.scss']
})

export class AvailImmediateComponent extends IntakeComponentBase implements OnInit {

  public availImmediateOptions: EnumOption[] = enumOptions(YesNo);
  public availImmediateReasonOptions: EnumOption[] = enumOptions(AvailImmediateReason);
  public interestedOptions: EnumOption[] = enumOptions(YesNoUnsure);

  constructor(fb: UntypedFormBuilder, candidateService: CandidateService) {
    super(fb, candidateService);
  }

  ngOnInit(): void {
    const interested = this.computeInterested();
    this.form = this.fb.group({
      availDate: [this.candidateIntakeData?.availDate],
      availImmediate: [this.candidateIntakeData?.availImmediate],
      availImmediateJobOps: [this.candidateIntakeData?.availImmediateJobOps],
      availImmediateReason: [this.candidateIntakeData?.availImmediateReason],
      availImmediateNotes: [this.candidateIntakeData?.availImmediateNotes],
      interested: [interested]
    });
  }

  /**
   * Returns whether candidate is interested based on their status and their answer to
   * availImmediate.
   * @return yes or no - or null if unsure.
   * @private
   */
  private computeInterested(): YesNo {
    //Default is that the candidate has not responded to this question, or that they are unsure.
    //Either way, we return null.
    let interested = null;

    //If the candidate's status is withdrawn, assume that they are not interested.
    if (this.candidate.status == 'withdrawn') {
      interested = YesNo.No;
    } else {
      //If the candidate's status is not that they have withdrawn then compute their status from
      //any response they have made to the availImmediate question.
      //If they have got as far as answering that question with a Yes or No,
      //we can assume that they said that they were interested.
      if (this.candidateIntakeData?.availImmediate &&
        this.candidateIntakeData?.availImmediate != YesNo.NoResponse) {
        interested = YesNo.Yes;
      }
    }
    return interested;
  }

  get availImmediateJobOps(): string {
    return this.form.value?.availImmediateJobOps;
  }

  get availImmediate(): string {
    return this.form.value?.availImmediate;
  }

  get interested(): string {
    return this.form.value?.interested;
  }

}
