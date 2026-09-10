/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 *  the terms of the GNU General Public License as published by the Free
 *  Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

import {NgxWigComponent} from 'ngx-wig';

import {CUSTOM_CLEAR_FORMAT_BUTTON} from './clear-format';

describe('CUSTOM_CLEAR_FORMAT_BUTTON', () => {
  it('should expose the clear-format button configuration', () => {
    const button = CUSTOM_CLEAR_FORMAT_BUTTON.clearFormat;

    expect(button.label).toBe('Clear Format');
    expect(button.title).toBe('Clear Formatting');
    expect(button.styleClass).toBe('nw-button');
    expect(button.icon).toBe('fas fa-remove-format');
  });

  it('should replace the selected formatted content with plain text', () => {
    const fragment = document.createDocumentFragment();
    const formattedNode = document.createElement('strong');
    fragment.appendChild(formattedNode);

    const range = jasmine.createSpyObj<Range>(
      'Range',
      [
        'cloneContents',
        'deleteContents',
        'insertNode',
        'setStartAfter',
        'collapse'
      ]
    );
    range.cloneContents.and.returnValue(fragment);

    const selection = jasmine.createSpyObj<Selection>(
      'Selection',
      ['getRangeAt', 'removeAllRanges', 'addRange']
    );
    Object.defineProperty(selection, 'rangeCount', {value: 1});
    selection.getRangeAt.and.returnValue(range);
    spyOn(window, 'getSelection').and.returnValue(selection);

    const tempDiv = document.createElement('div');
    Object.defineProperty(tempDiv, 'innerText', {
      configurable: true,
      value: 'Plain text'
    });
    spyOn(document, 'createElement').and.returnValue(tempDiv);

    const textNode = document.createTextNode('Plain text');
    const createTextNodeSpy = spyOn(document, 'createTextNode')
    .and.returnValue(textNode);

    CUSTOM_CLEAR_FORMAT_BUTTON.clearFormat.command(
      null as NgxWigComponent
    );

    expect(selection.getRangeAt).toHaveBeenCalledOnceWith(0);
    expect(range.cloneContents).toHaveBeenCalled();
    expect(range.deleteContents).toHaveBeenCalled();
    expect(tempDiv.firstChild).toBe(formattedNode);
    expect(createTextNodeSpy).toHaveBeenCalledOnceWith('Plain text');
    expect(range.insertNode).toHaveBeenCalledOnceWith(textNode);
    expect(range.setStartAfter).toHaveBeenCalledOnceWith(textNode);
    expect(range.collapse).toHaveBeenCalledOnceWith(true);
    expect(selection.removeAllRanges).toHaveBeenCalled();
    expect(selection.addRange).toHaveBeenCalledOnceWith(range);
  });

  it('should alert the user when no text is selected', () => {
    spyOn(window, 'getSelection').and.returnValue(null);
    const alertSpy = spyOn(window, 'alert');

    CUSTOM_CLEAR_FORMAT_BUTTON.clearFormat.command(
      null as NgxWigComponent
    );

    expect(alertSpy).toHaveBeenCalledOnceWith(
      'Please select some text to clear formatting.'
    );
  });
});
