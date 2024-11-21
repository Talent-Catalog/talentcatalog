import {Component, OnInit} from '@angular/core';
import {UntypedFormBuilder, UntypedFormGroup} from "@angular/forms";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";

export interface UpdateLinkRequest {
  name?: string,
  url?: string
}

@Component({
  selector: 'app-input-link',
  templateUrl: './input-link.component.html',
  styleUrls: ['./input-link.component.scss']
})
export class InputLinkComponent implements OnInit {
  form: UntypedFormGroup;
  initialValue: UpdateLinkRequest;
  instructions: string;
  showCancel: boolean = true;
  title: string = "Input Text";

  constructor(
    private activeModal: NgbActiveModal,
    private fb: UntypedFormBuilder) { }

  ngOnInit(): void {
    this.form = this.fb.group({
      name: [this.initialValue?.name],
      url: [this.initialValue?.url],
    });
  }

  dismiss() {
    this.activeModal.dismiss(false);
  }

  close() {
    const val: UpdateLinkRequest = {
      name: this.form.value.name,
      url: this.form.value.url
    }
    this.activeModal.close(val);
  }

}
