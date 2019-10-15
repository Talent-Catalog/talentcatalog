import {Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges} from '@angular/core';
import {Language} from "../../../../model/language";
import {LanguageService} from "../../../../services/language.service";
import {LanguageLevel} from "../../../../model/language-level";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {LanguageLevelService} from "../../../../services/language-level.service";
import {LanguageLevelFormControlModel} from "./language-level-form-control-model";

@Component({
  selector: 'app-language-level-form-control',
  templateUrl: './language-level-form-control.component.html',
  styleUrls: ['./language-level-form-control.component.scss']
})
export class LanguageLevelFormControlComponent implements OnInit, OnChanges {

  @Input() model: LanguageLevelFormControlModel;
  @Input() languageDisabled: boolean;
  @Input() languageLevels: LanguageLevel[];
  @Input() languages: Language[];
  @Input() spokenLevel: LanguageLevel;
  @Input() writtenLevel: LanguageLevel;

  @Output() modelUpdated = new EventEmitter<LanguageLevelFormControlModel>();

  showMenu: boolean;
  form: FormGroup;

  constructor(private languageService: LanguageService,
              private languageLevelService: LanguageLevelService,
              private fb: FormBuilder) { }

  ngOnInit() {
    this.form = this.fb.group({
      languageId: [this.model ? this.model.languageId : null, Validators.required],
      writtenLevelId: [this.model ? this.model.writtenLevelId : null, Validators.required],
      spokenLevelId: [this.model ? this.model.spokenLevelId : null, Validators.required],
    });
    if (this.languageDisabled) {
      this.form.controls['languageId'].disable()
    }

    /* Load missing language */
    if (!this.languages) {
      this.languageService.listLanguages().subscribe(
        (response) => {
          this.languages = response;
        },
        (error) => {
          console.error(error);
        });
    }

    /* Load missing language levels */
    if (!this.languageLevels) {
      this.languageLevelService.listLanguageLevels().subscribe(
        (response) => {
          this.languageLevels = response;
        },
        (error) => {
          console.log('error', error);
        });
    }

    /* Subscribe to form value changes to emit updates to parent component */
    this.form.valueChanges.subscribe(
      () => {
        this.modelUpdated.emit(this.form.value);
      },
      (error) => {
          console.log('error', error);
      });
  }

  ngOnChanges(c: SimpleChanges) {
    if (c.model && c.model.currentValue !== c.model.previousValue) {
      this.form.patchValue(c.model.currentValue);
    }
  }

  toggle() {
    this.showMenu = !this.showMenu;
  }

  open() {
    this.showMenu = true;
  }

  close() {
    this.showMenu = false;
  }

  renderLevel() {
    const val = (this.form.value as LanguageLevelFormControlModel);
    const language = val.languageId ? this.languages.find(l => l.id == val.languageId).name : '';
    const written = val.writtenLevelId ? 'Written: ' + this.languageLevels.find(l => l.id == val.writtenLevelId).level : '';
    const spoken = val.spokenLevelId ? 'Spoken: ' + this.languageLevels.find(l => l.id == val.spokenLevelId).level : '';
    const proficiencyString = written && spoken ? written + ', ' + spoken : written || spoken;
    return language && proficiencyString ? `${language} (${proficiencyString})` : language ? language : proficiencyString;
  }
}
