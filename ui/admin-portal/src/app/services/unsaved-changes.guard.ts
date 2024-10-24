import {Injectable} from '@angular/core';
import {ActivatedRouteSnapshot, CanDeactivate, RouterStateSnapshot} from '@angular/router';
import {Observable} from 'rxjs';

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
