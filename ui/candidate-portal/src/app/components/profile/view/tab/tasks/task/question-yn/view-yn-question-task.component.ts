import {Component, Input, OnInit} from '@angular/core';
import {FormGroup} from "@angular/forms";

@Component({
  selector: 'app-view-yn-question-task',
  templateUrl: './view-yn-question-task.component.html',
  styleUrls: ['./view-yn-question-task.component.scss']
})
export class ViewYnQuestionTaskComponent implements OnInit {
  @Input() form: FormGroup;

  constructor() { }

  ngOnInit(): void {
  }

}
