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

    function setMsSaveBlob(value: ((blob: Blob, defaultName?: string) => boolean) | undefined): void {
        Object.defineProperty(navigator, 'msSaveBlob', {
            configurable: true,
            value
        });
    }

    beforeEach(() => {
        blob = new Blob(['test'], { type: 'application/pdf' });
        originalMsSaveBlob =
            Object.getOwnPropertyDescriptor(navigator, 'msSaveBlob');
    });

    afterEach(() => {
        if (originalMsSaveBlob) {
            Object.defineProperty(navigator, 'msSaveBlob', originalMsSaveBlob);
        }
        else {
            delete navigator.msSaveBlob;
        }
    });

    it('uses msSaveBlob when the browser provides it', () => {
        const msSaveBlob = vi.fn().mockName('msSaveBlob').mockReturnValue(true);
        setMsSaveBlob(msSaveBlob);

        saveBlob(blob, filename);

        expect(msSaveBlob).toHaveBeenCalledTimes(1);

        expect(msSaveBlob).toHaveBeenCalledWith(blob, filename);
    });

    it('creates, clicks, and removes a download link in modern browsers', () => {
        setMsSaveBlob(undefined);
        const link = document.createElement('a');
        const createElementSpy = vi.spyOn(document, 'createElement').mockReturnValue(link);
        const createObjectUrlSpy = vi.spyOn(URL, 'createObjectURL').mockReturnValue('blob:test-url');
        const appendChildSpy = vi.spyOn(document.body, 'appendChild').mockReturnValue(link);
        const clickSpy = vi.spyOn(link, 'click').mockReturnValue(undefined);
        const removeChildSpy = vi.spyOn(document.body, 'removeChild').mockReturnValue(link);

        saveBlob(blob, filename);

        expect(createElementSpy).toHaveBeenCalledTimes(1);

        expect(createElementSpy).toHaveBeenCalledWith('a');
        expect(createObjectUrlSpy).toHaveBeenCalledTimes(1);
        expect(createObjectUrlSpy).toHaveBeenCalledWith(blob);
        expect(link.getAttribute('href')).toBe('blob:test-url');
        expect(link.getAttribute('download')).toBe(filename);
        expect(link.style.visibility).toBe('hidden');
        expect(appendChildSpy).toHaveBeenCalledTimes(1);
        expect(appendChildSpy).toHaveBeenCalledWith(link);
        expect(clickSpy).toHaveBeenCalled();
        expect(removeChildSpy).toHaveBeenCalledTimes(1);
        expect(removeChildSpy).toHaveBeenCalledWith(link);
    });

    it('does nothing further when the download attribute is unsupported', () => {
        setMsSaveBlob(undefined);
        const unsupportedLink = { download: undefined } as HTMLAnchorElement;
        vi.spyOn(document, 'createElement').mockReturnValue(unsupportedLink);
        const createObjectUrlSpy = vi.spyOn(URL, 'createObjectURL').mockReturnValue(undefined);

        saveBlob(blob, filename);

        expect(createObjectUrlSpy).not.toHaveBeenCalled();
    });
});
