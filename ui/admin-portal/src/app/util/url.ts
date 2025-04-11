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

import {Router, UrlTree} from "@angular/router";
import {Location} from "@angular/common";

/**
 * Creates an externally usable url from the given navigation path - eg ["list",1000] by prefixing it
 * with the apps base url - ie https://tctalent.org/admin-portal
 * <p/>
 * So for example, this could generate the external link for TC list number 1000, from the
 * navigation ["list",1000] giving https://tctalent.org/admin-portal/list/1000
 * @param router Current Router
 * @param location Current Location
 * @param navigation array of navigation elements
 */
export function getExternalHref(
  router: Router, location: Location, navigation: any[]): string {
  const urlTree: UrlTree = router.createUrlTree(navigation);
  return document.location.origin +
    location.prepareExternalUrl(router.serializeUrl(urlTree));
}
