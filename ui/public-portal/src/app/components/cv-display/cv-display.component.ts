import {Component, Input, OnInit} from '@angular/core';
import {Candidate} from "../../model/candidate";

@Component({
  selector: 'app-cv-display',
  templateUrl: './cv-display.component.html',
  styleUrls: ['./cv-display.component.scss']
})
export class CvDisplayComponent implements OnInit {

  @Input() candidate: Candidate;

  constructor() { }

  ngOnInit(): void {
  }

  print() {
    window.print();
  }

}
