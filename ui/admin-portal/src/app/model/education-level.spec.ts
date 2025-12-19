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

import {EducationLevel} from "./education-level";

describe('EducationLevel interface', () => {
  it('should create an EducationLevel instance', () => {
    const educationLevel: EducationLevel = {
      id: 1,
      name: 'Bachelor\'s Degree',
      status: 'Active',
      level: 6
    };

    expect(educationLevel).toBeTruthy();
    expect(educationLevel.id).toBe(1);
    expect(educationLevel.name).toBe('Bachelor\'s Degree');
    expect(educationLevel.status).toBe('Active');
    expect(educationLevel.level).toBe(6);
  });
});
