import {Component, Input} from '@angular/core';
import {TaskAssignment} from "../../../../../../../model/task-assignment";

@Component({
  selector: 'app-view-form-task',
  templateUrl: './view-form-task.component.html',
  styleUrls: ['./view-form-task.component.scss']
})
export class ViewFormTaskComponent {
  @Input() selectedTask: TaskAssignment;

}
