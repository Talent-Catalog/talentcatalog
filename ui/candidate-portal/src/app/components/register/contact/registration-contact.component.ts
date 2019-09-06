import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {Router} from "@angular/router";

@Component({
  selector: 'app-registration-contact',
  templateUrl: './registration-contact.component.html',
  styleUrls: ['./registration-contact.component.scss']
})
export class RegistrationContactComponent implements OnInit {

  form: FormGroup;

  constructor(private fb: FormBuilder,
              private router: Router) { }

  ngOnInit() {
    this.form = this.fb.group({
      email: ['', Validators.required]
    })
  }

  save() {
    // TODO save
    this.router.navigate(['register', 'contact', 'additional']);
  }

}
