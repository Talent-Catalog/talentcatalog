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


import {saveBlob} from "./file";

describe('saveBlob', () => {
  let blob: Blob;
  let filename: string;

  beforeEach(() => {
    blob = new Blob(['Hello, world!'], { type: 'text/plain' });
    filename = 'hello.txt';
  });

  it('should call msSaveBlob if navigator.msSaveBlob is available', () => {
    const msSaveBlobMock = jasmine.createSpy('msSaveBlob').and.returnValue(true);
    (navigator as any).msSaveBlob = msSaveBlobMock;

    saveBlob(blob, filename);

    expect(msSaveBlobMock).toHaveBeenCalledWith(blob, filename);
  });

  it('should create a link element and set attributes correctly if msSaveBlob is not available', () => {
    const link = document.createElement('a');
    const createElementSpy = spyOn(document, 'createElement').and.returnValue(link);
    const removeChildSpy = spyOn(document.body, 'removeChild').and.callThrough();

    (navigator as any).msSaveBlob = undefined; // Ensure msSaveBlob is not available

    saveBlob(blob, filename);

    expect(createElementSpy).toHaveBeenCalledWith('a');
    expect(link.href.startsWith('blob:')).toBeTrue();
    expect(link.download).toBe(filename);
    expect(link.style.visibility).toBe('hidden');
    expect(removeChildSpy).toHaveBeenCalledWith(link);
  });

});
