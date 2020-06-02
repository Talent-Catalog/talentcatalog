import {Component, OnInit} from '@angular/core';
import {CandidateAttachment} from "../../../../../model/candidate-attachment";
import {FormBuilder, FormControl, FormGroup, Validators} from "@angular/forms";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {CandidateAttachmentService} from "../../../../../services/candidate-attachment.service";

@Component({
  selector: 'app-edit-candidate-attachment',
  templateUrl: './edit-candidate-attachment.component.html',
  styleUrls: ['./edit-candidate-attachment.component.scss']
})
export class EditCandidateAttachmentComponent implements OnInit {

  loading: boolean;
  error: any;

  // Set in the parent component, by referencing the comoponent instance
  attachment: CandidateAttachment;
  cv: boolean;
  form: FormGroup;

  constructor(private fb: FormBuilder,
              private modal: NgbActiveModal,
              private candidateAttachmentService: CandidateAttachmentService) { }

  ngOnInit() {
    this.form = this.fb.group({
      id: [this.attachment.id],
      name: [this.attachment.name, Validators.required],
      cv: [this.cv]
    });
    if (this.attachment.type === 'link') {
      this.form.addControl('location', new FormControl(this.attachment.location, [Validators.required]));
    }
  }

  save() {
    this.candidateAttachmentService.updateAttachment(this.form.value).subscribe(
      (response) => {
        this.modal.close(response);
      },
      (error) => {
        console.log('error', error);
      });
  }

  close() {
    this.modal.close();
  }
}
