import {Component, OnInit} from '@angular/core';
import {ActivatedRoute} from "@angular/router";
import {RegistrationStep} from "./registration-step";

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.scss']
})
export class RegisterComponent implements OnInit {

  loading: boolean;
  error: any;

  steps: RegistrationStep[] = [
    {
      key: 'landing',
      title: '',
      section: 0,
      hideHeader: true
    },
    {
      key: 'contact',
      title: 'How can we contact you?',
      section: 1
    },
    {
      key: 'contact/alternate',
      title: 'How can we contact you?',
      section: 1
    },
    {
      key: 'contact/additional',
      title: 'How can we contact you?',
      section: 1
    },
    {
      key: 'personal',
      title: 'Tell us about yourself',
      section: 2
    },
    {
      key: 'location',
      title: 'Tell us about yourself',
      section: 2
    },
    {
      key: 'nationality',
      title: 'Tell us about yourself',
      section: 2
    },
    {
      key: 'candidateOccupation',
      title: 'Tell us about your occupation',
      section: 3
    },
    {
      key: 'experience',
      title: 'Tell us about your working history',
      section: 4
    },
    {
      key: 'education',
      title: 'Tell us about your education',
      section: 5
    },
    {
      key: 'education/masters',
      title: 'Tell us about your education',
      section: 5
    },
    {
      key: 'education/university',
      title: 'Tell us about your education',
      section: 5
    },
    {
      key: 'education/school',
      title: 'Tell us about your education',
      section: 5
    },
    {
      key: 'language',
      title: 'What languages do you speak?',
      section: 6
    },
    {
      key: 'certifications',
      title: 'Do you have any other professional certifications?',
      section: 7
    },
    {
      key: 'additional-information',
      title: 'Are you ready to submit your application?',
      section: 8
    }
  ];
  currentStepKey: string;
  currentStep: RegistrationStep;
  currentStepIndex: number;

  constructor(private route: ActivatedRoute) { }

  ngOnInit() {
    this.loading = true;
    this.route.queryParams.subscribe(
      (params) => {
       this.currentStepKey = params['step'] || 'landing';
       this.currentStep = this.steps.find(s => s.key === this.currentStepKey);
       this.currentStepIndex = this.steps.findIndex(s => s.key === this.currentStepKey);
       this.loading = false;
      },
      (error) => {
        console.log('error', error);
      });
  }

  back() {
    this.currentStepIndex--;
    this.currentStep = this.steps[this.currentStepIndex];
    this.currentStepKey = this.currentStep.key;
  }

  next() {
    this.currentStepIndex++;
    this.currentStep = this.steps[this.currentStepIndex];
    this.currentStepKey = this.currentStep.key;
  }
}

