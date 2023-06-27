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
    console.log(this.candidate);
  }

  print() {
    window.print();
  }

  isHtml(text) {
    // Very simple test for HTML tags - isn't not foolproof but probably good enough
    return /<\/?[a-z][\s\S]*>/i.test(text);
  }
}
