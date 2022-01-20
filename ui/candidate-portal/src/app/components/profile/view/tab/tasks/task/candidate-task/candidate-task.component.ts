import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {TaskAssignment} from "../../../../../../../model/candidate";
import {FormBuilder, FormGroup} from "@angular/forms";

@Component({
  selector: 'app-candidate-task',
  templateUrl: './candidate-task.component.html',
  styleUrls: ['./candidate-task.component.scss']
})
export class CandidateTaskComponent implements OnInit {
  @Input() selectedTask: TaskAssignment;
  @Output() back = new EventEmitter();
  form: FormGroup;
  loading: boolean;
  uploading: boolean;
  error;

  constructor(private fb: FormBuilder) { }

  ngOnInit(): void {
    this.form = this.fb.group({
      comment: [''],
    });
  }

  startServerUpload($event) {
    console.log('uploading');
  }

  goBack() {
    this.selectedTask = null;
    this.back.emit();
  }

  isOverdue(ta: TaskAssignment) {
    return (new Date(ta.dueDate) < new Date()) && !ta.task.optional;
  }

}
