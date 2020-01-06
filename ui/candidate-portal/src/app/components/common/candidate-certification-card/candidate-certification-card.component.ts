import {Component, EventEmitter, Input, Output} from '@angular/core';
import {CandidateCertification} from "../../../model/candidate-certification";

@Component({
  selector: 'app-candidate-certification-card',
  templateUrl: './candidate-certification-card.component.html',
  styleUrls: ['./candidate-certification-card.component.scss']
})
export class CandidateCertificationCardComponent {

  @Input() certificate: CandidateCertification;
  @Input() disabled: boolean = false;
  @Input() preview: boolean = false;

  @Output() onDelete = new EventEmitter<CandidateCertification>();

  constructor() { }

  deleteCertificate() {
    this.onDelete.emit(this.certificate);
  }
}
