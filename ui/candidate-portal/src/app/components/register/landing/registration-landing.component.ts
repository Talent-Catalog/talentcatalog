import {Component, OnInit} from '@angular/core';
import {RegistrationService} from '../../../services/registration.service';

@Component({
  selector: 'app-registration-landing',
  templateUrl: './registration-landing.component.html',
  styleUrls: ['./registration-landing.component.scss']
})

//todo this component is no longer used - can be removed. Replaced by squarespace website.
export class RegistrationLandingComponent implements OnInit {

  constructor(private registrationService: RegistrationService) { }

  ngOnInit() {
  }

  next() {
    this.registrationService.next();
  }
}
