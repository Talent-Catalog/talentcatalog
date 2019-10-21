import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {CandidateJobExperience} from "../../../model/candidate-job-experience";

@Component({
  selector: 'app-candidate-work-experience-card',
  templateUrl: './candidate-job-experience-card.component.html',
  styleUrls: ['./candidate-job-experience-card.component.scss']
})
export class CandidateJobExperienceCardComponent implements OnInit {

  @Input() editing: boolean = false;
  @Input() saving: boolean = false;
  @Input() experience: CandidateJobExperience;

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
