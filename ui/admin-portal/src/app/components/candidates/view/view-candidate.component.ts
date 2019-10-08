import { Component, OnInit } from '@angular/core';
import { CandidateService } from '../../../services/candidate.service';
import { Candidate } from '../../../model/candidate';
import { ActivatedRoute, Router } from '@angular/router';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { DeleteCandidateComponent } from './delete/delete-candidate.component';
import {EditCandidateComponent} from "./edit/edit-candidate.component";

@Component({
  selector: 'app-view-candidate',
  templateUrl: './view-candidate.component.html',
  styleUrls: ['./view-candidate.component.scss']
})
export class ViewCandidateComponent implements OnInit {

  loading: boolean;
  error;
  candidate: Candidate;

  constructor(private candidateService: CandidateService,
              private route: ActivatedRoute,
              private router: Router,
              private modalService: NgbModal) { }

  ngOnInit() {
    this.route.paramMap.subscribe(params => {
      let candidateId = +params.get('candidateId');
      this.loading = true;
      this.candidateService.get(candidateId).subscribe(candidate => {
        this.candidate = candidate;
        this.loading = false;
      },error => {
        this.error = error;
        this.loading = false;
      });
    });
  }

  deleteCandidate() {
    let modal = this.modalService.open(DeleteCandidateComponent);
    modal.componentInstance.candidate = this.candidate;
    modal.result.then(result => {
      this.router.navigate(['/candidates']);
    });
  }

  editCandidate() {
    let modal = this.modalService.open(EditCandidateComponent);
    modal.componentInstance.candidateId = this.candidate.id;
    modal.result.then(result => {
      console.log(result)
      this.candidate = result;
    });
  }
}
