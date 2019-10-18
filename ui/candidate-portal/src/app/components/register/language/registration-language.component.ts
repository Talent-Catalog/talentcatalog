import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {Router} from "@angular/router";
import {CandidateLanguage} from "../../../model/candidate-language";
import {CandidateLanguageService} from "../../../services/candidate-language.service";
import {CandidateService} from "../../../services/candidate.service";
import {Language} from "../../../model/language";
import {LanguageService} from "../../../services/language.service";
import {LanguageLevel} from "../../../model/language-level";
import {LanguageLevelService} from "../../../services/language-level.service";
import {RegistrationService} from "../../../services/registration.service";


@Component({
  selector: 'app-registration-language',
  templateUrl: './registration-language.component.html',
  styleUrls: ['./registration-language.component.scss']
})
export class RegistrationLanguageComponent implements OnInit {

  error: any;
  _loading = {
    candidate: true,
    languages: true,
    lanuageLevels: true
  };
  saving: boolean;

  form: FormGroup;
  candidateLanguages: CandidateLanguage[];
  languages: Language[];
  languageLevels: LanguageLevel[];

  constructor(private fb: FormBuilder,
              private router: Router,
              private candidateService: CandidateService,
              private candidateLanguageService: CandidateLanguageService,
              private languageService: LanguageService,
              private languageLevelService: LanguageLevelService,
              private registrationService: RegistrationService) { }

  ngOnInit() {
    this.candidateLanguages = [];
    this.saving = false;
    this.form = this.fb.group({
      id: ['', Validators.required],
      spokenLevelId: ['', Validators.required],
      writtenLevelId: ['', Validators.required]
    });

    /* Load the languages */
    this.languageService.listLanguages().subscribe(
      (response) => {
        this.languages = response;
        this._loading.languages = false;
        this.loadCandidateLanguages();
      },
      (error) => {
        this.error = error;
        this._loading.languages = false;
      }
    );

    /* Load the language levels */
    this.languageLevelService.listLanguageLevels().subscribe(
      (response) => {
        this.languageLevels = response;
        this._loading.lanuageLevels = false;
      },
      (error) => {
        this.error = error;
        this._loading.lanuageLevels = false;
      }
    );
  }

  patchForm(lang: CandidateLanguage) {
    this.form.patchValue({
      id: lang.language ? lang.language.id : null,
      spokenLevelId: lang.spokenLevel ? lang.spokenLevel.id : null,
      writtenLevelId: lang.writtenLevel ? lang.writtenLevel.id : null
    });
  };

  loadCandidateLanguages() {
    this.candidateService.getCandidateLanguages().subscribe(
      (candidate) => {
        this.candidateLanguages = candidate.candidateLanguages || [];
        /* Check if the candidate has already filled in their english form, otherwise populate the initial form with English */
        let english: CandidateLanguage | Language = this.candidateLanguages.find(l => l.language.name.toLowerCase() == "english");

        if (english) {
          /* Path the form with the saved candidate language */
          this.patchForm(english);
        } else {
          /* Path the form with the english language id */
          english = this.languages.find(lang => lang.name.toLowerCase() === 'english');
          this.form.patchValue({id: english.id});
        }
        this._loading.candidate = false;
      },
      (error) => {
        this.error = error;
        this._loading.candidate = false;
      }
    );
  }

  // ADD ANOTHER LANGUAGE
  addMore() {
    this.saving = true;
    this.candidateLanguageService.createCandidateLanguage(this.form.value).subscribe(
      (response) => {
        this.candidateLanguages.push(response);
        this.saving = false;
      },
      (error) => {
        this.error = error;
        this.saving = false;
      }
    );
  }

  //DELETE LANGUAGE
  deleteCandidateLanguage(index: number) {
    this.candidateLanguages.splice(index, 1);
  }

  // SAVE FORM
  save(dir: string) {
    if (dir === 'next') {
      this.registrationService.next();
    } else {
      this.registrationService.back();
    }
  }

  back() {
    this.save('back');
  }

  next() {
    this.save('next');
  }

  get loading() {
    const l = this._loading;
    return l.candidate ||  l.languages || l.lanuageLevels;
  }

  /* Takes an optional language ID as param, otherwise assumes it's the forms value */
  getLanguageName(id?: number) {
    id = id || this.form.value.id;
    /* DEBUG */
    console.log('this.form.value', this.form.value);
    console.log('id', id);
    if (id) {
      return this.languages.find(lang => lang.id == id).name;
    }
    return '';
  }
}
