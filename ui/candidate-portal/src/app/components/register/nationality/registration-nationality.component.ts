import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {Router} from "@angular/router";
import {nationalities} from "../../../model/nationality";
import {CandidateService} from "../../../services/candidate.service";

@Component({
  selector: 'app-registration-nationality',
  templateUrl: './registration-nationality.component.html',
  styleUrls: ['./registration-nationality.component.scss']
})
export class RegistrationNationalityComponent implements OnInit {

  form: FormGroup;
  nationalities: string[];
  error: any;
  // Component states
  loading: boolean;
  saving: boolean;

  constructor(private fb: FormBuilder,
              private router: Router,
              private candidateService: CandidateService) { }

  ngOnInit() {
    this.loading = true;
    this.saving = false;
    this.nationalities = nationalities;
    this.form = this.fb.group({
      nationality: ['', Validators.required],
      registeredWithUN: ['', Validators.required],
      registrationId: ['', Validators.required]
    });
    this.candidateService.getCandidateNationality().subscribe(
      (response) => {
        this.form.patchValue({
          nationality: response.nationality,
          registeredWithUN: response.registeredWithUN,
          registrationId: response.registrationId
        });
        this.loading = false;
      },
      (error) => {
        this.error = error;
        this.loading = false;
      }
    );
  }

  formValid() {
    const form = this.form.value;
    return form.nationality && form.registeredWithUN && (form.registeredWithUN === 'true' ? form.registrationId : true);
  }

  save() {
    this.candidateService.updateCandidateNationality(this.form.value).subscribe(
      (response) => {
        this.router.navigate(['register', 'profession']);
      },
      (error) => {
        this.error = error;
      }
    );
  }

}
