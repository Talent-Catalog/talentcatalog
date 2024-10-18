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

import {Component, EventEmitter, Input, Output, OnChanges, SimpleChanges} from '@angular/core';
import {CandidateLanguage} from "../../../model/candidate-language";
import {Language} from "../../../model/language";
import {LanguageLevel} from "../../../model/language-level";

@Component({
  selector: 'app-candidate-language-card',
  templateUrl: './candidate-language-card.component.html',
  styleUrls: ['./candidate-language-card.component.scss']
})
export class CandidateLanguageCardComponent implements OnChanges{

  @Input() language: CandidateLanguage;
  @Output() languageChange = new EventEmitter<CandidateLanguage>();

  @Input() preview: boolean = false;
  @Input() english: Language;
  @Input() languages: Language[];
  @Input() languageLevels: LanguageLevel[];

  @Output() onDelete = new EventEmitter();
  translatedLanguageName: string = '';

  constructor() { }
  delete() {
    this.onDelete.emit();
  }
  // Watch for changes in the inputs and update the language name accordingly
  ngOnChanges(changes: SimpleChanges) {
    if (changes.language || changes.languages) {
      this.getLanguageName();
    }
  }

  // Compute the language name once, when inputs change
  getLanguageName() {
    const l = this.language;
    if (l?.language?.id) {
      this.translatedLanguageName = this.languages?.find(lang => lang.id === l.language?.id)?.name || '';
    } else if (l?.languageId) {
      this.translatedLanguageName = this.languages?.find(lang => lang.id === l.languageId)?.name || '';
    } else {
      this.translatedLanguageName = '';
    }
  }

  getLangLevel(level: LanguageLevel) {
    return this.languageLevels?.find(ll => ll.id === level?.id)?.name;
  }

  isEnglish(id?: number) {
    if (id) {
      return id == this.english?.id;
    }
    return false;
  }
}
