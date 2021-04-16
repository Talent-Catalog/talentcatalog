import {
  Component,
  EventEmitter,
  Input,
  OnChanges,
  OnInit,
  Output,
  SimpleChanges
} from '@angular/core';
import {CandidateStatus, UpdateCandidateStatusInfo} from "../../../model/candidate";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {EnumOption, enumOptions} from "../../../util/enum";

@Component({
  selector: 'app-candidate-status-selector',
  templateUrl: './candidate-status-selector.component.html',
  styleUrls: ['./candidate-status-selector.component.scss']
})
export class CandidateStatusSelectorComponent implements OnInit, OnChanges {

  @Input() candidateStatus: CandidateStatus;
  @Output() statusInfoUpdate = new EventEmitter<UpdateCandidateStatusInfo>();

  candidateStatusInfoForm: FormGroup;
  candidateStatusOptions: EnumOption[] = enumOptions(CandidateStatus);

  constructor(private fb: FormBuilder) {
  }

  ngOnInit(): void {
    this.candidateStatusInfoForm = this.fb.group({
      status: [this.candidateStatus, Validators.required],
      comment: [null, Validators.required],
      candidateMessage: [null],
    });

    this.candidateStatusInfoForm.valueChanges.subscribe(() => this.onUpdate());
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.onUpdate();
  }

  get candidateMessage(): string {
    return this.candidateStatusInfoForm.value?.candidateMessage;
  }

  get comment(): string {
    return this.candidateStatusInfoForm.value?.comment;
  }

  get status(): CandidateStatus {
    return this.candidateStatusInfoForm.value?.status;
  }

  private onUpdate() {
    let info: UpdateCandidateStatusInfo;
    if (this.candidateStatusInfoForm) {
      //Create updated info event from the current form contents
      info = {
        candidateMessage: this.candidateMessage,
        comment: this.comment,
        status: this.status
      };
    } else {
      //Very first update will occur with initial status - before form has even been created.
      //See https://angular.io/guide/lifecycle-hooks
      //Create an event just containing the input status
      info = {
        status: this.candidateStatus
      }
    }
    this.statusInfoUpdate.emit(info);
  }

}
