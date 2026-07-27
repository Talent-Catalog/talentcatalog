/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 *  the terms of the GNU General Public License as published by the Free
 *  Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

import {Location} from '@angular/common';
import {Router, UrlTree} from '@angular/router';
import {getExternalHref} from './url';

describe('getExternalHref', () => {
  it('combines the origin, external base path, and serialized navigation', () => {
    const urlTree = {} as UrlTree;
    const router = jasmine.createSpyObj<Router>(
      'Router',
      ['createUrlTree', 'serializeUrl']
    );
    const location = jasmine.createSpyObj<Location>(
      'Location',
      ['prepareExternalUrl']
    );
    router.createUrlTree.and.returnValue(urlTree);
    router.serializeUrl.and.returnValue('/list/1000');
    location.prepareExternalUrl.and.returnValue('/admin-portal/list/1000');

    const result = getExternalHref(router, location, ['list', 1000]);

    expect(router.createUrlTree).toHaveBeenCalledOnceWith(['list', 1000]);
    expect(router.serializeUrl).toHaveBeenCalledOnceWith(urlTree);
    expect(location.prepareExternalUrl)
    .toHaveBeenCalledOnceWith('/list/1000');
    expect(result)
    .toBe(`${document.location.origin}/admin-portal/list/1000`);
  });
});
