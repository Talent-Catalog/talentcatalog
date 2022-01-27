import {Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {Task, UploadType} from "../../../model/candidate";

@Component({
  selector: 'app-view-task-details',
  templateUrl: './view-task-details.component.html',
  styleUrls: ['./view-task-details.component.scss']
})
export class ViewTaskDetailsComponent implements OnInit, OnChanges {
  @Input() task: Task;
  uploadTypeString: string;

  constructor() { }

  ngOnInit(): void {
    this.uploadTypeString = UploadType[this.task.uploadType];
  }

  ngOnChanges(changes: SimpleChanges) {
    this.uploadTypeString = UploadType[this.task.uploadType];
  }


}
