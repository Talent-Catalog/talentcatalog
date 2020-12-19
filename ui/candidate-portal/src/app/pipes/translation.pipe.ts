/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

import {Pipe, PipeTransform} from '@angular/core';
import {LanguageService} from "../services/language.service";

//todo This is not used - but maybe needs to be here
@Pipe({
  name: 'translation'
})
export class TranslationPipe implements PipeTransform {

  constructor(private languageService: LanguageService) {

  }

  transform(object, args?: any): any {
    return null;

  }



}
