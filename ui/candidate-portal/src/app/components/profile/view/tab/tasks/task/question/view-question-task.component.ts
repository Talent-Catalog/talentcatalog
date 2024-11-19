import {Component, Input, OnInit} from '@angular/core';
import {UntypedFormGroup} from "@angular/forms";
import {TaskAssignment} from "../../../../../../../model/task-assignment";

@Component({
  selector: 'app-view-question-task',
  templateUrl: './view-question-task.component.html',
  styleUrls: ['./view-question-task.component.scss']
})
export class ViewQuestionTaskComponent implements OnInit {
  @Input() form: UntypedFormGroup;
  @Input() selectedTask: TaskAssignment;

  constructor() { }

  ngOnInit(): void {
  }

}
