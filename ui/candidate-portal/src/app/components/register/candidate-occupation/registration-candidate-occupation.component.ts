import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {Router} from "@angular/router";
import {CandidateService} from "../../../services/candidate.service";
import {CandidateOccupationService} from "../../../services/candidate-occupation.service";
import {CandidateOccupation} from "../../../model/candidate-occupation";
import {Occupation} from "../../../model/occupation";
import {OccupationService} from "../../../services/occupation.service";

@Component({
  selector: 'app-registration-candidate-occupation',
  templateUrl: './registration-candidate-occupation.component.html',
  styleUrls: ['./registration-candidate-occupation.component.scss']
})
export class RegistrationCandidateOccupationComponent implements OnInit {

  error: any;
  loading: boolean;
  saving: boolean;
  form: FormGroup;
  candidateOccupations: CandidateOccupation[];
  occupations: Occupation[];

  constructor(private fb: FormBuilder,
              private router: Router,
              private candidateService: CandidateService,
              private occupationService: OccupationService,
              private candidateOccupationService: CandidateOccupationService) { }

  ngOnInit() {
    this.candidateOccupations = [];
    this.saving = false;
    this.loading = true;

    /* Load the candidate data */
    this.candidateService.getCandidateCandidateOccupations().subscribe(
      (candidate) => {
        this.candidateOccupations = candidate.candidateOccupations || [];

        /* Wait for the candidate then load the industries */
        this.occupationService.listOccupations().subscribe(
          (response) => {
            this.occupations = response;
            this.loading = false;
            this.setUpForm();
          },
          (error) => {
            this.error = error;
            this.loading = false;
          }
        );
      },
      (error) => {
        this.error = error;
        this.loading = false;
      }
    );
  }

  setUpForm(){
    this.form = this.fb.group({
      occupationId: [null, Validators.required],
      yearsExperience: [null, Validators.required],
    });
  }

  addMore(){
    this.saving = true;
    this.candidateOccupationService.createCandidateOccupation(this.form.value).subscribe(
      (response) => {
        this.candidateOccupations.push(response);
        this.saving = false;
      },
      (error) => {
        this.error = error;
        this.saving = false;
      }
    );
    console.log(this.form.value)
    this.setUpForm();
  }

  delete(candidateOccupation){
    this.saving = true;
    this.candidateOccupationService.deleteCandidateOccupation(candidateOccupation.id).subscribe(
      () => {
        this.candidateOccupations = this.candidateOccupations.filter(p => p !== candidateOccupation);
        this.saving = false;
      },
      (error) => {
        this.error = error;
        this.saving = false;
      }
    );
  }

  next() {
    console.log(this.candidateOccupations);
    // TODO check if the form is not empty and warn the user
    this.router.navigate(['register', 'experience']);
  }

}
