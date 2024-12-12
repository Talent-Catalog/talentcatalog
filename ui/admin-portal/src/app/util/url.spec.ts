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

import {Router, UrlTree} from '@angular/router';
import {Location} from '@angular/common';
import {getExternalHref} from "./url";
import {Environment} from "../services/env.service";
import {environment} from "../../environments/environment";

describe('getExternalHref', () => {
  let routerMock: jasmine.SpyObj<Router>;
  let locationMock: jasmine.SpyObj<Location>;

  beforeEach(() => {
    routerMock = jasmine.createSpyObj('Router', ['createUrlTree', 'serializeUrl']);
    locationMock = jasmine.createSpyObj('Location', ['prepareExternalUrl']);
  });

  it('should generate the correct external URL', () => {
    const navigation = ['list', 1000];
    const urlTreeMock = {} as UrlTree;
    const serializedUrl = '/admin-portal/list/1000';
    let externalUrl = 'https://tctalent.org/admin-portal'; // Default base URL

    if (environment.production) {
      externalUrl = 'https://tctalent.org/admin-portal/list/1000';
    } else {
      externalUrl = 'http://localhost:9876/admin-portal/list/1000'; // Local development URL
    }
    routerMock.createUrlTree.and.returnValue(urlTreeMock);
    routerMock.serializeUrl.and.returnValue(serializedUrl);
    locationMock.prepareExternalUrl.and.returnValue(serializedUrl);

    const result = getExternalHref(routerMock, locationMock, navigation);
    console.log(Environment.Prod)
    expect(routerMock.createUrlTree).toHaveBeenCalledWith(navigation);
    expect(routerMock.serializeUrl).toHaveBeenCalledWith(urlTreeMock);
    expect(locationMock.prepareExternalUrl).toHaveBeenCalledWith(serializedUrl);
  });
});
