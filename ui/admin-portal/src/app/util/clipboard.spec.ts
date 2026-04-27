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

import {copyToClipboard} from "./clipboard";

describe('copyToClipboard', () => {
  let execCommandSpy: jasmine.Spy;
  let appendChildSpy: jasmine.Spy;
  let removeChildSpy: jasmine.Spy;

  beforeEach(() => {
    execCommandSpy = spyOn(document, 'execCommand').and.returnValue(true);
    appendChildSpy = spyOn(document.body, 'appendChild').and.callThrough();
    removeChildSpy = spyOn(document.body, 'removeChild').and.callThrough();
  });

  it('should create a textarea element and append it to the document body', () => {
    copyToClipboard('test');
    expect(appendChildSpy).toHaveBeenCalled();
  });

  it('should set the value of the textarea element to the provided string', () => {
    const textarea = document.createElement('textarea');
    spyOn(document, 'createElement').and.returnValue(textarea);

    copyToClipboard('test');
    expect(textarea.value).toBe('test');
  });

  it('should set the correct styles for the textarea element', () => {
    const textarea = document.createElement('textarea');
    spyOn(document, 'createElement').and.returnValue(textarea);

    copyToClipboard('test');
    expect(textarea.style.position).toBe('fixed');
    expect(textarea.style.left).toBe('0px');
    expect(textarea.style.top).toBe('0px');
    expect(textarea.style.opacity).toBe('0');
  });

  it('should focus and select the textarea element', () => {
    const textarea = document.createElement('textarea');
    spyOn(document, 'createElement').and.returnValue(textarea);
    spyOn(textarea, 'focus');
    spyOn(textarea, 'select');

    copyToClipboard('test');
    expect(textarea.focus).toHaveBeenCalled();
    expect(textarea.select).toHaveBeenCalled();
  });

  it('should execute the copy command', () => {
    copyToClipboard('test');
    expect(execCommandSpy).toHaveBeenCalledWith('copy');
  });

  it('should remove the textarea element from the document body', () => {
    copyToClipboard('test');
    expect(removeChildSpy).toHaveBeenCalled();
  });
});
