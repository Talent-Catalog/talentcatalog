import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {Router} from "@angular/router";
import {CandidateService} from "../../../services/candidate.service";


@Component({
  selector: 'app-registration-additional-info',
  templateUrl: './registration-additional-info.component.html',
  styleUrls: ['./registration-additional-info.component.scss']
})
export class RegistrationAdditionalInfoComponent implements OnInit {

  form: FormGroup;
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
    this.form = this.fb.group({
      additionalInfo: ['']
    });
    this.candidateService.getCandidateAdditionalInfo().subscribe(
      (response) => {
        this.form.patchValue({
          additionalInfo: [response.additionalInfo],
        });
        this.loading = false;
      },
      (error) => {
        this.error = error;
        this.loading = false;
      }
    );
  }

  save(){
    this.candidateService.updateCandidateAdditionalInfo(this.form.value).subscribe(
      (response) => {
        this.router.navigate([''])
      },
      (error) => {
        this.error = error;
      }
    );
  }

}
