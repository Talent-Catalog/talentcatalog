import {Component, OnInit} from '@angular/core';
import {CandidateStatus, SubmitRegistrationRequest} from "../../../model/candidate";
import {CandidateService} from "../../../services/candidate.service";
import {AuthenticationService} from "../../../services/authentication.service";
import {UntypedFormBuilder, UntypedFormGroup, Validators} from "@angular/forms";
import {TermsInfo, TermsType} from "../../../model/terms-info";

@Component({
  selector: 'app-registration-submit',
  templateUrl: './registration-submit.component.html',
  styleUrls: ['./registration-submit.component.scss']
})
export class RegistrationSubmitComponent implements OnInit {
  error: any;
  loading: boolean;

  currentPrivacyPolicy: TermsInfo;

  form: UntypedFormGroup;

  constructor(private builder: UntypedFormBuilder,
              private candidateService: CandidateService,
              private authenticationService: AuthenticationService) {
  }

  ngOnInit(): void {
    this.form = this.builder.group({
      contactConsentRegistration: [false, Validators.required],
      contactConsentPartners: [false, Validators.required],
      acceptPolicy: [false, Validators.required]
    });

    //todo load current privacy policy
    this.currentPrivacyPolicy = {
      id: 1,
      type: TermsType.CANDIDATE_PRIVACY_POLICY,

      //todo We could substitute into this string using TimeLeaf or manually. But is it needed?
      content: "<p>John was here {{partnerName}}</p><p>and on this line</p>",
      version: "1.0",
      creationDate: new Date()
    }

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
