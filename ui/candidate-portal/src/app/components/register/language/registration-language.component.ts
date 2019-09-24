import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators, FormArray} from "@angular/forms";
import {Router} from "@angular/router";
import {CandidateLanguage} from "../../../model/candidate-language";
import {CandidateLanguageService} from "../../../services/candidate-language.service";
import {CandidateService} from "../../../services/candidate.service";
import {Language} from "../../../model/language";
import {LanguageService} from "../../../services/language.service";
import {LanguageLevel} from "../../../model/language-level";
import {LanguageLevelService} from "../../../services/language-level.service";


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
  candidateLanguages: CandidateLanguage[];
  languages: Language[];
  languageLevels: LanguageLevel[];
  english: CandidateLanguage[];

  constructor(private fb: FormBuilder,
              private router: Router,
              private candidateService: CandidateService,
              private candidateLanguageService: CandidateLanguageService,
              private languageService: LanguageService,
              private languageLevelService: LanguageLevelService) { }

  ngOnInit() {
    this.candidateLanguages = []
    this.saving = false;
    this.loading = true;
    this.setUpForm();

    // TODO update language.id for English (currently using value 1)
    this.form.controls.languageId.setValue('1');

    /* Load the candidate data */
    this.candidateService.getCandidateLanguages().subscribe(
      (candidate) => {
        this.candidateLanguages = candidate.candidateLanguages || [];

        this.english = this.candidateLanguages.filter(l => l.language.name == "English");
         if(this.english.length !== 0){
          this.form.patchValue({
             speakId: this.english[0].speak.id,
             readWriteId: this.english[0].readWrite.id
           });
         }

        /* Load the languages */
        this.languageService.listLanguages().subscribe(
          (response) => {
            this.languages = response;
            this.loading = false;
          },
          (error) => {
            this.error = error;
            this.loading = false;
          }
        );

        /* Load the language levels */
        this.languageLevelService.listLanguageLevels().subscribe(
          (response) => {
            this.languageLevels = response;
            this.loading = false;
          },
          (error) => {
            this.error = error;
            this.loading = false;
          }
        );
       },
      (error) => {
          this.error = error;
          this.loading = false;
      }
    );
  }

  setUpForm(){
    this.form = this.fb.group({
      languageId: ['', Validators.required],
      speakId: ['', Validators.required],
      readWriteId: ['', Validators.required],
      bilingual: ['', Validators.required]
      })
  };

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
  delete(candidateLanguage){
    this.saving = true;
    this.candidateLanguageService.deleteCandidateLanguage(candidateLanguage.id).subscribe(
      () => {
        this.candidateLanguages = this.candidateLanguages.filter(c => c !== candidateLanguage);
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
    this.router.navigate(['register', 'certifications']);
  }

}
