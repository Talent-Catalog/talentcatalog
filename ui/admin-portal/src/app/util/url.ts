/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

import {Router, UrlTree} from "@angular/router";
import {Location} from "@angular/common";

export function getExternalHref(
  router: Router, location: Location, navigation: any[]): string {
  const urlTree: UrlTree = router.createUrlTree(navigation);
  return document.location.origin +
    location.prepareExternalUrl(router.serializeUrl(urlTree));
}
