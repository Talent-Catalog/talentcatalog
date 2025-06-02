import {Component, OnInit} from '@angular/core';
import {Candidate, CandidateStatus, SubmitRegistrationRequest} from "../../../model/candidate";
import {CandidateService} from "../../../services/candidate.service";
import {AuthenticationService} from "../../../services/authentication.service";
import {UntypedFormBuilder, UntypedFormGroup} from "@angular/forms";
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

  candidate: Candidate;
  currentPrivacyPolicy: TermsInfoDto;

  form: UntypedFormGroup;

  constructor(private builder: UntypedFormBuilder,
              private candidateService: CandidateService,
              private authenticationService: AuthenticationService,
              private termsInfoService: TermsInfoService) {
  }

  ngOnInit(): void {
    this.form = this.builder.group({
      contactConsentRegistration: [false],
      contactConsentPartners: [false],
      acceptPolicy: [false]
    });

    //Fetch the current candidate privacy policy
    this.termsInfoService.getCurrentByType(TermsType.CANDIDATE_PRIVACY_POLICY).subscribe(
      {
        next: termsInfo => this.currentPrivacyPolicy = termsInfo,
        error: err => this.error = err
      }
    )

    //Grab info on candidate
    this.candidateService.getCandidatePersonal().subscribe(
      {
        next: candidate => this.candidate = candidate,
        error: err => this.error = err
      }
    )
  }

  get acceptedPolicy(): boolean {
    return this.form.get('acceptPolicy').value;
  }

  //Final registration step method
  submit() {
    this.loading = true;
    this.error = null;

    let request: SubmitRegistrationRequest = {
      acceptedPrivacyPolicyId: this.acceptedPolicy ? this.currentPrivacyPolicy.id : null,
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
     this.form.get('acceptPolicy').setValue(accepted);
  }

  protected readonly TermsType = TermsType;

  /**
   * Displays partner as string.
   * @param full if true, displays full details, otherwise just displays name
   */
  getPartnerDescription(full: boolean): string {
    let description = null;
    const partner = this.candidate?.user?.partner;
    if (partner) {
      description = partner.name;
      if (full) {
        if (partner.websiteUrl) {
          description += " (" + partner.websiteUrl + ")";
        }
      }
    }
    return description;
  }
}
