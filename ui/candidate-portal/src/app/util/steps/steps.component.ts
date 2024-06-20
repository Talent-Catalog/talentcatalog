import {Component, OnInit} from '@angular/core';
import {Observable} from "rxjs";
import {RegistrationStep} from "../../components/register/registration-step";
import {IntakeService} from "../../services/intake.service";

@Component({
  selector: 'app-steps',
  templateUrl: './steps.component.html',
  styleUrls: ['./steps.component.scss']
})
export class StepsComponent implements OnInit {

  steps: Observable<RegistrationStep[]>;
  currentStep: Observable<RegistrationStep>;

  constructor(private intakeService: IntakeService) { }

  ngOnInit(): void {
    this.steps = this.intakeService.getSteps();
    this.currentStep = this.intakeService.getCurrentStep();
  }

  onStepClick(step: RegistrationStep) {
    this.intakeService.setCurrentStep(step);
  }

}
