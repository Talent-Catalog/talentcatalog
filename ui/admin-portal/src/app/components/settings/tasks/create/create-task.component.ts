import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {CreateTaskRequest, TaskService} from "../../../../services/task.service";
import {CandidateSubfolderType, Task, TaskType} from "../../../../model/task";
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
  subFolderNames: EnumOption[] = enumOptions(CandidateSubfolderType);

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
      // Could I do this better by nesting other forms (e.g. a UploadTaskForm & QuestionTaskForm)
      // Fields related to Upload Tasks NEEDS VALIDATION
      uploadSubfolderName: [null],
      uploadableFileTypes: [null],
      // Fields related to Question Tasks NEEDS VALIDATION
      candidateAnswerField: [null],
      allowedAnswers: [null],
    });
    this.loading = false;

    // todo is there a better way to do this validation? Look into validation function?
    // this.taskForm.controls['taskType']?.valueChanges.subscribe((type) => {
    //   if (type == TaskType.Question) {
    //     this.taskForm.addControl('candidateAnswerField', new FormControl(null, [Validators.required]));
    //     this.taskForm.addControl('allowedAnswers', new FormControl(null, [Validators.required]));
    //   } else if (type == TaskType.Upload){
    //     this.taskForm.addControl('uploadSubfolderName', new FormControl(null, [Validators.required]));
    //     this.taskForm.addControl('uploadableFileTypes', new FormControl(null));
    //   }
    // })
  }

  get taskType() {
    return this.taskForm.value.taskType;
  }

  onSave() {
    this.saving = true;
    const request: CreateTaskRequest = {
      taskType: this.taskForm.value.taskType,
      name: this.taskForm.value.name,
      displayName: this.taskForm.value.displayName,
      description: this.taskForm.value.description,
      daysToComplete: this.taskForm.value.daysToComplete,
      optional: this.taskForm.value.optional,
      helpLink: this.taskForm.value.helpLink,
      uploadSubfolderName: this.taskForm.value.uploadSubfolderName ? this.taskForm.value.uploadSubfolderName : null,
      uploadableFileTypes: this.taskForm.value.uploadableFileTypes ? this.taskForm.value.uploadableFileTypes : null,
      candidateAnswerField: this.taskForm.value.candidateAnswerField ? this.taskForm.value.candidateAnswerField : null,
      allowedAnswers: this.taskForm.value.allowedAnswers ? this.taskForm.value.allowedAnswers : null,
    }
    this.taskService.create(request).subscribe(
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
