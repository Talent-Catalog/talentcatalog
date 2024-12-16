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

import {Translation} from "./translation";

describe('Translation Interface', () => {
  it('should create an instance of Translation', () => {
    const translation: Translation = {
      id: 1,
      objectId: 100,
      objectType: 'Task',
      value: 'Translated value'
    };

    expect(translation).toBeTruthy();
    expect(translation.id).toEqual(1);
    expect(translation.objectId).toEqual(100);
    expect(translation.objectType).toEqual('Task');
    expect(translation.value).toEqual('Translated value');
  });
});

