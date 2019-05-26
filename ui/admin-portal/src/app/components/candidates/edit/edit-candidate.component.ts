import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CandidateService } from '../../../services/candidate.service';
import { FormBuilder, FormGroup } from '@angular/forms';

@Component({
  selector: 'app-edit-candidate',
  templateUrl: './edit-candidate.component.html',
  styleUrls: ['./edit-candidate.component.scss']
})
export class EditCandidateComponent implements OnInit {

  candidateId: number;
  candidateForm: FormGroup;
  loading: boolean;
  saving: boolean;

  constructor(private fb: FormBuilder,
              private candidateService: CandidateService,
              private route: ActivatedRoute,
              private router: Router) {
  }

  ngOnInit() {
    this.route.paramMap.subscribe(params => {
      this.candidateId = +params.get('candidateId');
      this.loading = true;
      this.candidateService.get(this.candidateId).subscribe(candidate => {
        this.candidateForm = this.fb.group({
          candidateNumber: [candidate.candidateNumber],
          firstName: [candidate.firstName],
          lastName: [candidate.lastName],
        });
        this.loading = false;
      });
    });
  }

  onSave() {
    this.saving = true;
    this.candidateService.update(this.candidateId, this.candidateForm.value)
      .subscribe(candidate => {
        this.router.navigate(['candidates', candidate.id]);
        this.saving = false;
      });
  }
}
