import {Component, Input, OnInit} from '@angular/core';
import {UntypedFormGroup} from "@angular/forms";
import {TaskAssignment} from "../../../../../../../model/task-assignment";

@Component({
  selector: 'app-view-simple-task',
  templateUrl: './view-simple-task.component.html',
  styleUrls: ['./view-simple-task.component.scss']
})
export class ViewSimpleTaskComponent implements OnInit {
  @Input() form: UntypedFormGroup;
  @Input() selectedTask: TaskAssignment;
  hasDoc: boolean;

  constructor() { }

  ngOnInit(): void {
    this.hasDoc = this.selectedTask.task.helpLink != null;
  }

}
