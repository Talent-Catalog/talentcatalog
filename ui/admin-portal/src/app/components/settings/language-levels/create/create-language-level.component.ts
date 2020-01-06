import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {LanguageLevel} from "../../../../model/language-level";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {LanguageLevelService} from "../../../../services/language-level.service";

@Component({
  selector: 'app-create-language-level',
  templateUrl: './create-language-level.component.html',
  styleUrls: ['./create-language-level.component.scss']
})

export class CreateLanguageLevelComponent implements OnInit {

  languageLevelForm: FormGroup;
  error;
  saving: boolean;

  constructor(private activeModal: NgbActiveModal,
              private fb: FormBuilder,
              private languageLevelService: LanguageLevelService) {
  }

  ngOnInit() {
    this.languageLevelForm = this.fb.group({
      level: [null, Validators.required],
      name: [null, Validators.required],
      status: [null, Validators.required],
    });
  }

  onSave() {
    this.saving = true;
    this.languageLevelService.create(this.languageLevelForm.value).subscribe(
      (languageLevel) => {
        this.closeModal(languageLevel)
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
