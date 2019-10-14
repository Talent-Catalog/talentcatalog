import {Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {Candidate} from "../../../../../model/candidate";
import {Occupation} from "../../../../../model/occupation";
import {CandidateJobExperience} from "../../../../../model/candidate-job-experience";
import {CandidateOccupation} from "../../../../../model/candidate-occupation";

@Component({
  selector: 'app-view-candidate-experience',
  templateUrl: './view-candidate-experience.component.html',
  styleUrls: ['./view-candidate-experience.component.scss']
})
export class ViewCandidateExperienceComponent implements OnInit {

  @Input() candidate: Candidate;
  @Input() occupation: Occupation;
  @Input() editable: boolean;

  constructor() { }

  ngOnInit() {

  }

}
