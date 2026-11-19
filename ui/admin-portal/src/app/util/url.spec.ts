/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

import type {MockedObject} from "vitest";
import {Router, UrlTree} from '@angular/router';
import {Location} from '@angular/common';
import {getExternalHref} from "./url";
import {expect, vi} from 'vitest';

describe('getExternalHref', () => {
    let routerMock: MockedObject<Router>;
    let locationMock: MockedObject<Location>;

    beforeEach(() => {
        routerMock = ({
            createUrlTree: vi.fn().mockName("Router.createUrlTree"),
            serializeUrl: vi.fn().mockName("Router.serializeUrl")
        } as unknown as typeof routerMock);
        locationMock = ({
            prepareExternalUrl: vi.fn().mockName("Location.prepareExternalUrl")
        } as unknown as typeof locationMock);
    });

    it('should generate the correct external URL', () => {
        const navigation = ['list', 1000];
        const urlTreeMock = {} as UrlTree;
        const serializedUrl = '/admin-portal/list/1000';
        routerMock.createUrlTree.mockReturnValue(urlTreeMock);
        routerMock.serializeUrl.mockReturnValue(serializedUrl);
        locationMock.prepareExternalUrl.mockReturnValue(serializedUrl);

        const result = getExternalHref(routerMock, locationMock, navigation);
        expect(result).toBe(`${document.location.origin}${serializedUrl}`);
        expect(routerMock.createUrlTree).toHaveBeenCalledWith(navigation);
        expect(routerMock.serializeUrl).toHaveBeenCalledWith(urlTreeMock);
        expect(locationMock.prepareExternalUrl).toHaveBeenCalledWith(serializedUrl);
    });
});
