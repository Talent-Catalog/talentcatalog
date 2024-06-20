import {Component, Input, OnInit} from '@angular/core';
import {RegistrationStep} from "../../components/register/registration-step";

@Component({
  selector: 'app-step-template',
  templateUrl: './step-template.component.html',
  styleUrls: ['./step-template.component.scss']
})
export class StepTemplateComponent implements OnInit {

  @Input() step: RegistrationStep;

  constructor() { }

  ngOnInit(): void {
  }

  onCompleteStep() {
    this.step.isComplete = true;
  }

}
