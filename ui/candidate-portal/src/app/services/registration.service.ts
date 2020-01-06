import {Injectable} from '@angular/core';
import {RegistrationStep} from "../components/register/registration-step";
import {ActivatedRoute, Router} from "@angular/router";
import {Subscription} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class RegistrationService {

  private subscription: Subscription;

  public steps: RegistrationStep[] = [
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
      key: 'certifications',
      title: 'Do you have any other professional certifications?',
      section: 7
    },
    {
      key: 'submit',
      title: 'Are you ready to submit your application?',
      section: 8
    },
    {
      key: 'complete',
      title: '',
      hideHeader: true,
      section: 8
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
    if (!this.subscription || this.subscription.closed) {
      this.registering = true;
      this.subscription = this.route.queryParams.subscribe(
        params => {
          if (params['step']) {
            this.openStep(params['step'])
          } else {
            this.routeToStep('landing')
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
