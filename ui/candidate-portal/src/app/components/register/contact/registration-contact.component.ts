import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormControl, FormGroup, Validators} from "@angular/forms";
import {Router} from "@angular/router";
import {CandidateService} from "../../../services/candidate.service";
import {AuthService} from "../../../services/auth.service";
import {Candidate} from "../../../model/candidate";

@Component({
  selector: 'app-registration-contact',
  templateUrl: './registration-contact.component.html',
  styleUrls: ['./registration-contact.component.scss']
})
export class RegistrationContactComponent implements OnInit {

  form: FormGroup;
  error: any;
  // Form states
  loading: boolean;
  saving: boolean;
  // Candidate data
  candidate: Candidate;

  constructor(private fb: FormBuilder,
              private router: Router,
              private candidateService: CandidateService,
              private authService: AuthService) { }

  ngOnInit() {
    this.loading = true;
    this.candidate = null;
    this.form = this.fb.group({
      email: ['', [Validators.required, Validators.email]]
    });
    if (this.authService.isAuthenticated()) {
      this.candidateService.getCandidateContactInfo().subscribe(
        (response) => {
          this.candidate = response;
          this.form.patchValue({email: response.email});
          this.loading = false;
        },
        (error) => {
          this.error = error;
          this.loading = false;
        }
      );
    } else {
      // The user has not registered - add the password fields to the reactive form
      this.form.addControl('password', new FormControl('', [Validators.required]));
      this.form.addControl('passwordConfirmation', new FormControl('', [Validators.required]));
      this.loading = false;
    }
  }

  save() {
    this.saving = true;
    if (this.authService.isAuthenticated()) {
      // The user has already registered and is revisiting this page
      this.candidateService.updateCandidateContactInfo(this.form.value).subscribe(
        (response) => {
          // Success - navigate to next step
          this.navigateToNextStep();
        },
        (error) => {
          this.error = error;
          this.saving = false;
        }
      );
    } else {
      // The user has not yet registered - create an account for them
      this.authService.register(this.form.value).subscribe(
        (response) => {
          // Success - navigate to next step
          this.navigateToNextStep();
        },
        (error) => {
          this.error = error;
          this.saving = false;
        }
      );
    }
  }

  navigateToNextStep() {
    this.saving = false;
    this.router.navigate(['register', 'contact', 'additional']);
  }
}
