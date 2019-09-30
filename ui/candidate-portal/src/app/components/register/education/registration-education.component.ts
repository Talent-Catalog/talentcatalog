import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {Router} from "@angular/router";
import {CandidateService} from "../../../services/candidate.service";
import {EducationLevelService} from "../../../services/education-level.service";
import {EducationLevel} from "../../../model/education-level";

@Component({
  selector: 'app-registration-education',
  templateUrl: './registration-education.component.html',
  styleUrls: ['./registration-education.component.scss']
})
export class RegistrationEducationComponent implements OnInit {

  form: FormGroup;
  maxEducationLevelId: number;
  educationLevels: EducationLevel[];
  error: any;
  // Component states
  loading: boolean;
  saving: boolean;

  constructor(private fb: FormBuilder,
              private router: Router,
              private educationLevelService: EducationLevelService,
              private candidateService: CandidateService) { }

  ngOnInit() {
    this.loading = true;
    this.saving = false;
    this.form = this.fb.group({
      maxEducationLevelId: ['', Validators.required]
    });
    this.candidateService.getCandidateEducationLevel().subscribe(
      (response) => {
        this.form.patchValue({
          maxEducationLevelId: response.maxEducationLevel.id,
        });
        this.loading = false;
        /* Wait for the candidate then load the countries */
        this.educationLevelService.listEducationLevels().subscribe(
          (response) => {
            this.educationLevels = response;
            this.loading = false;
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
  };

    save() {
      console.log(this.form);

      this.candidateService.updateCandidateEducationLevel(this.form.value).subscribe(
        (response) => {

          let maxEducationLevel = response.maxEducationLevel;
          if(maxEducationLevel.name == 'mastersDegree' || maxEducationLevel.name == 'doctorateDegree'){
            this.router.navigate(['register', 'education', 'masters']);
          }else if(maxEducationLevel.name == 'bachelorsDegree'){
            this.router.navigate(['register', 'education', 'university']);
          }else{
            this.router.navigate(['register', 'education', 'school']);
          }
        },
        (error) => {
          this.error = error;
        }
      );
    };

}
