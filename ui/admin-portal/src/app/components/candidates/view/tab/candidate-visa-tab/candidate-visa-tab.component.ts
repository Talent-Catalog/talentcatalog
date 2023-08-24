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

import {Component, Input, OnInit} from '@angular/core';
import {CandidateService} from '../../../../../services/candidate.service';
import {FormBuilder, FormGroup} from '@angular/forms';
import {CountryService} from '../../../../../services/country.service';
import {
  CandidateVisaCheckService,
  CreateCandidateVisaCheckRequest
} from '../../../../../services/candidate-visa-check.service';
import {Country} from '../../../../../model/country';
import {HasNameSelectorComponent} from '../../../../util/has-name-selector/has-name-selector.component';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {ConfirmationComponent} from '../../../../util/confirm/confirmation.component';
import {Candidate, CandidateIntakeData, CandidateVisa} from '../../../../../model/candidate';
import {LocalStorageService} from "angular-2-local-storage";

@Component({
  selector: 'app-candidate-visa-tab',
  templateUrl: './candidate-visa-tab.component.html',
  styleUrls: ['./candidate-visa-tab.component.scss']
})
export class CandidateVisaTabComponent implements OnInit {
  @Input() candidate: Candidate;
  candidateIntakeData: CandidateIntakeData;
  tbbDestinations: Country[];
  form: FormGroup;
  selectedIndex: number;
  selectedCountry: string;

  loading: boolean;
  error: boolean;
  saving: boolean;

  constructor(private candidateService: CandidateService,
              private countryService: CountryService,
              private candidateVisaCheckService: CandidateVisaCheckService,
              private modalService: NgbModal,
              private fb: FormBuilder,
              private localStorageService: LocalStorageService) {
  }

  ngOnInit(): void {
    // FETCH INTAKE DATA
    this.candidateService.getIntakeData(this.candidate.id).subscribe((results) => {
      this.candidateIntakeData = results;
      this.setSelectedCountry()
    })
    // FETCH TBB DESTINATIONS
    this.countryService.listTBBDestinations().subscribe((results) => {
      this.tbbDestinations = results;
    })

    this.form = this.fb.group({
      visaCountry: [null]
    });
  }

  setSelectedCountry() {
    // If exists, get the last selected visa check from local storage. If nothing there, get the first one.
    if (this.candidateIntakeData?.candidateVisaChecks.length > 0) {
      const index: number = this.localStorageService.get('VisaCheckIndex');
      if (index) {
        this.selectedIndex = index;
      } else {
        this.selectedIndex = 0;
      }
    }

    this.form.controls.visaCountry.patchValue(this.selectedIndex);

    this.changeVisaCountry(null);
  }

  /**
   * Filters out destinations already used in existingRecords
   */
  private get filteredDestinations(): Country[] {
    if (!this.tbbDestinations) {
      return [];
    } else if (!this.candidateIntakeData.candidateCitizenships) {
      return this.tbbDestinations;
    } else {
      //Extract currently used ids
      const existingIds: number[] = this.candidateIntakeData.candidateVisaChecks
        .map(record => record.country?.id);
      return this.tbbDestinations.filter(
        //Exclude already used ids
        record => !existingIds.includes(record.id)
      );
    }
  }

  addRecord() {
    const modal = this.modalService.open(HasNameSelectorComponent);
    modal.componentInstance.hasNames = this.filteredDestinations;
    modal.componentInstance.label = "TC Destinations";

    modal.result
      .then((selection: Country) => {
        if (selection) {
          this.createRecord(selection);
        }
      })
      .catch(() => {
        //User cancelled selection
      });
  }

  createRecord(country: Country) {
    this.loading = true;
    const request: CreateCandidateVisaCheckRequest = {
      countryId: country.id
    };
    this.candidateVisaCheckService.create(this.candidate.id, request)
      .subscribe(
      (visaCheck) => {
        this.candidateIntakeData.candidateVisaChecks.push(visaCheck);
        this.form.controls['visaCountry'].patchValue(this.candidateIntakeData.candidateVisaChecks.lastIndexOf(visaCheck));
        this.changeVisaCountry(null)
        this.loading = false;
      },
      (error) => {
        this.error = error;
        this.loading = false;
      });

  }

  deleteRecord(i: number) {
    const confirmationModal = this.modalService.open(ConfirmationComponent);
    const visaCheck: CandidateVisa = this.candidateIntakeData.candidateVisaChecks[i];

    confirmationModal.componentInstance.message =
      "Are you sure you want to delete the visa check for " + visaCheck.country.name;
    confirmationModal.result
      .then((result) => {
        if (result === true) {
          this.doDelete(i, visaCheck);
        }
      })
      .catch(() => {});
  }

  private doDelete(i: number, visaCheck: CandidateVisa) {
    this.loading = true;
    this.candidateVisaCheckService.delete(visaCheck.id).subscribe(
      (done) => {
        this.loading = false;
        this.candidateIntakeData.candidateVisaChecks.splice(i, 1);
        this.changeVisaCountry(null);
      },
      (error) => {
        this.error = error;
        this.loading = false;
      });
  }

  changeVisaCountry(event: Event) {
    this.selectedIndex = this.form.controls.visaCountry.value;
    this.selectedCountry = this.candidateIntakeData?.candidateVisaChecks[this.selectedIndex]?.country?.name;
  }

  getVisaCheckCountryRecord(){
    return this.candidateIntakeData.candidateVisaChecks.find(v => v.country.name == this.selectedCountry)
  }

}
