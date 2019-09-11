import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {Router} from "@angular/router";

@Component({
  selector: 'app-registration-certifications',
  templateUrl: './registration-certifications.component.html',
  styleUrls: ['./registration-certifications.component.scss']
})
export class RegistrationCertificationsComponent implements OnInit {

  form: FormGroup;

  constructor(private fb: FormBuilder,
              private router: Router ) { }

  ngOnInit() {
    this.form = this.fb.group({
      certification: ['', Validators.required],
      institution: ['', Validators.required],
      dateOfReceipt: ['', Validators.required]
    })
  }

  save() {
    console.log(this.form);
    this.router.navigate(['register', 'additional-information']);
  }


}
