/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {
  CandidateCitizenship,
  HasPassport
} from "../../../../../model/candidate";
import {EnumOption, enumOptions} from "../../../../../util/enum";
import {FormBuilder} from "@angular/forms";
import {CandidateService} from "../../../../../services/candidate.service";
import {IntakeComponentBase} from "../../../../util/intake/IntakeComponentBase";
import {Nationality} from "../../../../../model/nationality";

@Component({
  selector: 'app-candidate-citizenship-card',
  templateUrl: './candidate-citizenship-card.component.html',
  styleUrls: ['./candidate-citizenship-card.component.scss']
})
export class CandidateCitizenshipCardComponent extends IntakeComponentBase implements OnInit {
  @Output() delete = new EventEmitter();
  @Input() candidateCitizenship: CandidateCitizenship;
  @Input() nationalities: Nationality[];
  hasPassportOptions: EnumOption[] = enumOptions(HasPassport);

  constructor(fb: FormBuilder, candidateService: CandidateService) {
    super(fb, candidateService);
  }

  ngOnInit(): void {
    const nationalityId = this.candidateCitizenship ?
      this.candidateCitizenship.citizenNationalityId :
      this.candidateIntakeData?.citizenNationalityId;
    const citizenHasPassport = this.candidateCitizenship ?
      this.candidateCitizenship.citizenHasPassport :
      this.candidateIntakeData?.citizenHasPassport;
    const citizenNotes = this.candidateCitizenship ?
      this.candidateCitizenship.citizenNotes :
      this.candidateIntakeData?.citizenNotes;

    this.form = this.fb.group({
      citizenNationalityId: [nationalityId],
      citizenHasPassport: [citizenHasPassport],
      citizenNotes: [citizenNotes],
    });
  }

  get hasSelectedNationality(): boolean {
    return this.form.value?.citizenNationalityId;
  }
}
