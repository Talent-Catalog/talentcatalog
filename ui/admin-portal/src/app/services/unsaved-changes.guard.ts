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

import {Injectable} from '@angular/core';
import {ActivatedRouteSnapshot, CanDeactivate, RouterStateSnapshot} from '@angular/router';
import {Observable} from 'rxjs';

/**
 * The Angular CanDeactivate Guard determines whether we can navigate away from a route. This custom guard
 * allows us to implement a custom canExit condition, which must be passed to navigate away.
 * See doc: https://www.tektutorialshub.com/angular/angular-candeactivate-guard/
 */
export interface BlockUnsavedChanges {
  canExit: () => Observable<boolean> | Promise<boolean> | boolean;
}
@Injectable()
export class UnsavedChangesGuard implements CanDeactivate<BlockUnsavedChanges>
{
  component: Object;
  route: ActivatedRouteSnapshot;
  constructor(){
  }
  canDeactivate(component:BlockUnsavedChanges,
                route: ActivatedRouteSnapshot,
                state: RouterStateSnapshot,
                nextState: RouterStateSnapshot) : Observable<boolean> | Promise<boolean> | boolean {

    return component.canExit ? component.canExit() : true;
  }

}
