import {Component, Input, OnInit} from '@angular/core';
import {Candidate} from "../../../../../../model/candidate";
import {IntakeService} from "../../../../../../services/intake.service";
import {Observable} from "rxjs";
import {RegistrationStep} from "../../../../../register/registration-step";
import {Router} from "@angular/router";

@Component({
  selector: 'app-mini-intake',
  templateUrl: './mini-intake.component.html',
  styleUrls: ['./mini-intake.component.scss']
})
export class MiniIntakeComponent implements OnInit {
  @Input() candidate: Candidate;

  currentStep: Observable<RegistrationStep>;

  constructor(
    private intakeService: IntakeService,
    private router: Router) { }

  ngOnInit(): void {
    this.currentStep = this.intakeService.getCurrentStep();
  }

  onNextStep() {
    if (!this.intakeService.isLastStep()) {
      this.intakeService.moveToNextStep();
    } else {
      this.onSubmit();
    }
  }

  showButtonLabel() {
    return !this.intakeService.isLastStep() ? 'Continue' : 'Finish';
  }

  onSubmit(): void {
    this.router.navigate(['/complete']);
  }

}
