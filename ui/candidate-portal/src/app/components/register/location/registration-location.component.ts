import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {Router} from "@angular/router";
import {CandidateService} from "../../../services/candidate.service";

@Component({
  selector: 'app-registration-location',
  templateUrl: './registration-location.component.html',
  styleUrls: ['./registration-location.component.scss']
})
export class RegistrationLocationComponent implements OnInit {

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
      country: ['', Validators.required],
      city: ['', Validators.required],
      yearOfArrival: ['', Validators.required]
    });
    this.candidateService.getCandidateLocation().subscribe(
      (response) => {
        this.form.patchValue({
          country: response.country,
          city: response.city,
          yearOfArrival: response.yearOfArrival
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
    this.candidateService.updateCandidateLocation(this.form.value).subscribe(
      (response) => {
        this.router.navigate(['register', 'nationality']);
      },
      (error) => {
        this.error = error;
      }
    );
  }

}
