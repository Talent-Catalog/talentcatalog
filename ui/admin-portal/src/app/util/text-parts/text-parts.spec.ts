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
import {TextPartsCodec} from './text-parts';

describe('TextPartsCodec', () => {
  it('reads null as empty original text', () => {
    expect(TextPartsCodec.read(null)).toEqual({ original: '' });
  });

  it('reads legacy plain text as original', () => {
    expect(TextPartsCodec.read('I work electrician 5 years')).toEqual({
      original: 'I work electrician 5 years'
    });
  });

  it('reads JSON text parts', () => {
    const stored = JSON.stringify({
      parts: {
        original: 'i work electrician',
        tidied: 'I worked as an electrician.',
        keywords: ['electrician', 'wiring']
      }
    });

    expect(TextPartsCodec.read(stored)).toEqual({
      original: 'i work electrician',
      tidied: 'I worked as an electrician.',
      keywords: ['electrician', 'wiring']
    });
  });

  it('writes text parts as JSON', () => {
    const stored = TextPartsCodec.write({
      original: 'i work electrician',
      tidied: 'I worked as an electrician.',
      keywords: ['electrician']
    });

    expect(JSON.parse(stored)).toEqual({
      parts: {
        original: 'i work electrician',
        tidied: 'I worked as an electrician.',
        keywords: ['electrician']
      }
    });
  });

  it('falls back to legacy text if JSON is not valid text parts', () => {
    expect(TextPartsCodec.read('{"hello":"world"}')).toEqual({
      original: '{"hello":"world"}'
    });
  });
});
