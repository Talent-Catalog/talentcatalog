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

import type {Mock} from "vitest";
import {SafePipe} from './safe.pipe';
import {DomSanitizer} from '@angular/platform-browser';
import {expect, vi} from 'vitest';

describe('SafePipe', () => {
    let pipe: SafePipe;
    let sanitizer: DomSanitizer;

    beforeEach(() => {
        sanitizer = ({
            bypassSecurityTrustResourceUrl: vi.fn().mockName("DomSanitizer.bypassSecurityTrustResourceUrl")
        } as unknown as typeof sanitizer);
        pipe = new SafePipe(sanitizer);
    });

    it('should create an instance', () => {
        expect(pipe).toBeTruthy();
    });

    it('should call DomSanitizer.bypassSecurityTrustResourceUrl', () => {
        const url = 'https://example.com/image.jpg';
        const safeUrl = 'safe-url';
        (sanitizer.bypassSecurityTrustResourceUrl as Mock).mockReturnValue(safeUrl);

        const result = pipe.transform(url);

        expect(sanitizer.bypassSecurityTrustResourceUrl).toHaveBeenCalledWith(url);
        expect(result).toEqual(safeUrl);
    });

    it('should return null for empty url', () => {
        const url = '';
        (sanitizer.bypassSecurityTrustResourceUrl as Mock).mockReturnValue(null);

        const result = pipe.transform(url);

        expect(sanitizer.bypassSecurityTrustResourceUrl).toHaveBeenCalledWith(url);
        expect(result).toBeNull();
    });

    it('should return null for null url', () => {
        const url = null;
        (sanitizer.bypassSecurityTrustResourceUrl as Mock).mockReturnValue(null);

        const result = pipe.transform(url);

        expect(sanitizer.bypassSecurityTrustResourceUrl).toHaveBeenCalledWith(url);
        expect(result).toBeNull();
    });

    it('should return null for undefined url', () => {
        const url = undefined;
        (sanitizer.bypassSecurityTrustResourceUrl as Mock).mockReturnValue(null);

        const result = pipe.transform(url);

        expect(sanitizer.bypassSecurityTrustResourceUrl).toHaveBeenCalledWith(url);
        expect(result).toBeNull();
    });
});
