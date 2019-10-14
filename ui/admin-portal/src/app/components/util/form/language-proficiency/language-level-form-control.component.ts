import {Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges} from '@angular/core';
import {Language} from "../../../../model/language";
import {LanguageService} from "../../../../services/language.service";
import {LanguageLevel} from "../../../../model/language-level";

@Component({
  selector: 'app-language-level-form-control',
  templateUrl: './language-level-form-control.component.html',
  styleUrls: ['./language-level-form-control.component.scss']
})
export class LanguageLevelFormControlComponent implements OnInit, OnChanges {

  @Input() language: Language;
  @Input() languageDisabled: boolean;
  @Input() languageLevels: LanguageLevel[];
  @Input() languages: Language[];

  @Output() change = new EventEmitter();

  showMenu: boolean;
  spokenLevel: LanguageLevel;
  writtenLevel: LanguageLevel;

  constructor(private languageService: LanguageService) { }

  ngOnInit() {
    if (!this.languages) {
      this.languageService.listLanguages().subscribe(
        (response) => {
          this.languages = response;
        },
        (error) => {
          console.error(error);
        });
    }
  }

  ngOnChanges(c: SimpleChanges) {
    if (c.language.currentValue !== c.language.previousValue
      || c.writtenLevel.currentValue !== c.writtenLevel.previousValue
      || c.spokenLevel.currentValue !== c.spokenLevel.previousValue) {
      // const result = {
      //   language: this.selectedLanguage,
      //   writtenLevel:
      // }
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
    return 'TODO: Render proficiency';
  }

  selectLanguage(language: Language) {
    // this.selectedLanguage =
  }

  selectLanguageLevel(level: LanguageLevel) {

  }
}
