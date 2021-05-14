import {Component, OnInit} from '@angular/core';
import {CandidateLanguage} from "../../../../../model/candidate-language";
import {FormBuilder, FormGroup} from "@angular/forms";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {LanguageService} from "../../../../../services/language.service";
import {
  CandidateLanguageService,
  CreateCandidateLanguageRequest
} from "../../../../../services/candidate-language.service";
import {CountryService} from "../../../../../services/country.service";
import {LanguageLevelService} from "../../../../../services/language-level.service";

@Component({
  selector: 'app-create-candidate-language',
  templateUrl: './create-candidate-language.component.html',
  styleUrls: ['./create-candidate-language.component.scss']
})
export class CreateCandidateLanguageComponent implements OnInit {

  candidateLanguage: CandidateLanguage;

  candidateForm: FormGroup;
  candidateId: number;
  languages = [];
  languageLevels = [];
  years = [];
  error;
  loading: boolean;
  saving: boolean;

  constructor(private activeModal: NgbActiveModal,
              private fb: FormBuilder,
              private languageService: LanguageService,
              private candidateLanguageService: CandidateLanguageService,
              private countryService: CountryService,
              private languageLevelService: LanguageLevelService) {
  }

  ngOnInit() {
    this.loading = true;

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

    this.candidateForm = this.fb.group({
      languageId: [null],
      spokenLevelId: [null],
      writtenLevelId: [null]
    });
    this.loading = false;
  }

  onSave() {
    this.saving = true;
    const request: CreateCandidateLanguageRequest = {
      candidateId: this.candidateId,
      languageId: this.candidateForm.value.languageId,
      spokenLevelId: this.candidateForm.value.spokenLevelId,
      writtenLevelId: this.candidateForm.value.writtenLevelId
    }
    this.candidateLanguageService.create(request).subscribe(
      (candidateLanguage) => {
        this.closeModal(candidateLanguage);
        this.saving = false;
      },
      (error) => {
        this.error = error;
        this.saving = false;
      });
  }

  closeModal(candidateLanguage: CandidateLanguage) {
    this.activeModal.close(candidateLanguage);
  }

  dismiss() {
    this.activeModal.dismiss(false);
  }

}
