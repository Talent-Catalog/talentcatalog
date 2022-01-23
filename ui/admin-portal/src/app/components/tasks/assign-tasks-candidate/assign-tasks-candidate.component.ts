import {Component, OnInit} from '@angular/core';
import {NgbActiveModal, NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {FormBuilder, FormGroup} from "@angular/forms";
import {Task} from "../../../model/candidate";

@Component({
  selector: 'app-assign-tasks-candidate',
  templateUrl: './assign-tasks-candidate.component.html',
  styleUrls: ['./assign-tasks-candidate.component.scss']
})
export class AssignTasksCandidateComponent implements OnInit {
  assignForm: FormGroup;
  allTasks: Task[];
  loading;
  error;

  constructor(private activeModal: NgbActiveModal,
              private fb: FormBuilder,
              private modalService: NgbModal) { }

  ngOnInit(): void {
    this.loading = true;
    this.assignForm = this.fb.group({
      task: [null],
      dueDate: [null]
    });
    this.getAllTasks();
  }

  getAllTasks() {
    this.allTasks = [];
    // todo a service call to fetch all available tasks, maybe async for search just a test sample provided
    for (let i = 0; i < 11; i++) {
      const task: Task = {name: 'Task ' + i, optional: false}
      this.allTasks.push(task);
    }
    this.loading = false;
    return this.allTasks
  }

  onSave() {
    // todo save the task and assign to the candidate returning the task assignment
    this.activeModal.close();
  }

  cancel() {
    this.activeModal.dismiss();
  }

}
