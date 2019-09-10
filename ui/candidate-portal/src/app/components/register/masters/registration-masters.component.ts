import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {Router} from "@angular/router";

@Component({
  selector: 'app-registration-masters',
  templateUrl: './registration-masters.component.html',
  styleUrls: ['./registration-masters.component.scss']
})
export class RegistrationMastersComponent implements OnInit {

  form: FormGroup;

  constructor(private fb: FormBuilder,
                          private router: Router ) { }

  ngOnInit() {
    this.form = this.fb.group({

     })
  }



}
