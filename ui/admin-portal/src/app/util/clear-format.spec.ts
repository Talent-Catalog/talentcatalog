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
import {expect, vi} from 'vitest';

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

        const range = {
            cloneContents: vi.fn().mockName("Range.cloneContents"),
            deleteContents: vi.fn().mockName("Range.deleteContents"),
            insertNode: vi.fn().mockName("Range.insertNode"),
            setStartAfter: vi.fn().mockName("Range.setStartAfter"),
            collapse: vi.fn().mockName("Range.collapse")
        };
        range.cloneContents.mockReturnValue(fragment);

        const selection = {
            getRangeAt: vi.fn().mockName("Selection.getRangeAt"),
            removeAllRanges: vi.fn().mockName("Selection.removeAllRanges"),
            addRange: vi.fn().mockName("Selection.addRange")
        };
        Object.defineProperty(selection, 'rangeCount', { value: 1 });
        selection.getRangeAt.mockReturnValue(range);
        vi.spyOn(window, 'getSelection').mockReturnValue((selection as unknown as Selection));

        const tempDiv = document.createElement('div');
        Object.defineProperty(tempDiv, 'innerText', {
            configurable: true,
            value: 'Plain text'
        });
        vi.spyOn(document, 'createElement').mockReturnValue(tempDiv);

        const textNode = document.createTextNode('Plain text');
        const createTextNodeSpy = vi.spyOn(document, 'createTextNode').mockReturnValue(textNode);

        CUSTOM_CLEAR_FORMAT_BUTTON.clearFormat.command(null as NgxWigComponent);

        expect(selection.getRangeAt).toHaveBeenCalledTimes(1);

        expect(selection.getRangeAt).toHaveBeenCalledWith(0);
        expect(range.cloneContents).toHaveBeenCalled();
        expect(range.deleteContents).toHaveBeenCalled();
        expect(tempDiv.firstChild).toBe(formattedNode);
        expect(createTextNodeSpy).toHaveBeenCalledTimes(1);
        expect(createTextNodeSpy).toHaveBeenCalledWith('Plain text');
        expect(range.insertNode).toHaveBeenCalledTimes(1);
        expect(range.insertNode).toHaveBeenCalledWith(textNode);
        expect(range.setStartAfter).toHaveBeenCalledTimes(1);
        expect(range.setStartAfter).toHaveBeenCalledWith(textNode);
        expect(range.collapse).toHaveBeenCalledTimes(1);
        expect(range.collapse).toHaveBeenCalledWith(true);
        expect(selection.removeAllRanges).toHaveBeenCalled();
        expect(selection.addRange).toHaveBeenCalledTimes(1);
        expect(selection.addRange).toHaveBeenCalledWith(range);
    });

    it('should alert the user when no text is selected', () => {
        vi.spyOn(window, 'getSelection').mockReturnValue(null);
        const alertSpy = vi.spyOn(window, 'alert').mockReturnValue(undefined);

        CUSTOM_CLEAR_FORMAT_BUTTON.clearFormat.command(null as NgxWigComponent);

        expect(alertSpy).toHaveBeenCalledTimes(1);

        expect(alertSpy).toHaveBeenCalledWith('Please select some text to clear formatting.');
    });
});
