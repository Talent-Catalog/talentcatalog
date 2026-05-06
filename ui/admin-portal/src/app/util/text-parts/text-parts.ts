/*
 * Copyright (c) 2026 Talent Catalog.
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

/**
 * Represents the parts of some other text, including the "original" text,
 * a "tidied" up version of the text, and keywords related to the text.
 */
export interface TextParts {
  /**
   * The original "source" text.
   * For example, this could be text entered by a candidate.
   */
  original: string;

  /**
   * A tidied-up version of the original text.
   * This could be used for constructing a CV.
   */
  tidied?: string;

  /**
   * Keywords extracted from the original text.
   */
  keywords?: string[];
}

/**
 * Codec (Code/Decode) for TextParts.
 * <p>
 * Provides methods for encoding and decoding TextParts objects to/from stored text.
 * <p>
 * The stored text can be either JSON or plain text.
 * </p>
 */
export class TextPartsCodec {

  /**
   * Reads the given text and returns a TextParts object.
   * @param value
   */
  static read(value: string | null | undefined): TextParts {
    if (!value?.trim()) {
      return { original: '' };
    }

    try {
      const parsed = JSON.parse(value);

      if (
        parsed &&
        typeof parsed === 'object' &&
        parsed.parts &&
        typeof parsed.parts === 'object' &&
        typeof parsed.parts.original === 'string'
      ) {
        return {
          original: parsed.parts.original,
          tidied: typeof parsed.parts.tidied === 'string'
            ? parsed.parts.tidied
            : undefined,
          keywords: Array.isArray(parsed.parts.keywords)
            ? parsed.parts.keywords.filter((k: unknown) => typeof k === 'string')
            : undefined
        };
      }
    } catch (error)  {
      // Legacy plain text
      console.warn('Legacy text detected, falling back to plain text. Error:' + error);
    }

    return { original: value };
  }

  /**
   * Writes the given TextParts object to a JSON string.
   * <p>
   * Note that the TextParts object is written as a simple JSON object with a single
   * "parts" property.
   * <p>
   * Example:
   * <pre>
   * {
   *   "parts": {
   *     "original": "Sample text",
   *     "tidied": "sample text",
   *     "keywords": ["sample", "text"]
   *   }
   * }
   * </pre>
   * @param parts Parts to be written as a JSON string.
   */
  static write(parts: TextParts): string {
    return JSON.stringify({
      parts: {
        original: parts.original ?? '',
        ...(parts.tidied !== undefined ? { tidied: parts.tidied } : {}),
        ...(parts.keywords !== undefined ? { keywords: parts.keywords } : {})
      }
    });
  }
}
