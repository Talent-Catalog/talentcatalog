import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators, FormArray} from "@angular/forms";
import {Router} from "@angular/router";
import {languageList} from "../../../model/languageList";
import {Language} from "../../../model/language";
import {LanguageService} from "../../../services/language.service";
import {CandidateService} from "../../../services/candidate.service";


@Component({
  selector: 'app-registration-language',
  templateUrl: './registration-language.component.html',
  styleUrls: ['./registration-language.component.scss']
})
export class RegistrationLanguageComponent implements OnInit {

  error: any;
  loading: boolean;
  saving: boolean;
  form: FormGroup;
  languages: Language[];
  languageList: string[];

  constructor(private fb: FormBuilder,
              private router: Router,
              private candidateService: CandidateService,
              private languageService: LanguageService,) { }

  ngOnInit() {
    this.languages = []
    this.saving = false;
    this.loading = true;
    this.languageList = languageList;

    this.form = this.fb.group({
      name: ['English', Validators.required],
      speak: ['', Validators.required],
      readWrite: ['', Validators.required],
      bilingual: ['', Validators.required]
      })
    };

    // TODO get method to retrieve old entry
    // ADD ANOTHER LANGUAGE
    addMore() {
      this.saving = true;
      this.languageService.createLanguage(this.form.value).subscribe(
        (response) => {
          console.log(response);
          this.languages.push(response);
          this.saving = false;
        },
        (error) => {
          this.error = error;
          this.saving = false;
        }
      );
    }

    // SAVE FORM
    save() {
      this.addMore()
      this.router.navigate(['register', 'certifications']);
    }

}
