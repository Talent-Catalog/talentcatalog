import {Inject, LOCALE_ID, Pipe, PipeTransform} from '@angular/core';
import {DatePipe} from '@angular/common';


// See this stack overflow article explaining this code: https://stackoverflow.com/questions/56020473/override-angular-default-date-pipe
@Pipe({
  name: 'date'
})
export class ExtendDatePipe extends DatePipe implements PipeTransform {
  readonly customFormats = {
    customDefault: 'dd MMM yy',
    customDateTime: 'dd MMM yy, h:mm:ss a',
    customMonthYear: 'MMM yy'
  };

  constructor(@Inject(LOCALE_ID) locale: string) {
    super(locale);
  }

  transform(value: any, format = 'customDefault', timezone?: string, locale?: string): string {
    format = this.customFormats[format] || format;

    return super.transform(value, format, timezone, locale);
  }
}
