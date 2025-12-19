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

import {Component, EventEmitter, Input, OnChanges, Output, SimpleChanges} from '@angular/core';
import {CandidateOccupation} from '../../../model/candidate-occupation';
import {Occupation} from '../../../model/occupation';

@Component({
  selector: 'app-candidate-occupation-card',
  templateUrl: './candidate-occupation-card.component.html',
  styleUrls: ['./candidate-occupation-card.component.scss']
})
export class CandidateOccupationCardComponent implements OnChanges {

  /*  PURPOSE: The purpose of this component is to display a candidate occupation object (occupation & years experience) and
  depending on the preview input allows for editing */

  // The currently selected candidate occupation to display. If it comes from the server it will have an occupation object, if it comes from the form it will only have an occupation Id.
  @Input() candidateOccupation: CandidateOccupation;
  // The two way binding for the candidate occupation change
  @Output() candidateOccupationChange = new EventEmitter<CandidateOccupation>();

  // Complete list of a particular candidate's occupations
  @Input() candidateOccupations: CandidateOccupation[];
  // Complete list of all occupations
  @Input() occupations: Occupation[];
  // If preview false doesn't allow editing, if preview true can change selection of occupation or years experience
  @Input() preview: boolean = false;

  @Input() disabled;
  @Output() onDelete = new EventEmitter();

  constructor() { }

  ngOnChanges(changes: SimpleChanges) {
    // If the candidate occupation comes from the server it will have an occupation object, we need to extract the Id.
    if (this.candidateOccupation.occupation) {
      this.candidateOccupation = Object.assign(this.candidateOccupation, {
        occupationId: this.candidateOccupation.occupation ? this.candidateOccupation.occupation.id : null,
      });
    }
  }

  /* This method removes the occupations from the dropdown that are already selected as a candidate occupation to avoid duplication
  (not including the current candidate occupation which is needed to display) */
  get filteredOccupations(): Occupation[] {
    if (!this.occupations) {
      return [];
    } else if (!this.candidateOccupations || !this.occupations.length) {
      return this.occupations;
    } else {
      let existingIds = this.candidateOccupations.map(candidateOcc => candidateOcc.occupationId.toString());
      // Remove the current occupation from the list so it appears in the dropdown to display.
      existingIds = existingIds.filter(id => id !== this.candidateOccupation.occupationId.toString());
      // Remove the Unknown occupation from the results if it isn't the current occupation input
      if (this.candidateOccupation.occupationId !== 0) {
        existingIds.push('0');
      }
      return this.occupations.filter(occ => !existingIds.includes(occ.id.toString()));
    }
  }

  delete() {
    this.onDelete.emit();
  }

  getOccupationName(occupation: Occupation) {
    return this.occupations?.find(o => o.id === occupation.id)?.name;
  }
}
