import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {LanguageLevel} from "../../../../model/language-level";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {LanguageLevelService} from "../../../../services/language-level.service";

@Component({
  selector: 'app-edit-language-level',
  templateUrl: './edit-language-level.component.html',
  styleUrls: ['./edit-language-level.component.scss']
})
export class EditLanguageLevelComponent implements OnInit {

  languageLevelId: number;
  languageLevelForm: FormGroup;
  error;
  loading: boolean;
  saving: boolean;

  constructor(private activeModal: NgbActiveModal,
              private fb: FormBuilder,
              private languageLevelService: LanguageLevelService) {
  }

  ngOnInit() {
    this.loading = true;
    this.languageLevelService.get(this.languageLevelId).subscribe(languageLevel => {
      this.languageLevelForm = this.fb.group({
        level: [languageLevel.level, Validators.required],
        status: [languageLevel.status, Validators.required],
      });
      this.loading = false;
    });
  }

  onSave() {
    this.saving = true;
    this.languageLevelService.update(this.languageLevelId, this.languageLevelForm.value).subscribe(
      (languageLevel) => {
        this.closeModal(languageLevel);
        this.saving = false;
      },
      (error) => {
        this.error = error;
        this.saving = false;
      });
  }

  closeModal(languageLevel: LanguageLevel) {
    this.activeModal.close(languageLevel);
  }

  dismiss() {
    this.activeModal.dismiss(false);
  }
}
