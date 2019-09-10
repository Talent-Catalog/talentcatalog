import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormControl, FormGroup, Validators} from "@angular/forms";
import {Router} from "@angular/router";
import {Candidate} from "../../../../model/candidate";
import {CandidateService} from "../../../../services/candidate.service";
import {AuthService} from "../../../../services/auth.service";

@Component({
  selector: 'app-registration-alternate-contact',
  templateUrl: './registration-alternate-contact.component.html',
  styleUrls: ['./registration-alternate-contact.component.scss']
})
export class RegistrationAlternateContactComponent implements OnInit {

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
    this.saving = false;
    this.loading = true;
    this.form = this.fb.group({
      phone: [''],
      whatsapp: [''],
      email: ['']
    });
    if (this.authService.isAuthenticated()) {
      this.candidateService.getCandidateAlternateContacts().subscribe(
        (response) => {
          this.candidate = response;
          this.form.patchValue({
            email: response.email,
            phone: response.phone,
            whatsapp: response.whatsapp
          });
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

  formValid() {
    const form = this.form.value;
    const hasAtleastOneContact = form.phone || form.whatsapp || form.email;
    return !this.candidate && hasAtleastOneContact && this.form.valid || this.candidate && hasAtleastOneContact;
  }

  save() {
    this.saving = true;
    if (this.authService.isAuthenticated()) {
      // The user has already registered and is revisiting this page
      this.candidateService.updateCandidateAlternateContacts(this.form.value).subscribe(
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
    this.router.navigate(['register', 'personal']);
  }

}
