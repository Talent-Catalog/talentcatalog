import {Component, Input, OnInit} from '@angular/core';
import {FormGroup} from "@angular/forms";

@Component({
  selector: 'app-view-simple-task',
  templateUrl: './view-simple-task.component.html',
  styleUrls: ['./view-simple-task.component.scss']
})
export class ViewSimpleTaskComponent implements OnInit {
  @Input() form: FormGroup;

  constructor() { }

  ngOnInit(): void {
  }

}
