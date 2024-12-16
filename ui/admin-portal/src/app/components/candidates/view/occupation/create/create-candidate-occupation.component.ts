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

import { Component, OnInit } from '@angular/core';
import {CandidateOccupation} from "../../../../../model/candidate-occupation";
import {UntypedFormBuilder, UntypedFormGroup, Validators} from "@angular/forms";
import {Occupation} from "../../../../../model/occupation";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {CandidateOccupationService} from "../../../../../services/candidate-occupation.service";
import {OccupationService} from "../../../../../services/occupation.service";

@Component({
  selector: 'app-create-candidate-occupation',
  templateUrl: './create-candidate-occupation.component.html',
  styleUrls: ['./create-candidate-occupation.component.scss']
})
export class CreateCandidateOccupationComponent implements OnInit {

  candidateOccupation: CandidateOccupation;

  form: UntypedFormGroup;
  candidateId: number;
  occupations: Occupation[];
  years = [];
  error;
  loading: boolean;
  saving: boolean;

  constructor(private activeModal: NgbActiveModal,
              private fb: UntypedFormBuilder,
              private candidateOccupationService: CandidateOccupationService,
              private occupationService: OccupationService ) {
  }

  ngOnInit() {
    this.loading = true;
    this.form = this.fb.group({
      occupationId: [null, Validators.required],
      yearsExperience: [null, [Validators.required, Validators.min(0)]],
    });

    /* LOAD OCCUPATIONS */
    this.occupationService.listOccupations().subscribe(
      (response) => {
        this.occupations = response;
        // console.log(this.occupations);
        this.loading = false;
      },
      (error) => {
        this.error = error;
        this.loading = false;
      }
    );
  }

  onSave() {
    this.saving = true;
    this.candidateOccupationService.create(this.candidateId, this.form.value).subscribe(
      (candidateOccupation) => {
        this.closeModal(candidateOccupation);
        this.saving = false;
      },
      (error) => {
        this.error = error;
        this.saving = false;
      });
  }

  closeModal(candidateOccupation: CandidateOccupation) {
    this.activeModal.close(candidateOccupation);
  }

  dismiss() {
    this.activeModal.dismiss(false);
  }

}
