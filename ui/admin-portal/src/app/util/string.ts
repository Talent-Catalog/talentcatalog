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

/**
 * Truncate given string down to max length
 * @param str String to be truncated
 * @param num Maximum length
 */
export function truncate(str: string, num: number): string {
  if (str && str.length > num) {
    return str.slice(0, num) + "...";
  } else {
    return str;
  }
}

export function isHtml(text): boolean {
  // Very simple test for HTML tags - isn't foolproof but probably good enough
  return /<\/?[a-z][\s\S]*>/i.test(text);
}

/**
 * Whether the given text - which may be HTML, eg from a rich text editor - has any visible text
 * content, ie anything other than HTML tags, non-breaking spaces and whitespace.
 * <p/>
 * For example "&lt;p&gt;&lt;/p&gt;", "&lt;p&gt;&amp;nbsp;&lt;/p&gt;" and "   " all have no text
 * content.
 */
export function hasTextContent(text: string | null | undefined): boolean {
  if (!text) {
    return false;
  }
  //Create a temporary element to strip HTML tags and get the pure text content.
  //This has the advantage of using built-in browser functionality.
  const tempElement = document.createElement('div');
  tempElement.innerHTML = text;
  //textContent and innerText are not always the same, so we check both and use whichever is
  // available. Different ones are used depending on the browser.
  const pureText = (tempElement.textContent ?? tempElement.innerText ?? '')
  .replace(/&nbsp;/g, '')
  .trim();
  return pureText.length > 0;
}

export function isNumeric(str: string): boolean {
  if (typeof str !== 'string' || str.trim() === '') {
    return false; // Not a string or an empty string after trimming
  }
  const num = Number(str);
  return !Number.isNaN(num) && Number.isFinite(num);
}

/**
 * Determines whether a given string is null, undefined, or an empty string after trimming whitespace.
 *
 * @param {string | null | undefined} value - The string to check.
 * @return {boolean} Returns true if the input is null, undefined, or an empty string; otherwise, false.
 */
export function isNullOrEmpty(value: string | null | undefined): boolean {
  return !value || value.trim().length === 0;
}
