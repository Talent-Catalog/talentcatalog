import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup} from "@angular/forms";
import {Router} from "@angular/router";
import {Candidate} from "../../../../model/candidate";
import {CandidateService} from "../../../../services/candidate.service";

@Component({
  selector: 'app-registration-additional-contact',
  templateUrl: './registration-additional-contact.component.html',
  styleUrls: ['./registration-additional-contact.component.scss']
})
export class RegistrationAdditionalContactComponent implements OnInit {

  form: FormGroup;
  error: any;
  // Component states
  loading: boolean;
  saving: boolean;
  candidate: Candidate;

  constructor(private router: Router,
              private fb: FormBuilder,
              private candidateService: CandidateService) { }

  ngOnInit() {
    this.loading = true;
    this.saving = false;
    this.form = this.fb.group({
      phone: [''],
      whatsapp: ['']
    });
    this.candidateService.getCandidateAdditionalContacts().subscribe(
      (response) => {
        this.form.patchValue({
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
  }

  formValid() {
    return this.form.value.phone || this.form.value.whatsapp;
  }

  save() {
    this.candidateService.updateCandidateAdditionalContacts(this.form.value).subscribe(
      (response) => {
        this.router.navigate(['register', 'personal']);
      },
      (error) => {
        this.error = error;
      }
    );
  }
}
