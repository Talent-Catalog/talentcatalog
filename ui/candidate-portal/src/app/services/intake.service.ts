/*
 * Copyright (c) 2024 Talent Beyond Boundaries.
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
import {BehaviorSubject, Observable} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class IntakeService {

  public miniSteps: RegistrationStep[] = [
    {
      key: 'account',
      title: 'Welcome to Talent Catalog!',
      section: 1,
      isComplete: false
    },
    {
      key: 'intRecruitment',
      title: 'Interest in International Recruitment',
      section: 2,
      isComplete: false
    },
    {
      key: 'destinations',
      title: 'Destinations',
      section: 3,
      isComplete: false
    },
    {
      key: 'personalEligibility',
      title: 'Personal Status / Program Eligibility',
      section: 4,
      isComplete: false
    },
    {
      key: 'langExams',
      title: 'Language Exams',
      section: 5,
      isComplete: false
    },
    {
      key: 'langAssessment',
      title: 'Language Assessment',
      section: 6,
      isComplete: false
    },
    {
      key: 'registration',
      title: 'Registration',
      section: 7,
      isComplete: false
    },
    {
      key: 'complete',
      title: '',
      hideHeader: true,
      section: 8,
      isComplete: false
    }
  ];
  steps$: BehaviorSubject<RegistrationStep[]> = new BehaviorSubject<RegistrationStep[]>(this.miniSteps);
  currentStep$: BehaviorSubject<RegistrationStep> = new BehaviorSubject<RegistrationStep>(null);

  constructor() {
    this.currentStep$.next(this.steps$.value[0]);
  }

  setCurrentStep(step: RegistrationStep): void {
    this.currentStep$.next(step);
  }

  getCurrentStep(): Observable<RegistrationStep> {
    return this.currentStep$.asObservable();
  }

  getSteps(): Observable<RegistrationStep[]> {
    return this.steps$.asObservable();
  }

  moveToNextStep(): void {
    const index = this.currentStep$.value.section;

    if (index < this.steps$.value.length) {
      this.currentStep$.next(this.steps$.value[index]);
    }
  }

  isLastStep(): boolean {
    return this.currentStep$.value.section === this.steps$.value.length;
  }
}
