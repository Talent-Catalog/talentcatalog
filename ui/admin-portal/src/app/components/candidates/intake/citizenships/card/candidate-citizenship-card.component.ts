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

import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {CandidateCitizenship, HasPassport} from '../../../../../model/candidate';
import {EnumOption, enumOptions} from '../../../../../util/enum';
import {UntypedFormBuilder} from '@angular/forms';
import {CandidateService} from '../../../../../services/candidate.service';
import {IntakeComponentBase} from '../../../../util/intake/IntakeComponentBase';
import {CandidateCitizenshipService} from '../../../../../services/candidate-citizenship.service';
import {Country} from "../../../../../model/country";

@Component({
  selector: 'app-candidate-citizenship-card',
  templateUrl: './candidate-citizenship-card.component.html',
  styleUrls: ['./candidate-citizenship-card.component.scss']
})
export class CandidateCitizenshipCardComponent extends IntakeComponentBase implements OnInit {
  @Output() delete = new EventEmitter();

  //All known nationalities - a filtered version of this is used for drop downs
  //Filtered to remove nationalities already used in existingRecords.
  @Input() nationalities: Country[];

  //Drop down values for enumeration
  hasPassportOptions: EnumOption[] = enumOptions(HasPassport);

  today: Date;

  constructor(fb: UntypedFormBuilder, candidateService: CandidateService,
              private candidateCitizenshipService: CandidateCitizenshipService) {
    super(fb, candidateService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      citizenId: [this.myRecord?.id],
      citizenNationalityId: [this.myRecord?.nationality?.id],
      citizenHasPassport: [this.myRecord?.hasPassport],
      citizenPassportExp: [this.myRecord?.passportExp],
      citizenNotes: [this.myRecord?.notes],
    });

    this.today = new Date();

    //Subscribe to changes on the nationality id so that we can keep local
    //intake data up to date - used to filter ids on new records so that we
    //don't get duplicates.
    //Even though the change has been saved on the server and is reflected
    //on the html form, it is not stored in the local copy of the candidate
    //intake data. We could refresh the whole page which will reload all
    //candidate intake data with the saved values - but more efficient just
    //to update it here.
    this.form.controls['citizenNationalityId']?.valueChanges.subscribe(
      change => {
        //Update my existingRecord
        this.myRecord.nationality.id = +change;
      }
    );
  }

  /**
   * Filters out nationalities already used in existingRecords
   */
  get filteredNationalities(): Country[] {
    if (!this.nationalities) {
      return [];
    } else if (!this.candidateIntakeData.candidateCitizenships) {
      return this.nationalities;
    } else {
      const existingIds: number[] =
        this.candidateIntakeData.candidateCitizenships.map(record => record.nationality?.id);
      return this.nationalities.filter(
        record =>
          //Include current id associated with this record
          record.id === this.myRecord?.nationality?.id ||
          //But not any other ids already associated with existing records
          !existingIds.includes(record.id)
      );
    }
  }

  get hasSelectedNationality(): boolean {
    let found: boolean = false;
    if (this.form?.value) {
      found = this.form.value.citizenNationalityId;
    }
    return found;
  }

  get hasPassport(): string {
    return this.form.value.citizenHasPassport;
  }

  get passportExpiry(): string {
    return this.form.value.citizenPassportExp;
  }

  dateDifference() {
    if (this.passportExpiry) {
      const expDate = new Date(this.passportExpiry)
      return expDate < this.today
    }
  }

  private get myRecord(): CandidateCitizenship {
    return this.candidateIntakeData.candidateCitizenships ?
      this.candidateIntakeData.candidateCitizenships[this.myRecordIndex]
      : null;
  }

  doDelete() {
    this.candidateCitizenshipService.delete(this.myRecord.id)
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
