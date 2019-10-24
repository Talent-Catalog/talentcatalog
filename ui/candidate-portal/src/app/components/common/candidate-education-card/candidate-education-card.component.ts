import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {CandidateEducation} from "../../../model/candidate-education";

@Component({
  selector: 'app-candidate-education-card',
  templateUrl: './candidate-education-card.component.html',
  styleUrls: ['./candidate-education-card.component.scss']
})
export class CandidateEducationCardComponent implements OnInit {

  @Input() preview: boolean = false;
  @Input() disabled: boolean = false;
  @Input() candidateEducation: CandidateEducation;

  @Output() onDelete = new EventEmitter();

  constructor() { }

  ngOnInit() {
  }

  delete() {
    this.onDelete.emit()
  }
}
