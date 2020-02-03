import {Pipe, PipeTransform} from '@angular/core';
import {LanguageService} from "../services/language.service";

@Pipe({
  name: 'translation'
})
export class TranslationPipe implements PipeTransform {

  constructor(private languageService: LanguageService) {

  }

  transform(object, args?: any): any {
    if (object) {
      return this.languageService.getTranslation(object, args);
    }
    return null;

  }



}
