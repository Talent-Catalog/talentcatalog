/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

import {InputFilterDirective} from './input-filter.directive';
import {ElementRef} from '@angular/core';

describe('InputFilterDirective', () => {

  let input: HTMLInputElement;
  let directive: InputFilterDirective;

  beforeEach(() => {
    input = document.createElement('input');
    directive = new InputFilterDirective(
      new ElementRef<HTMLInputElement>(input)
    );
  });


  it('should create an instance', () => {
    expect(directive).toBeTruthy();
  });

  it('should create an instance', () => {
    const mockElementRef = new ElementRef(document.createElement('input'));
    const directive = new InputFilterDirective(mockElementRef);
    expect(directive).toBeTruthy();
  });

  it('should remove non-digit characters using the default pattern', () => {
    const event = jasmine.createSpyObj('event', ['stopPropagation']);
    input.value = 'abc123-def456';

    directive.onInputChange(event);

    expect(input.value).toBe('123456');
    expect(event.stopPropagation).toHaveBeenCalledTimes(1);
  });

  it('should not stop propagation when the value does not change', () => {
    const event = jasmine.createSpyObj('event', ['stopPropagation']);
    input.value = '123456';

    directive.onInputChange(event);

    expect(input.value).toBe('123456');
    expect(event.stopPropagation).not.toHaveBeenCalled();
  });

  it('should support a custom filter pattern', () => {
    const event = jasmine.createSpyObj('event', ['stopPropagation']);
    directive.filterPattern = /[^a-z]/gi;
    input.value = 'Ehsan-123';

    directive.onInputChange(event);

    expect(input.value).toBe('Ehsan');
    expect(event.stopPropagation).toHaveBeenCalledTimes(1);
  });

  it('should handle an empty input value', () => {
    const event = jasmine.createSpyObj('event', ['stopPropagation']);
    input.value = '';

    directive.onInputChange(event);

    expect(input.value).toBe('');
    expect(event.stopPropagation).not.toHaveBeenCalled();
  });

  it('should remove whitespace with a custom pattern', () => {
    const event = jasmine.createSpyObj('event', ['stopPropagation']);
    directive.filterPattern = /\s/g;
    input.value = 'Talent Catalog';

    directive.onInputChange(event);

    expect(input.value).toBe('TalentCatalog');
    expect(event.stopPropagation).toHaveBeenCalled();
  });

});

