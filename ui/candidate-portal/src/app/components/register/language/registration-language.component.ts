import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {Router} from "@angular/router";

@Component({
  selector: 'app-registration-language',
  templateUrl: './registration-language.component.html',
  styleUrls: ['./registration-language.component.scss']
})
export class RegistrationLanguageComponent implements OnInit {

  form: FormGroup;
  languages: string[];

  constructor(private fb: FormBuilder,
              private router: Router) { }

  ngOnInit() {
    this.form = this.fb.group({
      speakEnglish: ['', Validators.required],
      readWriteEnglish: ['', Validators.required],
      otherLanguages: ['', Validators]
    })
  }

  addMore(){
    // TODO add another language

  }

   save() {
      // TODO save
      this.router.navigate(['register', 'certifications']);
    }

}
