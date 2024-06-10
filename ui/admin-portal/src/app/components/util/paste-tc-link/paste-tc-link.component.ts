import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup} from "@angular/forms";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";

@Component({
  selector: 'app-paste-tc-link',
  templateUrl: './paste-tc-link.component.html',
  styleUrls: ['./paste-tc-link.component.scss']
})
export class PasteTcLinkComponent implements OnInit {

  cancel: string = "Cancel";
  form: FormGroup;
  label: string = "Label"
  select: string = "Select";
  tcLinkRegExp: RegExp;

  constructor(
    private activeModal: NgbActiveModal,
    private fb: FormBuilder) { }

  ngOnInit(): void {
    this.form = this.fb.group({
      tcLink: [],
    });
    const tcUrlPattern: string =
      'https://www.tctalent.org/admin-portal/list/' +
      '\d+';
    this.tcLinkRegExp = new RegExp(tcUrlPattern);
  }

  onCancel() {
    this.activeModal.dismiss();
  }

  onSelect() {
    this.activeModal.close(this.form.value.tcLink);
  }
}
