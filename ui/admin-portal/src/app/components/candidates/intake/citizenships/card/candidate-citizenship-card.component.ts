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

  //This shows all records currently in use. It is used to avoid having
  //duplicate records.
  @Input() existingRecords: CandidateCitizenship[];

  //Index into the above existingRecords array of the record corresponding to
  //this component instance.
  @Input() myRecordIndex: number;

  //All known nationalities - a filtered version of this is used for drop downs
  //Filtered to remove nationalities already used in existingRecords.
  @Input() nationalities: Nationality[];

  //Drop down values for enumeration
  hasPassportOptions: EnumOption[] = enumOptions(HasPassport);

  constructor(fb: FormBuilder, candidateService: CandidateService) {
    super(fb, candidateService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      citizenNationalityId: [this.myRecord?.citizenNationalityId],
      citizenHasPassport: [this.myRecord?.citizenHasPassport],
      citizenNotes: [this.myRecord?.citizenNotes],
    });

    //Subscribe to changes on the id so that we can keep existing records up
    //to date - used to filter ids on new records so that we don't get
    //duplicates/
    this.form.controls['citizenNationalityId'].valueChanges.subscribe(
      change => {
        //Update my existingRecord
        this.myRecord.citizenNationalityId = +change;
      }
    );
  }

  /**
   * Filters out nationalities already used in existingRecords
   */
  get filteredNationalities(): Nationality[] {
    if (!this.nationalities) {
      return [];
    } else if (!this.existingRecords) {
      return this.nationalities;
    } else {
      const existingIds: number[] =
        this.existingRecords.map(record => record.citizenNationalityId);
      return this.nationalities.filter(
        record =>
          //Include current id associated with this record
          record.id === this.myRecord?.citizenNationalityId ||
          //But not any other ids already associated with existing records
          !existingIds.includes(record.id)
      );
    }
  }

  get hasSelectedNationality(): boolean {
    return this.form.value?.citizenNationalityId;
  }

  private get myRecord() {
    return this.existingRecords[this.myRecordIndex];
  }
}
