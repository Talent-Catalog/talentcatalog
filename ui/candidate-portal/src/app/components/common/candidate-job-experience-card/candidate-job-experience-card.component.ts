import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {CandidateJobExperience} from "../../../model/candidate-job-experience";

@Component({
  selector: 'app-candidate-job-experience-card',
  templateUrl: './candidate-job-experience-card.component.html',
  styleUrls: ['./candidate-job-experience-card.component.scss']
})
export class CandidateJobExperienceCardComponent implements OnInit {

  @Input() preview: boolean = false;
  @Input() experience: CandidateJobExperience;
  @Input() disabled: boolean;

  @Output() onEdit = new EventEmitter<CandidateJobExperience>();
  @Output() onDelete = new EventEmitter<CandidateJobExperience>();

  constructor() { }

  ngOnInit() {

  }

  edit() {
    this.onEdit.emit(this.experience);
  }

  delete() {
    this.onDelete.emit(this.experience);
  }

}
