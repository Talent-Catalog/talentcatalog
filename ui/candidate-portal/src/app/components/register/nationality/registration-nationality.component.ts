import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {Router} from "@angular/router";
import {CandidateService} from "../../../services/candidate.service";
import {NationalityService} from "../../../services/nationality.service";
import {Nationality} from "../../../model/nationality";

@Component({
  selector: 'app-registration-nationality',
  templateUrl: './registration-nationality.component.html',
  styleUrls: ['./registration-nationality.component.scss']
})
export class RegistrationNationalityComponent implements OnInit {

  form: FormGroup;
  nationalities: Nationality[];
  error: any;
  // Component states
  loading: boolean;
  saving: boolean;

  constructor(private fb: FormBuilder,
              private router: Router,
              private candidateService: CandidateService,
              private nationalityService: NationalityService) { }

  ngOnInit() {
    this.loading = true;
    this.saving = false;
    this.nationalities = [];

    /* Wait for the candidate then load the nationalities */
    this.nationalityService.listNationalities().subscribe(
      (response) => {
        this.nationalities = response;
        this.loading = false;
      },
      (error) => {
        this.error = error;
        this.loading = false;
      }
    );

    this.form = this.fb.group({
      nationality: ['', Validators.required],
      registeredWithUN: ['', Validators.required],
      registrationId: ['', Validators.required]
    });

    this.candidateService.getCandidateNationality().subscribe(
      (response) => {
        if(response.nationality){
          this.form.patchValue({
            nationality: response.nationality.id,
            registeredWithUN: response.registeredWithUN,
            registrationId: response.registrationId
          });
          this.loading = false;
        }
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
        this.router.navigate(['register', 'candidateOccupation']);
      },
      (error) => {
        this.error = error;
      }
    );
  }

}
