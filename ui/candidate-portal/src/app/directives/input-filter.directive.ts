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

import {Directive, ElementRef, HostListener, Input} from '@angular/core';

/*
** A directive designed to be used on input[type="text"] elements to filter text based on the provided regular
** expression.
**/

@Directive({
  selector: '[appInputFilter]'
})
export class InputFilterDirective {

  // A regular expression to determine which characters should be filtered out
  // Default: Non-digit characters
  @Input() filterPattern: any = /[^0-9]*/g;

  // List for oninput events and filter out characters based on pattern
  @HostListener('input', ['$event']) onInputChange(event) {
    const val = this._el.nativeElement.value;
    this._el.nativeElement.value = val.replace(this.filterPattern, '');
    // Stop the input event from propagating to the element
    if (val !== this._el.nativeElement.value) {
      event.stopPropagation();
    }
  }

  constructor(private _el: ElementRef) { }

}
