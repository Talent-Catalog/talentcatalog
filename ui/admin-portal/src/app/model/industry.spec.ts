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

import {Industry} from "./industry";

describe('Industry', () => {
  it('should create an Industry instance', () => {
    // Mock data
    const industry: Industry = {
      id: 1,
      name: 'Technology',
      status: 'Active'
    };

    // Assertions
    expect(industry).toBeTruthy(); // Check if industry instance exists
    expect(industry.id).toBe(1); // Check id property
    expect(industry.name).toBe('Technology'); // Check name property
    expect(industry.status).toBe('Active'); // Check status property
  });
});
