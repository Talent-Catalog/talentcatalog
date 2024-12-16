/*
 * Copyright (c) 2024 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

import {Injectable} from '@angular/core';
import {RegistrationStep} from '../components/register/registration-step';
import {ActivatedRoute, Router} from '@angular/router';
import {Subscription} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class RegistrationService {

  private subscription: Subscription;

  public steps: RegistrationStep[] = [
    {
      key: 'account',
      title: 'Welcome to Talent Catalog!',
      section: 0
    },
    {
      key: 'contact',
      title: 'How can we contact you?',
      section: 1
    },
    {
      key: 'personal',
      title: 'Tell us about yourself',
      section: 2
    },
    {
      key: 'occupation',
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
      key: 'language',
      title: 'What languages do you speak?',
      section: 6
    },
    {
      key: 'exam',
      title: 'Provide details of your language exams',
      section: 7
    },
    {
      key: 'certifications',
      title: 'Do you have any other professional certifications?',
      section: 8
    },
    {
      key: 'destinations',
      title: 'Do you have any destination preferences?',
      section: 8
    },
    {
      key: 'additional',
      title: 'How did you hear about us?',
      section: 9
    },
    {
      key: 'upload',
      title: 'Do you have any files to upload?',
      section: 10
    },
    {
      key: 'complete',
      title: '',
      hideHeader: true,
      section: 10
    }
  ];
  public totalSections: number = Math.max(...this.steps.map(s => s.section));
  public currentStepKey: string;
  public currentStep: RegistrationStep;
  public currentStepIndex: number;
  registering: boolean = false;

  constructor(private router: Router,
              private route: ActivatedRoute) { }

  // Observe the query params in the url to determine which step to display
  start() {
    // Set step back to 0 before starting a new registration.
    this.currentStepIndex = 0;
    this.currentStep = this.steps[this.currentStepIndex];
    this.currentStepKey = this.currentStep.key;
    if (!this.subscription || this.subscription.closed) {
      this.registering = true;
      this.subscription = this.route.queryParams.subscribe(
        params => {
          if (params['step']) {
            this.openStep(params['step']);
          } else {
            this.routeToStep('landing');
          }
        }
      );
    }
  }

  // Stop observing the url changes
  stop() {
    if (this.subscription) {
      this.subscription.unsubscribe();
      this.registering = false;
    }
  }

  openStep(stepKey: string) {
    stepKey = stepKey || 'landing';
    this.currentStepIndex = this.steps.findIndex(step => step.key === stepKey);
    this.setStep();
  }

  back() {
    if (!this.registering) {return;}
    this.currentStepIndex--;
    this.setStep();
  }

  next() {
    if (!this.registering) {return;}
    this.currentStepIndex++;
    this.setStep();
  }

  setStep() {
    if (this.currentStepIndex < 1 || this.currentStepIndex > this.steps.length - 1) {
      this.currentStepIndex = 0;
    }
    this.currentStep = this.steps[this.currentStepIndex];
    this.currentStepKey = this.currentStep.key;
    this.routeToStep(this.currentStepKey);
  }

  routeToStep(key: string) {
    this.router.navigate([], {queryParams: {step: key}, queryParamsHandling: "merge"});
  }
}
