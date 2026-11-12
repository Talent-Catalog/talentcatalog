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

import { Location } from '@angular/common';
import { Router, UrlTree } from '@angular/router';
import { getExternalHref } from './url';

describe('getExternalHref', () => {
    it('combines the origin, external base path, and serialized navigation', () => {
        const urlTree = {} as UrlTree;
        const router = {
            createUrlTree: vi.fn().mockName("Router.createUrlTree"),
            serializeUrl: vi.fn().mockName("Router.serializeUrl")
        };
        const location = {
            prepareExternalUrl: vi.fn().mockName("Location.prepareExternalUrl")
        };
        router.createUrlTree.mockReturnValue(urlTree);
        router.serializeUrl.mockReturnValue('/list/1000');
        location.prepareExternalUrl.mockReturnValue('/admin-portal/list/1000');

        const result = getExternalHref(router as unknown as Router, location as unknown as Location, ['list', 1000]);

        expect(router.createUrlTree).toHaveBeenCalledTimes(1);

        expect(router.createUrlTree).toHaveBeenCalledWith(['list', 1000]);
        expect(router.serializeUrl).toHaveBeenCalledTimes(1);
        expect(router.serializeUrl).toHaveBeenCalledWith(urlTree);
        expect(location.prepareExternalUrl).toHaveBeenCalledTimes(1);
        expect(location.prepareExternalUrl).toHaveBeenCalledWith('/list/1000');
        expect(result)
            .toBe(`${document.location.origin}/admin-portal/list/1000`);
    });
});
