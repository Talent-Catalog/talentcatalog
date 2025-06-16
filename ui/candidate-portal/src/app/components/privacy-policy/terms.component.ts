import {Component, Input, OnInit} from '@angular/core';
import {TermsInfoDto, TermsType} from "../../model/terms-info-dto";
import {TermsInfoService} from "../../services/terms-info.service";
import {CandidateService} from "../../services/candidate.service";
import {forkJoin} from "rxjs";
import {Candidate, CandidateStatus} from "../../model/candidate";

@Component({
  selector: 'app-terms',
  templateUrl: './terms.component.html',
  styleUrls: ['./terms.component.scss']
})
export class TermsComponent implements OnInit {
  content: string;
  currentPrivacyPolicy: TermsInfoDto;
  error: any;
  partnerName: string;
  requestAcceptance: boolean;
  termsRead: boolean = false;

  constructor(
    private candidateService: CandidateService,
    private termsInfoService: TermsInfoService) {
  }

  ngOnInit(): void {

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

    //Check if candidate has accepted the current policy. If not they need to accept it.
    this.requestAcceptance = currentPolicy.id != candidate.acceptedPrivacyPolicyId;

    if (this.requestAcceptance) {
      //todo Need to tag candidate as pendingTermsAcceptance
    }
  }

  private setCurrentPolicy(termsInfo: TermsInfoDto) {
    this.currentPrivacyPolicy = termsInfo;
    this.content = this.currentPrivacyPolicy?.content;
  }

  setTermsRead() {
    this.termsRead = true;
  }

  acceptTerms() {
    //todo Mark candidate as having accepted these terms.
    this.candidateService.updateAcceptedPrivacyPolicy(this.currentPrivacyPolicy?.id).subscribe(
      {
        next: candidate => {this.requestAcceptance = false},
        error: err => {this.error = err}
      }
    );

  }
}
