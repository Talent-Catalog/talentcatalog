/*
 * Copyright (c) 2025 Talent Catalog.
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

import {AfterViewInit, Directive, ElementRef, Input, Renderer2} from '@angular/core';

@Directive({
  selector: '[appReadOnlyInputs]'
})
export class ReadOnlyInputsDirective implements AfterViewInit {
  @Input('appReadOnlyInputs') isReadonly: boolean;

  constructor(private el: ElementRef, private renderer: Renderer2) {}

  ngAfterViewInit(): void {
    // Delay to allow for accordion panels/view to render
    setTimeout(() => this.setReadonlyState(this.el.nativeElement, this.isReadonly));
  }

  private setReadonlyState(root: HTMLElement, isReadonly: boolean): void {
    const inputTags = ['NG-SELECT, TEXTAREA', 'INPUT', 'APP-DATE-PICKER'];

    const elements = root.querySelectorAll(inputTags.join(','));
    elements.forEach((element: HTMLElement) => {
      if (isReadonly) {
        this.renderer.setAttribute(element, 'disabled', 'true');
        if (element.tagName === 'NG-SELECT') {
          this.renderer.addClass(element, 'read-only')
        }
      } else {
        this.renderer.removeAttribute(element, 'disabled', 'false');
        if (element.tagName === 'NG-SELECT') {
          this.renderer.removeClass(element, 'read-only')
        }
      }
    });
  }
}
