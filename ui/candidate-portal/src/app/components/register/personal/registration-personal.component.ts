import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {Router} from "@angular/router";
import {Candidate} from "../../../model/candidate";
import {CandidateService} from "../../../services/candidate.service";

@Component({
  selector: 'app-registration-personal',
  templateUrl: './registration-personal.component.html',
  styleUrls: ['./registration-personal.component.scss']
})
export class RegistrationPersonalComponent implements OnInit {

  form: FormGroup;
  error: any;
  // Component states
  loading: boolean;
  saving: boolean;
  candidate: Candidate;

  constructor(private fb: FormBuilder,
              private router: Router,
              private candidateService: CandidateService) { }

  ngOnInit() {
    this.loading = true;
    this.saving = false;
    this.form = this.fb.group({
      firstName: [null, Validators.required],
      lastName: [null, Validators.required],
      gender: [null, Validators.required],
      dob: [null, Validators.required],
    });
    this.candidateService.getCandidatePersonal().subscribe(
      (response) => {
        this.form.patchValue({
          firstName: response.user.firstName,
          lastName: response.user.lastName,
          gender: response.gender,
          dob: response.dob,
        });
        this.loading = false;
      },
      (error) => {
        this.error = error;
        this.loading = false;
      }
    );
  }

  save() {
    this.candidateService.updateCandidatePersonal(this.form.value).subscribe(
      (response) => {
        this.router.navigate(['register', 'location']);
      },
      (error) => {
        this.error = error;
      }
    );
  }

}
