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
  visaChecks: CandidateVisa[];
  tbbDestinations: Country[];
  form: FormGroup;
  selectedIndex: number;
  selectedCountry: string;
  selectedVisaCheck: CandidateVisa;

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

    })
    // FETCH TBB DESTINATIONS
    this.countryService.listTBBDestinations().subscribe((results) => {
      this.tbbDestinations = results;
    })

    // FETCH VISA CHECKS
    this.candidateVisaCheckService.list(this.candidate.id).subscribe((results) => {
      this.visaChecks = results;
      this.changeVisaCountry(null);
    })

    this.form = this.fb.group({
      visaCountry: [0]
    });
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
        this.visaChecks.push(visaCheck);
        this.form.controls['visaCountry'].patchValue(this.visaChecks.lastIndexOf(visaCheck));
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
    const visaCheck: CandidateVisa = this.visaChecks[i];

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
        this.visaChecks.splice(i, 1);
        this.changeVisaCountry(null);
      },
      (error) => {
        this.error = error;
        this.loading = false;
      });
  }

  changeVisaCountry(event: Event) {
    let index = this.form.controls.visaCountry.value;
    this.selectedCountry = this.visaChecks[index]?.country?.name;
    this.getVisaCheckCountryRecord();
  }

  getVisaCheckCountryRecord(){
    return this.visaChecks.find(v => v.country.name == this.selectedCountry)
  }

}
