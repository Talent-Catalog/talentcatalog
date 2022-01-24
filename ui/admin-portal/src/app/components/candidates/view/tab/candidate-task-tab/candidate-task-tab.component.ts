import {Component, Input, OnInit} from '@angular/core';
import {Candidate} from "../../../../../model/candidate";

@Component({
  selector: 'app-candidate-task-tab',
  templateUrl: './candidate-task-tab.component.html',
  styleUrls: ['./candidate-task-tab.component.scss']
})
export class CandidateTaskTabComponent implements OnInit {

  @Input() candidate: Candidate;
  @Input() editable: boolean;

  constructor() { }

  ngOnInit(): void {
  }
}
