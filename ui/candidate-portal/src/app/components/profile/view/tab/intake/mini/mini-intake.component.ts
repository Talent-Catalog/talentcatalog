import {Component, Input, OnInit} from '@angular/core';
import {Candidate} from "../../../../../../model/candidate";

@Component({
  selector: 'app-mini-intake',
  templateUrl: './mini-intake.component.html',
  styleUrls: ['./mini-intake.component.scss']
})
export class MiniIntakeComponent implements OnInit {
  @Input() candidate: Candidate;
  constructor() { }

  ngOnInit(): void {
  }

}
