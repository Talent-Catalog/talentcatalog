import {Pipe, PipeTransform} from '@angular/core';

@Pipe({
  name: 'pascalCase'
})
export class PascalCasePipe implements PipeTransform {

  transform(value: string): string {
    if (!value) return '';
    return value
      .toLowerCase()
      .replace(/(?:^|\s|_|-)(\w)/g, (_, c) => (c ? c.toUpperCase() : ''))
      .replace(/(\w)(\w*)/g, (_, first, rest) => first.toUpperCase() + rest);
  }

}
