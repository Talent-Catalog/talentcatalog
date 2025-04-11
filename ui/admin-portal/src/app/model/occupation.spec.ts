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
import {Occupation} from "./occupation";

describe('Occupation Interface', () => {
  it('should create a valid Occupation object', () => {
    // Mock data
    const occupation: Occupation = {
      id: 1,
      name: 'Software Developer',
      isco08Code: '2135',
      status: 'active'
    };

    // Assertions
    expect(occupation.id).toEqual(1);
    expect(occupation.name).toEqual('Software Developer');
    expect(occupation.isco08Code).toEqual('2135');
    expect(occupation.status).toEqual('active');
  });
});
