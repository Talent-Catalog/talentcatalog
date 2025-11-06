import {Component, OnInit} from '@angular/core';
import {TermsInfoDto, TermsType} from "../../model/terms-info-dto";
import {TermsInfoService} from "../../services/terms-info.service";
import {CandidateService} from "../../services/candidate.service";
import {forkJoin} from "rxjs";
import {Candidate} from "../../model/candidate";

@Component({
  selector: 'app-terms',
  templateUrl: './terms.component.html',
  styleUrls: ['./terms.component.scss']
})
export class TermsComponent implements OnInit {
  content: string;
  currentPrivacyPolicy: TermsInfoDto;
  error: any;
  partnerEmail: string;
  partnerName: string;
  requestAcceptance: boolean;
  termsRead: boolean = false;
  acceptedPrivacyPolicyDate: string | null = null;
  acceptedPrivacyPolicyId: string | null = null;
  acceptedPrivacyPolicyPartner: string | null = null;

  constructor(
    private candidateService: CandidateService,
    private termsInfoService: TermsInfoService) {
  }

  ngOnInit(): void {
    this.loadCandidate();
  }

  loadCandidate(){
    //Fetch the current candidate privacy policy and candidate info
    forkJoin({
      'currentPolicy': this.termsInfoService.getCurrentByType(TermsType.CANDIDATE_PRIVACY_POLICY),
      'candidate': this.candidateService.getCandidatePersonal()
    }).subscribe(
      results => {
        this.configure(results.candidate, results.currentPolicy)
      },
      err => this.error = err
    )
  }

  private configure(candidate: Candidate, currentPolicy: TermsInfoDto) {
    //Store the current policy
    this.setCurrentPolicy(currentPolicy);

    //Store the name of the candidate's partner
    this.partnerName = candidate?.user?.partner?.name;
    this.partnerEmail = candidate?.user?.partner?.notificationEmail;
    this.acceptedPrivacyPolicyId = candidate?.acceptedPrivacyPolicyId;
    this.acceptedPrivacyPolicyDate = candidate?.acceptedPrivacyPolicyDate;
    this.acceptedPrivacyPolicyPartner = candidate?.acceptedPrivacyPolicyPartner?.name || this.partnerName;

    //Check if candidate has accepted the current policy. If not they need to accept it.
    this.setRequestAcceptance(currentPolicy.id != candidate.acceptedPrivacyPolicyId)
  }

  private setCurrentPolicy(termsInfo: TermsInfoDto) {
    this.currentPrivacyPolicy = termsInfo;
    this.content = this.currentPrivacyPolicy?.content;
  }

  setTermsRead() {
    this.termsRead = true;
  }

  acceptTerms() {
    //Mark candidate as having accepted these terms.
    this.candidateService.updateAcceptedPrivacyPolicy(this.currentPrivacyPolicy?.id).subscribe(
      {
        next: () => {
          this.loadCandidate();
          this.setRequestAcceptance(false);
          },
        error: err => {this.error = err}
      }
    );
  }

  setRequestAcceptance(requestAcceptance: boolean) {
    this.requestAcceptance = requestAcceptance;
    if (this.requestAcceptance) {
      //Tag candidate as pendingTermsAcceptance.
      //Note that we don't have to untag them when the candidate accepts the terms because that
      //is done automatically on the server.
      this.candidateService.updatePendingTermsAcceptance(true).subscribe({
        next: () => {},
        error: err => {this.error = err}
      });
    }
  }
}
