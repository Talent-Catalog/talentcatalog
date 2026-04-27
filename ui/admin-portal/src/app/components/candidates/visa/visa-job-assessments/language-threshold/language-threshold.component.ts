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

import {Component, OnInit} from '@angular/core';
import {VisaCheckComponentBase} from "../../../../util/intake/VisaCheckComponentBase";
import {UntypedFormBuilder} from "@angular/forms";
import {CandidateVisaCheckService} from "../../../../../services/candidate-visa-check.service";
import {EnumOption, enumOptions} from "../../../../../util/enum";
import {YesNo} from "../../../../../model/candidate";
import {Language} from "../../../../../model/language";
import {LanguageService} from "../../../../../services/language.service";

@Component({
  selector: 'app-language-threshold',
  templateUrl: './language-threshold.component.html',
  styleUrls: ['./language-threshold.component.scss']
})
export class LanguageThresholdComponent extends VisaCheckComponentBase implements OnInit {
  languages: Language[];

//Drop down values for enumeration
  languagesThresholdMetOptions: EnumOption[] = enumOptions(YesNo);

  constructor(fb: UntypedFormBuilder,
              candidateVisaCheckService: CandidateVisaCheckService,
              private languageService: LanguageService) {
    super(fb, candidateVisaCheckService);
  }

  ngOnInit(): void {
    this.languageService.listLanguages().subscribe((result: Language[]): void => {
      // Put english first in list as it will be most common language
      const english: Language = result.find(x => x.name === "English");
      if (english) {
        result.splice(result.indexOf(english), 1);
        result.unshift(english);
      }
      this.languages = result;
    });

    this.form = this.fb.group({
      visaJobId: [this.visaJobCheck?.id],
      visaJobLanguagesRequired: [this.visaJobCheck?.languagesRequired],
      visaJobLanguagesThresholdMet: [this.visaJobCheck?.languagesThresholdMet],
      visaJobLanguagesThresholdNotes: [this.visaJobCheck?.languagesThresholdNotes],
    });
  }

  get hasRequiredLanguages(): boolean {
    return this.form.value.visaJobLanguagesRequired?.length > 0;
  }

  get hasNotes(): boolean {
    let found: boolean = false;
    if (this.form.value.visaJobLanguagesThresholdMet) {
      if (this.form.value.visaJobLanguagesThresholdMet === 'Yes') {
        found = true
      }
      if (this.form.value.visaJobLanguagesThresholdMet === 'No') {
        found = true
      }
    }
    return found;
  }

}
