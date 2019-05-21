import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { CandidateService } from '../../../services/candidate.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-create-candidate',
  templateUrl: './create-candidate.component.html',
  styleUrls: ['./create-candidate.component.scss']
})
export class CreateCandidateComponent implements OnInit {

  candidateForm: FormGroup;
  saving: boolean;

  constructor(private fb: FormBuilder,
              private candidateService: CandidateService,
              private router: Router) {}

  ngOnInit() {
    this.candidateForm = this.fb.group({
      firstName: [''],
      lastName: [''],
    });
  }

  onSave() {
    this.saving = true;
    this.candidateService.create(this.candidateForm.value).subscribe(candidate => {
      this.router.navigate(['candidates']);
      this.saving = false;
    });
  }
}
