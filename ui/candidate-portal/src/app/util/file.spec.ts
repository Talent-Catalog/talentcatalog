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

import {saveBlob} from './file';

describe('saveBlob', () => {
  const filename = 'report.pdf';
  let blob: Blob;
  let originalMsSaveBlob: PropertyDescriptor | undefined;

  function setMsSaveBlob(
    value: ((blob: Blob, defaultName?: string) => boolean) | undefined
  ): void {
    Object.defineProperty(navigator, 'msSaveBlob', {
      configurable: true,
      value
    });
  }

  beforeEach(() => {
    blob = new Blob(['test'], {type: 'application/pdf'});
    originalMsSaveBlob =
      Object.getOwnPropertyDescriptor(navigator, 'msSaveBlob');
  });

  afterEach(() => {
    if (originalMsSaveBlob) {
      Object.defineProperty(navigator, 'msSaveBlob', originalMsSaveBlob);
    } else {
      delete navigator.msSaveBlob;
    }
  });

  it('uses msSaveBlob when the browser provides it', () => {
    const msSaveBlob = jasmine.createSpy('msSaveBlob').and.returnValue(true);
    setMsSaveBlob(msSaveBlob);

    saveBlob(blob, filename);

    expect(msSaveBlob).toHaveBeenCalledOnceWith(blob, filename);
  });

  it('creates, clicks, and removes a download link in modern browsers', () => {
    setMsSaveBlob(undefined);
    const link = document.createElement('a');
    const createElementSpy = spyOn(document, 'createElement')
    .and.returnValue(link);
    const createObjectUrlSpy = spyOn(URL, 'createObjectURL')
    .and.returnValue('blob:test-url');
    const appendChildSpy = spyOn(document.body, 'appendChild')
    .and.returnValue(link);
    const clickSpy = spyOn(link, 'click');
    const removeChildSpy = spyOn(document.body, 'removeChild')
    .and.returnValue(link);

    saveBlob(blob, filename);

    expect(createElementSpy).toHaveBeenCalledOnceWith('a');
    expect(createObjectUrlSpy).toHaveBeenCalledOnceWith(blob);
    expect(link.getAttribute('href')).toBe('blob:test-url');
    expect(link.getAttribute('download')).toBe(filename);
    expect(link.style.visibility).toBe('hidden');
    expect(appendChildSpy).toHaveBeenCalledOnceWith(link);
    expect(clickSpy).toHaveBeenCalled();
    expect(removeChildSpy).toHaveBeenCalledOnceWith(link);
  });

  it('does nothing further when the download attribute is unsupported', () => {
    setMsSaveBlob(undefined);
    const unsupportedLink = {download: undefined} as HTMLAnchorElement;
    spyOn(document, 'createElement').and.returnValue(unsupportedLink);
    const createObjectUrlSpy = spyOn(URL, 'createObjectURL');

    saveBlob(blob, filename);

    expect(createObjectUrlSpy).not.toHaveBeenCalled();
  });
});
