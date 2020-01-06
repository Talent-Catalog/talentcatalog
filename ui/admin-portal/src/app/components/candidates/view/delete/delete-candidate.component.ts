import { Component, OnInit } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { CandidateService } from '../../../../services/candidate.service';
import { Candidate } from '../../../../model/candidate';

@Component({
  selector: 'app-delete-candidate',
  templateUrl: './delete-candidate.component.html',
  styleUrls: ['./delete-candidate.component.scss']
})
export class DeleteCandidateComponent implements OnInit {

  candidate: Candidate;
  deleting: boolean;

  constructor(private activeModal: NgbActiveModal,
              private candidateService: CandidateService) { }

  ngOnInit() {
  }

  cancel() {
    this.activeModal.dismiss();
  }

  confirm() {
    this.deleting = true;
    this.candidateService.delete(this.candidate.id).subscribe(result => {
      this.deleting = false;
      this.activeModal.close();
    });
  }
}
