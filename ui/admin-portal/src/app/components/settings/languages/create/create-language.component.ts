import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {Language} from "../../../../model/language";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {LanguageService} from "../../../../services/language.service";

@Component({
  selector: 'app-create-language',
  templateUrl: './create-language.component.html',
  styleUrls: ['./create-language.component.scss']
})

export class CreateLanguageComponent implements OnInit {

  languageForm: FormGroup;
  error;
  saving: boolean;

  constructor(private activeModal: NgbActiveModal,
              private fb: FormBuilder,
              private languageService: LanguageService) {
  }

  ngOnInit() {
    this.languageForm = this.fb.group({
      name: [null, Validators.required],
      status: [null, Validators.required],
    });
  }

  onSave() {
    this.saving = true;
    this.languageService.create(this.languageForm.value).subscribe(
      (language) => {
        this.closeModal(language)
        this.saving = false;
      },
      (error) => {
        this.error = error;
        this.saving = false;
      });
  }

  closeModal(language: Language) {
    this.activeModal.close(language);
  }

  dismiss() {
    this.activeModal.dismiss(false);
  }
}
