import {Component, EventEmitter, Input, OnChanges, Output} from '@angular/core';
import {CandidateOccupation} from "../../../model/candidate-occupation";
import {Occupation} from "../../../model/occupation";

@Component({
  selector: 'app-candidate-occupation-card',
  templateUrl: './candidate-occupation-card.component.html',
  styleUrls: ['./candidate-occupation-card.component.scss']
})
export class CandidateOccupationCardComponent implements OnChanges {

  /* Two way binding */
  @Input() candidateOccupation: CandidateOccupation;
  @Output() candidateOccupationChange = new EventEmitter<CandidateOccupation>();

  @Input() preview: boolean = false;
  @Input() disabled;
  @Input() occupations: Occupation[];

  @Output() onDelete = new EventEmitter();

  constructor() { }

  ngOnChanges() {
    if (!this.candidateOccupation.occupationId) {
      this.candidateOccupation = Object.assign(this.candidateOccupation, {
        occupationId: this.candidateOccupation.occupation ? this.candidateOccupation.occupation.id : null
      })
    }
  }

  delete() {
    this.onDelete.emit();
  }
}
