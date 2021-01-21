import {formatDate} from '@angular/common';

export function dateString(dateObj: Date): string {
  return formatDate(dateObj, 'dd MMM yy' , 'en-US');
}
