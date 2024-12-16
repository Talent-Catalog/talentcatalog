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

import {LanguageLevel} from "./language-level";

describe('LanguageLevel Interface', () => {
  it('should create a valid LanguageLevel object', () => {
    // Mock data
    const languageLevel: LanguageLevel = {
      id: 1,
      name: 'Advanced',
      level: 3,
      status: 'active'
    };

    // Assertions
    expect(languageLevel.id).toEqual(1);
    expect(languageLevel.name).toEqual('Advanced');
    expect(languageLevel.level).toEqual(3);
    expect(languageLevel.status).toEqual('active');
  });
});
