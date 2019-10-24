import {Component, EventEmitter, Input, Output} from '@angular/core';
import {RegistrationService} from "../../../services/registration.service";

@Component({
  selector: 'app-registration-footer',
  templateUrl: './registration-footer.component.html',
  styleUrls: ['./registration-footer.component.scss']
})
export class RegistrationFooterComponent {

  @Input() nextDisabled: boolean = false;
  @Input() backDisabled: boolean = false;
  @Input() hideBack: boolean = false;
  @Input() hideNext: boolean = false;
  @Input() type: 'step' | 'submit' | 'update' = 'step';

  @Output() backClicked = new EventEmitter();
  @Output() nextClicked = new EventEmitter();

  constructor(public registrationService: RegistrationService) { }

  back() {
    this.backClicked.emit();
  }

  next() {
    this.nextClicked.emit();
  }

}
