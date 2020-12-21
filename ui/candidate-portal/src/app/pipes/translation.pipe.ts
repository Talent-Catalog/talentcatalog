/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

import {Pipe, PipeTransform} from '@angular/core';
import {LanguageService} from "../services/language.service";

//todo This is not used. ngx-translate does translation - but if this is not here
//the build on Bitkeeper fails because no translate pipe is found.
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
