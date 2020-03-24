import {Component, OnInit} from '@angular/core';
import {CandidateService} from '../../../services/candidate.service';
import {Candidate} from '../../../model/candidate';
import {ActivatedRoute, Router} from '@angular/router';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {DeleteCandidateComponent} from './delete/delete-candidate.component';
import {EditCandidateStatusComponent} from "./status/edit-candidate-status.component";
import {Title} from "@angular/platform-browser";
import {AuthService} from "../../../services/auth.service";
import {User} from "../../../model/user";

@Component({
  selector: 'app-view-candidate',
  templateUrl: './view-candidate.component.html',
  styleUrls: ['./view-candidate.component.scss']
})
export class ViewCandidateComponent implements OnInit {

  loading: boolean;
  loadingError : boolean;
  error;
  candidate: Candidate;
  mainColWidth=8;
  sidePanelColWidth=4;
  loggedInUser: User;

  constructor(private candidateService: CandidateService,
              private route: ActivatedRoute,
              private router: Router,
              private modalService: NgbModal,
              private titleService: Title,
              private authService: AuthService) { }

  ngOnInit() {
    this.loadingError = false;
    this.route.paramMap.subscribe(params => {
      let candidateNumber = params.get('candidateNumber');
      this.loading = true;
      this.error = null;
      this.loadingError = false;
      this.candidateService.getByNumber(candidateNumber).subscribe(candidate => {
        if (candidate == null) {
          this.loadingError = true;
          this.error = 'There is no candidate with number: ' + params.get('candidateNumber');
        } else {
          this.setCandidate(candidate);
        }
        this.loading = false;
      },error => {
        this.loadingError = true;
        this.error = error;
        this.loading = false;
      });
    });
    this.loggedInUser = this.authService.getLoggedInUser();
    console.log(this.loggedInUser);
  }

  deleteCandidate() {
    let modal = this.modalService.open(DeleteCandidateComponent);
    modal.componentInstance.candidate = this.candidate;
    modal.result.then(result => {
      this.router.navigate(['/candidates']);
    });
  }

  editCandidate() {
    let modal = this.modalService.open(EditCandidateStatusComponent);
    modal.componentInstance.candidateId = this.candidate.id;
    modal.result
      .then(result => {this.setCandidate(result);})
      .catch(() => { /* Isn't possible */ });
  }

  resizeSidePanel(){
    this.mainColWidth = this.mainColWidth == 8 ? this.mainColWidth - 4 : this.mainColWidth + 4;
    this.sidePanelColWidth = this.mainColWidth == 4 ? this.sidePanelColWidth + 4 : this.sidePanelColWidth - 4;
  }


  setCandidate(value: Candidate) {
    this.candidate = value;
    if(this.candidate.user.firstName && this.candidate.user.lastName){
      this.titleService.setTitle(this.candidate.user.firstName + ' '
        + this.candidate.user.lastName + ' ' + this.candidate.candidateNumber);
    } else {
      this.titleService.setTitle(this.candidate.candidateNumber)
    }
  }

  downloadCV() {
      let tab = window.open();
      this.candidateService.downloadCv(this.candidate.id).subscribe(
        result => {
          tab.location.href = URL.createObjectURL(result);
        },
        error => {
          this.error = error;
        }
      )
  }

}
