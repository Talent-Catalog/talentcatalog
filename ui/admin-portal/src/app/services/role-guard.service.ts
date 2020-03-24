import {Injectable} from '@angular/core';
import {ActivatedRouteSnapshot, CanActivate, Router} from '@angular/router';
import {AuthService} from './auth.service';

@Injectable()
export class RoleGuardService implements CanActivate {

  constructor(public auth: AuthService,
              public router: Router) {
  }

  canActivate(route: ActivatedRouteSnapshot): boolean {
    // this will be passed from the route config
    // on the data property
    const expectedRole = route.data.expectedRole;
    const user = this.auth.getLoggedInUser();

    // decode the token to get its payload
    if (user.role !== expectedRole ) {
      // this.router.navigate(['']);
      return false;
    }else{
      return true;
    }
  }
}
