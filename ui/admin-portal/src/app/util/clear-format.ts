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

import {NgxWigComponent} from "ngx-wig";

/**
 * Creates a custom button configuration for clearing formatting from selected text
 * in the NgxWig rich text editor. This button allows users to remove all formatting
 * and revert the selected content to plain text.
 * <p/>
 * The button checks if any text is selected in the editor. If text is selected,
 * it clears the formatting and inserts the plain text back into the editor. If no
 * text is selected, it alerts the user to select some text first.
 *
 * @constant {Object} CUSTOM_CLEAR_FORMAT_BUTTON
 * @property {Object} clearFormat - The configuration for the "Clear Format" button.
 * @property {string} clearFormat.label - The label displayed on the button.
 * @property {string} clearFormat.title - The tooltip text shown when the button is hovered over.
 * @property {Function} clearFormat.command - The function that executes the clear formatting command.
 *    It takes the following parameter:
 *    @param {NgxWigComponent} ctx - The current NgxWig component context.
 *
 * Example usage:
 * <pre>
 * import { CUSTOM_CLEAR_FORMAT_BUTTON } from 'path/to/your/clear-format-button-file';
 *
 * // In your component where NgxWig is used
 * toolbarButtons = [
 *   CUSTOM_CLEAR_FORMAT_BUTTON.clearFormat,
 *   // other buttons...
 * ];
 * </pre>
 *
 * @throws {Error} If no text is selected, it alerts the user to select some text.
 *
 * @example
 * // Example implementation of the clearFormat command
 * const selection = window.getSelection();
 * if (selection && selection.rangeCount > 0) {
 *   // ...clear formatting logic...
 * } else {
 *   alert("Please select some text to clear formatting.");
 * }
 */
export const CUSTOM_CLEAR_FORMAT_BUTTON = {
  clearFormat: {
    label: 'Clear Format',
    title: 'Clear Formatting',
    command: (ctx: NgxWigComponent) => {
      const selection = window.getSelection();
      if (selection && selection.rangeCount > 0) {
        const range = selection.getRangeAt(0);
        const selectedContent = range.cloneContents();

        // Clear the selection
        range.deleteContents();

        // Insert plain text
        const tempDiv = document.createElement('div');
        tempDiv.appendChild(selectedContent);
        const plainText = tempDiv.innerText;

        // Insert the plain text
        const textNode = document.createTextNode(plainText);
        range.insertNode(textNode);

        // Reset the selection to the end of the inserted text
        range.setStartAfter(textNode);
        range.collapse(true);
        selection.removeAllRanges();
        selection.addRange(range);
      } else {
        alert("Please select some text to clear formatting."); // Handle case where no text is selected
      }
    },
    styleClass: 'nw-button',
    icon: 'fas fa-remove-format', // Optional: Add an icon
  }
};
