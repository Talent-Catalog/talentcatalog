import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {FormBuilder, FormControl, FormGroup, Validators} from "@angular/forms";
import {AuthService} from "../../../services/auth.service";
import {ActivatedRoute} from "@angular/router";
import {BrandingService} from "../../../services/branding.service";
import {CandidateService, UpdateCandidateSurvey} from "../../../services/candidate.service";
import {AuthenticationService} from "../../../services/authentication.service";
import {RegistrationService} from "../../../services/registration.service";
import {LanguageService} from "../../../services/language.service";
import {RegisterCandidateRequest} from "../../../model/candidate";
import {US_AFGHAN_SURVEY_TYPE} from "../../../model/survey-type";

@Component({
  selector: 'app-registration-create-account',
  templateUrl: './registration-create-account.component.html',
  styleUrls: ['./registration-create-account.component.scss']
})
export class RegistrationCreateAccountComponent implements OnInit {

  /* A flag to indicate if the component is being used on the profile component */
  @Input() edit: boolean = false;

  @Output() onSave = new EventEmitter();

  registrationForm: FormGroup;
  error: any;

  // Form states
  loading: boolean;
  saving: boolean;

  authenticated: boolean;

  usAfghan: boolean;
  partnerName: string;

  constructor(private builder: FormBuilder,
              private route: ActivatedRoute,
              private brandingService: BrandingService,
              private candidateService: CandidateService,
              private authService: AuthService,
              private authenticationService: AuthenticationService,
              private registrationService: RegistrationService,
              private languageService: LanguageService) { }

  ngOnInit(): void {
    this.authenticated = false;
    this.loading = true;

    this.registrationForm = this.builder.group({
      username: ['', Validators.required],
      password: ['', Validators.required],
      passwordConfirmation: ['', Validators.required]
    });

    if (this.authenticationService.isAuthenticated()) {
      // Skip this step if logged in
      this.authenticated = true;
      this.registrationService.next();

    } else {

      //If we are not yet authenticated, look for us-afghan query parameter.
      //(if we are authenticated we pick up US Afghan tagging from the survey type)

      //Record if this is a US Afghan candidate - can this processing be removed? (See issue #527)
      this.usAfghan = this.route.snapshot.queryParams['source'] === 'us-afghan';
      this.languageService.setUsAfghan(this.usAfghan);

      // Get the partner name from the branding info object.
      this.brandingService.getBrandingInfo().subscribe((brandingInfo) => this.partnerName = brandingInfo.partnerName)

      // The user has not registered - add the password fields to the reactive form
      this.registrationForm.addControl('password', new FormControl('', [Validators.required, Validators.minLength(8)]));
      this.registrationForm.addControl('passwordConfirmation', new FormControl('', [Validators.required, Validators.minLength(8)]));

      // The user has not registered - add the email consent fields
      this.registrationForm.addControl('contactConsentRegistration', new FormControl(false, [Validators.requiredTrue]));
      this.registrationForm.addControl('contactConsentPartners', new FormControl(false));

      this.loading = false;
    }
  }

  get username(): string {
    return this.registrationForm.value.username;
  }

  get password(): string {
    return this.registrationForm.value.password;
  }

  get passwordConfirmation(): string {
    return this.registrationForm.value.passwordConfirmation;
  }

  getPartnerName(): string {
    return this.partnerName;
  }

  register() {
    this.saving = true;
    this.error = null;

    // The user has not yet registered - create an account for them
    this.getParamsAndRegister();
  }

  private getParamsAndRegister() {
    const params = this.route.snapshot.queryParamMap;

    // Check for the partner query param and use it to configure the branding service.
    const req: RegisterCandidateRequest = new RegisterCandidateRequest();
    req.username = this.username;

    // See Issue #526 which has been opened for clarification of the long term use of username /
    // email / alternative email
    req.email = this.username;

    req.password = this.password;
    req.passwordConfirmation = this.passwordConfirmation;
    req.partnerAbbreviation = this.brandingService.partnerAbbreviation;

    //Populate query params
    if (params.has('r')) {
      req.referrerParam = params.get('r');
    }
    if (params.has('utm_source')) {
      req.utmSource = params.get('utm_source');
    }
    if (params.has('utm_campaign')) {
      req.utmCampaign = params.get('utm_campaign');
    }
    if (params.has('utm_medium')) {
      req.utmMedium = params.get('utm_medium');
    }
    if (params.has('utm_content')) {
      req.utmContent = params.get('utm_content');
    }
    if (params.has('utm_term')) {
      req.utmContent = params.get('utm_term');
    }

    //Populate contact consent
    req.contactConsentRegistration = this.registrationForm.value.contactConsentRegistration;
    req.contactConsentPartners = this.registrationForm.value.contactConsentPartners;

    this.authService.register(req).subscribe(
      (response) => {
        // If successfully registered, check if US-Afghan and if so update the survey.
        if (this.usAfghan) {
          // Set special value of candidate survey type indicating US Afghan
          const request: UpdateCandidateSurvey = {
            surveyTypeId: US_AFGHAN_SURVEY_TYPE,
          }
          this.candidateService.updateCandidateSurvey(request).subscribe(
            (res) => {
              this.saving = false;
            }, (error) => {
              this.error = error;
              this.saving = false;
            }
          )
        }
        this.registrationService.next();
      },
      (error) => {
        this.error = error;
        this.saving = false;
      }
    );
  }
}
