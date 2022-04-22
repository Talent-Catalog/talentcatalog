import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormControl, FormGroup, Validators} from "@angular/forms";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {CreateTaskRequest, TaskService} from "../../../../services/task.service";
import {Task, TaskType} from "../../../../model/task";
import {EnumOption, enumOptions} from "../../../../util/enum";

@Component({
  selector: 'app-create-task',
  templateUrl: './create-task.component.html',
  styleUrls: ['./create-task.component.scss']
})
export class CreateTaskComponent implements OnInit {

  taskId: number;
  taskForm: FormGroup;
  error;
  loading: boolean;
  saving: boolean;
  taskTypes: EnumOption[] = enumOptions(TaskType);

  constructor(private activeModal: NgbActiveModal,
              private fb: FormBuilder,
              private taskService: TaskService) {
  }

  ngOnInit() {
    this.loading = true;
    this.taskForm = this.fb.group({
      taskType: [null, Validators.required],
      name: [null, Validators.required],
      displayName: [null, Validators.required],
      description: [null, Validators.required],
      daysToComplete: [null, Validators.required],
      optional: [null, Validators.required],
      helpLink: [null],
      // Fields related to Upload Tasks
      uploadType: [null],
      uploadSubfolderName: [null],
      uploadableFileTypes: [null],
      // Fields related to Question Tasks
      candidateAnswerField: [null],
      allowedAnswers: [null],
    });
    this.loading = false;

    this.taskForm.controls['taskType']?.valueChanges.subscribe((type) => {
      if (type == TaskType.Question) {
        this.taskForm.addControl('candidateAnswerField', new FormControl(null, [Validators.required]));
        this.taskForm.addControl('allowedAnswers', new FormControl(null, [Validators.required]));
      } else if (type == TaskType.Upload){
        this.taskForm.addControl('uploadType', new FormControl(null, [Validators.required]));
        this.taskForm.addControl('uploadSubfolderName', new FormControl(null, [Validators.required]));
        this.taskForm.addControl('uploadableFileTypes', new FormControl(null));
      }
    })
  }

  get taskType() {
    return this.taskForm.value.taskType;
  }

  onSave() {
    this.saving = true;
    const request: CreateTaskRequest = {
      taskType: null,
      name: null,
      displayName: null,
      description: null,
      daysToComplete: null,
      optional: null,
      helpLink: null,
    }
    this.taskService.update(this.taskId, request).subscribe(
      (task) => {
        this.closeModal(task);
        this.saving = false;
      },
      (error) => {
        this.error = error;
        this.saving = false;
      });
  }

  closeModal(task: Task) {
    this.activeModal.close(task);
  }

  dismiss() {
    this.activeModal.dismiss(false);
  }

}
