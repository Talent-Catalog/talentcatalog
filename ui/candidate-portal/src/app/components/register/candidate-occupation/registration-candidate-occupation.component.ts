import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {Router} from "@angular/router";
import {CandidateService} from "../../../services/candidate.service";
import {CandidateOccupationService} from "../../../services/candidate-occupation.service";
import {CandidateOccupation} from "../../../model/candidate-occupation";
import {Occupation} from "../../../model/occupation";
import {OccupationService} from "../../../services/occupation.service";
import {RegistrationService} from "../../../services/registration.service";

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
  occupationRequests: {occupationId: number, yearsExperience: number}[];

  constructor(private fb: FormBuilder,
              private router: Router,
              private candidateService: CandidateService,
              private occupationService: OccupationService,
              private candidateOccupationService: CandidateOccupationService,
              public registrationService: RegistrationService) { }

  ngOnInit() {
    this.candidateOccupations = [];
    this.saving = false;
    this.loading = true;
    this.occupationRequests = [];

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

  addOccupationToRequest() {
    this.occupationRequests.push(this.form.value);
    this.setUpForm();
  }

  save(dir: string) {
    if (this.form.valid) {
      this.addOccupationToRequest();
    }
    this.candidateOccupationService.updateCandidateOccupations(this.occupationRequests).subscribe(
      () => {
        if (dir === 'next') {
          this.registrationService.next();
        } else {
          this.registrationService.back();
        }
      },
      (error) => {
        console.log('error', error);
      });
  }

  back() {
    this.save('back');
  }

  next() {
    this.save('next');
  }

  deleteOccupation(index: number) {
    this.occupationRequests.splice(index, 1);
  }
}
