import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {FormBuilder, FormControl, FormGroup, Validators} from "@angular/forms";
import {Router} from "@angular/router";
import {CandidateService} from "../../../services/candidate.service";
import {AuthService} from "../../../services/auth.service";
import {Candidate} from "../../../model/candidate";
import {RegistrationService} from "../../../services/registration.service";
import {LanguageService} from "../../../services/language.service";
import {SystemLanguage} from "../../../model/language";

@Component({
  selector: 'app-registration-contact',
  templateUrl: './registration-contact.component.html',
  styleUrls: ['./registration-contact.component.scss']
})
export class RegistrationContactComponent implements OnInit {

  /* A flag to indicate if the component is being used on the profile component */
  @Input() edit: boolean = false;

  @Output() onSave = new EventEmitter();

  form: FormGroup;
  error: any;
  // Form states
  loading: boolean;
  saving: boolean;
  // Candidate data
  authenticated: boolean;
  candidate: Candidate;

  systemLanguages: SystemLanguage[];

  constructor(private fb: FormBuilder,
              private router: Router,
              private candidateService: CandidateService,
              private authService: AuthService,
              private languageService: LanguageService,
              private registrationService: RegistrationService) { }

  ngOnInit() {
    this.authenticated = false;
    this.loading = true;
    this.candidate = null;
    this.form = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      phone: ['', [Validators.required]],
      preferredLanguage: ['', [Validators.required]],
      whatsapp: [''],
      // username: ['']
    });

    this.languageService.listSystemLanguages().subscribe(
      (response) => this.systemLanguages = response,
      (error) => this.error = error
    );

    if (this.authService.isAuthenticated()) {
      this.authenticated = true;
      this.candidateService.getCandidateContact().subscribe(
        (candidate) => {
          this.candidate = candidate;
          this.form.patchValue({
            email: candidate.user ? candidate.user.email : '',
            phone: candidate.phone,
            whatsapp: candidate.whatsapp,
            preferredLanguage: candidate.preferredLanguage,
            //username: candidate.user ? response.user.username : ''
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
      this.form.addControl('password', new FormControl('', [Validators.required, Validators.minLength(8)]));
      this.form.addControl('passwordConfirmation', new FormControl('', [Validators.required, Validators.minLength(8)]));
      this.loading = false;
    }
  }

  cancel() {
    this.onSave.emit();
  }

  save() {
    this.saving = true;
    this.error = null;
    if (this.authService.isAuthenticated()) {

      // If the candidate hasn't changed anything, skip the update service call
      if (this.form.pristine) {
        this.registrationService.next();
        this.onSave.emit();
        return;
      }

      // The user has already registered and is revisiting this page
      this.candidateService.updateCandidateContact(this.form.value).subscribe(
        (response) => {
          this.registrationService.next();
          this.onSave.emit();
        },
        (error) => {
          // console.log(error);
          this.error = error;
          this.saving = false;
        }
      );
    } else {
      // The user has not yet registered - create an account for them
      this.authService.register(this.form.value).subscribe(
        (response) => {
          this.registrationService.next();
        },
        (error) => {
          // console.log(error);
          this.error = error;
          this.saving = false;
        }
      );
    }
  }

}
