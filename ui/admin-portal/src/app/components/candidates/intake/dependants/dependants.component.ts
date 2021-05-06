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
import {Candidate, CandidateIntakeData} from '../../../../model/candidate';
import {Nationality} from '../../../../model/nationality';
import {
  CandidateDependantService,
  CreateCandidateDependantRequest
} from '../../../../services/candidate-dependant.service';
import {Subject} from "rxjs";

@Component({
  selector: 'app-dependants',
  templateUrl: './dependants.component.html',
  styleUrls: ['./dependants.component.scss']
})
export class DependantsComponent implements OnInit {

  @Input() candidate: Candidate;
  @Input() candidateIntakeData: CandidateIntakeData;
  error: boolean;
  @Input() nationalities: Nationality[];
  saving: boolean;
  activeIds: string;
  open: boolean;

  @Input() toggleAll: Subject<any>;


  constructor(
    private candidateDependantService: CandidateDependantService
  ) {}

  ngOnInit(): void {
    this.activeIds = 'intake-dependants';
    this.open = true;
    // called when the toggleAll method is called in the parent component
    this.toggleAll.subscribe(isOpen => {
      this.open = isOpen;
      this.setActiveIds();
    })
  }

  toggleOpen() {
    this.open = !this.open
    this.setActiveIds();
  }

  setActiveIds(){
    if (this.open) {
      this.activeIds = 'intake-dependants';
    } else {
      this.activeIds = '';
    }
  }

  addRecord() {
    this.saving = true;
    this.open = true;
    this.setActiveIds();
    const request: CreateCandidateDependantRequest = {};
    this.candidateDependantService.create(this.candidate.id, request).subscribe(
      (dependant) => {
        this.candidateIntakeData?.candidateDependants.unshift(dependant)
        this.saving = false;
      },
      (error) => {
        this.error = error;
        this.saving = false;
      });
  }

  deleteRecord(i: number) {
    this.candidateIntakeData?.candidateDependants.splice(i, 1);
  }

}
