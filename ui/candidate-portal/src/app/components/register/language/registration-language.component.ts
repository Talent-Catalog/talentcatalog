import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators, FormArray} from "@angular/forms";
import {Router} from "@angular/router";
import {languages} from "../../../model/languages";

@Component({
  selector: 'app-registration-language',
  templateUrl: './registration-language.component.html',
  styleUrls: ['./registration-language.component.scss']
})
export class RegistrationLanguageComponent implements OnInit {

  form: FormGroup;
  otherLanguages: FormArray;
  languages: string[];

  constructor(private fb: FormBuilder,
              private router: Router) { }

  ngOnInit() {
    this.languages = languages;
    this.form = this.fb.group({
      speakEnglish: ['', Validators.required],
      readWriteEnglish: ['', Validators.required],
      bilingual: [false, Validators.required],
      otherLanguages: this.fb.array([
        this.fb.group({
          language: [''],
          speakLanguage: [''],
          readWriteLanguage: ['']
         })
        ])
      })
  }

  // ADD ANOTHER LANGUAGE
  addMore() {
    this.otherLanguages = this.form.controls.otherLanguages as FormArray;
    this.otherLanguages.push(this.fb.group({
      language: [''],
      speakLanguage: [''],
      readWriteLanguage: [''],
    }));
    console.log(this.otherLanguages);
  }

   save() {
      // TODO save
      console.log(this.form);
      this.router.navigate(['register', 'certifications']);
    }

}
