/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

import {Component, OnInit} from '@angular/core';
import {UntypedFormBuilder, UntypedFormGroup, Validators} from '@angular/forms';
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {
  CandidateJobExperienceService
} from "../../../../../../services/candidate-job-experience.service";
import {CandidateJobExperience} from "../../../../../../model/candidate-job-experience";
import {CountryService} from "../../../../../../services/country.service";
import {Candidate} from "../../../../../../model/candidate";

@Component({
  selector: 'app-edit-candidate-job-experience',
  templateUrl: './edit-candidate-job-experience.component.html',
  styleUrls: ['./edit-candidate-job-experience.component.scss']
})
export class EditCandidateJobExperienceComponent implements OnInit {

  candidateJobExperience: CandidateJobExperience;
  candidate: Candidate;

  candidateForm: UntypedFormGroup;

  countries = [];
  extractedSkills: string[];
  years = [];
  error;
  loading: boolean;
  saving: boolean;

  fullTime = [
    { displayText: 'Full Time', value: true },
    { displayText: 'Part Time', value: false },
  ];

  paid = [
    { displayText: 'Paid', value: true },
    { displayText: 'Voluntary', value: false },
  ];

  constructor(private activeModal: NgbActiveModal,
              private fb: UntypedFormBuilder,
              private candidateJobExperienceService: CandidateJobExperienceService,
              private countryService: CountryService ) {
  }

  ngOnInit() {
    this.loading = true;

    /*load the countries */
    this.countryService.listCountries().subscribe(
      (response) => {
        this.countries = response;
      },
      (error) => {
        this.error = error;
        this.loading = false;
      }
    );

    //Convert the keywordsInDescription array into a comma-separated string for display in the form.
    let keywordsAsCsv = null;
    if (this.candidateJobExperience.keywordsInDescription && Array.isArray(this.candidateJobExperience.keywordsInDescription)) {
      keywordsAsCsv = this.candidateJobExperience.keywordsInDescription.join(', ');
    }
    this.candidateForm = this.fb.group({
      countryId: [this.candidateJobExperience.country ? this.candidateJobExperience.country.id : null, Validators.required],
      companyName: [this.candidateJobExperience.companyName],
      role: [this.candidateJobExperience.role],
      startDate: [this.candidateJobExperience.startDate],
      endDate: [this.candidateJobExperience.endDate],
      fullTime: [this.candidateJobExperience.fullTime],
      paid: [this.candidateJobExperience.paid],
      description: [this.candidateJobExperience.description],
      tidiedDescription: [this.candidateJobExperience.tidiedDescription],
      keywordsInDescription: [keywordsAsCsv],
    });
    this.loading = false;
  }

  onSave() {
    this.saving = true;
    //Populate an UpdateCandidateJobExperienceRequest object with the form values and send it to the backend.
    const updateRequest = { ...this.candidateForm.value };
    //Convert keywords into an array of strings. Currently, it is a csv string.
    if (updateRequest.keywordsInDescription && typeof updateRequest.keywordsInDescription === 'string') {
      updateRequest.keywordsInDescription = (updateRequest.keywordsInDescription as string)
      .split(',').map((s: string) => s.trim());
    }
    this.candidateJobExperienceService.update(this.candidateJobExperience.id, updateRequest).subscribe(
      (candidateJobExperience) => {
        this.closeModal(candidateJobExperience);
        this.saving = false;
      },
      (error) => {
        this.error = error;
        this.saving = false;
      });
  }

  closeModal(candidateJobExperience: CandidateJobExperience) {
    this.activeModal.close(candidateJobExperience);
  }

  dismiss() {
    this.activeModal.dismiss(false);
  }
}
