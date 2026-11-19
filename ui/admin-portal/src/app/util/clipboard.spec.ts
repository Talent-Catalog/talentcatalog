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
import {copyToClipboard} from "./clipboard";
import {expect, vi} from 'vitest';

describe('copyToClipboard', () => {
    let execCommandSpy: Mock;
    let appendChildSpy: Mock;
    let removeChildSpy: Mock;

    beforeEach(() => {
        execCommandSpy = vi.spyOn(document, 'execCommand').mockReturnValue(true);
        appendChildSpy = vi.spyOn(document.body, 'appendChild');
        removeChildSpy = vi.spyOn(document.body, 'removeChild');
    });

    it('should create a textarea element and append it to the document body', () => {
        copyToClipboard('test');
        expect(appendChildSpy).toHaveBeenCalled();
    });

    it('should set the value of the textarea element to the provided string', () => {
        const textarea = document.createElement('textarea');
        vi.spyOn(document, 'createElement').mockReturnValue(textarea);

        copyToClipboard('test');
        expect(textarea.value).toBe('test');
    });

    it('should set the correct styles for the textarea element', () => {
        const textarea = document.createElement('textarea');
        vi.spyOn(document, 'createElement').mockReturnValue(textarea);

        copyToClipboard('test');
        expect(textarea.style.position).toBe('fixed');
        expect(textarea.style.left).toBe('0px');
        expect(textarea.style.top).toBe('0px');
        expect(textarea.style.opacity).toBe('0');
    });

    it('should focus and select the textarea element', () => {
        const textarea = document.createElement('textarea');
        vi.spyOn(document, 'createElement').mockReturnValue(textarea);
        vi.spyOn(textarea, 'focus').mockReturnValue(undefined);
        vi.spyOn(textarea, 'select').mockReturnValue(undefined);

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
