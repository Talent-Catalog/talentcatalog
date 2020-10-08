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
import {CandidateCitizenshipService} from "../../../../../services/candidate-citizenship.service";

@Component({
  selector: 'app-candidate-citizenship-card',
  templateUrl: './candidate-citizenship-card.component.html',
  styleUrls: ['./candidate-citizenship-card.component.scss']
})
export class CandidateCitizenshipCardComponent extends IntakeComponentBase implements OnInit {
  @Output() delete = new EventEmitter();

  //All known nationalities - a filtered version of this is used for drop downs
  //Filtered to remove nationalities already used in existingRecords.
  @Input() nationalities: Nationality[];

  //Drop down values for enumeration
  hasPassportOptions: EnumOption[] = enumOptions(HasPassport);

  constructor(fb: FormBuilder, candidateService: CandidateService,
              private candidateCitizenshipService: CandidateCitizenshipService) {
    super(fb, candidateService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      citizenId: [this.myRecord?.id],
      citizenNationalityId: [this.myRecord?.nationality?.id],
      citizenHasPassport: [this.myRecord?.hasPassport],
      citizenNotes: [this.myRecord?.notes],
    });

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
        this.myRecord.nationality = {id: +change};
      }
    );
  }

  /**
   * Filters out nationalities already used in existingRecords
   */
  get filteredNationalities(): Nationality[] {
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
