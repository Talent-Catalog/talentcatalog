import {Component, OnInit} from '@angular/core';
import {CandidateStatus, SubmitRegistrationRequest} from "../../../model/candidate";
import {CandidateService} from "../../../services/candidate.service";
import {AuthenticationService} from "../../../services/authentication.service";
import {UntypedFormBuilder, UntypedFormGroup, Validators} from "@angular/forms";
import {TermsInfoDto, TermsType} from "../../../model/terms-info-dto";
import {TermsInfoService} from "../../../services/terms-info.service";

@Component({
  selector: 'app-registration-submit',
  templateUrl: './registration-submit.component.html',
  styleUrls: ['./registration-submit.component.scss']
})
export class RegistrationSubmitComponent implements OnInit {
  error: any;
  loading: boolean;

  currentPrivacyPolicy: TermsInfoDto;

  form: UntypedFormGroup;

  constructor(private builder: UntypedFormBuilder,
              private candidateService: CandidateService,
              private authenticationService: AuthenticationService,
              private termsInfoService: TermsInfoService) {
  }

  ngOnInit(): void {
    this.form = this.builder.group({
      contactConsentRegistration: [false, Validators.required],
      contactConsentPartners: [false, Validators.required],
      acceptPolicy: [false, Validators.required]
    });

    //Fetch the current candidate privacy policy
    this.termsInfoService.getCurrentByType(TermsType.CANDIDATE_PRIVACY_POLICY).subscribe(
      {
        next: termsInfo => this.currentPrivacyPolicy = termsInfo,
        error: err => this.error = err
      }
    )
  }

  //Final registration step method
  submit() {
    this.loading = true;
    this.error = null;

    let acceptedPolicy: boolean = this.form.get('acceptPolicy').value;

    let request: SubmitRegistrationRequest = {
      acceptedPrivacyPolicyId: acceptedPolicy ? this.currentPrivacyPolicy.id : null,
      contactConsentPartners: this.form.get('contactConsentPartners').value,
      contactConsentRegistration: this.form.get('contactConsentRegistration').value
    }
    this.candidateService.submitRegistration(request).subscribe(
      (candidate) => {
        this.loading = false;
        //Successful registration changes candidate status
        this.authenticationService.setCandidateStatus(CandidateStatus[candidate.status]);
      },
      (error) => {
        this.error = error;
        this.loading = false;
      }
    );
  }

  setAcceptedTerms(accepted: boolean) {
     this.form.get('acceptPolicy').setValue(true);
  }

  protected readonly TermsType = TermsType;
}
