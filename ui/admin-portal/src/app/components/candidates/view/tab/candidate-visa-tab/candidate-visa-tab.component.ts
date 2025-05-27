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

import {Component, Input, OnInit} from '@angular/core';
import {CandidateService} from '../../../../../services/candidate.service';
import {UntypedFormBuilder, UntypedFormGroup} from '@angular/forms';
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
import {AuthorizationService} from "../../../../../services/authorization.service";
import {LocalStorageService} from "../../../../../services/local-storage.service";

@Component({
  selector: 'app-candidate-visa-tab',
  templateUrl: './candidate-visa-tab.component.html',
  styleUrls: ['./candidate-visa-tab.component.scss']
})
export class CandidateVisaTabComponent implements OnInit {
  @Input() candidate: Candidate;
  candidateIntakeData: CandidateIntakeData;
  visaChecks: CandidateVisa[];
  tcDestinations: Country[];
  form: UntypedFormGroup;
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
              private fb: UntypedFormBuilder,
              private localStorageService: LocalStorageService,
              private authService: AuthorizationService) {
  }

  ngOnInit(): void {
    this.loading = true;

    // FETCH INTAKE DATA
    this.candidateService.getIntakeData(this.candidate.id).subscribe((results) => {
      this.candidateIntakeData = results;
      this.loading = false;

    })
    // FETCH TBB DESTINATIONS
    this.countryService.listTCDestinations().subscribe((results) => {
      /**
       * todo: Remove/alter this filter once no longer needed or find other solution.
       * It is a temporary filter to only display the TBB destinations (Australia, Canada & UK) that have functioning visa checks.
       */
      this.tcDestinations = results.filter(c => c.id == 6191 || c.id == 6216 || c.id == 6179);
    })

    this.reloadAndSelectVisaCheck(0)

    this.form = this.fb.group({
      visaCountry: [0]
    });
  }

  /**
   * Filters out destinations already used in existingRecords
   */
  private get filteredDestinations(): Country[] {
    if (!this.tcDestinations) {
      return [];
    } else if (this.visaChecks?.length <= 0) {
      return this.tcDestinations;
    } else {
      //Extract currently used ids
      const existingIds: number[] = this.visaChecks?.map(record => record.country?.id);
      return this.tcDestinations.filter(
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
          this.filteredDestinations;
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
        this.reloadAndSelectVisaCheck(this.visaChecks.indexOf(visaCheck));
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
        if (this.visaChecks.length > 0) {
          this.form.controls['visaCountry'].patchValue(0);
          this.reloadAndSelectVisaCheck(0);
        } else {
          this.selectedVisaCheck = null;
        }
      },
      (error) => {
        this.error = error;
        this.loading = false;
      });
  }

  /**
   * Reloads the visa checks and then sets the updated selected visa check.
   */
  reloadAndSelectVisaCheck(index: number) {
    this.candidateVisaCheckService.list(this.candidate.id).subscribe(
      (results) => {
        this.visaChecks = results;
        this.selectedVisaCheck = this.visaChecks[index];
        this.selectedCountry = this.selectedVisaCheck?.country?.name;
      })
  }

  canDeleteVisa() : boolean {
    return this.authService.isSystemAdminOnly();
  }

  isEditable(): boolean {
    return this.authService.isEditableCandidate(this.candidate);
  }

}
