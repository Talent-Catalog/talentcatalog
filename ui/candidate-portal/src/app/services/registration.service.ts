/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

import {Injectable} from '@angular/core';
import {RegistrationStep} from '../components/register/registration-step';
import {ActivatedRoute, Router} from '@angular/router';
import {Subscription} from 'rxjs';
import {AuthenticationService} from './authentication.service';
import {isLocalOrStaging, isVerifyPlusUiEnabled} from '../util/verify-plus-ui';

@Injectable({
  providedIn: 'root'
})
export class RegistrationService {

  private static readonly VERIFY_PLUS_STEP_KEY = 'verifyplus';

  private subscription: Subscription;

  private readonly allSteps: RegistrationStep[] = [
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
      key: 'verifyplus',
      // Header title is sourced from REGISTRATION.HEADER.TITLE.VERIFYPLUS translations.
      title: '',
      section: 2
    },
    {
      key: 'personal',
      title: 'Tell us about yourself',
      section: 3
    },
    {
      key: 'occupation',
      title: 'Tell us about your occupation',
      section: 4
    },
    {
      key: 'experience',
      title: 'Tell us about your working history',
      section: 5
    },
    {
      key: 'education',
      title: 'Tell us about your education',
      section: 6
    },
    {
      key: 'language',
      title: 'What languages do you speak?',
      section: 7
    },
    {
      key: 'exam',
      title: 'Provide details of your language exams',
      section: 8
    },
    {
      key: 'certifications',
      title: 'Do you have any other professional certifications?',
      section: 9
    },
    {
      key: 'destinations',
      title: 'Do you have any destination preferences?',
      section: 9
    },
    {
      key: 'additional',
      title: 'How did you hear about us?',
      section: 10
    },
    {
      key: 'upload',
      title: 'Do you have any files to upload?',
      section: 11
    },
    {
      key: 'complete',
      title: '',
      hideHeader: true,
      section: 11
    }
  ];
  public steps: RegistrationStep[] = [];
  public totalSections: number = 0;
  public currentStepKey: string;
  public currentStep: RegistrationStep;
  public currentStepIndex: number;
  registering: boolean = false;

  constructor(private router: Router,
              private route: ActivatedRoute,
              private authenticationService: AuthenticationService) {
    this.syncSteps();
  }

  // Observe the query params in the url to determine which step to display
  start() {
    this.syncSteps();
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
    this.syncSteps();
    stepKey = stepKey || 'landing';
    this.currentStepIndex = this.steps.findIndex(step => step.key === stepKey);
    this.setStep();
  }

  back() {
    if (!this.registering) {return;}
    this.syncSteps();
    this.currentStepIndex--;
    this.setStep();
  }

  next() {
    if (!this.registering) {return;}
    this.syncSteps();
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

  private syncSteps() {
    const previousStepKey = this.currentStepKey;
    this.steps = this.buildSteps();
    this.totalSections = Math.max(...this.steps.map(s => s.section));

    if (previousStepKey == null) {
      return;
    }

    const nextIndex = this.steps.findIndex(step => step.key === previousStepKey);
    if (nextIndex >= 0) {
      this.currentStepIndex = nextIndex;
      return;
    }

    if (this.currentStepIndex != null && this.currentStepIndex >= this.steps.length) {
      this.currentStepIndex = this.steps.length - 1;
    }
  }

  private buildSteps(): RegistrationStep[] {
    return this.allSteps
      .filter(step => {
        if (step.key !== RegistrationService.VERIFY_PLUS_STEP_KEY) {
          return true;
        }
        return this.shouldIncludeVerifyPlusStep();
      })
      .map(step => ({...step}));
  }

  private shouldIncludeVerifyPlusStep(): boolean {
    if (!isLocalOrStaging()) {
      return false;
    }

    if (!this.authenticationService.isAuthenticated()) {
      return true;
    }

    return isVerifyPlusUiEnabled(this.authenticationService.isGrnInstance());
  }
}
