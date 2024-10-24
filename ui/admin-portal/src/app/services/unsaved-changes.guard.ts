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
