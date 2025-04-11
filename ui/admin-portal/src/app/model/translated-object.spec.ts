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

import {TranslatedObject} from "./translated-object";

describe('TranslatedObject Interface', () => {
  it('should create an instance of TranslatedObject', () => {
    const translatedObject: TranslatedObject = {
      id: 1,
      name: 'Original Name',
      status: 'Active',
      translatedId: 2,
      translatedName: 'Translated Name'
    };

    expect(translatedObject).toBeTruthy();
    expect(translatedObject.id).toEqual(1);
    expect(translatedObject.name).toEqual('Original Name');
    expect(translatedObject.status).toEqual('Active');
    expect(translatedObject.translatedId).toEqual(2);
    expect(translatedObject.translatedName).toEqual('Translated Name');
  });
});

