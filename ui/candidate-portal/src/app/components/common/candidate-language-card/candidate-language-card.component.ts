/*
 * Copyright (c) 2021 Talent Beyond Boundaries.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

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

  // No long need this method? Replaced with getLangName in order for translations to work.
  getLanguageName(id?: number) {
    const l = this.language;
    if (l && l.language && l.language.name) {
      return l.language.name
    } else if (id) {
      return this.languages.find(lang => lang.id == id).name;
    }
    return '';
  }

  getLangName(language: Language) {
    return this.languages?.find(l => l.id === language.id)?.name;
  }

  getLangLevel(level: LanguageLevel) {
    return this.languageLevels?.find(ll => ll.id === level.id)?.name;
  }

  isEnglish(id?: number) {
    if (id) {
      return id == this.english.id;
    }
    return false;
  }
}
