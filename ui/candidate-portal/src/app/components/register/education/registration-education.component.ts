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
  educationLevels: EducationLevel[];
  error: any;
  // Component states
  _loading = {
    levels: true,
    candidate: true
  };
  saving: boolean;

  constructor(private fb: FormBuilder,
              private router: Router,
              private educationLevelService: EducationLevelService,
              private candidateService: CandidateService) {
  }

  ngOnInit() {
    this.saving = false;
    this.form = this.fb.group({
      maxEducationLevelId: ['', Validators.required]
    });

    this._loading.levels = true;
    this.educationLevelService.listEducationLevels().subscribe(
      (response) => {
        this.educationLevels = response;
        this._loading.levels = false;
      },
      (error) => {
        this.error = error;
        this._loading.levels = false;
      }
    );

    this._loading.candidate = true;
    this.candidateService.getCandidateEducation().subscribe(
      (response) => {
        /* DEBUG */
        console.log('response', response);
        this.form.patchValue({
          maxEducationLevelId: response.maxEducationLevel ? response.maxEducationLevel.id : null,
        });
        this._loading.candidate = false;
      },
      (error) => {
        this.error = error;
        this._loading.candidate = false;
      }
    );
  }

  save(dir: string) {
    console.log(this.form);

    this.candidateService.updateCandidateEducationLevel(this.form.value).subscribe(
      (response) => {

        let maxEducationLevel = response.maxEducationLevel;
        if (maxEducationLevel.name == 'mastersDegree' || maxEducationLevel.name == 'doctorateDegree') {
          this.router.navigate(['register', 'education', 'masters']);
        } else if (maxEducationLevel.name == 'bachelorsDegree') {
          this.router.navigate(['register', 'education', 'university']);
        } else {
          this.router.navigate(['register', 'education', 'school']);
        }
      },
      (error) => {
        this.error = error;
      }
    );
  };

  back() {
    this.save('back');
  }

  next() {
    this.save('next');
  }

  get loading() {
    return this._loading.levels || this._loading.candidate;
  }
}
