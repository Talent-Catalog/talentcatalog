import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {Router} from "@angular/router";
import {Industry} from "../../../model/industry";
import {IndustryService} from "../../../services/industry.service";
import {CandidateService} from "../../../services/candidate.service";
import {ProfessionService} from "../../../services/profession.service";
import {Profession} from "../../../model/profession";

@Component({
  selector: 'app-registration-profession',
  templateUrl: './registration-profession.component.html',
  styleUrls: ['./registration-profession.component.scss']
})
export class RegistrationProfessionComponent implements OnInit {

  error: any;
  loading: boolean;
  saving: boolean;
  form: FormGroup;
  professions: Profession[];
  industries: Industry[];

  constructor(private fb: FormBuilder,
              private router: Router,
              private candidateService: CandidateService,
              private industryService: IndustryService,
              private professionService: ProfessionService) { }

  ngOnInit() {
    this.professions = [];
    this.saving = false;
    this.loading = true;

    /* Load the candidate data */
    this.candidateService.getCandidateProfessions().subscribe(
      (candidate) => {
        console.log(candidate);
        this.professions = candidate.professions || [];

        /* Wait for the candidate then load the industries */
        this.industryService.listIndustries().subscribe(
          (response) => {
            this.industries = response;
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
      industryId: [null, Validators.required],
      yearsExperience: [null, Validators.required],
    });
  }

  addMore(){
    this.saving = true;
    this.professionService.createProfession(this.form.value).subscribe(
      (response) => {
        this.professions.push(response);
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

  delete(profession){
    this.saving = true;
    this.professionService.deleteProfession(profession.id).subscribe(
      () => {
        this.professions = this.professions.filter(p => p !== profession);
        this.saving = false;
      },
      (error) => {
        this.error = error;
        this.saving = false;
      }
    );
  }

  next() {
    console.log(this.professions);
    // TODO check if the form is not empty and warn the user
    this.router.navigate(['register', 'experience']);
  }

}
