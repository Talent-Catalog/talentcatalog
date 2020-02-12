import {Component, EventEmitter, Input, Output} from '@angular/core';
import {CandidateLanguage} from "../../../model/candidate-language";
import {Language} from "../../../model/language";
import {LanguageLevel} from "../../../model/language-level";

@Component({
  selector: 'app-candidate-language-card',
  templateUrl: './candidate-language-card.component.html',
  styleUrls: ['./candidate-language-card.component.scss']
})
export class CandidateLanguageCardComponent {

  @Input() language: CandidateLanguage;
  @Output() languageChange = new EventEmitter<CandidateLanguage>();

  @Input() preview: boolean = false;
  @Input() english: Language;
  @Input() languages: Language[];
  @Input() languageLevels: LanguageLevel[];

  @Output() onDelete = new EventEmitter();

  constructor() { }

  delete() {
    this.onDelete.emit();
  }

  getLanguageName(id?: number) {
    const l = this.language;
    if (l && l.language && l.language.name) {
      return l.language.name
    } else if (id) {
      return this.languages.find(lang => lang.id == id).name;
    }
    return '';
  }

  isEnglish(id?: number) {
    if (id) {
      return id == this.english.id;
    }
    return false;
  }
}
