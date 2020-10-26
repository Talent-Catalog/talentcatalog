import {Component, Input, OnInit} from '@angular/core';

@Component({
  selector: 'app-fixed-input',
  templateUrl: './fixed-input.component.html',
  styleUrls: ['./fixed-input.component.scss']
})
export class FixedInputComponent implements OnInit {
  @Input() question: string;
  @Input() answer: string;

  constructor() { }

  ngOnInit(): void {
  }

}
