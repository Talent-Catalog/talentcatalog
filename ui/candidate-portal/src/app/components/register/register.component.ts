import {Component, OnDestroy, OnInit} from '@angular/core';
import {RegistrationService} from "../../services/registration.service";

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.scss']
})
export class RegisterComponent implements OnInit, OnDestroy {

  constructor(public registrationService: RegistrationService) { }

  ngOnInit() {
    this.registrationService.start();
  }

  ngOnDestroy(): void {
    this.registrationService.stop();
  }

}

