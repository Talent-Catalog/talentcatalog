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
export interface TextParts {
  original: string;
  tidied?: string;
  keywords?: string[];
}

export class TextPartsCodec {
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
