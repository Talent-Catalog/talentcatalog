import {Component, EventEmitter, Input, OnChanges, Output} from '@angular/core';
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

  // The currently selected candidate occupation to display
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

  ngOnChanges() {
  }

  /* This method removes the occupations from the dropdown that are already selected as a candidate occupation to avoid duplication
  (not including the current candidate occupation which is needed to display) */
  get filteredOccupations(): Occupation[] {
    if (!this.occupations) {
      return [];
    } else if (!this.candidateOccupations || !this.occupations.length) {
      return this.occupations;
    } else {
      let existingIds = this.candidateOccupations.map(candidateOcc => candidateOcc.occupation.id.toString());
      existingIds = existingIds.filter(id => id !== this.candidateOccupation.occupation.id.toString());
      if (this.candidateOccupation.occupation.id !== 0) {
        existingIds.push('0');
      }
      return this.occupations.filter(occ => !existingIds.includes(occ.id.toString()));
    }
  }

  delete() {
    this.onDelete.emit();
  }
}
