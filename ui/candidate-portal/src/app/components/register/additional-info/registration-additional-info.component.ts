import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {Router} from "@angular/router";


@Component({
  selector: 'app-registration-additional-info',
  templateUrl: './registration-additional-info.component.html',
  styleUrls: ['./registration-additional-info.component.scss']
})
export class RegistrationAdditionalInfoComponent implements OnInit {

  form: FormGroup;

  constructor(private fb: FormBuilder,
              private router: Router ) { }

  ngOnInit() {
    this.form = this.fb.group({
      addInfo: ['']
    });
  }

  save(){
    this.router.navigate([''])
  }

}
