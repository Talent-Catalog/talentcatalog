import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {Router} from "@angular/router";
import {CandidateCertification} from "../../../model/candidate-certification";
import {CandidateService} from "../../../services/candidate.service";
import {CertificationService} from "../../../services/certification.service";

@Component({
  selector: 'app-registration-certifications',
  templateUrl: './registration-certifications.component.html',
  styleUrls: ['./registration-certifications.component.scss']
})
export class RegistrationCertificationsComponent implements OnInit {

  error: any;
  loading: boolean;
  saving: boolean;
  form: FormGroup;
  candidateCertifications: CandidateCertification[];

  constructor(private fb: FormBuilder,
              private router: Router,
              private candidateService: CandidateService,
              private certificationService: CertificationService ) { }

  ngOnInit() {
    this.candidateCertifications = [];
    this.saving = false;
    this.loading = false;
    this.setUpForm();

   /* Load the candidate data */
    this.candidateService.getCandidateCertifications().subscribe(
      (candidate) => {
        this.candidateCertifications = candidate.candidateCertifications || [];
      },
      (error) => {
        this.error = error;
        this.loading = false;
      }
    );
  }

  setUpForm(){
    this.form = this.fb.group({
      name: ['', Validators.required],
      institution: ['', Validators.required],
      dateCompleted: ['', Validators.required]
    })
  }

  addMore(){
    this.saving = true;
    this.certificationService.createCertification(this.form.value).subscribe(
      (response) => {
        this.candidateCertifications.push(response);
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

  delete(certification){
    this.saving = true;
    this.certificationService.deleteCertification(certification.id).subscribe(
      () => {
        this.candidateCertifications = this.candidateCertifications.filter(c => c !== certification);
        this.saving = false;
      },
      (error) => {
        this.error = error;
        this.saving = false;
      }
    );
  }

    next() {
      console.log(this.candidateCertifications);
      // TODO check if the form is not empty and warn the user
      this.router.navigate(['register', 'additional-information']);
    }

}
