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
import {Country} from "./country";

describe('Country interface', () => {
  it('should create a Country instance', () => {
    const country: Country = {
      id: 1,
      name: 'United States',
      status: 'Active',
      translatedName: 'Estados Unidos' // Example translated name
    };

    expect(country).toBeTruthy();
    expect(country.id).toBe(1);
    expect(country.name).toBe('United States');
    expect(country.status).toBe('Active');
    expect(country.translatedName).toBe('Estados Unidos');
  });
});
