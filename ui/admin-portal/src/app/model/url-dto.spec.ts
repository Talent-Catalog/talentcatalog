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

import {UrlDto} from "./url-dto";

describe('UrlDto Class', () => {
  it('should create an instance of UrlDto', () => {
    const urlDto = new UrlDto();
    expect(urlDto).toBeTruthy();
    expect(urlDto instanceof UrlDto).toBe(true);
  });

  it('should have a url property', () => {
    const urlDto = new UrlDto();
    expect(urlDto.url).toBeUndefined(); // By default, url should be undefined
    urlDto.url = 'https://example.com';
    expect(urlDto.url).toEqual('https://example.com');
  });
});

