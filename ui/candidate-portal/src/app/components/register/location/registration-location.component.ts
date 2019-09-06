import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {Router} from "@angular/router";

@Component({
  selector: 'app-registration-location',
  templateUrl: './registration-location.component.html',
  styleUrls: ['./registration-location.component.scss']
})
export class RegistrationLocationComponent implements OnInit {

  form: FormGroup;

  constructor(private fb: FormBuilder,
              private router: Router) { }

  ngOnInit() {
    this.form = this.fb.group({
      country: ['', Validators.required],
      city: ['', Validators.required],
      yearOfArrival: ['', Validators.required]
    })
  }

  save() {
    // TODO save
    this.router.navigate(['register', 'nationality']);
  }

}
