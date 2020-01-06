import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {Language} from "../../../../model/language";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {LanguageService} from "../../../../services/language.service";

@Component({
  selector: 'app-edit-language',
  templateUrl: './edit-language.component.html',
  styleUrls: ['./edit-language.component.scss']
})
export class EditLanguageComponent implements OnInit {

  languageId: number;
  languageForm: FormGroup;
  error;
  loading: boolean;
  saving: boolean;

  constructor(private activeModal: NgbActiveModal,
              private fb: FormBuilder,
              private languageService: LanguageService) {
  }

  ngOnInit() {
    this.loading = true;
    this.languageService.get(this.languageId).subscribe(language => {
      this.languageForm = this.fb.group({
        name: [language.name, Validators.required],
        status: [language.status, Validators.required],
      });
      this.loading = false;
    });
  }

  onSave() {
    this.saving = true;
    this.languageService.update(this.languageId, this.languageForm.value).subscribe(
      (language) => {
        this.closeModal(language);
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
