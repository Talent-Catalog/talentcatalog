/*
 * Copyright (c) 2025 Talent Catalog.
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

import {Injectable} from '@angular/core';
import {ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot} from '@angular/router';
import {Observable} from 'rxjs';
import {map, tap} from 'rxjs/operators';
import {PartnerService} from './partner.service';
import {AuthorizationService} from "./authorization.service";

@Injectable({
  providedIn: 'root'
})
export class DpaGuard implements CanActivate {
  constructor(
    private authorizationService: AuthorizationService,
    private partnerService: PartnerService,
    private router: Router
  ) {
  }

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<boolean> {
    // Only check DPA for source partner users
    if (!this.authorizationService.isSourcePartner()) {
      return new Observable<boolean>(observer => observer.next(true));
    }
    return this.partnerService.requiresDpaAcceptance().pipe(
      map(requiresDpa => !requiresDpa),
      tap(isAllowed => {
        if (!isAllowed) {
          this.router.navigate(['/dpa']);
        }
      })
    );
  }
}
