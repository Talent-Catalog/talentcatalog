/*
 * Copyright (c) 2024 Talent Catalog.
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
export class CandidateLanguageCardComponent implements OnChanges {

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
      this.translatedLanguageName = this.lookupLanguageName(this.language);
    }
  }

  // Lookup the language name given a CandidateLanguage
  lookupLanguageName(language: CandidateLanguage): string {
    if (language?.language?.id) {
      return this.languages?.find(lang => lang.id === language.language.id)?.name || '';
    } else if (language?.languageId) {
      return this.languages?.find(lang => lang.id === language.languageId)?.name || '';
    }
    return '';
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
