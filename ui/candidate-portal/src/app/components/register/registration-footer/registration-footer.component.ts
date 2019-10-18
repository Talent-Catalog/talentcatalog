import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {RegistrationService} from "../../../services/registration.service";

@Component({
  selector: 'app-registration-footer',
  templateUrl: './registration-footer.component.html',
  styleUrls: ['./registration-footer.component.scss']
})
export class RegistrationFooterComponent implements OnInit {

  @Input() disabled: boolean;
  @Input() hideBack: boolean;
  @Input() hideNext: boolean;

  @Output() backClicked = new EventEmitter();
  @Output() nextClicked = new EventEmitter();

  constructor(public registrationService: RegistrationService) { }

  ngOnInit() {

  }

  back() {
    this.backClicked.emit();
  }

  next() {
    this.nextClicked.emit();
  }

}
