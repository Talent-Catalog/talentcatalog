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

import {Language, SystemLanguage} from "./language";

describe('Language Interface', () => {
  it('should create a valid Language object', () => {
    // Mock data
    const language: Language = {
      id: 1,
      name: 'English',
      status: 'active'
    };

    // Assertions
    expect(language.id).toEqual(1);
    expect(language.name).toEqual('English');
    expect(language.status).toEqual('active');
  });

  it('should create a valid SystemLanguage object', () => {
    // Mock data
    const systemLanguage: SystemLanguage = {
      id: 1,
      label: 'en',
      language: 'English',
      rtl: false
    };

    // Assertions
    expect(systemLanguage.id).toEqual(1);
    expect(systemLanguage.label).toEqual('en');
    expect(systemLanguage.language).toEqual('English');
    expect(systemLanguage.rtl).toEqual(false);
});

});
